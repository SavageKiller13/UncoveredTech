package com.thesecretden.uncovered_tech.main.common;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.Queue;

public class EventHandler {
    public static final Queue<Runnable> SERVER_TASKS = new ArrayDeque<>();

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        UncoveredTech.proxy.onWorldLoad();
    }



}
