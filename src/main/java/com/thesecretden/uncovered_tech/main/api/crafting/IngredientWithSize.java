package com.thesecretden.uncovered_tech.main.api.crafting;

import com.thesecretden.uncovered_tech.main.api.utils.SetRestrictedField;
import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class IngredientWithSize implements Predicate<ItemStack> {
    public static final SetRestrictedField<IIngredientWithSizeSerializer> SERIALIZER = SetRestrictedField.common();
    protected final Ingredient basePredicate;
    protected final int count;

    public IngredientWithSize(Ingredient basePredicate, int count) {
        this.basePredicate = basePredicate;
        this.count = count;
    }

    public IngredientWithSize(Ingredient basePredicate) {
        this(basePredicate, 1);
    }

    public IngredientWithSize(TagKey<Item> basePredicate, int count) {
        this(Ingredient.of(basePredicate), 1);
    }

    public IngredientWithSize(TagKey<Item> basePredicate) {
        this(basePredicate, 1);
    }

    public static IngredientWithSize deserialize(JsonElement input) {
        return SERIALIZER.getValue().parse(input);
    }

    public static IngredientWithSize read(FriendlyByteBuf input) {
        return SERIALIZER.getValue().parse(input);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null)
            return false;
        return basePredicate.test(stack) && stack.getCount() >= this.count;
    }

    @Nonnull
    public ItemStack[] getMatchingStacks() {
        ItemStack[] baseStacks = basePredicate.getItems();
        ItemStack[] ret = new ItemStack[baseStacks.length];
        for (int i = 0; i < baseStacks.length; i++)
            ret[i] = ItemHandlerHelper.copyStackWithSize(baseStacks[i], this.count);
        return ret;
    }

    @Nonnull
    public List<ItemStack> getMatchingStackList() {
        return Arrays.asList(getMatchingStacks());
    }

    @Nonnull
    public JsonElement serialize() {
        return SERIALIZER.getValue().write(this);
    }

    public boolean hasNoMatchingItems() {
        return basePredicate.isEmpty();
    }

    public int getCount() {
        return count;
    }

    public Ingredient getBaseIngredient() {
        return basePredicate;
    }

    public IngredientWithSize withSize(int size) {
        return new IngredientWithSize(this.basePredicate, size);
    }

    public static IngredientWithSize of(ItemStack stack) {
        return new IngredientWithSize(Ingredient.of(stack), stack.getCount());
    }

    public ItemStack getRandomizedExampleStack(int rand) {
        ItemStack[] all = getMatchingStacks();
        if (all.length == 0)
            return ItemStack.EMPTY;
        else
            return all[(rand/20) % all.length];
    }

    public boolean testIgnoringSize(ItemStack stack) {
        return basePredicate.test(stack);
    }

    public void write(FriendlyByteBuf out) {
        SERIALIZER.getValue().write(out, this);
    }
}























