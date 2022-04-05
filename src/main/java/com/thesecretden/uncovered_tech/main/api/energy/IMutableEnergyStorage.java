package com.thesecretden.uncovered_tech.main.api.energy;

import net.minecraftforge.energy.IEnergyStorage;

public interface IMutableEnergyStorage extends IEnergyStorage {
    void setStoredEnergy(int storedEnergy);

    default void modifyEnergyStored(int changeBy) {
        setStoredEnergy(getEnergyStored() + changeBy);
    }
}
