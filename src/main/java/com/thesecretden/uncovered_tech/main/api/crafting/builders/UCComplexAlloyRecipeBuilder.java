package com.thesecretden.uncovered_tech.main.api.crafting.builders;

import com.google.gson.JsonArray;
import com.thesecretden.uncovered_tech.main.api.crafting.IngredientWithSize;
import com.thesecretden.uncovered_tech.main.api.crafting.UCComplexAlloyRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UCComplexAlloyRecipeBuilder extends UCFinishedRecipe<UCComplexAlloyRecipeBuilder> {
    private final JsonArray secondArray = new JsonArray();

    private UCComplexAlloyRecipeBuilder() {
        super(UCComplexAlloyRecipe.SERIALIZER.get());
        setMultipleResults(3);
        setUseInputArray(6, "extraInputs");
        addWriter(jsonObject -> {
            if (!secondArray.isEmpty())
                jsonObject.add("secondaries", secondArray);
        });
    }

    public static UCComplexAlloyRecipeBuilder builder(Item result) {
        return new UCComplexAlloyRecipeBuilder().addResult(result);
    }

    public static UCComplexAlloyRecipeBuilder builder(ItemStack result) {
        return new UCComplexAlloyRecipeBuilder().addResult(result);
    }

    public static UCComplexAlloyRecipeBuilder builder(TagKey<Item> result, int count) {
        return new UCComplexAlloyRecipeBuilder().addResult(new IngredientWithSize(result, count));
    }

    public UCComplexAlloyRecipeBuilder addSecondary(TagKey<Item> tag, float chance) {
        secondArray.add(serializeStackWithChance(new IngredientWithSize(tag), chance));
        return this;
    }
}
