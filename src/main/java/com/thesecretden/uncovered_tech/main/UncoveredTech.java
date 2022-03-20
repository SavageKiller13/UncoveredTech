package com.thesecretden.uncovered_tech.main;

import com.mojang.logging.LogUtils;
import com.thesecretden.uncovered_tech.main.common.registries.BlockRegistry;
import com.thesecretden.uncovered_tech.main.common.registries.ItemRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(UncoveredTech.MODID)
public class UncoveredTech {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "uncovered_tech";

    public UncoveredTech() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::sendIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::receiveIMC);

        BlockRegistry.registerBlocks();
        ItemRegistry.registerItems();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Uncovered Tech is Loading");
    }

    private void sendIMC(final InterModEnqueueEvent event) {

    }

    private void receiveIMC(final InterModProcessEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Uncovered Tech is starting on the server");
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

    }
}
