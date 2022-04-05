package com.thesecretden.uncovered_tech.main.api.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thesecretden.uncovered_tech.main.api.UCAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public abstract class UCRecipeSerializer<R extends Recipe<?>> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<R> {
    public abstract ItemStack getIcon();

    @Override
    public R fromJson(ResourceLocation id, JsonObject object) {
        if (CraftingHelper.processConditions(object, "conditions"))
            return readFromJson(id, object);
        return null;
    }

    protected static Lazy<ItemStack> readOutput(JsonElement outObject) {
        if (outObject.isJsonObject() && outObject.getAsJsonObject().has("item"))
            return Lazy.of(() -> ShapedRecipe.itemStackFromJson(outObject.getAsJsonObject()));
        IngredientWithSize outIngredient = IngredientWithSize.deserialize(outObject);
        return Lazy.of(() -> UCAPI.getPreferredStackbyMod(outIngredient.getMatchingStacks()));
    }

    @Nullable
    protected static StackWithChance readConditionStackWithChance(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        if (CraftingHelper.processConditions(object, "conditions")) {
            float chance = GsonHelper.getAsFloat(object, "chance");
            Lazy<ItemStack> stack = readOutput(object.get("output"));
            return new StackWithChance(stack, chance);
        }
        return null;
    }

    public abstract R readFromJson(ResourceLocation recipeId, JsonObject object);

    protected static Lazy<ItemStack> readLazyStack(FriendlyByteBuf buf) {
        ItemStack stack = buf.readItem();
        return Lazy.of(() -> stack);
    }

    protected static void writeLazyStack(FriendlyByteBuf buf, Lazy<ItemStack> stack) {
        buf.writeItem(stack.get());
    }
}






























