package com.thesecretden.uncovered_tech.main.common.gui;

import com.thesecretden.uncovered_tech.main.common.blocks.UCBlockInterfaces;
import com.thesecretden.uncovered_tech.main.common.util.inventory.IUCInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class TileEntityInventory implements Container {
    final BlockEntity tile;
    final IUCInventory inv;
    final AbstractContainerMenu eventHandler;

    public TileEntityInventory(BlockEntity tile, AbstractContainerMenu eventHandler) {
        this.tile = tile;
        this.inv = (IUCInventory) tile;
        this.eventHandler = eventHandler;
    }

    @Override
    public int getContainerSize() {
        return inv.getInventory().size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inv.getInventory())
            if (!stack.isEmpty())
                return false;

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inv.getInventory().get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = inv.getInventory().get(slot);
        if (!stack.isEmpty()) {
            if (stack.getCount() <= amount)
                inv.getInventory().set(slot, ItemStack.EMPTY);
            else {
                stack = stack.split(amount);
                if (stack.getCount() == 0)
                    inv.getInventory().set(slot, ItemStack.EMPTY);
            }
            eventHandler.slotsChanged(this);
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inv.getInventory().get(slot).copy();
        inv.getInventory().set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inv.getInventory().set(slot, stack);
        eventHandler.slotsChanged(this);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        tile.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return isValidForPlayer(tile, player);
    }

    public static boolean isValidForPlayer(BlockEntity tileEntity, Player player) {
        if (tileEntity instanceof UCBlockInterfaces.IInteractionObjectUC<?> interactionObject && !interactionObject.canUseGui(player))
            return false;
        return !tileEntity.isRemoved() && Vec3.atCenterOf(tileEntity.getBlockPos()).distanceToSqr(player.position()) < 64;
    }

    @Override
    public void startOpen(Player player) {}

    @Override
    public void stopOpen(Player player) {
        inv.doGraphicalUpdates();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return inv.isStackValid(slot, stack);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inv.getInventory().size(); i++)
            inv.getInventory().set(i, ItemStack.EMPTY);
    }
}






















