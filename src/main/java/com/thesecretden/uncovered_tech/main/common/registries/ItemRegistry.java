package com.thesecretden.uncovered_tech.main.common.registries;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.api.EnumMetals;
import com.thesecretden.uncovered_tech.main.common.items.UCBaseItem;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemRegistry {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, UncoveredTech.MODID);

    private ItemRegistry() {}

    public static final class Metals {
        public static final Map<EnumMetals, ItemRegObject<Item>> INGOTS = new EnumMap<>(EnumMetals.class);
        public static final Map<EnumMetals, ItemRegObject<Item>> NUGGETS = new EnumMap<>(EnumMetals.class);
        public static final Map<EnumMetals, ItemRegObject<Item>> RAW_ORES = new EnumMap<>(EnumMetals.class);
        public static final Map<EnumMetals, ItemRegObject<UCBaseItem>> DUSTS = new EnumMap<>(EnumMetals.class);

        private static void init() {
            for (EnumMetals metal : EnumMetals.values()) {
                String name = metal.tagName();
                ItemRegObject<Item> nugget;
                ItemRegObject<Item> ingot;
                ItemRegObject<Item> rawOre = null;
                if (!metal.isVanillaMetal())
                    ingot = register(name + "_ingot", UCBaseItem::new);
                else if (metal == EnumMetals.IRON)
                    ingot = of(Items.IRON_INGOT);
                else if (metal == EnumMetals.GOLD)
                    ingot = of(Items.GOLD_INGOT);
                else if (metal == EnumMetals.COPPER)
                    ingot = of(Items.COPPER_INGOT);
                else
                    throw new RuntimeException("Unknown vanilla metal: " + metal.name());

                if (metal.shouldAddNugget())
                    nugget = register(name + "_nugget", UCBaseItem::new);
                else if (metal == EnumMetals.IRON)
                    nugget = of(Items.IRON_NUGGET);
                else if (metal == EnumMetals.GOLD)
                    nugget = of(Items.GOLD_NUGGET);
                else if (metal == EnumMetals.COPPER)
                    nugget = register("copper_nugget", UCBaseItem::new);
                else
                    throw new RuntimeException("Unknown vanilla metal: " + metal.name());
                if (metal.shouldAddOre())
                    rawOre = register("raw_" + name, UCBaseItem::new);
                else if (metal == EnumMetals.IRON)
                    rawOre = of(Items.RAW_IRON);
                else if (metal == EnumMetals.GOLD)
                    rawOre = of(Items.RAW_GOLD);
                else if (metal == EnumMetals.COPPER)
                    rawOre = of(Items.RAW_COPPER);
                NUGGETS.put(metal, nugget);
                INGOTS.put(metal, ingot);
                if (rawOre != null)
                    RAW_ORES.put(metal, rawOre);
                DUSTS.put(metal, simple(name + "_dust"));
            }
        }
    }

    public static void registerItems() {
        REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static <T> Consumer<T> nothing() {
        return $ -> {};
    }

    private static ItemRegObject<UCBaseItem> simpleWithStackSize(String name, int maxSize) {
        return simple(name, p -> p.stacksTo(maxSize), i -> {});
    }

    private static ItemRegObject<UCBaseItem> simple(String name) {
        return simple(name, $ -> {}, $ -> {});
    }

    private static ItemRegObject<UCBaseItem> simple(String name, Consumer<Item.Properties> makeProperties, Consumer<UCBaseItem> processItem) {
        return register(name, () -> Util.make(new UCBaseItem(Util.make(new Item.Properties(), makeProperties)), processItem));
    }

    private static <T extends Item> ItemRegObject<T> register(String name, Supplier<? extends T> make) {
        return new ItemRegObject<>(REGISTER.register(name, make));
    }

    private static <T extends Item> ItemRegObject<T> of(T existing) {
        return new ItemRegObject<>(RegistryObject.of(existing.getRegistryName(), ForgeRegistries.ITEMS));
    }

    public static class ItemRegObject<T extends Item> implements Supplier<T>, ItemLike {
        private final RegistryObject<T> regObject;

        private ItemRegObject(RegistryObject<T> regObject) {
            this.regObject = regObject;
        }

        @Override
        @Nonnull
        public T get() {
            return regObject.get();
        }

        @Nonnull
        @Override
        public Item asItem() {
            return regObject.get();
        }

        public ResourceLocation getId() {
            return regObject.getId();
        }
    }
}
