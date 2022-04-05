package com.thesecretden.uncovered_tech.main.common.blocks.multiblock;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.api.UCProperties;
import com.thesecretden.uncovered_tech.main.api.multiblock.TemplateMultiblock;
import com.thesecretden.uncovered_tech.main.common.blocks.generic.MultiblockPartTileEntity;
import com.thesecretden.uncovered_tech.main.common.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UCTemplateMultiblock extends TemplateMultiblock {
    private final BlockRegistry.BlockEntry<?> baseState;

    public UCTemplateMultiblock(ResourceLocation location, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, BlockRegistry.BlockEntry<?> baseState) {
        super(location, masterFromOrigin, triggerFromOrigin, size, ImmutableMap.of());
        this.baseState = baseState;
    }

    @Override
    protected void replaceStructureBlock(StructureTemplate.StructureBlockInfo info, Level level, BlockPos actualPos, boolean mirrored, Direction dir, Vec3i offsetFromMaster) {
        BlockState state = baseState.get().defaultBlockState();
        if (!offsetFromMaster.equals(Vec3i.ZERO))
            state = state.setValue(UCProperties.MULTIBLOCKSLAVE, true);
        level.setBlockAndUpdate(actualPos, state);
        BlockEntity current = level.getBlockEntity(actualPos);
        if (current instanceof MultiblockPartTileEntity<?> tile) {
            tile.formed = true;
            tile.offsetToMaster = new BlockPos(offsetFromMaster);
            tile.posInMultiblock = info.pos;
            tile.setFacing(transformDirection(dir.getOpposite()));
            tile.setChanged();
            level.blockEvent(actualPos, level.getBlockState(actualPos).getBlock(), 255, 0);
        } else
            UncoveredTech.LOGGER.error("Expected Multiblock TileEntity at {} during placement", actualPos);
    }

    public Direction transformDirection(Direction direction) {
        return direction;
    }

    public Direction untransformDirection(Direction direction) {
        return direction;
    }

    public BlockPos multiblockToModelPos(BlockPos posInMultiblock) {
        return posInMultiblock.subtract(masterFromOrigin);
    }

    @Override
    public Vec3i getSize(@Nullable Level level) {
        return size;
    }

    @NotNull
    @Override
    protected StructureTemplate getTemplate(@Nullable Level level) {
        StructureTemplate result = super.getTemplate(level);
        Preconditions.checkState(result.getSize().equals(size), "Wrong template size for multiblock %s, template size %s", getTemplateLocation(), result.getSize());
        return result;
    }

    @Override
    protected void prepareBlockForDisassembly(Level level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof MultiblockPartTileEntity<?> multiblockTE)
            multiblockTE.formed = false;
        else if (te != null)
            UncoveredTech.LOGGER.error("Expected multiblock TileEntity at {}, got {}", pos, te);
    }

    public ResourceLocation getBlockName() {
        return baseState.getId();
    }

    @Override
    public Component getDisplayName() {
        return baseState.get().getName();
    }

    public Block getBlock() {
        return baseState.get();
    }
}

























