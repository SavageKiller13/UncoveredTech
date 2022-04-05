package com.thesecretden.uncovered_tech.main.common.util.inventory;

import com.thesecretden.uncovered_tech.main.api.utils.UCCapabillityUtils;
import com.thesecretden.uncovered_tech.main.common.items.InternalStorageItem;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class UCItemStackHandler extends ItemStackHandler implements ICapabilityProvider {
    public UCItemStackHandler(ItemStack stack) {
        super();
        int idealSize = ((InternalStorageItem)stack.getItem()).getSlotCount();
        NonNullList<ItemStack> list = NonNullList.withSize(idealSize, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(stacks.size(), idealSize); i++)
            list.set(i, stacks.get(i));
        stacks = list;
    }

    @Nonnull
    private Runnable onChange = () -> {};

    public void setTile(BlockEntity tile) {
        if (tile != null)
            onChange = tile::setChanged;
        else
            onChange = () -> {};
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        onChange.run();
    }

    private final LazyOptional<IItemHandler> thisOpt = UCCapabillityUtils.constantOptional(this);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, thisOpt);
    }

    public NonNullList<ItemStack> getContainedItems() {
        return stacks;
    }
}
