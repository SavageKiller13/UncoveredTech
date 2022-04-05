package com.thesecretden.uncovered_tech.main.common.registries;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.api.EnumMetals;
import com.thesecretden.uncovered_tech.main.common.blocks.UCBlockBase;
import com.thesecretden.uncovered_tech.main.common.blocks.UCBlockItem;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistry {
    private static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, UncoveredTech.MODID);

    private BlockRegistry() {}

    public static final class Metals {
        public static final Map<EnumMetals, BlockEntry<Block>> ORES = new EnumMap<>(EnumMetals.class);
        public static final Map<EnumMetals, BlockEntry<Block>> DEEPSLATE_ORES = new EnumMap<>(EnumMetals.class);
        public static final Map<EnumMetals, BlockEntry<Block>> RAW_ORES = new EnumMap<>(EnumMetals.class);
        public static final Map<EnumMetals, BlockEntry<Block>> STORAGE = new EnumMap<>(EnumMetals.class);

        private static void init() {
            for (EnumMetals metal : EnumMetals.values()) {
                String name = metal.tagName();
                BlockEntry<Block> storage;
                BlockEntry<Block> ore = null;
                BlockEntry<Block> deepslateOre = null;
                BlockEntry<Block> rawOre = null;

                if (metal.shouldAddOre()) {
                    ore = new BlockEntry<>(BlockEntry.simple(name + "_ore", () -> BlockBehaviour.Properties.of(Material.STONE).strength(3, 3).requiresCorrectToolForDrops()));
                    deepslateOre = new BlockEntry<>(BlockEntry.simple(name + "_deepslate_ore", () -> BlockBehaviour.Properties.of(Material.STONE).color(MaterialColor.DEEPSLATE).sound(SoundType.DEEPSLATE).strength(4.5f, 3).requiresCorrectToolForDrops()));
                    rawOre = new BlockEntry<>(BlockEntry.simple(name + "_raw_block", () -> BlockBehaviour.Properties.of(Material.STONE).color(MaterialColor.STONE).strength(5, 6).requiresCorrectToolForDrops()));
                }
                if (!metal.isVanillaMetal()) {
                    BlockEntry<UCBlockBase> storageUC = BlockEntry.simple(name + "_block", () -> BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(5, 10).requiresCorrectToolForDrops());
                    storage = new BlockEntry<>(storageUC);
                } else if (metal == EnumMetals.IRON) {
                    storage = new BlockEntry<>(Blocks.IRON_BLOCK);
                    ore = new BlockEntry<>(Blocks.IRON_ORE);
                    deepslateOre = new BlockEntry<>(Blocks.DEEPSLATE_IRON_ORE);
                    rawOre = new BlockEntry<>(Blocks.RAW_IRON_BLOCK);
                } else if (metal == EnumMetals.GOLD) {
                    storage = new BlockEntry<>(Blocks.GOLD_BLOCK);
                    ore = new BlockEntry<>(Blocks.GOLD_ORE);
                    deepslateOre = new BlockEntry<>(Blocks.DEEPSLATE_GOLD_ORE);
                    rawOre = new BlockEntry<>(Blocks.RAW_GOLD_BLOCK);
                } else if (metal == EnumMetals.COPPER) {
                    storage = new BlockEntry<>(Blocks.COPPER_BLOCK);
                    ore = new BlockEntry<>(Blocks.COPPER_ORE);
                    deepslateOre = new BlockEntry<>(Blocks.DEEPSLATE_COPPER_ORE);
                    rawOre = new BlockEntry<>(Blocks.RAW_COPPER_BLOCK);
                } else
                    throw new RuntimeException("Unknown vanilla metal: " + metal.name());
                STORAGE.put(metal, storage);
                if (ore != null)
                    ORES.put(metal, ore);
                if (deepslateOre != null) {
                    DEEPSLATE_ORES.put(metal, deepslateOre);
                    RAW_ORES.put(metal, rawOre);
                }
            }
        }
    }

    public static void registerBlocks() {
        REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        Metals.init();

        for (BlockEntry<?> entry : BlockEntry.ALL_ENTRIES) {
            Function<Block, UCBlockItem> toItem;
            toItem = UCBlockItem::new;
            Function<Block, UCBlockItem> finalToItem = toItem;
            ItemRegistry.REGISTER.register(entry.getId().getPath(), () -> finalToItem.apply(entry.get()));
        }
    }

    public static final class BlockEntry<T extends Block> implements Supplier<T>, ItemLike {
        public static final Collection<BlockEntry<?>> ALL_ENTRIES = new ArrayList<>();

        private final RegistryObject<T> regObject;
        private final Supplier<Properties> properties;

        public static BlockEntry<UCBlockBase> simple(String name, Supplier<Properties> properties, Consumer<UCBlockBase> extra) {
            return new BlockEntry<>(name, properties, p -> Util.make(new UCBlockBase(p), extra));
        }

        public static BlockEntry<UCBlockBase> simple(String name, Supplier<Properties> properties) {
            return simple(name, properties, $ -> {});
        }

        public BlockEntry(String name, Supplier<Properties> properties, Function<Properties, T> make) {
            this.properties = properties;
            this.regObject = REGISTER.register(name, () -> make.apply(properties.get()));
            ALL_ENTRIES.add(this);
        }

        public BlockEntry(T existing) {
            this.properties = () -> Properties.copy(existing);
            this.regObject = RegistryObject.of(existing.getRegistryName(), ForgeRegistries.BLOCKS);
        }

        @SuppressWarnings("unchecked")
        public BlockEntry(BlockEntry<? extends T> toCopy) {
            this.properties = toCopy.properties;
            this.regObject = (RegistryObject<T>) toCopy.regObject;
        }

        @Override
        public T get() {
            return regObject.get();
        }

        public BlockState defaultBlockState() {
            return get().defaultBlockState();
        }

        public ResourceLocation getId() {
            return regObject.getId();
        }

        public Properties getProperties() {
            return properties.get();
        }

        @Nonnull
        @Override
        public Item asItem() {
            return get().asItem();
        }
    }
}




























