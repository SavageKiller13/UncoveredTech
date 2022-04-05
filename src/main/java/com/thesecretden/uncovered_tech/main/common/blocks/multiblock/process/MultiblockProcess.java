package com.thesecretden.uncovered_tech.main.common.blocks.multiblock.process;

import com.thesecretden.uncovered_tech.main.api.crafting.MultiRecipe;
import com.thesecretden.uncovered_tech.main.common.blocks.generic.PoweredMultiblockTileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public abstract class MultiblockProcess<R extends MultiRecipe> {
    private final ResourceLocation recipeId;
    private final BiFunction<Level, ResourceLocation, R> getRecipe;
    public int processTick;
    private LevelDependentData<R> levelData;
    public boolean clearProcess = false;

    public MultiblockProcess(ResourceLocation recipeId, BiFunction<Level, ResourceLocation, R> getRecipe) {
        this.recipeId = recipeId;
        this.getRecipe = getRecipe;
        this.processTick = 0;
    }

    public MultiblockProcess(R recipe, BiFunction<Level, ResourceLocation, R> getRecipe) {
        this.recipeId = recipe.getId();
        this.getRecipe = getRecipe;
        this.processTick = 0;
        populateLevelData(recipe);
    }

    protected List<ItemStack> getRecipeItemOutputs(PoweredMultiblockTileEntity<?, R> multiblock) {
        R recipe = getLevelData(multiblock.getLevel()).recipe;
        if (recipe == null)
            return List.of();
        return recipe.getActualItemOutputs(multiblock);
    }

    protected List<FluidStack> getRecipeFluidOutputs(PoweredMultiblockTileEntity<?, R> multiblock) {
        R recipe = getLevelData(multiblock.getLevel()).recipe;
        if (recipe == null)
            return List.of();
        return recipe.getActualFluidOutputs(multiblock);
    }

    public boolean canProcess(PoweredMultiblockTileEntity<?, R> multiblock) {
        LevelDependentData<R> levelData = getLevelData(multiblock.getLevel());
        if (levelData.recipe == null)
            return true;
        if (multiblock.energyStorage.extractEnergy(levelData.energyPerTick, true) == levelData.energyPerTick) {
            List<ItemStack> outputs = getRecipeItemOutputs(multiblock);
            if (outputs != null && !outputs.isEmpty()) {
                int[] outputSlots = multiblock.getOutputSlots();
                for (ItemStack output : outputs)
                    if (!output.isEmpty()) {
                        boolean canOutput = false;
                        if (outputSlots == null)
                            canOutput = true;
                        else {
                            for (int iOutputSlot : outputSlots) {
                                ItemStack stack = multiblock.getInventory().get(iOutputSlot);
                                if (stack.isEmpty() || (ItemHandlerHelper.canItemStacksStack(stack, output) && stack.getCount() + output.getCount() <= multiblock.getSlotLimit(iOutputSlot))) {
                                    canOutput = true;
                                    break;
                                }
                            }
                        }
                        if (!canOutput)
                            return false;
                    }
            }
            List<FluidStack> fluidOutputs = levelData.recipe.getFluidOutputs();
            if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
                IFluidTank[] tanks = multiblock.getInternalTanks();
                int[] outputTanks = multiblock.getOutputTanks();
                for (FluidStack output : fluidOutputs)
                    if (output != null && output.getAmount() > 0) {
                        boolean canOutput = false;
                        if (tanks == null || outputTanks == null)
                            canOutput = true;
                        else {
                            for (int iOutputTank : outputTanks)
                                if (iOutputTank >= 0 && iOutputTank < tanks.length && tanks[iOutputTank] != null && tanks[iOutputTank].fill(output, IFluidHandler.FluidAction.SIMULATE) == output.getAmount()) {
                                    canOutput = true;
                                    break;
                                }
                        }
                        if (!canOutput)
                            return false;
                    }
            }
            return multiblock.additionalCanProcessCheck(this);
        }
        return false;
    }

    public void doProcessTick(PoweredMultiblockTileEntity<?, R> multiblock) {
        LevelDependentData<R> levelData = getLevelData(multiblock.getLevel());
        if (levelData.recipe == null) {
            this.clearProcess = true;
            return;
        }
        int energyExtracted = levelData.energyPerTick;
        int ticksAdded = 1;
        if (levelData.recipe.getMultipleProcessTicks() > 1) {
            int averageInsertion = multiblock.energyStorage.getAverageInsertion();
            averageInsertion = multiblock.energyStorage.extractEnergy(averageInsertion, true);
            if (averageInsertion > energyExtracted) {
                int possibleTicks = Math.min(averageInsertion / levelData.energyPerTick, Math.min(levelData.recipe.getMultipleProcessTicks(), levelData.maxTicks - this.processTick));
                if (possibleTicks > 1) {
                    ticksAdded = possibleTicks;
                    energyExtracted *= ticksAdded;
                }
            }
        }
        multiblock.energyStorage.extractEnergy(energyExtracted, false);
        this.processTick += ticksAdded;

        if (this.processTick >= levelData.maxTicks)
            this.processFinish(multiblock);
    }

    protected void processFinish(PoweredMultiblockTileEntity<?, R> multiblock) {
        List<ItemStack> outputs = getRecipeItemOutputs(multiblock);
        if (outputs != null && !outputs.isEmpty()) {
            int[] outputSlots = multiblock.getOutputSlots();
            for (ItemStack output : outputs)
                if (output.isEmpty())
                    if (outputSlots == null || multiblock.getInventory() == null)
                        multiblock.doProcessOutput(output.copy());
                    else {
                        for (int iOutputSlot : outputSlots) {
                            ItemStack stack = multiblock.getInventory().get(iOutputSlot);
                            if (stack.isEmpty()) {
                                multiblock.getInventory().set(iOutputSlot, output.copy());
                                break;
                            } else if(ItemHandlerHelper.canItemStacksStack(stack, output) && stack.getCount() + output.getCount() <= multiblock.getSlotLimit(iOutputSlot)) {
                                multiblock.getInventory().get(iOutputSlot).grow(output.getCount());
                                break;
                            }
                        }
                    }
        }

        List<FluidStack> fluidOutputs = getRecipeFluidOutputs(multiblock);
        if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
            IFluidTank[] tanks = multiblock.getInternalTanks();
            int[] outputTanks = multiblock.getOutputTanks();
            for (FluidStack output : fluidOutputs)
                if (output != null && output.getAmount() > 0) {
                    if (tanks == null || outputTanks == null)
                        multiblock.doProcessFluidOutput(output);
                    else {
                        for (int iOutputTank : outputTanks)
                            if (iOutputTank >= 0 && iOutputTank < tanks.length && tanks[iOutputTank] != null && tanks[iOutputTank].fill(output, IFluidHandler.FluidAction.SIMULATE) == output.getAmount()) {
                                tanks[iOutputTank].fill(output, IFluidHandler.FluidAction.EXECUTE);
                                break;
                            }
                    }
                }
        }

        multiblock.onProcessFinish(this);
        this.clearProcess = true;
    }

    public abstract void writeExtraDataToNBT(CompoundTag tag);

    protected LevelDependentData<R> getLevelData(Level level) {
        if (levelData == null)
            populateLevelData(getRecipe.apply(level, recipeId));
        return levelData;
    }

    private void populateLevelData(R recipe) {
        if (recipe != null) {
            int maxTicks = recipe.getTotalProcessTime();
            int energyPerTick = recipe.getTotalProcessEnergy() / maxTicks;
            this.levelData = new LevelDependentData<>(recipe, maxTicks, energyPerTick);
        } else
            this.levelData = new LevelDependentData<>(null, 20, 0);
    }

    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    public int getMaxTicks(Level level) {
        return getLevelData(level).maxTicks;
    }

    @Nullable
    public R getRecipe(Level level) {
        return getLevelData(level).recipe;
    }

    protected record LevelDependentData<R extends MultiRecipe>(@Nullable R recipe, int maxTicks, int energyPerTick) { }
}




















