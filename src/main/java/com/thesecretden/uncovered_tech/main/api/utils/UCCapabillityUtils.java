package com.thesecretden.uncovered_tech.main.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Objects;

public class UCCapabillityUtils {
    public static <T> LazyOptional<T> findCapabilityAtPos(Capability<T> capability, Level level, BlockPos pos, Direction dir) {
        BlockEntity neighbourTile = level.getBlockEntity(pos);
        if (neighbourTile != null) {
            LazyOptional<T> cap = neighbourTile.getCapability(capability, dir);
            if (cap.isPresent())
                return cap;
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IItemHandler> findItemHandlerAtPos(Level level, BlockPos pos, Direction dir) {
        return findCapabilityAtPos(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, level, pos, dir);
    }

    public static LazyOptional<IFluidHandler> findFluidHandlerAtPos(Level level, BlockPos pos, Direction dir) {
        return findCapabilityAtPos(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, level, pos, dir);
    }

    public static boolean canInsertStackIntoInventory(BlockEntity inv, ItemStack stack, Direction dir) {
        if (!stack.isEmpty() && inv != null) {
            return inv.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).map(handler -> {
                ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
                return temp.isEmpty() || temp.getCount() < stack.getCount();
            }).orElse(false);
        }
        return false;
    }

    public static ItemStack insertStackIntoInventory(BlockEntity inv, ItemStack stack, Direction dir) {
        if (!stack.isEmpty() && inv != null) {
            return inv.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).map(handler -> {
                ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
                if (temp.isEmpty() || temp.getCount() < stack.getCount())
                    return ItemHandlerHelper.insertItem(handler, stack, false);
                return stack;
            }).orElse(stack);
        }
        return stack;
    }

    public static ItemStack insertStackIntoInventory(BlockEntity inv, ItemStack stack, Direction dir, boolean simulate) {
        if (inv != null && !stack.isEmpty())
            return inv.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).map(handler -> ItemHandlerHelper.insertItem(handler, stack.copy(), simulate)).orElse(stack);
        return stack;
    }

    public static <T> LazyOptional<T> constantOptional(T val) {
        LazyOptional<T> result = LazyOptional.of(() -> Objects.requireNonNull(val));
        result.resolve();
        return result;
    }

    public static <T> T getPresentCapability(ICapabilityProvider provider, Capability<T> cap) {
        return Objects.requireNonNull(getCapability(provider, cap, null));
    }

    @Nullable
    public static <T> T getCapability(ICapabilityProvider provider, Capability<T> cap) {
        return getCapability(provider, cap, null);
    }

    @Nullable
    public static <T> T getCapability(ICapabilityProvider provider, Capability<T> cap, @Nullable Direction dir) {
        LazyOptional<T> optional = provider.getCapability(cap, dir);
        if (optional.isPresent())
            return optional.orElseThrow(RuntimeException::new);
        else
            return null;
    }
}
























