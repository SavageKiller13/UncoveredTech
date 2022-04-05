package com.thesecretden.uncovered_tech.main.api.fluid;

import net.minecraftforge.fluids.FluidAttributes;

public interface IFluidConduit {
    int BASE_AMOUNT = FluidAttributes.BUCKET_VOLUME/2;

    static int getTransferableAmount(boolean pressurized) {
        return BASE_AMOUNT;
    }
}
