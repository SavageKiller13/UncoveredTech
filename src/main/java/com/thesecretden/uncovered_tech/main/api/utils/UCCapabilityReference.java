package com.thesecretden.uncovered_tech.main.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class UCCapabilityReference<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    public static <T> UCCapabilityReference<T> forTileEntityAt(BlockEntity entity, Supplier<DirectionalBlockPos> pos, Capability<T> cap) {
        return new TECapReference<>(entity::getLevel, pos, cap);
    }

    public static <T> UCCapabilityReference<T> forRelative(BlockEntity entity, Capability<T> cap, Vec3i offset, Direction dir) {
        return forTileEntityAt(entity, () -> new DirectionalBlockPos(entity.getBlockPos().offset(offset), dir.getOpposite()), cap);
    }

    public static <T> UCCapabilityReference<T> forNeighbour(BlockEntity entity, Capability<T> cap, NonNullSupplier<Direction> dir) {
        return forTileEntityAt(entity, () -> {
            Direction d = dir.get();
            return new DirectionalBlockPos(entity.getBlockPos().relative(d), d.getOpposite());
        }, cap);
    }

    public static <T> UCCapabilityReference<T> forNeighbour(BlockEntity entity, Capability<T> cap, @Nonnull Direction dir) {
        return forRelative(entity, cap, BlockPos.ZERO.relative(dir), dir);
    }

    public static <T> Map<Direction, UCCapabilityReference<T>> forAllNeighbours(BlockEntity entity, Capability<T> cap) {
        Map<Direction, UCCapabilityReference<T>> neighbours = new EnumMap<>(Direction.class);
        for (Direction dir : UCDirectionUtils.VALUES)
            neighbours.put(dir, UCCapabilityReference.forNeighbour(entity, cap, dir));
        return neighbours;
    }

    protected final Capability<T> cap;

    protected UCCapabilityReference(Capability<T> cap) {
        this.cap = Objects.requireNonNull(cap);
    }

    @Nullable
    public abstract T getNullable();

    @Nonnull
    public T get() {
        return Objects.requireNonNull(getNullable());
    }

    public abstract boolean isPresent();

    private static class TECapReference<T> extends UCCapabilityReference<T> {
        private final Supplier<Level> getLevel;
        private final Supplier<DirectionalBlockPos> getPos;
        @Nonnull
        private LazyOptional<T> currentCap = LazyOptional.empty();
        private DirectionalBlockPos lastPos;
        private Level lastLevel;
        private BlockEntity lastTE;

        public TECapReference(Supplier<Level> getLevel, Supplier<DirectionalBlockPos> getPos, Capability<T> cap) {
            super(cap);
            this.getLevel = getLevel;
            this.getPos = getPos;
        }

        @Nullable
        @Override
        public T getNullable() {
            updateLazyOptional();
            return currentCap.orElse(null);
        }

        @Override
        public boolean isPresent() {
            updateLazyOptional();
            return currentCap.isPresent();
        }

        private void updateLazyOptional() {
            Level currentLevel = getLevel.get();
            DirectionalBlockPos currentPos = getPos.get();
            if (currentLevel == null && currentPos == null) {
                currentCap = LazyOptional.empty();
                lastLevel = null;
                lastPos = null;
                lastTE = null;
            } else if (currentLevel != lastLevel || !currentPos.equals(lastPos) || !currentCap.isPresent() || (lastTE != null && lastTE.isRemoved())) {
                if (currentCap.isPresent() && lastTE != null && lastTE.isRemoved()) {
                    LOGGER.warn("The tile entity {} (class {}) has been removed, but the value {} provided by it "+"for the capability {} is still marked as valid. This is likely a big in the mod(s) adding "+"the tile entity/the capability", lastTE, lastTE.getClass(), currentCap.orElseThrow(RuntimeException::new), cap.getName());
                }
                lastTE = UCSafeChunkUtils.getSafeTE(currentLevel, currentPos.pos());
                if (lastTE != null)
                    currentCap = lastTE.getCapability(cap, currentPos.dir());
                else
                    currentCap = LazyOptional.empty();
                lastLevel = currentLevel;
                lastPos = currentPos;
            }
        }
    }
}






















