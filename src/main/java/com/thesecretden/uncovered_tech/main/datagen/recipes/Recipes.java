package com.thesecretden.uncovered_tech.main.datagen.recipes;

import com.google.gson.JsonObject;
import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.api.UCTags;
import com.thesecretden.uncovered_tech.main.api.crafting.IngredientWithSize;
import com.thesecretden.uncovered_tech.main.api.crafting.builders.UCComplexAlloyRecipeBuilder;
import com.thesecretden.uncovered_tech.main.api.crafting.builders.UCSimpleAlloyRecipeBuilder;
import com.thesecretden.uncovered_tech.main.datagen.resources.RecipeMetals;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Consumer;

import static com.thesecretden.uncovered_tech.main.api.utils.UCTagUtils.createItemWrapper;

public class Recipes extends RecipeProvider {
    private final Path ADVANCEMENT_ROOT;
    private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();

    private static final int standardSmeltingTime = 200;

    public Recipes(DataGenerator gen) {
        super(gen);
        ADVANCEMENT_ROOT = gen.getOutputFolder().resolve("data/minecraft/advancements/recipes/root.json");
    }

    @Override
    protected void saveAdvancement(HashCache cache, JsonObject object, Path path) {
        if (path.equals(ADVANCEMENT_ROOT)) return;
        super.saveAdvancement(cache, object, path);
    }

    private void alloyRecipes(@Nonnull Consumer<FinishedRecipe> out) {
        UCSimpleAlloyRecipeBuilder simpleAlloyBuilder;
        UCComplexAlloyRecipeBuilder complexAlloyBuilder;

        for (RecipeMetals metal : RecipeMetals.values()) {
            RecipeMetals.AlloyProperties alloy = metal.getAlloyProperties();

            if (alloy != null) {
                IngredientWithSize[] ingredients = alloy.getAlloyIngredients();

                if (alloy.isSimple()) {
                    simpleAlloyBuilder = UCSimpleAlloyRecipeBuilder.builder(metal.getIngot(), alloy.getOutSize());
                    if (!metal.isNative())
                        simpleAlloyBuilder.addCondition(getTagCondition(metal.getIngot()));
                    for (ICondition condition : alloy.getConditions())
                        simpleAlloyBuilder.addCondition(condition);
                    for (IngredientWithSize ingredient : ingredients)
                        simpleAlloyBuilder.addInput(ingredient);
                    simpleAlloyBuilder.build(out, toRL("simple_alloy/" + metal.getName()));
                }

                complexAlloyBuilder = UCComplexAlloyRecipeBuilder.builder(metal.getIngot(), alloy.getOutSize());
                if (!metal.isNative())
                    complexAlloyBuilder.addCondition(getTagCondition(metal.getIngot()));
                for (ICondition condition : alloy.getConditions())
                    complexAlloyBuilder.addCondition(condition);
                complexAlloyBuilder.addIngredient("input", ingredients[0]);
                for (int i = 1; i < ingredients.length; i++)
                    complexAlloyBuilder.addInput(ingredients[i]);
                complexAlloyBuilder.setTime(200).setEnergy(5000).build(out, toRL("complex_alloy/" + metal.getName()));
            }
        }
    }

    private String toPath(ItemLike src) {
        return src.asItem().getRegistryName().getPath();
    }

    private ResourceLocation toRL(String stringLoc) {
        if (!stringLoc.contains("/"))
            stringLoc = "crafting/" + stringLoc;

        if (PATH_COUNT.containsKey(stringLoc)) {
            int count = PATH_COUNT.get(stringLoc) + 1;
            PATH_COUNT.put(stringLoc, count);
            return new ResourceLocation(UncoveredTech.MODID, stringLoc + count);
        }
        PATH_COUNT.put(stringLoc, 1);
        return new ResourceLocation(UncoveredTech.MODID, stringLoc);
    }

    @Nonnull
    private Ingredient makeIngredient(ItemLike input) {
        return Ingredient.of(input);
    }

    @Nonnull
    private Ingredient makeIngredient(TagKey<Item> input) {
        return Ingredient.of(input);
    }

    @Nonnull
    private Ingredient makeIngredientFromBlock(TagKey<Block> input) {
        TagKey<Item> itemTag = UCTags.getItemTag(input);
        return makeIngredient(itemTag);
    }

    public static ICondition getTagCondition(TagKey<?> tag) {
        return new NotCondition(new TagEmptyCondition(tag.location()));
    }

    public static ICondition getTagCondition(ResourceLocation tag) {
        return getTagCondition(createItemWrapper(tag));
    }

    private static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(item).build());
    }
}
