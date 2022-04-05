package com.thesecretden.uncovered_tech.main.api.energy;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public interface IRotationAcceptor {
    Capability<IRotationAcceptor> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    void inputRotation(double rotation);
}
