package com.thesecretden.uncovered_tech.main.common.blocks.ticking;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface UCClientTickableTE extends UCTickableBase{
    void tickClient();

    static <T extends BlockEntity> BlockEntityTicker<T> makeTicker() {
        return (level, pos, state, tileEntity) -> {
            UCClientTickableTE tickable = (UCClientTickableTE) tileEntity;
            if (tickable.canTickAny())
                tickable.tickClient();
        };
    }
}
