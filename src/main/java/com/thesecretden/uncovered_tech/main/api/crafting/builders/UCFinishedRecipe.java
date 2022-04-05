package com.thesecretden.uncovered_tech.main.api.crafting.builders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thesecretden.uncovered_tech.main.api.crafting.IngredientWithSize;
import com.thesecretden.uncovered_tech.main.api.crafting.UCRecipeSerializer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Recipe Class Code Used from Immersive Engineering. Credit to BluSunrize
public class UCFinishedRecipe<R extends UCFinishedRecipe<R>> implements FinishedRecipe {
    private final UCRecipeSerializer<?> serializer;
    private final List<Consumer<JsonObject>> writerFunctions;
    private ResourceLocation id;

    protected JsonArray inputArray = null;
    protected int inputCount = 0;
    protected int maxInputCount = 1;

    protected JsonArray resultArray = null;
    protected int resultCount = 0;
    protected int maxResultCount = 1;

    protected JsonArray conditions = null;

    protected UCFinishedRecipe(UCRecipeSerializer<?> serializer) {
        this.serializer = serializer;
        this.writerFunctions = new ArrayList<>();
    }

    protected boolean isComplete() {
        return true;
    }

    public void build(Consumer<FinishedRecipe> out, ResourceLocation id) {
        Preconditions.checkArgument(isComplete(), "Recipe not Complete");
        this.id = id;
        out.accept(this);
    }

    @SuppressWarnings("unchecked cast")
    public R addWriter(Consumer<JsonObject> writer) {
        Preconditions.checkArgument(id==null, "Recipe already finalized");
        this.writerFunctions.add(writer);
        return (R)this;
    }

    @SuppressWarnings("unchecked cast")
    public R addCondition(ICondition condition) {
        if (this.conditions==null) {
            this.conditions = new JsonArray();
            addWriter(jsonObject -> jsonObject.add("conditions", conditions));
        }
        this.conditions.add(CraftingHelper.serialize(condition));
        return (R)this;
    }

    // Setter for recipe time (machines)
    public R setTime(int time) {
        return addWriter(jsonObject -> jsonObject.addProperty("time", time));
    }

    // Setter for energy usage in recipe
    public R setEnergy(int energy) {
        return addWriter(jsonObject -> jsonObject.addProperty("energy", energy));
    }

    public R setMultipleResults(int maxResultCount) {
        this.resultArray = new JsonArray();
        this.maxResultCount = maxResultCount;
        return addWriter(jsonObject -> jsonObject.add("results", resultArray));
    }

    @SuppressWarnings("unchecked cast")
    public R addMultiResult(JsonElement object) {
        Preconditions.checkArgument(maxResultCount > 1, "Recipe doesn't support multiple results");
        Preconditions.checkArgument(resultCount < maxResultCount, "Recipe can only have " + maxResultCount + " results");
        resultArray.add(object);
        resultCount++;
        return (R)this;
    }

    public R addResult(ItemLike itemProvider) {
        return addResult(new ItemStack(itemProvider));
    }

    public R addResult(ItemStack stack) {
        if (resultArray != null)
            return addMultiResult(serializeItemStack(stack));
        else
            return addItem("result", stack);
    }

    public R addResult(Ingredient ingredient) {
        if (resultArray != null)
            return addMultiResult(ingredient.toJson());
        else
            return addWriter(jsonObject -> jsonObject.add("result", ingredient.toJson()));
    }

    public R addResult(IngredientWithSize ingredientWithSize) {
        if (resultArray != null)
            return addMultiResult(ingredientWithSize.serialize());
        else
            return addWriter(jsonObject -> jsonObject.add("result", ingredientWithSize.serialize()));
    }


    public R setUseInputArray(int maxInputCount, String key) {
        this.inputArray = new JsonArray();
        this.maxInputCount = maxInputCount;
        return addWriter(jsonObject -> jsonObject.add(key, inputArray));
    }

    public R setUseInputArray(int maxInputCount) {
        return setUseInputArray(maxInputCount, "inputs");
    }

    @SuppressWarnings("unchecked cast")
    public R addMultiInput(JsonElement object) {
        Preconditions.checkArgument(maxInputCount > 1, "This recipe doesn't support multiple inputs");
        Preconditions.checkArgument(inputCount < maxInputCount, "Recipe can only have " + maxInputCount + " inputs");
        inputArray.add(object);
        inputCount++;
        return (R)this;
    }

    public R addMultiInput(Ingredient ingredient) {
        return addMultiInput(ingredient.toJson());
    }

    public R addMultiInput(IngredientWithSize ingredientWithSize) {
        return addMultiInput(ingredientWithSize.serialize());
    }

    protected String genSafeInputKey() {
        Preconditions.checkArgument(inputCount < maxInputCount, "Recipe can only have " + maxInputCount + " inputs");
        String key = maxInputCount == 1 ? "input" : "input" + inputCount;
        inputCount++;
        return key;
    }

    public R addInput(ItemLike... itemProviders) {
        if (inputArray != null)
            return addMultiInput(Ingredient.of(itemProviders));
        else
            return addIngredient(genSafeInputKey(), itemProviders);
    }

    public R addInput(ItemStack... stacks) {
        if (inputArray != null)
            return addMultiInput(Ingredient.of(stacks));
        else
            return addIngredient(genSafeInputKey(), stacks);
    }

    public R addInput(TagKey<Item> tag) {
        if (inputArray != null)
            return addMultiInput(Ingredient.of(tag));
        else
            return addIngredient(genSafeInputKey(), tag);
    }

    public R addInput(Ingredient input) {
        if (inputArray != null)
            return addMultiInput(input);
        else
            return addIngredient(genSafeInputKey(), input);
    }

    public R addInput(IngredientWithSize input) {
        if (inputArray != null)
            return addMultiInput(input);
        else
            return addIngredient(genSafeInputKey(), input);
    }

    public JsonObject serializeItemStack(ItemStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("item", stack.getItem().getRegistryName().toString());
        if (stack.getCount() > 1)
            object.addProperty("count", stack.getCount());
        if (stack.hasTag())
            object.addProperty("nbt", stack.getTag().toString());
        return object;
    }

    protected R addSimpleItem(String key, ItemLike item) {
        return addWriter(json -> json.addProperty(key, item.asItem().getRegistryName().toString()));
    }

    public R addItem(String key, ItemLike item) {
        return addItem(key, new ItemStack(item));
    }

    public R addItem(String key, ItemStack stack) {
        Preconditions.checkArgument(!stack.isEmpty(), "You cannot add an empty ItemStack to a recipe");
        return addWriter(jsonObject -> jsonObject.add(key, serializeItemStack(stack)));
    }

    public R addIngredient(String key, ItemLike... itemProviders) {
        return addIngredient(key, Ingredient.of(itemProviders));
    }

    public R addIngredient(String key, ItemStack... stacks) {
        return addIngredient(key, Ingredient.of(stacks));
    }

    public R addIngredient(String key, TagKey<Item> tag) {
        return addIngredient(key, Ingredient.of(tag));
    }

    public R addIngredient(String key, Ingredient ingredient) {
        return addWriter(jsonObject -> jsonObject.add(key, ingredient.toJson()));
    }

    public R addIngredient(String key, IngredientWithSize ingredient) {
        return addWriter(jsonObject -> jsonObject.add(key, ingredient.serialize()));
    }




    @Override
    public void serializeRecipeData(JsonObject jsonObject) {
        for(Consumer<JsonObject> writer : this.writerFunctions) {
            writer.accept(jsonObject);
        }
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getType() {
        return serializer;
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }

    protected static JsonObject serializeStackWithChance(IngredientWithSize ingredient, float chance, ICondition... conditions) {
        JsonObject object = new JsonObject();
        object.addProperty("chance", chance);
        object.add("output", ingredient.serialize());
        if (conditions.length > 0) {
            JsonArray jsonArray = new JsonArray();
            for (ICondition condition : conditions)
                jsonArray.add(CraftingHelper.serialize(condition));

            object.add("conditions", jsonArray);
        }
        return object;
    }
}
















