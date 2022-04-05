package com.thesecretden.uncovered_tech.main.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class EnergyHelper {
    public static final String LEGACY_ENERGY_KEY = "ifluxenergy";
    public static final String ENERGY_KEY = "energy";
    static HashMap<Item, Boolean> reverseInsertion = new HashMap<>();

    public static void deserializeFrom(EnergyStorage storage, CompoundTag tag) {
        Tag subTag;
        if (tag.contains(LEGACY_ENERGY_KEY, Tag.TAG_INT))
            subTag = tag.get(LEGACY_ENERGY_KEY);
        else if (tag.contains(ENERGY_KEY, Tag.TAG_INT))
            subTag = tag.get(ENERGY_KEY);
        else
            subTag = IntTag.valueOf(0);
        storage.deserializeNBT(subTag);
    }

    public static void serializeTo(EnergyStorage storage, CompoundTag tag) {
        tag.put(ENERGY_KEY, storage.serializeNBT());
    }

    public static int forceExtractFlux(ItemStack stack, int energy, boolean simulate) {
        if (stack.isEmpty())
            return 0;
        Boolean b = reverseInsertion.get(stack.getItem());
        if (b) {
            int stored = getEnergyStored(stack);
            insertFlux(stack, -energy, simulate);
            return stored - getEnergyStored(stack);
        } else {
            int drawn = extractFlux(stack, energy, simulate);
            if (b == null) {
                int stored = getEnergyStored(stack);
                insertFlux(stack, -energy, simulate);
                drawn = stored - getEnergyStored(stack);
                reverseInsertion.put(stack.getItem(), drawn > 0);
            }
            return drawn;
        }
    }

    public static int getEnergyStored(ICapabilityProvider stack) {
        return getEnergyStored(stack, null);
    }

    public static int getEnergyStored(ICapabilityProvider stack, @Nullable Direction direction) {
        if (stack == null)
            return 0;
        return stack.getCapability(CapabilityEnergy.ENERGY, direction).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public static int getMaxEnergyStored(ICapabilityProvider stack) {
        return getMaxEnergyStored(stack, null);
    }

    public static int getMaxEnergyStored(ICapabilityProvider stack, @Nullable Direction direction) {
        if (stack == null)
            return 0;
        return stack.getCapability(CapabilityEnergy.ENERGY, direction).map(IEnergyStorage::getMaxEnergyStored).orElse(0);
    }

    public static boolean isFluxReceiver(ICapabilityProvider tile) {
        return isFluxReceiver(tile, null);
    }

    public static boolean isFluxReceiver(ICapabilityProvider tile, @Nullable Direction direction) {
        if (tile == null)
            return false;
        return tile.getCapability(CapabilityEnergy.ENERGY, direction).map(IEnergyStorage::canReceive).orElse(false);
    }

    public static boolean isFluxRelated(ICapabilityProvider tile) {
        return isFluxRelated(tile, null);
    }

    public static boolean isFluxRelated(ICapabilityProvider tile, @Nullable Direction direction) {
        if (tile == null)
            return false;
        return tile.getCapability(CapabilityEnergy.ENERGY, direction).isPresent();
    }

    public static int insertFlux(ICapabilityProvider tile, int energy, boolean simulate) {
        return insertFlux(tile, null, energy, simulate);
    }

    public static int insertFlux(ICapabilityProvider tile, @Nullable Direction direction, int energy, boolean simulate) {
        if (tile == null)
            return 0;
        return tile.getCapability(CapabilityEnergy.ENERGY, direction).map(storage -> storage.receiveEnergy(energy, simulate)).orElse(0);
    }

    public static int extractFlux(ICapabilityProvider tile, int energy, boolean simulate) {
        return extractFlux(tile, null, energy, simulate);
    }

    public static int extractFlux(ICapabilityProvider tile, @Nullable Direction direction, int energy, boolean simulate) {
        if (tile == null)
            return 0;
        return tile.getCapability(CapabilityEnergy.ENERGY, direction).map(storage -> storage.extractEnergy(energy, simulate)).orElse(0);
    }

    public static int distributeFlux(Collection<IEnergyStorage> storages, int amount, boolean simulate) {
        final int finalAmount = amount;
        storages = storages.stream().filter(Objects::nonNull).map(storage -> Pair.of(storage, storage.receiveEnergy(finalAmount, true))).sorted(Comparator.comparingInt(Pair::getSecond)).map(Pair::getFirst).collect(Collectors.toList());
        int remainingOutputs = storages.size();
        for (IEnergyStorage storage : storages) {
            int possibleOutput = (int)Math.ceil(amount / (float)remainingOutputs);
            int inserted = storage.receiveEnergy(possibleOutput, simulate);
            amount -= inserted;
            remainingOutputs--;
        }
        return amount;
    }

    public static class ItemEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;
        private final ToIntFunction<ItemStack> getCapacity;

        public ItemEnergyStorage(ItemStack item, ToIntFunction<ItemStack> getCapacity) {
            this.stack = item;
            this.getCapacity = getCapacity;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int stored = getEnergyStored();
            int accepted = Math.min(maxReceive, getMaxEnergyStored() - stored);
            if (!simulate) {
                stored += accepted;
                ItemNBTHelper.putInt(stack, "energy", stored);
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int stored = getEnergyStored();
            int extracted = Math.min(maxExtract, stored);
            if (!simulate) {
                stored -= extracted;
                ItemNBTHelper.putInt(stack, "energy", stored);
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return ItemNBTHelper.getInt(stack, "energy");
        }

        @Override
        public int getMaxEnergyStored() {
            return getCapacity.applyAsInt(stack);
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}






















