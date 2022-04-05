package com.thesecretden.uncovered_tech.main.common.fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.CallbackI;

public record ArrayFluidHandler(IFluidTank[] internal, boolean allowDrain, boolean allowFill, Runnable afterTransfer) implements IFluidHandler {
    @Override
    public int getTanks() {
        return internal.length;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return internal[tank].getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return internal[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return internal[tank].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!allowFill || resource.isEmpty())
            return 0;
        FluidStack remaining = resource.copy();
        for (IFluidTank tank : internal) {
            int filledHere = tank.fill(remaining, action);
            remaining.shrink(filledHere);
            if (remaining.isEmpty())
                break;
        }
        if (resource.getAmount() != remaining.getAmount())
            afterTransfer.run();
        return resource.getAmount() - remaining.getAmount();
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!allowDrain)
            return FluidStack.EMPTY;
        for (IFluidTank tank : internal) {
            FluidStack drainedHere = tank.drain(resource, action);
            if (!drainedHere.isEmpty()) {
                afterTransfer.run();
                return drainedHere;
            }
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!allowDrain)
            return FluidStack.EMPTY;
        for (IFluidTank tank : internal) {
            FluidStack drainedHere = tank.drain(maxDrain, action);
            if (!drainedHere.isEmpty()) {
                afterTransfer.run();
                return drainedHere;
            }
        }
        return FluidStack.EMPTY;
    }
}


























