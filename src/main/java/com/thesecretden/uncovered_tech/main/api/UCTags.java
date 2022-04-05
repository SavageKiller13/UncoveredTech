package com.thesecretden.uncovered_tech.main.api;

import com.google.common.base.Preconditions;
import com.thesecretden.uncovered_tech.main.UncoveredTech;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.Tags.Items;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.thesecretden.uncovered_tech.main.api.utils.UCTagUtils.*;

public class UCTags {
    private static final Map<TagKey<Block>, TagKey<Item>> toItemTag = new HashMap<>();
    private static final Map<EnumMetals, MetalTags> metals = new EnumMap<>(EnumMetals.class);

    static {
        toItemTag.put(Blocks.STORAGE_BLOCKS, Items.STORAGE_BLOCKS);
        toItemTag.put(Blocks.ORES, Items.ORES);
        toItemTag.put(Blocks.ORES_IN_GROUND_STONE, Items.ORES_IN_GROUND_STONE);
        toItemTag.put(Blocks.ORES_IN_GROUND_DEEPSLATE, Items.ORES_IN_GROUND_DEEPSLATE);
        toItemTag.put(Blocks.ORE_RATES_SINGULAR, Items.ORE_RATES_SINGULAR);
    }

    public static final TagKey<Item> clay = createItemWrapper(forgeLocation("clay"));
    public static final TagKey<Block> clayBlock = createBlockTag(getStorageBlock("clay"));
    public static final TagKey<Item> charCoal = createItemWrapper(forgeLocation("charcoal"));
    public static final TagKey<Block> glowstoneBlock = createBlockTag(getStorageBlock("glowstone"));
    public static final TagKey<Block> sandstoneBlocks = createBlockTag(forgeLocation("sandstone/colorless"));
    public static final TagKey<Block> redSandstoneBlocks = createBlockTag(forgeLocation("sandstone/red"));
    public static final TagKey<Item> coal = createItemWrapper(forgeLocation("coal"));
    public static final TagKey<Block> coalBlock = createBlockTag(getStorageBlock("coal_block"));

    static {
        for (EnumMetals m : EnumMetals.values()) {
            metals.put(m, new MetalTags(m));
        }
    }

    public static TagKey<Item> getItemTag(TagKey<Block> tag) {
        Preconditions.checkArgument(toItemTag.containsKey(tag));
        return toItemTag.get(tag);
    }

    public static MetalTags getTagsFor(EnumMetals metal) {
        return metals.get(metal);
    }

    private static TagKey<Block> createBlockTag(ResourceLocation name) {
        TagKey<Block> blockTag = createBlockWrapper(name);
        toItemTag.put(blockTag, createItemWrapper(name));
        return blockTag;
    }

    public static void forAllBlockTags(BiConsumer<TagKey<Block>, TagKey<Item>> out) {
        for (Map.Entry<TagKey<Block>, TagKey<Item>> entry : toItemTag.entrySet())
            out.accept(entry.getKey(), entry.getValue());
    }

    public static class MetalTags {
        public final TagKey<Item> ingot;
        public final TagKey<Item> nugget;
        @Nullable
        public final TagKey<Item> rawOre;
        public final TagKey<Item> plate;
        public final TagKey<Item> dust;
        public final TagKey<Block> storage;
        @Nullable
        public final TagKey<Block> ore;
        @Nullable
        public final TagKey<Block> rawBlock;

        private MetalTags(EnumMetals m) {
            String name = m.tagName();
            TagKey<Block> ore = null;
            TagKey<Item> rawOre = null;
            TagKey<Block> rawBlock = null;

            if (m.shouldAddOre()) {
                ore = createBlockTag(getOre(name));
                rawOre = createItemWrapper(getRawOre(name));
                rawBlock = createBlockTag(getRawBlock(name));
            }

            if (m.isVanillaMetal())
                storage = createBlockTag(getStorageBlock(name));
            else if (m == EnumMetals.COPPER) {
                storage = Blocks.STORAGE_BLOCKS_COPPER;
                ore = Blocks.ORES_COPPER;
                rawBlock = Blocks.STORAGE_BLOCKS_RAW_COPPER;
            } else if (m == EnumMetals.IRON) {
                storage = Blocks.STORAGE_BLOCKS_IRON;
                ore = Blocks.ORES_IRON;
                rawBlock = Blocks.STORAGE_BLOCKS_RAW_IRON;
            } else if (m == EnumMetals.GOLD) {
                storage = Blocks.STORAGE_BLOCKS_GOLD;
                ore = Blocks.ORES_GOLD;
                rawBlock = Blocks.STORAGE_BLOCKS_RAW_GOLD;
            } else
                throw new RuntimeException("Unknown Vanilla metal: " + m.name());

            nugget = createItemWrapper(getNugget(name));
            ingot = createItemWrapper(getIngot(name));
            plate = createItemWrapper(getPlate(name));
            dust = createItemWrapper(getDust(name));
            this.ore = ore;
            this.rawOre = rawOre;
            this.rawBlock = rawBlock;
        }
    }

    private static ResourceLocation forgeLocation(String path) {
        return new ResourceLocation("forge", path);
    }

    public static ResourceLocation getOre(String type) {
        return forgeLocation("ores/" + type);
    }

    public static ResourceLocation getRawOre(String type) {
        return forgeLocation("raw_materials/" + type);
    }

    public static ResourceLocation getNugget(String type) {
        return forgeLocation("nuggets/" + type);
    }

    public static ResourceLocation getIngot(String type) {
        return forgeLocation("ingots/" + type);
    }

    public static ResourceLocation getGem(String type) {
        return forgeLocation("gems/" + type);
    }

    public static ResourceLocation getStorageBlock(String type) {
        return forgeLocation("storage_blocks/" + type);
    }

    public static ResourceLocation getRawBlock(String type) {
        return getStorageBlock("raw_" + type);
    }

    public static ResourceLocation getDust(String type) {
        return forgeLocation("dusts/" + type);
    }

    public static ResourceLocation getPlate(String type) {
        return forgeLocation("plates/" + type);
    }

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(UncoveredTech.MODID, path);
    }
}



































