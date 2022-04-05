package com.thesecretden.uncovered_tech.main.api.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

import java.util.Arrays;
import java.util.Comparator;

import static net.minecraft.core.Direction.*;

public class UCDirectionUtils {
    public static final Direction[] VALUES = Direction.values();
    public static final Direction[] BY_HORIZONTAL_INDEX = Arrays.stream(VALUES).filter((direction) -> direction.getAxis().isHorizontal()).sorted(Comparator.comparingInt(Direction::get2DDataValue)).toArray(Direction[]::new);

    public static Rotation getRotationBetweenFacings(Direction origin, Direction to) {
        if (to == origin)
            return Rotation.NONE;
        if (origin.getAxis() == Axis.Y || to.getAxis() == Axis.Y)
            return null;
        origin = origin.getClockWise();
        if (origin == to)
            return Rotation.CLOCKWISE_90;
        origin = origin.getClockWise();
        if (origin == to)
            return Rotation.CLOCKWISE_180;
        origin = origin.getClockWise();
        if (origin == to)
            return Rotation.COUNTERCLOCKWISE_90;
        return null;
    }

    public static Direction rotateAround(Direction dir, Direction.Axis axis) {
        if (axis == dir.getAxis())
            return dir;
        return switch (axis) {
            case X -> rotateX(dir);
            case Y -> dir.getClockWise();
            case Z -> rotateZ(dir);
        };
    }

    public static Direction rotateX(Direction dir) {
        return switch (dir) {
            case NORTH -> DOWN;
            case SOUTH -> UP;
            case UP -> NORTH;
            case DOWN -> SOUTH;
            case EAST, WEST -> throw new IllegalStateException("Unable to get X-rotated facing of" + dir);
        };
    }

    public static Direction rotateZ(Direction dir) {
        return switch (dir) {
            case EAST -> DOWN;
            case WEST -> UP;
            case UP -> EAST;
            case DOWN -> WEST;
            case NORTH, SOUTH -> throw new IllegalStateException("Unable to get Z-rotated facing of" + dir);
        };
    }
}
