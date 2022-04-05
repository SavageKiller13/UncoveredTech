package com.thesecretden.uncovered_tech.main.common;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UCCommonProxy {
    public void onWorldLoad() {}

    public void handleTileSound(SoundEvent soundEvent, BlockEntity tile, boolean tileActive, float volume, float pitch) {}

    public void stopTileSound(String soundName, BlockEntity tile) {}

    public Level getClientWorld() {
        return null;
    }

    public Player getClientPlayer() {
        return null;
    }

    public void reInitGui() {}

    public void openTileScreen(String guiId, BlockEntity tileEntity) {}
}
