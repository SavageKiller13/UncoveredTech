package com.thesecretden.uncovered_tech.main.common.util.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class EmptyContainer implements Container {
    public static final Container INSTANCE = new EmptyContainer();

    private EmptyContainer() {}

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot,@Nonnull ItemStack stack) {}

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return true;
    }

    @Override
    public void clearContent() {}
}

























