package com.thesecretden.uncovered_tech.main.api.crafting;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public interface IIngredientWithSizeSerializer {
    IngredientWithSize parse(@Nonnull FriendlyByteBuf buf);
    void write(@Nonnull FriendlyByteBuf buf, @Nonnull IngredientWithSize ingredient);
    IngredientWithSize parse(@Nonnull JsonElement json);
    JsonElement write(@Nonnull IngredientWithSize ingredient);
}
