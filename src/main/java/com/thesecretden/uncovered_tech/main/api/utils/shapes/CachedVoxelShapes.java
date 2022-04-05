package com.thesecretden.uncovered_tech.main.api.utils.shapes;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CachedVoxelShapes<Key> {
    private final Map<Key, VoxelShape> calculatedShapes = new ConcurrentHashMap<>();
    private final Function<Key, List<AABB>> creator;

    public CachedVoxelShapes(Function<Key, List<AABB>> creator) {
        this.creator = creator;
    }

    public VoxelShape get(Key k) {
        return calculatedShapes.computeIfAbsent(k, this::calculateShape);
    }

    private VoxelShape calculateShape(Key k) {
        List<AABB> subShapes = creator.apply(k);
        VoxelShape shapeReturn = Shapes.empty();
        if (subShapes != null)
            for (AABB aabb : subShapes)
                shapeReturn = Shapes.joinUnoptimized(shapeReturn, Shapes.create(aabb), BooleanOp.OR);
        return shapeReturn.optimize();
    }
}
