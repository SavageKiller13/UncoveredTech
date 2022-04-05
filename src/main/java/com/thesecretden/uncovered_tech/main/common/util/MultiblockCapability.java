package com.thesecretden.uncovered_tech.main.common.util;

import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;

public abstract class MultiblockCapability<T> {
    public static <TE, T> MultiblockCapability<T> make(TE owner, Function<TE, MultiblockCapability<T>> getCap, Function<TE, TE> getMaster, ResettableCapability<T> ownValue) {
        return new Impl<>(getCap, getMaster, owner, ownValue);
    }

    public abstract LazyOptional<T> get();

    public final <T2> LazyOptional<T2> getAndCast() {
        return get().cast();
    }

    private static final class Impl<T, TE> extends MultiblockCapability<T> {
        private final TE owner;
        private final ResettableCapability<T> ownValue;
        private final Function<TE, MultiblockCapability<T>> getCap;
        private final Function<TE, TE> getMaster;
        private LazyOptional<T> cached = LazyOptional.empty();

        public Impl(Function<TE, MultiblockCapability<T>> getCap, Function<TE, TE> getMaster, TE owner, ResettableCapability<T> ownValue) {
            this.owner = owner;
            this.getCap = getCap;
            this.getMaster = getMaster;
            this.ownValue = ownValue;
            this.ownValue.addResetListener(cached::invalidate);
        }

        @Override
        public LazyOptional<T> get() {
            if (!cached.isPresent()) {
                TE master = getMaster.apply(owner);
                if (master != null)
                    cached = ((Impl<T, ?>)getCap.apply(master)).ownValue.getLO();
            }
            return cached;
        }
    }
}
