package com.thesecretden.uncovered_tech.main;

import com.mojang.logging.LogUtils;
import com.thesecretden.uncovered_tech.main.api.UCAPI;
import com.thesecretden.uncovered_tech.main.common.UCCommonProxy;
import com.thesecretden.uncovered_tech.main.common.registries.BlockRegistry;
import com.thesecretden.uncovered_tech.main.common.registries.ItemRegistry;
import com.thesecretden.uncovered_tech.main.common.util.MissingMappingsHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(UncoveredTech.MODID)
public class UncoveredTech {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "uncovered_tech";
    public static final String VERSION = UCAPI.getCurrentVersion();
    public static final UCCommonProxy proxy = DistExecutor.safeRunForDist(bootstrapErrorToXCPInDev(() -> UCClientProxy::new), bootstrapErrorToXCPInDev(() -> UCCommonProxy::new));

    public static final SimpleChannel packetHandler = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MODID, "main")).networkProtocolVersion(() -> VERSION).serverAcceptedVersions(VERSION::equals).clientAcceptedVersions(VERSION::equals).simpleChannel();

    public static final String DESC = "desc." + MODID + ".";
    public static final String DESC_INFO = DESC + "info.";

    public static <T> Supplier<T> bootstrapErrorToXCPInDev(Supplier<T> in) {
        if (FMLLoader.isProduction())
            return in;
        return () -> {
            try {
                return in.get();
            } catch (BootstrapMethodError error) {
                throw new RuntimeException(error);
            }
        };
    }

    public UncoveredTech() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::sendIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::receiveIMC);
        MinecraftForge.EVENT_BUS.addGenericListener(Block.class, (Consumer<RegistryEvent.MissingMappings<Block>>) MissingMappingsHelper::handleRemapping);
        MinecraftForge.EVENT_BUS.addGenericListener(Item.class, (Consumer<RegistryEvent.MissingMappings<Item>>) MissingMappingsHelper::handleRemapping);
        MinecraftForge.EVENT_BUS.addGenericListener(Fluid.class, (Consumer<RegistryEvent.MissingMappings<Fluid>>) MissingMappingsHelper::handleRemapping);


        BlockRegistry.registerBlocks();
        ItemRegistry.registerItems();
    }

    private void setup(final FMLCommonSetupEvent event) {


        UCAPI.prefixToIngotMap.put("ingots", new Integer[]{1, 1});
        UCAPI.prefixToIngotMap.put("nuggets", new Integer[]{1, 9});
        UCAPI.prefixToIngotMap.put("storage_blocks", new Integer[]{9, 1});

        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    private void sendIMC(final InterModEnqueueEvent event) {

    }

    private void receiveIMC(final InterModProcessEvent event) {

    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static final CreativeModeTab MATERIALS_TAB = new CreativeModeTab(MODID) {
        @Nullable
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.ITEM_ALUMINIUM_INGOT.get());
        }
    };

    
}
