package com.thesecretden.uncovered_tech.main.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record DirectionalBlockPos(BlockPos pos, Direction dir) {
}
