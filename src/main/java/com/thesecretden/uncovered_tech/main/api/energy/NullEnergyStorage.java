package com.thesecretden.uncovered_tech.main.api.energy;

import net.minecraftforge.energy.IEnergyStorage;

public class NullEnergyStorage implements IEnergyStorage {
    public static IEnergyStorage INSTANCE = new NullEnergyStorage();

    private NullEnergyStorage() {}

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
