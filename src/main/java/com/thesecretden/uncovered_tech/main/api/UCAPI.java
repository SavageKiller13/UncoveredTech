package com.thesecretden.uncovered_tech.main.api;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.api.utils.UCTagUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class UCAPI {
    public static List<? extends String> modPrefs;

    private static HashMap<TagKey<Item>, ItemStack> oreOutPref = new HashMap<>();

    public static HashMap<String, Integer[]> prefixToIngotMap = new HashMap<String, Integer[]>();

    public static ItemStack getPreferredTagStack(RegistryAccess tags, TagKey<Item> tag) {
        return oreOutPref.computeIfAbsent(tag, r1 -> getPreferredElementbyMod(UCTagUtils.elementStream(tags, r1)).orElse(Items.AIR).getDefaultInstance()).copy();
    }

    public static <T extends IForgeRegistryEntry<T>> Optional<T> getPreferredElementbyMod(Stream<T> list) {
        return getPreferredElementbyMod(list, T::getRegistryName);
    }

    public static <T> Optional<T> getPreferredElementbyMod(Stream<T> list, Function<T, ResourceLocation> getName) {
        return list.min(Comparator.<T>comparingInt(t -> {
            ResourceLocation name = getName.apply(t);
            String modId = name.getNamespace();
            int idx = modPrefs.indexOf(modId);
            if (idx < 0)
                return modPrefs.size();
            else
                return idx;
        }).thenComparing(getName));
    }

    public static ItemStack getPreferredStackbyMod(ItemStack[] array) {
        return getPreferredElementbyMod(Arrays.stream(array), stack -> stack.getItem().getRegistryName()).orElseThrow(() -> new RuntimeException("Array is empty"));
    }

    public static String getCurrentVersion() {
        return ModList.get().getModFileById(UncoveredTech.MODID).versionString();
    }
}
