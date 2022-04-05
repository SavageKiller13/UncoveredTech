package com.thesecretden.uncovered_tech.main.api.crafting;

import com.thesecretden.uncovered_tech.main.api.crafting.cache.CachedRecipeList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class UCSimpleAlloyRecipe extends UCSerializableRecipe {
    public static RecipeType<UCSimpleAlloyRecipe> TYPE;
    public static RegistryObject<UCRecipeSerializer<UCSimpleAlloyRecipe>> SERIALIZER;
    public static final CachedRecipeList<UCSimpleAlloyRecipe> RECIPES = new CachedRecipeList<>(() -> TYPE, UCSimpleAlloyRecipe.class);

    public final IngredientWithSize input0;
    public final IngredientWithSize input1;
    public final Lazy<ItemStack> output;
    public final int time;

    public UCSimpleAlloyRecipe(ResourceLocation id, Lazy<ItemStack> output, IngredientWithSize input0, IngredientWithSize input1, int time) {
        super(output, TYPE, id);
        this.output = output;
        this.input0 = input0;
        this.input1 = input1;
        this.time = time;
    }

    @Override
    protected UCRecipeSerializer<UCSimpleAlloyRecipe> getUCSerializer() {
        return SERIALIZER.get();
    }

    @Override
    public ItemStack getResultItem() {
        return this.output.get();
    }

    public boolean matches(ItemStack input0, ItemStack input1) {
        if (this.input0.test(input0) && this.input1.test(input1))
            return true;
        else if (this.input0.test(input1) && this.input1.test(input0))
            return true;
        else
            return false;
    }

    public static UCSimpleAlloyRecipe findRecipe(Level level, ItemStack input0, ItemStack input1, @Nullable UCSimpleAlloyRecipe hint) {
        if (input0.isEmpty() || input1.isEmpty())
            return null;
        if (hint != null && hint.matches(input0, input1))
            return hint;
        for (UCSimpleAlloyRecipe recipe : RECIPES.getRecipes(level))
            if (recipe.matches(input0, input1))
                return recipe;
        return null;
    }
}
