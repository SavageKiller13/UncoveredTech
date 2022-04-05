package com.thesecretden.uncovered_tech.main.common.items;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.common.util.inventory.UCItemStackHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class InternalStorageItem extends UCBaseItem {
    public InternalStorageItem(Properties properties) {
        super(properties);
    }

    public abstract int getSlotCount();

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag tag) {
        if (!stack.isEmpty())
            return new UCItemStackHandler(stack);
        return null;
    }

    public void setContainedItems(ItemStack stack, NonNullList<ItemStack> inv) {
        LazyOptional<IItemHandler> lazyHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        lazyHandler.ifPresent(handler -> {
            if (handler instanceof IItemHandlerModifiable) {
                if (inv.size() != handler.getSlots())
                    throw new IllegalArgumentException("Parameter inventory has " + inv.size() + " slots, capability inventory has " + handler.getSlots());
                for (int i = 0; i < handler.getSlots(); i++)
                    ((IItemHandlerModifiable)handler).setStackInSlot(i, inv.get(i));
            } else
                UncoveredTech.LOGGER.warn("No valid inventory handler for " + stack);
        });
    }

    public NonNullList<ItemStack> getContainedItems(ItemStack stack) {
        LazyOptional<IItemHandler> lazyHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        Optional<NonNullList<ItemStack>> returnItemList = lazyHandler.map(handler -> {
            if (handler instanceof UCItemStackHandler)
                return ((UCItemStackHandler)handler).getContainedItems();
            else {
                UncoveredTech.LOGGER.warn("Inefficiently getting contained items. Why does " + stack + " have an non-Uncovered Tech IItemHandler?");
                NonNullList<ItemStack> inv = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
                for (int i = 0; i < handler.getSlots(); i++)
                    inv.set(i, handler.getStackInSlot(i));
                return inv;
            }
        });
        return returnItemList.orElse(NonNullList.create());
    }
}























