package com.thesecretden.uncovered_tech.main.common.blocks.ticking;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface UCServerTickableTE extends UCTickableBase {
    void tickServer();

    static <T extends BlockEntity> BlockEntityTicker<T> makeTicker() {
        return (level, pos, state, tileEntity) -> {
            UCServerTickableTE tickable = (UCServerTickableTE) tileEntity;
            if (tickable.canTickAny())
                tickable.tickServer();
        };
    }
}
