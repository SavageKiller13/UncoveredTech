package com.thesecretden.uncovered_tech.main.api.utils.shapes;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CachedShapesWithTransform<ShapeKey, TransformKey> extends CachedVoxelShapes<Pair<ShapeKey, TransformKey>> {
    public CachedShapesWithTransform(Function<ShapeKey, List<AABB>> creator, BiFunction<TransformKey, AABB, AABB> transform) {
        super(p -> {
            List<AABB> base = creator.apply(p.getFirst());
            if (base == null)
                return ImmutableList.of();
            List<AABB> transformReturn = new ArrayList<>(base.size());
            for (AABB aabb : base)
                transformReturn.add(transform.apply(p.getSecond(), aabb));
            return transformReturn;
        });
    }

    public VoxelShape get(ShapeKey key, TransformKey transformKey) {
        return get(Pair.of(key, transformKey));
    }

    public static CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> createForMultiblock(Function<BlockPos, List<AABB>> create) {
        return new CachedShapesWithTransform<>(create, (key, box) -> withFacingAndMirror(box, key.getFirst(), key.getSecond()));
    }

    public static AABB withFacingAndMirror(AABB in, Direction direction, boolean mirror) {
        AABB mirrored = in;
        if (mirror)
            mirrored = new AABB(1 - in.minX, in.minY, in.minZ, 1 - in.maxX, in.maxY, in.maxZ);
        return UCShapeUtils.transformAABB(mirrored, direction);
    }

    public static <T> CachedShapesWithTransform<T, Direction> createDirectional(Function<T, List<AABB>> create) {
        return new CachedShapesWithTransform<>(create, (key, box) -> UCShapeUtils.transformAABB(box, key));
    }
}




















