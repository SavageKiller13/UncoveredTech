package com.thesecretden.uncovered_tech.main.common.util.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class UCInventoryHandler implements IItemHandlerModifiable {
    int slots;
    IUCInventory inv;
    int slotOffset;
    boolean[] canInsert;
    boolean[] canExtract;

    public UCInventoryHandler(int slots, IUCInventory inv, int slotOffset, boolean[] canInsert, boolean[] canExtract) {
        this.slots = slots;
        this.inv = inv;
        this.slotOffset = slotOffset;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    public UCInventoryHandler(int slots, IUCInventory inv) {
        this(slots, inv, 0, new boolean[slots], new boolean[slots]);
        for (int i = 0; i < slots; i++)
            this.canExtract[i] = this.canInsert[i] = true;
    }

    public UCInventoryHandler(int slots, IUCInventory inv, int slotOffset, boolean canInsert, boolean canExtract) {
        this(slots, inv, slotOffset, new boolean[slots], new boolean[slots]);
        for (int i = 0; i < slots; i++) {
            this.canInsert[i] = canInsert;
            this.canExtract[i] = canExtract;
        }
    }

    public UCInventoryHandler blockInsert(int... blockedSlots) {
        for (int s : blockedSlots)
            this.canInsert[s] = false;
        return this;
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.inv.getInventory().get(this.slotOffset + slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!canInsert[slot] || stack.isEmpty())
            return stack;

        if (!inv.isStackValid(this.slotOffset + slot, stack))
            return stack;

        int offsetSlot = this.slotOffset + slot;
        ItemStack currentStack = inv.getInventory().get(offsetSlot);

        if (currentStack.isEmpty()) {
            int accepted = Math.min(stack.getMaxStackSize(), inv.getSlotLimit(offsetSlot));
            if (accepted < stack.getCount()) {
                stack = stack.copy();
                if (!simulate) {
                    inv.getInventory().set(offsetSlot, stack.split(accepted));
                    inv.doGraphicalUpdates();
                } else
                    stack.shrink(accepted);
                return stack;
            } else {
                if (!simulate) {
                    inv.getInventory().set(offsetSlot, stack.copy());
                    inv.doGraphicalUpdates();
                }
                return ItemStack.EMPTY;
            }
        } else {
            if (!ItemHandlerHelper.canItemStacksStack(stack, currentStack))
                return stack;

            int accepted = Math.min(stack.getMaxStackSize(), inv.getSlotLimit(offsetSlot)) - currentStack.getCount();
            if (accepted < stack.getCount()) {
                stack = stack.copy();
                if (!simulate) {
                    ItemStack newStack = stack.split(accepted);
                    newStack.grow(currentStack.getCount());
                    inv.getInventory().set(offsetSlot, newStack);
                    inv.doGraphicalUpdates();
                } else
                    stack.shrink(accepted);
                return stack;
            } else {
                if (!simulate) {
                    ItemStack newStack = stack.copy();
                    newStack.grow(currentStack.getCount());
                    inv.getInventory().set(offsetSlot, newStack);
                    inv.doGraphicalUpdates();
                }
                return ItemStack.EMPTY;
            }
        }
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!canExtract[slot] || amount == 0)
            return ItemStack.EMPTY;

        int offsetSlot = this.slotOffset + slot;
        ItemStack currentStack = inv.getInventory().get(offsetSlot);

        if (currentStack.isEmpty())
            return ItemStack.EMPTY;

        int extracted = Math.min(currentStack.getCount(), amount);

        ItemStack copy = currentStack.copy();
        copy.setCount(extracted);
        if (!simulate) {
            if (extracted < currentStack.getCount())
                currentStack.shrink(extracted);
            else
                currentStack = ItemStack.EMPTY;
            inv.getInventory().set(offsetSlot, currentStack);
            inv.doGraphicalUpdates();
        }
        return copy;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return canInsert[slot] && inv.isStackValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        inv.getInventory().set(this.slotOffset + slot, stack);
        inv.doGraphicalUpdates();
    }
}






























