package com.thesecretden.uncovered_tech.main.api.energy;

import net.minecraftforge.energy.EnergyStorage;

public class MutableEnergyStorage extends EnergyStorage implements IMutableEnergyStorage {
    public MutableEnergyStorage(int capacity) {
        super(capacity);
    }

    public MutableEnergyStorage(int capacity, int maxIO) {
        super(capacity, maxIO);
    }

    public MutableEnergyStorage(int capacity, int maxInsert, int maxExtract) {
        super(capacity, maxInsert, maxExtract);
    }

    @Override
    public void setStoredEnergy(int storedEnergy) {
        this.energy = storedEnergy;
    }
}
