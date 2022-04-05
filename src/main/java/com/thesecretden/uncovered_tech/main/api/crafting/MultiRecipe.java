package com.thesecretden.uncovered_tech.main.api.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

public abstract class MultiRecipe extends UCSerializableRecipe implements IMultiRecipe, IJEIRecipe {
    protected MultiRecipe(Lazy<ItemStack> outDummy, RecipeType<?> type, ResourceLocation id) {
        super(outDummy, type, id);
    }

    @Override
    public ItemStack getResultItem() {
        NonNullList<ItemStack> outputs = getItemOutputs();
        if (outputs != null && outputs.size() > 0)
            return outputs.get(0);
        return ItemStack.EMPTY;
    }

    private List<IngredientWithSize> inputList = new ArrayList<>(0);

    @Override
    public List<IngredientWithSize> getItemInputs() {
        return inputList;
    }

    protected void setInputListWithSizes(List<IngredientWithSize> inputList) {
        this.inputList = new ArrayList<>(inputList);
    }

    protected void setInputList(List<Ingredient> inputList) {
        this.inputList = inputList.stream().map(IngredientWithSize::new).collect(Collectors.toList());
    }

    protected Lazy<NonNullList<ItemStack>> outputList = Lazy.of(NonNullList::create);

    @Override
    public NonNullList<ItemStack> getItemOutputs() {
        return outputList.get();
    }

    protected List<FluidTagInput> fluidInputList;

    @Override
    public List<FluidTagInput> getFluidInputs() {
        return fluidInputList;
    }

    protected List<FluidStack> fluidOutputList;

    @Override
    public List<FluidStack> getFluidOutputs() {
        return fluidOutputList;
    }

    Lazy<Integer> totalProcessTime;

    @Override
    public int getTotalProcessTime() {
        return this.totalProcessTime.get();
    }

    Lazy<Integer> totalProcessEnergy;

    @Override
    public int getTotalProcessEnergy() {
        return this.totalProcessEnergy.get();
    }

    void setTimeAndEnergy(int time, int energy) {
        totalProcessEnergy = Lazy.of(() -> energy);
        totalProcessTime = Lazy.of(() -> time);
    }

    public void modifyTimeAndEnergy(DoubleSupplier timeMod, DoubleSupplier energyMod) {
        final Lazy<Integer> oldTime = totalProcessTime;
        final Lazy<Integer> oldEnergy = totalProcessEnergy;
        this.totalProcessTime = Lazy.of(() -> (int)(Math.max(1, oldTime.get() * timeMod.getAsDouble())));
        this.totalProcessEnergy = Lazy.of(() -> (int)(Math.max(1, oldEnergy.get() * energyMod.getAsDouble())));

    }
}
