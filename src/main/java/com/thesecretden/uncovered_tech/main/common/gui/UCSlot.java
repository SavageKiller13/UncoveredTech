package com.thesecretden.uncovered_tech.main.common.gui;

import com.thesecretden.uncovered_tech.main.common.util.inventory.EmptyContainer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class UCSlot extends Slot {
    final AbstractContainerMenu containerMenu;

    public UCSlot(AbstractContainerMenu containerMenu, Container inv, int id, int x, int y) {
        super(inv, id, x, y);
        this.containerMenu = containerMenu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return true;
    }

    public static class Output extends UCSlot {
        public Output(AbstractContainerMenu container, Container inv, int id, int x, int y) {
            super(container, inv, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    public static class UCFuelSlot extends UCSlot {
        public UCFuelSlot(AbstractContainerMenu container, Container inv, int id, int x, int y) {
            super(container, inv, id, x, y);
        }

        public boolean mayPlace(ItemStack stack) {
            return AbstractFurnaceBlockEntity.isFuel(stack) || isBucket(stack);
        }

        public int getMaxStackSize(ItemStack stack) {
            return isBucket(stack) ? 1 : super.getMaxStackSize(stack);
        }

        public static boolean isBucket(ItemStack stack) {
            return stack.getItem() == Items.BUCKET;
        }
    }

    public static class FluidContainer extends UCSlot {
        int filter;

        public FluidContainer(AbstractContainerMenu container, Container inv, int id, int x, int y, int filter) {
            super(container, inv, id, x, y);
            this.filter = filter;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            LazyOptional<IFluidHandlerItem> handlerCap = FluidUtil.getFluidHandler(stack);
            return handlerCap.map(handler -> {
                if (handler.getTanks() <= 0)
                    return false;

                if (filter == 1)
                    return handler.getFluidInTank(0).isEmpty();
                else if (filter == 2)
                    return !handler.getFluidInTank(0).isEmpty();
                return true;
            }).orElse(false);
        }
    }

    public static class WithPredicate extends SlotItemHandler {
        final Predicate<ItemStack> predicate;
        final Consumer<ItemStack> onChange;

        public WithPredicate(IItemHandler inv, int id, int x, int y, Predicate<ItemStack> predicate) {
            this(inv, id, x, y, predicate, s -> {});
        }

        public WithPredicate(IItemHandler inv, int id, int x, int y, Predicate<ItemStack> predicate, Consumer<ItemStack> onChange) {
            super(inv, id, x, y);
            this.predicate = predicate;
            this.onChange = onChange;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return !stack.isEmpty() && this.predicate.test(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            onChange.accept(getItem());
        }
    }

    public static class ItemHandlerGhost extends SlotItemHandler {
        public ItemHandlerGhost(IItemHandler inv, int slot, int x, int y) {
            super(inv, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            return false;
        }
    }

    public static class ItemDisplay extends UCSlot {
        public ItemDisplay(AbstractContainerMenu container, Container inv, int id, int x, int y) {
            super(container, inv, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    public static class Tagged extends UCSlot {
        private final TagKey<Item> tag;

        public Tagged(AbstractContainerMenu container, Container inv, int id, int x, int y, TagKey<Item> tag) {
            super(container, inv, id, x, y);
            this.tag = tag;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(tag);
        }
    }

    public static class AlwaysEmptySlot extends UCSlot {
        public AlwaysEmptySlot(AbstractContainerMenu containerMenu) {
            super(containerMenu, EmptyContainer.INSTANCE, 0, 0, 0);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }

    public static class ContainerCallback extends SlotItemHandler {
        AbstractContainerMenu container;

        public ContainerCallback(AbstractContainerMenu container, IItemHandler inv, int id, int x, int y) {
            super(inv, id, x, y);
            this.container = container;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            if (this.container instanceof ICallbackContainer)
                return ((ICallbackContainer)this.container).canInsert(this.getItem(), getSlotIndex(), this);
            return true;
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            if (this.container instanceof ICallbackContainer)
                return ((ICallbackContainer)this.container).canTake(this.getItem(), getSlotIndex(), this);
            return true;
        }
    }

    public interface ICallbackContainer {
        boolean canInsert(ItemStack stack, int slot, Slot slotObject);

        boolean canTake(ItemStack stack, int slot, Slot slotObject);
    }
}



















