package com.thesecretden.uncovered_tech.main.api.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class UCTagUtils {
    public static boolean isNonemptyItemTag(RegistryAccess tags, ResourceLocation name) {
        return holderStream(tags, Registry.ITEM_REGISTRY, name).findAny().isPresent();
    }

    private static List<ResourceLocation> getTags(Reference<?> ref) {
        return ref.tags().map(TagKey::location).toList();
    }

    public static Collection<ResourceLocation> getMatchingTagNames(RegistryAccess tags, ItemStack stack) {
        Collection<ResourceLocation> ret = new HashSet<>(getTags(stack.getItem().builtInRegistryHolder()));
        Block b = Block.byItem(stack.getItem());
        if (b != Blocks.AIR) {
            ret.addAll(getTags(b.builtInRegistryHolder()));
        }
        return ret;
    }

    public static String[] getMatchingPrefixRemaining(RegistryAccess tags, ItemStack stack, String... componentTypes) {
        for (ResourceLocation name : getMatchingTagNames(tags, stack)) {
            for (String componentType : componentTypes)
                if (name.getPath().startsWith(componentType)) {
                    String material = name.getPath().substring(componentType.length());
                    if (material.startsWith("/"))
                        material = material.substring(1);
                    if (material.length() > 0) {
                        return new String[]{componentType, material};
                    }
                }
        }
        return null;
    }

    public static <T> Stream<T> elementStream(RegistryAccess tags, ResourceKey<Registry<T>> registry, ResourceLocation tag) {
        return holderStream(tags, registry, tag).map(Holder::value);
    }

    public static <T> Stream<T> elementStream(RegistryAccess tags, TagKey<T> key) {
        return holderStream(tags.registryOrThrow(key.registry()), key).map(Holder::value);
    }

    public static <T> Stream<T> elementStream(Registry<T> registry, TagKey<T> tag) {
        return holderStream(registry, tag).map(Holder::value);
    }

    public static <T> Stream<Holder<T>> holderStream(RegistryAccess tags, ResourceKey<Registry<T>> registry, ResourceLocation tag) {
        return holderStream(tags.registryOrThrow(registry), TagKey.create(registry, tag));
    }

    public static <T> Stream<Holder<T>> holderStream(Registry<T> registry, TagKey<T> tag) {
        return StreamSupport.stream(registry.getTagOrEmpty(tag).spliterator(), false);
    }

    public static TagKey<Item> createItemWrapper(ResourceLocation name) {
        return TagKey.create(Registry.ITEM_REGISTRY, name);
    }

    public static TagKey<Block> createBlockWrapper(ResourceLocation name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, name);
    }

    public static TagKey<Fluid> createFluidWrapper(ResourceLocation name) {
        return TagKey.create(Registry.FLUID_REGISTRY, name);
    }
}




























