package com.thesecretden.uncovered_tech.main.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.Vec3;

public enum PlacementLimitation {
    SIDE_CLICKED((dir, placer, hitPos) -> dir),
    PISTON_LIKE((dir, placer, hitPos) -> Direction.orderedByNearest(placer)[0]),
    HORIZONTAL((dir, placer, hitPos) -> Direction.fromYRot(placer.getYRot())),
    VERTICAL((dir, placer, hitPos) -> (dir != Direction.DOWN && (dir == Direction.UP || hitPos.y <= .5)) ? Direction.UP : Direction.DOWN),
    HORIZONTAL_AXIS((dir, placer, hitPos) -> {
        Direction dirF = Direction.fromYRot(placer.getYRot());
        if (dirF == Direction.SOUTH || dirF == Direction.WEST)
            return dirF.getOpposite();
        else
            return dirF;
    }),
    HORIZONTAL_QUADRANT((dir, placer, hitPos) -> {
        if (dir.getAxis() != Direction.Axis.Y)
            return dir.getOpposite();
        else {
            double xFromMid = hitPos.x - .5;
            double zFromMid = hitPos.z - .5;
            double max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
            if (max == Math.abs(xFromMid))
                return xFromMid < 0 ? Direction.WEST : Direction.EAST;
            else
                return zFromMid < 0 ? Direction.NORTH : Direction.SOUTH;
        }
    }),
    HORIZONTAL_PREFER_SIDE((dir, placer, hitPos) -> dir.getAxis() != Direction.Axis.Y ? dir.getOpposite() : placer.getDirection()),
    FIXED_DOWN((dir, placer, hitPos) -> Direction.DOWN);

    private final DirectionGetter dirGetter;

    PlacementLimitation(DirectionGetter dirGetter) {
        this.dirGetter = dirGetter;
    }

    public Direction getDirectionForPlacement(Direction dir, LivingEntity entity, Vec3 clickLocation) {
        return this.dirGetter.getDirectionForPlacement(dir, entity, clickLocation);
    }

    public Direction getDirectionForPlacement(BlockPlaceContext ctx) {
        Vec3 clickLocation = ctx.getClickLocation();
        BlockPos pos = ctx.getClickedPos();
        clickLocation = clickLocation.subtract(pos.getX(), pos.getY(), pos.getZ());
        return getDirectionForPlacement(ctx.getClickedFace(), ctx.getPlayer(), clickLocation);
    }

    private interface DirectionGetter {
        Direction getDirectionForPlacement(Direction dir, LivingEntity entity, Vec3 clickPos);
    }
}
