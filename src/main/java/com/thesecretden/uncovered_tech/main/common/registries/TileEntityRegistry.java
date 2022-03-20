package com.thesecretden.uncovered_tech.main.common.registries;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.common.tile_entities.abstractTE.AbstractTEFurnace;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TileEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, UncoveredTech.MODID);

    //public static final RegistryObject<BlockEntityType<AbstractTEFurnace>> TILE_ENTITY_SIMPLE_FURNACE = TILE_ENTITIES.register("te_simple_furnace", () -> BlockEntityType.Builder.of(AbstractTEFurnace::new).build(null));


}
