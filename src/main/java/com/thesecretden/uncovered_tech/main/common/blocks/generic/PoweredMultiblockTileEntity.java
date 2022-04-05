package com.thesecretden.uncovered_tech.main.common.blocks.generic;

import com.thesecretden.uncovered_tech.main.api.crafting.MultiRecipe;
import com.thesecretden.uncovered_tech.main.api.energy.AveragingEnergyStorage;
import com.thesecretden.uncovered_tech.main.api.multiblock.TemplateMultiblock;
import com.thesecretden.uncovered_tech.main.common.blocks.UCBlockInterfaces.*;
import com.thesecretden.uncovered_tech.main.common.blocks.multiblock.UCTemplateMultiblock;
import com.thesecretden.uncovered_tech.main.common.blocks.multiblock.process.MultiblockProcess;
import com.thesecretden.uncovered_tech.main.common.blocks.multiblock.process.MultiblockProcessInMachine;
import com.thesecretden.uncovered_tech.main.common.blocks.multiblock.process.MultiblockProcessInWorld;
import com.thesecretden.uncovered_tech.main.common.util.EnergyHelper;
import com.thesecretden.uncovered_tech.main.common.util.MultiblockCapability;
import com.thesecretden.uncovered_tech.main.common.util.UCUtils;
import com.thesecretden.uncovered_tech.main.common.util.inventory.IUCInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class PoweredMultiblockTileEntity<T extends PoweredMultiblockTileEntity<T, R>, R extends MultiRecipe> extends MultiblockPartTileEntity<T> implements IUCInventory, IProcessTE, IComparatorOverride {
    public final AveragingEnergyStorage energyStorage;
    protected final MultiblockCapability<IEnergyStorage> energyCap;

    private final MutableInt cachedComparatorValue = new MutableInt(-1);

    public PoweredMultiblockTileEntity(UCTemplateMultiblock multiblockInstance, int energyCapacity, boolean redstoneControl, BlockEntityType<? extends T> type, BlockPos pos, BlockState state) {
        super(multiblockInstance, type, redstoneControl, pos, state);
        this.energyStorage = new AveragingEnergyStorage(energyCapacity);
        this.energyCap = MultiblockCapability.make(this, te -> te.energyCap, PoweredMultiblockTileEntity::master, registerEnergyInput(this.energyStorage));
    }

    @Override
    public void readCustomNBT(CompoundTag tag, boolean descPacket) {
        super.readCustomNBT(tag, descPacket);
        EnergyHelper.deserializeFrom(energyStorage, tag);
        if (!descPacket || shouldSyncProcessQueue()) {
            ListTag processTag = tag.getList("processQueue", Tag.TAG_COMPOUND);
            processQueue.clear();
            for (int i = 0; i < processTag.size(); i++) {
                CompoundTag tag1 = processTag.getCompound(i);
                if (tag1.contains("recipe")) {
                    int processTick = tag1.getInt("process_processTick");
                    MultiblockProcess<R> process = loadProcessFromNBT(tag1);
                    if (process != null) {
                        process.processTick = processTick;
                        processQueue.add(process);
                    }
                }
            }
        }
        if (descPacket)
            renderAsActiveClient = tag.getBoolean("renderActive");
    }

    @Override
    public void writeCustomNBT(CompoundTag tag, boolean descPacket) {
        super.writeCustomNBT(tag, descPacket);
        if (!descPacket || shouldSyncProcessQueue) {
            EnergyHelper.serializeTo(energyStorage, tag);
            ListTag processTag = new ListTag();
            for (MultiblockProcess<?> process : this.processQueue)
                processTag.accept(writeProcessToNBT(process));
            tag.put("processQueue", processTag);
        }
        if (descPacket)
            tag.putBoolean("renderActive", renderAsActiveClient);
    }

    @Nullable
    protected abstract R getRecipeForId(Level level, ResourceLocation id);

    @Nullable
    protected MultiblockProcess<R> loadProcessFromNBT(CompoundTag tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("recipe"));
        if (isInWorldProcessingMachine())
            return MultiblockProcessInWorld.load(id, this::getRecipeForId, tag);
        else
            return MultiblockProcessInMachine.load(id, this::getRecipeForId, tag);
    }

    protected CompoundTag writeProcessToNBT(MultiblockProcess<?> process) {
        CompoundTag tag = new CompoundTag();
        tag.putString("recipe", process.getRecipeId().toString());
        tag.putInt("process_processTick", process.processTick);
        process.writeExtraDataToNBT(tag);
        return tag;
    }

    public abstract Set<MultiblockFace> getEnergyPos();

    public boolean isEnergyPos(Direction absoluteFace) {
        return getEnergyPos().contains(asRelativeFace(absoluteFace));
    }

    @NotNull
    @Override
    public <C> LazyOptional<C> getCapability(@NotNull Capability<C> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY && (side == null || isEnergyPos(side)))
            return energyCap.getAndCast();
        return super.getCapability(cap, side);
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (!isDummy()) {
            BlockPos nullPos = this.getOrigin();
            return new AABB(nullPos, TemplateMultiblock.withSettingsAndOffset(nullPos, new BlockPos(structureDimensions.get()), getIsMirrored(), multiblockInstance.untransformDirection(getFacing())));
        }
        return super.getRenderBoundingBox();
    }

    @Override
    public int getComparatorInputOverride() {
        if (!this.isRedstonePos())
            return 0;
        PoweredMultiblockTileEntity<?, ?> master = master();
        if (master == null)
            return 0;
        return master.getComparatorValueOnMaster();
    }

    protected int getComparatorValueOnMaster() {
        return UCUtils.calcRedstoneFromInventory(this);
    }

    public final List<MultiblockProcess<R>> processQueue = new ArrayList<>();
    public int tickedProcesses = 0;
    private boolean renderAsActiveClient = false;

    public void syncRenderActive() {
        boolean renderActive = shouldRenderAsActive();
        if (renderAsActiveClient == renderActive)
            return;
        renderAsActiveClient = renderActive;
        updateMasterBlock(null, true);
    }

    @Override
    public void tickServer() {
        syncRenderActive();
        if (isRSDisabled())
            return;

        int max = getMaxProcessPerTick();
        int i = 0;
        Iterator<MultiblockProcess<R>> processIterator = processQueue.iterator();
        tickedProcesses = 0;
        while (processIterator.hasNext() && i++ < max) {
            MultiblockProcess<R> process = processIterator.next();
            if (process.canProcess(this)) {
                process.doProcessTick(this);
                tickedProcesses++;
                updateMasterBlock(null, true);
            }
            if (process.clearProcess)
                processIterator.remove();
        }
        updateComparators(this, getRedstonePos(), cachedComparatorValue, getComparatorValueOnMaster());
    }

    protected boolean shouldSyncProcessQueue() {
        return true;
    }

    @Nullable
    public abstract IFluidTank[] getInternalTanks();

    @Nullable
    public abstract R findRecipeForInsertion(ItemStack inserting);

    @Nullable
    public abstract int[] getOutputSlots();

    @Nullable
    public abstract int[] getOutputTanks();

    public abstract boolean additionalCanProcessCheck(MultiblockProcess<R> process);

    public abstract void doProcessOutput(ItemStack output);

    public abstract void doProcessFluidOutput(FluidStack output);

    public abstract void onProcessFinish(MultiblockProcess<R> process);

    public abstract int getMaxProcessPerTick();

    public abstract int getProcessQueueMaxLength();

    public abstract float getMinProcessDistance(MultiblockProcess<R> process);

    public abstract boolean isInWorldProcessingMachine();

    public boolean addProcessToQueue(MultiblockProcess<R> process, boolean simulate) {
        return addProcessToQueue(process, simulate, false);
    }

    public boolean addProcessToQueue(MultiblockProcess<R> process, boolean simulate, boolean addToPrevious) {
        if (addToPrevious && process instanceof MultiblockProcessInWorld<R> newProcess) {
            for (MultiblockProcess<R> current : processQueue)
                if (current instanceof MultiblockProcessInWorld<R> existingProcesses && process.getRecipeId().equals(current.getRecipeId())) {
                    boolean canStack = true;
                    for (ItemStack old : existingProcesses.inputItems) {
                        for (ItemStack input : newProcess.inputItems)
                            if (ItemStack.isSame(old, input) && UCUtils.compareItemNBT(old, input))
                                if (old.getCount() + input.getCount() > old.getMaxStackSize()) {
                                    canStack = false;
                                    break;;
                                }
                        if (!canStack)
                            break;
                    }
                    if (canStack) {
                        if (!simulate)
                            for (ItemStack old : existingProcesses.inputItems) {
                                for (ItemStack input : newProcess.inputItems)
                                    if (ItemStack.isSame(old, input) && UCUtils.compareItemNBT(old, input)) {
                                        old.grow(input.getCount());
                                        break;
                                    }
                            }
                        return true;
                    }
                }
        }if (getProcessQueueMaxLength() < 0 || processQueue.size() < getProcessQueueMaxLength()) {
            float dist = 1;
            MultiblockProcess<R> p = null;
            iif (processQueue.size() > 0) {
            p = processQueue.get(processQueue.size() - 1);
            if (p != null)
                dist= p.processTick / (float)p.getMaxTicks(level);
            }
            if  (p != null && dist < getMinProcessDistance(p))
                return false;

            if (!simulate)
                processQueue.add(process);
            markContainingBlockForUpdate(null);
            markChunkDirty();
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public int[] getCurrentProcessesStep() {
        T master = master();
        if (master != this && master == null)
            return master.getCurrentProcessesStep();
        int [] ia = new int[processQueue.size()];
        for (int i = 0; i < ia.length; i++)
            ia[i] = processQueue.get(i).getMaxTicks(level);
        return ia;
    }

    @Nonnull
    @Override
    public int[] getCurrentProcessesMax() {
        T master = master();
        if (master != this && master != null)
            return master.getCurrentProcessesMax();
        int[] ia = new int[processQueue.size()];
        for (int i = 0; i < ia.length; i++)
            ia[i] = processQueue.get(i).getMaxTicks(level);
        return ia;
    }

    public final boolean shouldRenderAsActive() {
        if ( level != null && !level.isClientSide)
            return shouldRenderAsActiveImpl();
        else
            return renderAsActiveClient;
    }

    protected boolean shouldRenderAsActiveImpl() {
        return energyStorage.getEnergyStored() > 0 && !isRSDisabled() && !processQueue.isEmpty();
    }

    protected final MultiblockFace asRelativeFace(Direction absoluteFace) {
        return new MultiblockFace(posInMultiblock, RelativeBlockFace.from(getFacing().getOpposite(), getIsMirrored(), absoluteFace));
    }

    protected static record MultiblockFace(BlockPos posInMultiblock, RelativeBlockFace face) {
        public MultiblockFace(int x, int y, int z, RelativeBlockFace face) {
            this(new BlockPos(x, y, z), face);
        }
    }


}

























