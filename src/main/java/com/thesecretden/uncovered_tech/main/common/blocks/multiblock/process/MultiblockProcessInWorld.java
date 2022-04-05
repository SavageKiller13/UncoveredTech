package com.thesecretden.uncovered_tech.main.common.blocks.multiblock.process;

import com.thesecretden.uncovered_tech.main.api.crafting.IngredientWithSize;
import com.thesecretden.uncovered_tech.main.api.crafting.MultiRecipe;
import com.thesecretden.uncovered_tech.main.common.blocks.generic.PoweredMultiblockTileEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.BiFunction;

public class MultiblockProcessInWorld<R extends MultiRecipe> extends MultiblockProcess<R> {
    public NonNullList<ItemStack> inputItems;
    protected float transformationPoint;

    public MultiblockProcessInWorld(ResourceLocation recipeId, BiFunction<Level, ResourceLocation, R> getRecipe, float transformationPoint, NonNullList<ItemStack> inputItem) {
        super(recipeId, getRecipe);
        this.inputItems = inputItem;
        this.transformationPoint = transformationPoint;
    }

    public List<ItemStack> getDisplayItem(Level level) {
        LevelDependentData<R> levelData = getLevelData(level);
        if (processTick / (float)levelData.maxTicks() > transformationPoint && levelData.recipe() != null) {
            List<ItemStack> list = levelData.recipe().getItemOutputs();
            if (!list.isEmpty())
                return list;
        }
        return inputItems;
    }

    public static <R extends MultiRecipe> MultiblockProcessInWorld<R> load(ResourceLocation recipeId, BiFunction<Level, ResourceLocation, R> getRecipe, CompoundTag tag) {
        NonNullList<ItemStack> inputs = NonNullList.withSize(tag.getInt("numInputs"), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inputs);
        float transformationPoint = tag.getFloat("process_transformationPoint");
        return new MultiblockProcessInWorld<>(recipeId, getRecipe, transformationPoint, inputs);
    }

    @Override
    public void writeExtraDataToNBT(CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, inputItems);
        tag.putInt("numInputs", inputItems.size());
        tag.putFloat("process_transformationPoint", transformationPoint);
    }

    @Override
    protected void processFinish(PoweredMultiblockTileEntity<?, R> multiblock) {
        super.processFinish(multiblock);
        int size = -1;

        R recipe = getLevelData(multiblock.getLevel()).recipe();
        if ( recipe == null)
            return;
        for (ItemStack stack : this.inputItems) {
            for (IngredientWithSize ingredient : recipe.getItemInputs())
                if (ingredient.test(stack)) {
                    size = ingredient.getCount();
                    break;;
                }
            if (size > 0 && stack.getCount() > size) {
                stack.split(size);
                processTick = 0;
                clearProcess = false;
            }
        }
    }
}
























