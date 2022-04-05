package com.thesecretden.uncovered_tech.main.common.gui;

import com.mojang.datafixers.util.Pair;
import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.common.gui.sync.GenericContainerData;
import com.thesecretden.uncovered_tech.main.common.gui.sync.GenericDataSerializers;
import com.thesecretden.uncovered_tech.main.common.network.MessageContainerData;
import com.thesecretden.uncovered_tech.main.common.util.inventory.IUCInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = UncoveredTech.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UCBaseContainer<T extends BlockEntity> extends AbstractContainerMenu {
    public T tile;
    @Nullable
    public Container inv;
    public int slotCount;
    private final List<GenericContainerData<?>> genericData = new ArrayList<>();
    private final List<ServerPlayer> usingPlayers = new ArrayList<>();

    public UCBaseContainer(MenuType<?> type, T tile, int id) {
        super(type, id);
        this.tile = tile;
        if (tile instanceof IUCInventory)
            this.inv = new TileEntityInventory(tile, this);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return inv != null && inv.stillValid(player);
    }

    public void addGenericData(GenericContainerData<?> newData) {
        genericData.add(newData);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        List<Pair<Integer, GenericDataSerializers.DataPair<?>>> toSync = new ArrayList<>();
        for (int i = 0; i < genericData.size(); i++) {
            GenericContainerData<?> data = genericData.get(i);
            if (data.needsUpdate())
                toSync.add(Pair.of(i, data.dataPair()));
        }

        if (!toSync.isEmpty())
            for (ServerPlayer player : usingPlayers)
                UncoveredTech.packetHandler.sendTo(new MessageContainerData(toSync), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public void receiveSync(List<Pair<Integer, GenericDataSerializers.DataPair<?>>> synced) {
        for (Pair<Integer, GenericDataSerializers.DataPair<?>> syncElement : synced)
            genericData.get(syncElement.getFirst()).processSync(syncElement.getSecond().data());
    }

    @Override
    public void clicked(int id, int dragType, ClickType type, Player player) {
        Slot slot = id < 0 ? null : this.slots.get(id);
        if (!(slot instanceof UCSlot.ItemHandlerGhost)) {
            super.clicked(id, dragType, type, player);
            return;
        }

        ItemStack stackSlot = slot.getItem();

        if (dragType == 2)
            slot.set(ItemStack.EMPTY);
        else if (dragType == 0 || dragType == 1) {
            ItemStack stackHeld = getCarried();
            int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
            if (dragType == 1)
                amount = 1;
            if (stackSlot.isEmpty())
                if (!stackHeld.isEmpty() && slot.mayPlace(stackHeld))
                    slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
            else if (stackHeld.isEmpty())
                slot.set(ItemStack.EMPTY);
            else if (slot.mayPlace(stackHeld)) {
                if (ItemStack.isSame(stackSlot, stackHeld))
                    stackSlot.grow(amount);
                else
                    slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
            }
            if (stackSlot.getCount() > slot.getMaxStackSize())
                stackSlot.setCount(slot.getMaxStackSize());
        } else if (dragType == 5) {
            ItemStack stackHeld = getCarried();
            int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
            if (!slot.hasItem())
                slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);
        if  (slotObject != null && slotObject.hasItem()) {
            ItemStack stack = slotObject.getItem();
            newStack = stack.copy();
            if (slot < slotCount)
                if (!this.moveItemStackTo(stack, slotCount, this.slots.size(), true))
                    return ItemStack.EMPTY;
            else if (!this.moveItemStackToWithMayPlace(stack, 0, slotCount))
                return ItemStack.EMPTY;

            if (stack.isEmpty())
                slotObject.set(ItemStack.EMPTY);
            else
                slotObject.setChanged();
        }
        return newStack;
    }

    protected boolean moveItemStackToWithMayPlace(ItemStack stack, int startIndex, int endIndex) {
        boolean inAllowedRange = true;
        int allowedStart = startIndex;
        for (int i = startIndex; i < endIndex; i++) {
            boolean mayPlace = this.slots.get(i).mayPlace(stack);
            if (inAllowedRange && !mayPlace) {
                if (moveItemStackTo(stack, allowedStart, i, false))
                    return true;
                inAllowedRange = false;
            } else if (!inAllowedRange && mayPlace) {
                allowedStart = i;
                inAllowedRange = true;
            }
        }
        return inAllowedRange && moveItemStackTo(stack, allowedStart, endIndex, false);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (inv != null)
            this.inv.stopOpen(player);
    }

    public void receiveMessageFromScreen(CompoundTag tag) {}

    @SubscribeEvent
    public static void onContainerOpened(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof UCBaseContainer<?> ucContainer && event.getPlayer() instanceof ServerPlayer player) {
            ucContainer.usingPlayers.add(player);
            List<Pair<Integer, GenericDataSerializers.DataPair<?>>> list = new ArrayList<>();
            for (int i = 0; i < ucContainer.genericData.size(); i++)
                list.add(Pair.of(i, ucContainer.genericData.get(i).dataPair()));
            UncoveredTech.packetHandler.sendTo(new MessageContainerData(list), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    @SubscribeEvent
    public static void onContainerClosed(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof UCBaseContainer<?> ucContainer && event.getPlayer() instanceof ServerPlayer player)
            ucContainer.usingPlayers.remove(player);
    }
}



























