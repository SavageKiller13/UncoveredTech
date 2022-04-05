package com.thesecretden.uncovered_tech.main.api.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class UCPlayerUtils {
    public static void resetFloatingState(@Nullable Entity player) {
        if (player instanceof ServerPlayer) {
            ConnectionAccess access = (ConnectionAccess)((ServerPlayer)player).connection;
            access.setClientIsFloating(false);
            access.setAboveGroundTickCount(0);
        }
    }

    public interface ConnectionAccess {
        void setClientIsFloating(boolean shouldFloat);

        void setAboveGroundTickCount(int ticks);
    }
}
