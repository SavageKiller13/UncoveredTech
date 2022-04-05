package com.thesecretden.uncovered_tech.main.api.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

public abstract class UCSerializableRecipe implements Recipe<Container> {
    public static final Lazy<ItemStack> LAZY_EMPTY = Lazy.of(() -> ItemStack.EMPTY);

    protected final Lazy<ItemStack> outDummy;
    protected final RecipeType<?> type;
    protected final ResourceLocation id;

    protected UCSerializableRecipe(Lazy<ItemStack> outDummy, RecipeType<?> type, ResourceLocation id) {
        this.outDummy = outDummy;
        this.type = type;
        this.id = id;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getToastSymbol() {
        return getUCSerializer().getIcon();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container p_44001_) {
        return this.outDummy.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return getUCSerializer();
    }

    protected abstract UCRecipeSerializer<?> getUCSerializer();

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }
}
