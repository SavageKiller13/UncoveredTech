package com.thesecretden.uncovered_tech.main.api.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.thesecretden.uncovered_tech.main.api.UCAPI;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static com.thesecretden.uncovered_tech.main.api.UCTags.getIngot;

public class UCAPIUtils {
    public static final Random RANDOM = new Random();

    public static JsonElement jsonSerializeFluidStack(FluidStack fluidStack) {
        if (fluidStack == null)
            return JsonNull.INSTANCE;

        JsonObject obj = new JsonObject();
        obj.addProperty("fluid", fluidStack.getFluid().getRegistryName().toString());
        obj.addProperty("amount", fluidStack.getAmount());
        if (fluidStack.hasTag())
            obj.addProperty("tag", fluidStack.getTag().toString());
        return obj;
    }

    public static FluidStack jsonDeserializeFluidStack(JsonObject obj) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(obj, "fluid")));
        int amount = GsonHelper.getAsInt(obj, "amount");
        FluidStack fluidStack = new FluidStack(fluid, amount);
        if (GsonHelper.isValidNode(obj, "tag"))
            fluidStack.setTag(JsonUtils.readNBT(obj, "tag"));
        return fluidStack;
    }

    public static Pair<ItemStack, Double> breakStackIntoPreciseIngots(RegistryAccess tags, ItemStack stack) {
        String[] keys = UCAPI.prefixToIngotMap.keySet().toArray(new String[0]);
        String[] type = UCTagUtils.getMatchingPrefixRemaining(tags, stack, keys);
        if (type != null) {
            Integer[] relation = UCAPI.prefixToIngotMap.get(type[0]);
            if (relation != null && relation.length > 1) {
                double val = relation[0]/(double)relation[1];
                return Pair.of(UCAPI.getPreferredTagStack(tags, UCTagUtils.createItemWrapper(getIngot(type[1]))), val);
            }
        }
        return null;
    }

    public static <T extends Comparable<T>> Map<T, Integer> sortMap(Map<T, Integer> map, boolean inverse) {
        TreeMap<T, Integer> sortedMap = new TreeMap<>(new ValueComparator<T>(map, inverse));
        sortedMap.putAll(map);
        return sortedMap;
    }

    public static void addFutureServerTask(Level level, Runnable task, boolean forceFuture) {
        LogicalSide side = level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;

        BlockableEventLoop<? super TickTask> tmp = LogicalSidedProvider.WORKQUEUE.get(side);
        if (forceFuture) {
            int tick;
            if (level.isClientSide)
                tick = 0;
            else
                tick = ((MinecraftServer)tmp).getTickCount();
            tmp.tell(new TickTask(tick, task));
        } else
            tmp.submitAsync(task);
    }

    public static void addFutureServerTask(Level level, Runnable task) {
        addFutureServerTask(level, task, false);
    }

    public static record ValueComparator<T extends Comparable<T>>(Map<T, Integer> base, boolean inverse) implements java.util.Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            int v0 = base.get(o1);
            int v1 = base.get(o2);
            int ret;
            if (v0 > v1)
                ret = -1;
            else if (v0 < v1)
                ret = 1;
            else
                ret = o1.compareTo(o2);
            return ret * (inverse ? -1 : 1);
        }
    }
}
