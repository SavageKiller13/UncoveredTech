package com.thesecretden.uncovered_tech.main.common.blocks.ticking;

public interface UCTickableBase {
    default boolean canTickAny() {
        return true;
    }
}
