package com.thesecretden.uncovered_tech.main.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ComparableItemStack {
    public ItemStack stack;
    public boolean useNBT;

    public ComparableItemStack(ItemStack stack) {
        this(stack, false);
    }

    public ComparableItemStack(ItemStack stack, boolean copy) {
        if (stack == null)
            throw new RuntimeException("A ComparableItemStack with null as an Item cannot be instantiated!");
        this.stack = stack;
        if (copy)
            copy();
    }

    public static ComparableItemStack create(ItemStack stack, boolean copy) {
        return create(stack, copy, stack.hasTag() && !stack.getOrCreateTag().isEmpty());
    }

    public static ComparableItemStack create(ItemStack stack, boolean copy, boolean useNBT) {
        ComparableItemStack compStack = new ComparableItemStack(stack, copy);
        compStack.setUseNBT(useNBT);
        return compStack;
    }

    public void copy() {
        stack = stack.copy();
    }

    public ComparableItemStack setUseNBT(boolean useNBT) {
        this.useNBT = useNBT;
        return this;
    }

    @Override
    public String toString() {
        return "ComparableStack: {" + this.stack.toString() + "}; checkNBT: " + this.useNBT;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ComparableItemStack))
            return false;

        ItemStack otherStack = ((ComparableItemStack)object).stack;
        if (!ItemStack.isSame(stack, otherStack))
            return false;
        if (this.useNBT)
            return ItemStack.tagMatches(stack, otherStack);
        return true;
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.put("stack", stack.save(new CompoundTag()));
        tag.putBoolean("useNBT", useNBT);
        return tag;
    }

    public static ComparableItemStack readFromNBT(CompoundTag tag) {
        ComparableItemStack compStack = new ComparableItemStack(ItemStack.of(tag.getCompound("stack")), false);
        compStack.useNBT = tag.getBoolean("useNBT");
        return compStack;
    }
}
