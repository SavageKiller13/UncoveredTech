package com.thesecretden.uncovered_tech.main.common.util.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IUCInventory {
    @Nullable
    NonNullList<ItemStack> getInventory();

    boolean isStackValid(int slot, ItemStack stack);

    int getSlotLimit(int slot);

    void doGraphicalUpdates();

    default NonNullList<ItemStack> getDroppedItems() {
        return getInventory();
    }

    default int getComparatedSize() {
        return getInventory() != null ? getInventory().size() : 0;
    }
}
