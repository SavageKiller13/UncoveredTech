package com.thesecretden.uncovered_tech.main.common.registries;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.common.gui.UCBaseContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class UCContainerTypes {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, UncoveredTech.MODID);

    public static <T extends BlockEntity, C extends UCBaseContainer<? super T>> TEContainer<T, C> register(String name, TEContainerConstructor<T, C> container) {
        RegistryObject<MenuType<C>> typeRef = REGISTER.register(name, () -> {
            Mutable<MenuType<C>> typeBox = new MutableObject<>();
            MenuType<C> type = new MenuType<>((IContainerFactory<C>)(windowId, inv, data) -> {
                Level level = UncoveredTech.proxy.getClientWorld();
                BlockPos pos = data.readBlockPos();
                BlockEntity te = level.getBlockEntity(pos);
                return container.construct(typeBox.getValue(), windowId, inv, (T)te);
            });
            typeBox.setValue(type);
            return type;
        });
        return new TEContainer<>(typeRef, container);
    }

    public static <M extends AbstractContainerMenu>RegistryObject<MenuType<M>> registerSimple(String name, SimpleContainerConstructor<M> factory) {
        return REGISTER.register(name, () -> {
            Mutable<MenuType<M>> typeBox = new MutableObject<>();
            MenuType<M> type = new MenuType<>((id, inv) -> factory.construct(typeBox.getValue(), id, inv));
            typeBox.setValue(type);
            return type;
        });
    }

    public static class TEContainer<T extends BlockEntity, C extends UCBaseContainer<? super T>> {
        private final RegistryObject<MenuType<C>> type;
        private final TEContainerConstructor<T, C> factory;
        private TEContainer(RegistryObject<MenuType<C>> type, TEContainerConstructor<T, C> factory) {
            this.type = type;
            this.factory = factory;
        }

        public C create(int windowId, Inventory inventory, T tile) {
            return factory.construct(getType(), windowId, inventory, tile);
        }

        public MenuType<C> getType() {
            return type.get();
        }
    }

    public static class ItemContainerType<C extends AbstractContainerMenu> {
        final RegistryObject<MenuType<C>> type;
        private final ItemContainerConstructor<C> factory;

        private ItemContainerType(RegistryObject<MenuType<C>> type, ItemContainerConstructor<C> factory) {
            this.type = type;
            this.factory = factory;
        }

        public C create(int id, Inventory inv, Level level, EquipmentSlot slot, ItemStack stack) {
            return factory.construct(getType(), id, inv, level, slot, stack);
        }

        public MenuType<C> getType() {
            return type.get();
        }
    }

    public interface TEContainerConstructor<T extends BlockEntity, C extends UCBaseContainer<? super T>> {
        C construct(MenuType<C> type, int windowId, Inventory inventory, T te);
    }

    public interface ItemContainerConstructor<C extends AbstractContainerMenu> {
        C construct(MenuType<C> type, int windowId, Inventory inventory, Level level, EquipmentSlot slot, ItemStack stack);
    }

    public interface SimpleContainerConstructor<C extends AbstractContainerMenu> {
        C construct(MenuType<?> type, int windowId, Inventory inventory);
    }
}
