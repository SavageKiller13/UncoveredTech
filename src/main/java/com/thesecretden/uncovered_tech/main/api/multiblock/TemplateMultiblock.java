package com.thesecretden.uncovered_tech.main.api.multiblock;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.api.utils.SetRestrictedField;
import com.thesecretden.uncovered_tech.main.api.utils.UCDirectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class TemplateMultiblock implements MultiblockHandler.IMultiblock {
    private static final SetRestrictedField<Function<BlockState, ItemStack>> PICK_BLOCK = SetRestrictedField.common();
    private static final SetRestrictedField<BiFunction<ResourceLocation, MinecraftServer, StructureTemplate>> LOAD_TEMPLATE = SetRestrictedField.common();
    private static final SetRestrictedField<Function<StructureTemplate, List<StructureTemplate.Palette>>> GET_PALETTES = SetRestrictedField.common();

    private final ResourceLocation rl;
    protected final BlockPos masterFromOrigin;
    protected final BlockPos triggerFromOrigin;
    protected final BlockPos size;
    protected final List<BlockMatcher.MatcherPredicate> additionalPredicates;
    @Nullable
    private StructureTemplate template;
    private BlockState trigger = Blocks.AIR.defaultBlockState();

    public TemplateMultiblock(ResourceLocation rl, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, List<BlockMatcher.MatcherPredicate> additionalPredicates) {
        this.rl = rl;
        this.masterFromOrigin = masterFromOrigin;
        this.triggerFromOrigin = triggerFromOrigin;
        this.size = size;
        this.additionalPredicates = additionalPredicates;
    }

    public TemplateMultiblock(ResourceLocation rl, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size) {
        this(rl, masterFromOrigin, triggerFromOrigin, size, ImmutableMap.of());
    }

    public TemplateMultiblock(ResourceLocation rl, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, Map<Block, TagKey<Block>> tags) {
        this(rl, masterFromOrigin, triggerFromOrigin, size, ImmutableList.of(
                ((expectedState, foundState, level, pos) -> {
                    TagKey<Block> tag = tags.get(expectedState.getBlock());
                    if (tag != null) {
                        if (foundState.is(tag))
                            return BlockMatcher.Result.allow(2);
                        else
                            return BlockMatcher.Result.deny(2);
                    } else
                        return BlockMatcher.Result.DEFAULT;
                })
        ));
    }

    @Nonnull
    protected StructureTemplate getTemplate(@Nullable Level level) {
        return getTemplate(level == null ? null : level.getServer());
    }

    public ResourceLocation getTemplateLocation() {
        return rl;
    }

    @Nonnull
    public StructureTemplate getTemplate(@Nullable MinecraftServer server) {
        if (template == null) {
            template = LOAD_TEMPLATE.getValue().apply(rl, server);
            List<StructureBlockInfo> blocks = getStructureFromTemplate(template);
            for (int i = 0; i < blocks.size(); i++) {
                StructureBlockInfo info = blocks.get(i);
                if (info.pos.equals(triggerFromOrigin))
                    trigger = info.state;
                if (info.state == Blocks.AIR.defaultBlockState()) {
                    blocks.remove(i);
                    i--;
                } else if (info.state.isAir())
                    UncoveredTech.LOGGER.error("Found non-default air block in template");
            }
        }
        return Objects.requireNonNull(template);
    }

    public void reset() {
        template = null;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return rl;
    }

    @Override
    public boolean isBlockTrigger(BlockState state, Direction dir, @Nullable Level level) {
        getTemplate(level);
        Rotation rot = UCDirectionUtils.getRotationBetweenFacings(Direction.NORTH, dir.getOpposite());
        if (rot == null)
            return false;
        for (Mirror mirror : getPossibleMirrorStates()) {
            BlockState modifiedTrigger = applyToState(trigger, mirror, rot);
            if (BlockMatcher.matches(modifiedTrigger, state, null, null, additionalPredicates).isAllow())
                return true;
        }
        return false;
    }

    @Override
    public boolean createStructure(Level level, BlockPos pos, Direction dir, Player player) {
        Rotation rotation = UCDirectionUtils.getRotationBetweenFacings(Direction.NORTH, dir.getOpposite());
        if (rotation == null)
            return false;
        List<StructureBlockInfo> structure = getStructure(level);
        mirrorLoop:
        for (Mirror mirror : getPossibleMirrorStates()) {
            StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(mirror).setRotation(rotation);
            BlockPos origin = pos.subtract(StructureTemplate.calculateRelativePosition(settings, triggerFromOrigin));
            for (StructureBlockInfo info : structure) {
                BlockPos realRelPos = StructureTemplate.calculateRelativePosition(settings, info.pos);
                BlockPos here = origin.offset(realRelPos);

                BlockState expectedState = applyToState(info.state, mirror, rotation);
                BlockState worldState = level.getBlockState(here);
                if (!BlockMatcher.matches(expectedState, worldState, level, here, additionalPredicates).isAllow())
                    continue mirrorLoop;
            }
            form(level, origin, rotation, mirror, dir);
            return true;
        }
        return false;
    }

    private BlockState applyToState(BlockState inState, Mirror mirror, Rotation rotation) {
        return inState.mirror(mirror).rotate(rotation);
    }

    private List<Mirror> getPossibleMirrorStates() {
        if (canBeMirrored())
            return ImmutableList.of(Mirror.NONE, Mirror.FRONT_BACK);
        else
            return ImmutableList.of(Mirror.NONE);
    }

    protected void form(Level level, BlockPos pos, Rotation rot, Mirror mirror, Direction dir) {
        BlockPos masterPos = withSettingsAndOffset(pos, masterFromOrigin, mirror, rot);
        for (StructureBlockInfo block : getStructure(level)) {
            BlockPos actualPos = withSettingsAndOffset(pos, block.pos, mirror, rot);
            replaceStructureBlock(block, level, actualPos, mirror != Mirror.NONE, dir, actualPos.subtract(masterPos));
        }
    }

    public BlockPos getMasterFromOriginOffset() {
        return masterFromOrigin;
    }

    protected abstract void replaceStructureBlock(StructureBlockInfo info, Level level, BlockPos actualPos, boolean mirrored, Direction dir, Vec3i offsetFromMaster);

    @Override
    public List<StructureBlockInfo> getStructure(@Nullable Level level) {
        return getStructureFromTemplate(getTemplate(level));
    }

    private static List<StructureBlockInfo> getStructureFromTemplate(StructureTemplate template) {
        return GET_PALETTES.getValue().apply(template).get(0).blocks();
    }

    @Override
    public Vec3i getSize(@Nullable Level level) {
        return getTemplate(level).getSize();
    }

    public static BlockPos withSettingsAndOffset(BlockPos origin, BlockPos relative, Mirror mirror, Rotation rot) {
        StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(mirror).setRotation(rot);
        return origin.offset(StructureTemplate.calculateRelativePosition(settings, relative));
    }

    public static BlockPos withSettingsAndOffset(BlockPos origin, BlockPos relative, boolean mirrored, Direction dir) {
        Rotation rot = UCDirectionUtils.getRotationBetweenFacings(Direction.NORTH, dir);
        if (rot == null)
            return origin;
        return withSettingsAndOffset(origin, relative, mirrored ? Mirror.FRONT_BACK : Mirror.NONE, rot);
    }

    @Override
    public void disassemble(Level level, BlockPos pos, boolean mirrored, Direction dir) {
        Mirror mirror = mirrored ? Mirror.FRONT_BACK : Mirror.NONE;
        Rotation rot = UCDirectionUtils.getRotationBetweenFacings(Direction.NORTH, dir);
        Preconditions.checkNotNull(rot);
        for (StructureBlockInfo block : getStructure(level)) {
            BlockPos actualPos = withSettingsAndOffset(pos, block.pos, mirror, rot);
            prepareBlockForDisassembly(level, actualPos);
            level.setBlockAndUpdate(actualPos, block.state.mirror(mirror).rotate(rot));
        }
    }

    protected void prepareBlockForDisassembly(Level level, BlockPos pos) {}

    @Override
    public BlockPos getTriggerOffset() {
        return triggerFromOrigin;
    }

    public boolean canBeMirrored() {
        return true;
    }

    public static void setCallBacks(Function<BlockState, ItemStack> pickBlock, BiFunction<ResourceLocation, MinecraftServer, StructureTemplate> loadTemplate, Function<StructureTemplate, List<StructureTemplate.Palette>> getPalettes) {
        PICK_BLOCK.setValue(pickBlock);
        LOAD_TEMPLATE.setValue(loadTemplate);
        GET_PALETTES.setValue(getPalettes);
    }
}























