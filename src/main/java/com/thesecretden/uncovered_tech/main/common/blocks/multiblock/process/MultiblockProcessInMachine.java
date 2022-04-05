package com.thesecretden.uncovered_tech.main.common.blocks.multiblock.process;

import com.thesecretden.uncovered_tech.main.api.crafting.FluidTagInput;
import com.thesecretden.uncovered_tech.main.api.crafting.IngredientWithSize;
import com.thesecretden.uncovered_tech.main.api.crafting.MultiRecipe;
import com.thesecretden.uncovered_tech.main.api.utils.UCIngredientUtils;
import com.thesecretden.uncovered_tech.main.common.blocks.generic.PoweredMultiblockTileEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class MultiblockProcessInMachine<R extends MultiRecipe> extends MultiblockProcess<R> {
    protected final int[] inputSlots;
    protected int[] inputAmounts = null;
    protected int[] inputTanks = new int[0];

    public MultiblockProcessInMachine(ResourceLocation recipeId, BiFunction<Level, ResourceLocation, R> getRecipe, int... inputSlots) {
        super(recipeId, getRecipe);
        this.inputSlots = inputSlots;
    }

    public MultiblockProcessInMachine(R recipe, BiFunction<Level, ResourceLocation, R> getRecipe, int... inputSlots) {
        super(recipe, getRecipe);
        this.inputSlots = inputSlots;
    }

    public MultiblockProcessInMachine<R> setInputTanks(int... inputTanks) {
        this.inputTanks = inputTanks;
        return this;
    }

    public MultiblockProcessInMachine<R> setInputAmounts(int... inputAmounts) {
        this.inputAmounts = inputAmounts;
        return this;
    }

    public int[] getInputSlots() {
        return this.inputSlots;
    }

    @Nullable
    public int[] getInputAmounts() {
        return this.inputAmounts;
    }

    public int[] getInputTanks() {
        return this.inputTanks;
    }

    protected List<IngredientWithSize> getRecipeItemInputs(PoweredMultiblockTileEntity<?, R> multiblock) {
        R recipe = getLevelData(multiblock.getLevel()).recipe();
        return recipe == null ? List.of() : recipe.getItemInputs();
    }

    protected List<FluidTagInput> getRecipeFluidInputs(PoweredMultiblockTileEntity<?, R> multiblock) {
        R recipe = getLevelData(multiblock.getLevel()).recipe();
        return recipe == null ? List.of() : recipe.getFluidInputs();
    }

    @Override
    public void doProcessTick(PoweredMultiblockTileEntity<?, R> multiblock) {
        R recipe = getLevelData(multiblock.getLevel()).recipe();
        if (recipe == null)
            return;
        NonNullList<ItemStack> inv = multiblock.getInventory();
        if (recipe.shouldCheckItemAvailable() && recipe.getItemInputs() != null && inv != null) {
            NonNullList<ItemStack> query = NonNullList.withSize(inputSlots.length, ItemStack.EMPTY);
            for (int i = 0; i < inputSlots.length; i++)
                if (inputSlots[i] >= 0 && inputSlots[i] < inv.size())
                    query.set(i, multiblock.getInventory().get(inputSlots[i]));
            if (!UCIngredientUtils.stacksMatchIngredientWithSizeList(recipe.getItemInputs(), query)) {
                this.clearProcess = true;
                return;
            }
        }
        super.doProcessTick(multiblock);
    }

    @Override
    protected void processFinish(PoweredMultiblockTileEntity multiblock) {
        super.processFinish(multiblock);
        NonNullList<ItemStack> inv = multiblock.getInventory();
        List<IngredientWithSize> itemInputList = this.getRecipeItemInputs(multiblock);
        if (inv != null && this.inputSlots != null & itemInputList != null) {
            if (this.inputAmounts != null && this.inputSlots.length == this.inputAmounts.length) {
                for (int i = 0; i < this.inputSlots.length; i++)
                    if (this.inputAmounts[i] > 0)
                        inv.get(this.inputSlots[i]).shrink(this.inputAmounts[i]);
            } else {
                for (IngredientWithSize ingredient : new ArrayList<>(itemInputList)) {
                    int ingredientSize = ingredient.getCount();
                    for (int slot : this.inputSlots)
                        if (!inv.get(slot).isEmpty() && ingredient.test(inv.get(slot))) {
                            int taken = Math.min(inv.get(slot).getCount(), ingredientSize);
                            inv.get(slot).shrink(taken);
                            if (inv.get(slot).getCount() <= 0)
                                inv.set(slot, ItemStack.EMPTY);
                            if ((ingredientSize -= taken) <= 0)
                                break;
                        }
                }
            }
        }

        IFluidTank[] tanks = multiblock.getInternalTanks();
        List<FluidTagInput> fluidInputList = this.getRecipeFluidInputs(multiblock);
        if (tanks != null && this.inputTanks != null && fluidInputList != null) {
            for (FluidTagInput ingredient : new ArrayList<>(fluidInputList)) {
                int ingredientSize = ingredient.getAmount();
                for (int tank : this.inputTanks)
                    if (tanks[tank] != null && ingredient.testIgnoringAmount(tanks[tank].getFluid())) {
                        int taken = Math.min(tanks[tank].getFluidAmount(), ingredientSize);
                        tanks[tank].drain(taken, IFluidHandler.FluidAction.EXECUTE);
                        if ((ingredientSize -= taken) <= 0)
                            break;
                    }
            }
        }
    }

    public static <R extends MultiRecipe> MultiblockProcessInMachine<R> load(ResourceLocation recipeId, BiFunction<Level, ResourceLocation, R> getRecipe, CompoundTag tag) {
        return new MultiblockProcessInMachine<>(recipeId, getRecipe, tag.getIntArray("process_inputSlots")).setInputAmounts(tag.getIntArray("process_inputAmounts")).setInputTanks(tag.getIntArray("process_inputTanks"));
    }

    @Override
    public void writeExtraDataToNBT(CompoundTag tag) {
        if (inputSlots != null)
            tag.putIntArray("process_inputSlots", inputSlots);
        if (inputAmounts != null)
            tag.putIntArray("process_inputAmounts", inputAmounts);
        if (inputTanks != null)
            tag.putIntArray("process_inputTanks", inputTanks);
    }
}

























