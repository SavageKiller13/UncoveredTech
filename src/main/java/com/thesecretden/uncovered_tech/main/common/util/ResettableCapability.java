package com.thesecretden.uncovered_tech.main.common.util;

import com.thesecretden.uncovered_tech.main.api.utils.UCCapabillityUtils;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;

public class ResettableCapability<T> {
    private final T containedValue;
    private final List<Runnable> onReset = new ArrayList<>();
    private LazyOptional<T> currentOptional = LazyOptional.empty();

    public ResettableCapability(T containedValue) {
        this.containedValue = containedValue;
    }

    public LazyOptional<T> getLO() {
        if (!currentOptional.isPresent())
            currentOptional = UCCapabillityUtils.constantOptional(containedValue);
        return currentOptional;
    }

    public T get() {
        return containedValue;
    }

    public <A> LazyOptional<A> cast() {
        return getLO().cast();
    }

    public void reset() {
        currentOptional.invalidate();
        this.onReset.forEach(Runnable::run);
    }

    public void addResetListener(Runnable onReset) {
        this.onReset.add(onReset);
    }
}
