package com.thesecretden.uncovered_tech.main.common.blocks.generic;

import com.mojang.datafixers.util.Pair;
import com.thesecretden.uncovered_tech.main.api.UCProperties;
import com.thesecretden.uncovered_tech.main.api.multiblock.TemplateMultiblock;
import com.thesecretden.uncovered_tech.main.api.utils.UCSafeChunkUtils;
import com.thesecretden.uncovered_tech.main.api.utils.shapes.CachedShapesWithTransform;
import com.thesecretden.uncovered_tech.main.common.blocks.PlacementLimitation;
import com.thesecretden.uncovered_tech.main.common.blocks.UCBaseTileEntity;
import com.thesecretden.uncovered_tech.main.common.blocks.UCBlockInterfaces.*;
import com.thesecretden.uncovered_tech.main.common.blocks.multiblock.UCTemplateMultiblock;
import com.thesecretden.uncovered_tech.main.common.blocks.ticking.UCServerTickableTE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public abstract class MultiblockPartTileEntity<T extends MultiblockPartTileEntity<T>> extends UCBaseTileEntity implements UCServerTickableTE, IStateBasedDirectional, IGeneralMultiblock, IMirrorable {
    public boolean formed = false;
    public BlockPos posInMultiblock = BlockPos.ZERO;
    public BlockPos offsetToMaster = BlockPos.ZERO;
    protected final UCTemplateMultiblock multiblockInstance;
    public long onlyLocalDissasembly = -1;
    protected final Lazy<Vec3i> structureDimensions;
    protected final boolean hasRedstoneControl;
    protected boolean redstonControlInverted = false;

    protected MultiblockPartTileEntity(UCTemplateMultiblock multiblockInstance, BlockEntityType<? extends T> type, boolean hasRSControl, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.multiblockInstance = multiblockInstance;
        this.structureDimensions = Lazy.of(() -> multiblockInstance.getSize(level));
        this.hasRedstoneControl = hasRSControl;
    }

    @Nonnull
    @Override
    public Direction getFacing() {
        return IStateBasedDirectional.super.getFacing();
    }

    @Override
    public Property<Direction> getFacingProperty() {
        return UCProperties.FACING_HORIZONTAL;
    }

    @Override
    public PlacementLimitation getFacingLimitation() {
        return PlacementLimitation.HORIZONTAL;
    }

    @Override
    public boolean canHammerRotate(Direction dir, Vec3 hit, LivingEntity entity) {
        return false;
    }

    @Override
    public void readCustomNBT(CompoundTag tag, boolean descPacket) {
        formed = tag.getBoolean("formed");
        posInMultiblock = NbtUtils.readBlockPos(tag.getCompound("posInMultiblock"));
        offsetToMaster = NbtUtils.readBlockPos(tag.getCompound("offset"));
        redstonControlInverted = tag.getBoolean("redstoneControlInverted");
    }

    @Override
    public void writeCustomNBT(CompoundTag tag, boolean descPacket) {
        tag.putBoolean("formed", formed);
        tag.put("posInMultiblock", NbtUtils.writeBlockPos(new BlockPos(posInMultiblock)));
        tag.put("offset", NbtUtils.writeBlockPos(new BlockPos(offsetToMaster)));
        tag.putBoolean("redstoneControlInverted", redstonControlInverted);
    }

    @Override
    @Nullable
    public T master() {
        if (offsetToMaster.equals(Vec3i.ZERO))
            return (T)this;
        if (tempMasterTE != null)
            return (T)tempMasterTE;
        return getEntityForPos(multiblockInstance.getMasterFromOriginOffset());
    }

    public void updateMasterBlock(BlockState state, boolean blockUpdate) {
        T master = master();
        if (master != null) {
            master.markChunkDirty();
            if (blockUpdate)
                master.markContainingBlockForUpdate(state);
        }
    }

    @Override
    public boolean isDummy() {
        return !offsetToMaster.equals(Vec3i.ZERO);
    }

    public BlockState getOriginalBlock() {
        for (StructureTemplate.StructureBlockInfo block : multiblockInstance.getStructure(level))
            if (block.pos.equals(posInMultiblock))
                return block.state;
        return Blocks.AIR.defaultBlockState();
    }

    public void disassemble() {
        if (formed && !level.isClientSide) {
            tempMasterTE = master();
            BlockPos startPos = getOrigin();
            multiblockInstance.disassemble(level, startPos, getIsMirrored(), multiblockInstance.untransformDirection(getFacing()));
            level.removeBlock(worldPosition, false);
        }
    }

    public BlockPos getOrigin() {
        return TemplateMultiblock.withSettingsAndOffset(worldPosition, BlockPos.ZERO.subtract(posInMultiblock), getIsMirrored(), multiblockInstance.untransformDirection(getFacing()));
    }

    public BlockPos getBlockPosForPos(BlockPos targetPos) {
        BlockPos origin = getOrigin();
        return TemplateMultiblock.withSettingsAndOffset(origin, targetPos, getIsMirrored(), multiblockInstance.untransformDirection(getFacing()));
    }

    public void replaceStructureBlock(BlockPos pos, BlockState state, ItemStack stack, int h, int l, int w) {
        if (state.getBlock() == this.getBlockState().getBlock())
            getLevelNonnull().removeBlock(pos, false);
        getLevelNonnull().setBlockAndUpdate(pos, state);
        BlockEntity tile = getLevelNonnull().getBlockEntity(pos);
        if (tile instanceof IReadOnPlacement readOnPlacement)
            readOnPlacement.readOnPlacement(null, stack);
    }

    public Set<BlockPos> getRedstonePos() {
        throw new UnsupportedOperationException("Tried to get Redstone position for a multiblock without Redstone control");
    }

    public boolean isRedstonePos() {
        if (!hasRedstoneControl || getRedstonePos() == null)
            return false;
        for (BlockPos pos : getRedstonePos())
            if (posInMultiblock.equals(pos))
                return true;
        return false;
    }

    public boolean isRSDisabled() {
        Set<BlockPos> rsPositions = getRedstonePos();
        if (rsPositions == null || rsPositions.isEmpty())
            return false;
        MultiblockPartTileEntity<?> master = master();
        if (master == null)
            master = this;
        for (BlockPos pos : rsPositions) {
            T tile = this.getEntityForPos(pos);
            if (tile != null) {
                boolean b = tile.isRSPowered();
                if (redstonControlInverted != b)
                    return true;
            }
        }
        return false;
    }

    @Nullable
    public T getEntityForPos(BlockPos targetPosInMB) {
        BlockPos target = getBlockPosForPos(targetPosInMB);
        BlockEntity tile = UCSafeChunkUtils.getSafeTE(getLevelNonnull(), target);
        if (this.getClass().isInstance(tile))
            return (T)tile;
        return null;
    }

    public VoxelShape getShape(CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> cache) {
        return cache.get(posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
    }

    public static <T extends MultiblockPartTileEntity<?> & IComparatorOverride> void updateComparators(T tile, Collection<BlockPos> offsets, MutableInt cachedValue, int newValue) {
        if ( newValue == cachedValue.intValue())
            return;
        cachedValue.setValue(newValue);
        final Level level = tile.getLevelNonnull();
        for (BlockPos offset : offsets) {
            final BlockPos worldPos = tile.getBlockPosForPos(offset);
            final BlockState stateAt = level.getBlockState(worldPos);
            level.updateNeighbourForOutputSignal(worldPos, stateAt.getBlock());
        }
    }
}





























