package com.thesecretden.uncovered_tech.main.common.blocks;

import com.google.common.base.Preconditions;
import com.thesecretden.uncovered_tech.main.api.energy.WrappingEnergyStorage;
import com.thesecretden.uncovered_tech.main.api.utils.UCDirectionUtils;
import com.thesecretden.uncovered_tech.main.api.utils.UCSafeChunkUtils;
import com.thesecretden.uncovered_tech.main.common.fluids.ArrayFluidHandler;
import com.thesecretden.uncovered_tech.main.common.util.ResettableCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public abstract class UCBaseTileEntity extends BlockEntity implements UCBlockInterfaces.BlockstateProvider {
    protected UCBlockInterfaces.IGeneralMultiblock tempMasterTE;

    @Nullable
    private BlockState overrideBlockState = null;

    private final EnumMap<Direction, Integer> redstoneBySide = new EnumMap<>(Direction.class);

    public UCBaseTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.readCustomNBT(tag, false);
    }

    public abstract void readCustomNBT(CompoundTag tag, boolean descPacket);

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.writeCustomNBT(tag, false);
    }

    public abstract void writeCustomNBT(CompoundTag tag, boolean descPacket);

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, te -> {
            CompoundTag tag = new CompoundTag();
            this.writeCustomNBT(tag, true);
            return tag;
        });
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag() != null ? pkt.getTag() : new CompoundTag();
        this.readCustomNBT(tag, true);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.readCustomNBT(tag, true);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeCustomNBT(tag, true);
        return tag;
    }

    public void receiveMessageFromClient(CompoundTag message) {}

    public void receiveMessageFromServer(CompoundTag message) {}

    public void onEntityCollision(Level level, Entity entity) {}

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 0 || id == 255) {
            markContainingBlockForUpdate(null);
            return true;
        } else if (id == 254) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public void markContainingBlockForUpdate(@Nullable BlockState state) {
        if (this.level != null)
            markBlockForUpdate(getBlockPos(), state);
    }

    public void markBlockForUpdate(BlockPos pos, @Nullable BlockState newState) {
        BlockState state = level.getBlockState(pos);
        if (newState == null)
            newState = state;
        level.sendBlockUpdated(pos, state, newState, 3);
        level.updateNeighborsAt(pos, newState.getBlock());
    }

    private final List<ResettableCapability<?>> caps = new ArrayList<>();
    private final List<Runnable> onCapInvalidate = new ArrayList<>();

    protected <T> ResettableCapability<T> registerCapability(T val) {
        ResettableCapability<T> cap = new ResettableCapability<>(val);
        caps.add(cap);
        return cap;
    }

    public void addCapInvalidateHook(Runnable hook) {
        onCapInvalidate.add(hook);
    }

    protected ResettableCapability<IEnergyStorage> registerEnergyInput(IEnergyStorage directStorage) {
        return registerCapability(new WrappingEnergyStorage(directStorage, true, false, this::setChanged));
    }

    protected ResettableCapability<IEnergyStorage> registerEnergyOutput(IEnergyStorage directStorage) {
        return registerCapability(new WrappingEnergyStorage(directStorage, false, true, this::setChanged));
    }

    protected ResettableCapability<IFluidHandler> registerFluidHandler(IFluidTank[] tanks, boolean allowDrain, boolean allowFill) {
        return registerCapability(new ArrayFluidHandler(tanks, allowDrain, allowFill, () -> markContainingBlockForUpdate(null)));
    }

    protected final ResettableCapability<IFluidHandler> registerFluidHandler(FluidTank... tanks) {
        return registerFluidHandler(tanks, true, true);
    }

    protected final ResettableCapability<IFluidHandler> registerFluidInput(FluidTank... tanks) {
        return registerFluidHandler(tanks, false, true);
    }

    protected final ResettableCapability<IFluidHandler> registerFluidOutput(FluidTank... tanks) {
        return registerFluidHandler(tanks, true, false);
    }

    protected final ResettableCapability<IFluidHandler> registerFluidView(IFluidTank... tanks) {
        return registerFluidHandler(tanks, false, false);
    }

    @Override
    public void setRemoved() {
        if (!isUnloaded)
            setRemovedUC();
        super.setRemoved();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        resetAllCaps();
        caps.clear();
        onCapInvalidate.forEach(Runnable::run);
        onCapInvalidate.clear();
    }

    protected void resetAllCaps() {
        caps.forEach(ResettableCapability::reset);
    }

    private boolean isUnloaded = false;

    @Override
    public void onLoad() {
        super.onLoad();
        isUnloaded = false;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        isUnloaded = true;
    }

    public void setRemovedUC() {}

    @Nonnull
    public Level getLevelNonnull() {
        return Objects.requireNonNull(super.getLevel());
    }

    protected void checkLight() {
        checkLight(worldPosition);
    }

    protected void checkLight(BlockPos pos) {
        getLevelNonnull().getBlockTicks().schedule(new ScheduledTick<Block>(getBlockState().getBlock(), pos, 4, 0));
    }

    public void setOverrideState(@Nullable BlockState state) {
        overrideBlockState = state;
    }

    @Override
    public BlockState getBlockState() {
        if (overrideBlockState != null)
            return overrideBlockState;
        else
            return super.getBlockState();
    }

    @Override
    public void setBlockState(BlockState state) {
        BlockState oldState = getBlockState();
        super.setBlockState(state);
        if (getType().isValid(oldState) && !getType().isValid(state))
            setOverrideState(oldState);
        else if (getType().isValid(state))
            setOverrideState(null);
        resetAllCaps();
    }

    @Override
    public void setState(BlockState newState) {
        if (getLevelNonnull().getBlockState(worldPosition) == getState())
            getLevelNonnull().setBlockAndUpdate(worldPosition, newState);
    }

    @Override
    public BlockState getState() {
        return getBlockState();
    }

    protected void markChunkDirty() {
        if (this.level != null && this.level.hasChunkAt(this.worldPosition))
            this.level.getChunkAt(this.worldPosition).setUnsaved(true);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        this.redstoneBySide.clear();
    }

    @Override
    public void setChanged() {
        if (this.level != null) {
            markChunkDirty();
            BlockState state = getBlockState();
            if (state.hasAnalogOutputSignal())
                this.level.updateNeighbourForOutputSignal(this.worldPosition, state.getBlock());
        }
    }

    protected void onNeighbourBlockChange(BlockPos pos) {
        BlockPos delta = pos.subtract(worldPosition);
        Direction dir = Direction.getNearest(delta.getX(), delta.getY(), delta.getZ());
        Preconditions.checkNotNull(dir);
        updateRSForSide(dir);
    }

    private void updateRSForSide(Direction dir) {
        int rsStrength = getLevelNonnull().getSignal(worldPosition.relative(dir), dir);
        if (rsStrength == 0 && this instanceof UCBlockInterfaces.IRedstoneOutput && ((UCBlockInterfaces.IRedstoneOutput)this).canConnectRedstone(dir)) {
            BlockState state = UCSafeChunkUtils.getBlockState(level, worldPosition.relative(dir));
            if (state.getBlock() == Blocks.REDSTONE_WIRE && state.getValue(RedStoneWireBlock.POWER) > rsStrength)
                rsStrength = state.getValue(RedStoneWireBlock.POWER);
        }
        redstoneBySide.put(dir, rsStrength);
    }

    protected int getRSInput(Direction dir) {
        if ( level.isClientSide || !redstoneBySide.containsKey(dir))
            updateRSForSide(dir);
        return redstoneBySide.get(dir);
    }

    protected int getMaxRSInput() {
        int rsReturn = 0;
        for (Direction dir : UCDirectionUtils.VALUES)
            rsReturn = Math.max(rsReturn, getRSInput(dir));
        return rsReturn;
    }

    protected boolean isRSPowered() {
        for (Direction dir : UCDirectionUtils.VALUES)
            if (getRSInput(dir) > 0)
                return true;
        return false;
    }
}





















