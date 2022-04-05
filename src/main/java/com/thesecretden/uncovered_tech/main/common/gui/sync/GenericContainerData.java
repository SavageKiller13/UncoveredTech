package com.thesecretden.uncovered_tech.main.common.gui.sync;

import com.thesecretden.uncovered_tech.main.api.energy.IMutableEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericContainerData<T> {
    private final GenericDataSerializers.DataSerializer<T> serializer;
    private final Supplier<T> get;
    private final Consumer<T> set;
    private T current;

    public GenericContainerData(GenericDataSerializers.DataSerializer<T> serializer, Supplier<T> get, Consumer<T> set) {
        this.serializer = serializer;
        this.get = get;
        this.set = set;
    }

    public static GenericContainerData<Integer> int32(Supplier<Integer> get, Consumer<Integer> set) {
        return new GenericContainerData<>(GenericDataSerializers.INT32, get, set);
    }

    public static GenericContainerData<?> energy(IMutableEnergyStorage storage) {
        return int32(storage::getEnergyStored, storage::setStoredEnergy);
    }

    public static GenericContainerData<FluidStack> fluid(FluidTank tank) {
        return new GenericContainerData<>(GenericDataSerializers.FLUID_STACK, tank::getFluid, tank::setFluid);
    }

    public static GenericContainerData<Boolean> bool(Supplier<Boolean> get, Consumer<Boolean> set) {
        return new GenericContainerData<>(GenericDataSerializers.BOOLEAN, get, set);
    }

    public static GenericContainerData<Float> float32(Supplier<Float> get, Consumer<Float> set) {
        return new GenericContainerData<>(GenericDataSerializers.FLOAT, get, set);
    }

    public boolean needsUpdate() {
        T newValue = get.get();
        if (newValue == null && current == null)
            return false;
        if (current != null && newValue != null && serializer.equals().test(current, newValue))
            return false;
        current = serializer.copy().apply(newValue);
        return true;
    }

    public void processSync(Object receivedData) {
        current = (T)receivedData;
        set.accept(serializer.copy().apply(current));
    }

    public GenericDataSerializers.DataPair<T> dataPair() {
        return new GenericDataSerializers.DataPair<>(serializer, current);
    }
}


























