package com.thesecretden.uncovered_tech.main.api.crafting.builders;

import com.thesecretden.uncovered_tech.main.api.crafting.IngredientWithSize;
import com.thesecretden.uncovered_tech.main.api.crafting.UCSimpleAlloyRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UCSimpleAlloyRecipeBuilder extends UCFinishedRecipe<UCSimpleAlloyRecipeBuilder> {
    private UCSimpleAlloyRecipeBuilder() {
        super(UCSimpleAlloyRecipe.SERIALIZER.get());
        this.maxInputCount = 2;
        this.setTime(200);
    }

    public static UCSimpleAlloyRecipeBuilder builder(Item result) {
        return new UCSimpleAlloyRecipeBuilder().addResult(result);
    }

    public static UCSimpleAlloyRecipeBuilder builder(ItemStack result) {
        return new UCSimpleAlloyRecipeBuilder().addResult(result);
    }

    public static UCSimpleAlloyRecipeBuilder builder(TagKey<Item> result, int count) {
        return new UCSimpleAlloyRecipeBuilder().addResult(new IngredientWithSize(result, count));
    }
}

























