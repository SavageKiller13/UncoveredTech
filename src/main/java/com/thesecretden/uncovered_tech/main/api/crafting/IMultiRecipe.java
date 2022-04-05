package com.thesecretden.uncovered_tech.main.api.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IMultiRecipe {
    List<IngredientWithSize> getItemInputs();

    default boolean shouldCheckItemAvailable() {
        return true;
    }

    List<FluidTagInput> getFluidInputs();

    NonNullList<ItemStack> getItemOutputs();

    default NonNullList<ItemStack> getActualItemOutputs(BlockEntity tile) {
        return getItemOutputs();
    }

    List<FluidStack> getFluidOutputs();

    default ItemStack getDisplayStack(ItemStack input) {
        for (IngredientWithSize ingredient : getItemInputs()) {
            if (ingredient.test(input)) {
                if (ingredient.hasNoMatchingItems())
                    return input;
                else
                    return ingredient.getMatchingStacks()[0];
            }
        }
        return ItemStack.EMPTY;
    }

    default List<FluidStack> getActualFluidOutputs(BlockEntity tile) {
        return getFluidOutputs();
    }

    int getTotalProcessTime();

    int getTotalProcessEnergy();

    int getMultipleProcessTicks();
}
