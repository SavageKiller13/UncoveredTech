package com.thesecretden.uncovered_tech.main.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiblockHandler {
    static List<IMultiblock> multiblocks = new ArrayList<>();
    static Map<ResourceLocation, IMultiblock> byUniqueName = new HashMap<>();

    public static synchronized void registerMultiblock(IMultiblock multiblock) {
        multiblocks.add(multiblock);
        byUniqueName.put(multiblock.getUniqueName(), multiblock);
    }

    public static List<IMultiblock> getMultiblocks() {
        return multiblocks;
    }

    @Nullable
    public static IMultiblock getByUniqueName(ResourceLocation name) {
        return byUniqueName.get(name);
    }

    public interface IMultiblock {
        ResourceLocation getUniqueName();

        boolean isBlockTrigger(BlockState state, Direction dir, @Nullable Level level);

        boolean createStructure(Level level, BlockPos pos, Direction dir, Player player);

        List<StructureTemplate.StructureBlockInfo> getStructure(@Nullable Level level);

        Vec3i getSize(@Nullable Level level);

        void disassemble(Level level, BlockPos pos, boolean mirrored, Direction dir);

        BlockPos getTriggerOffset();

        Component getDisplayName();
    }

    public static MultiblockFormEvent postMultiblockFormationEvent(Player player, IMultiblock multiblock, BlockPos clickedPos, ItemStack formItem) {
        MultiblockFormEvent event = new MultiblockFormEvent(player, multiblock, clickedPos, formItem);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    @Cancelable
    public static class MultiblockFormEvent extends PlayerEvent {
        private final IMultiblock multiblock;
        private final BlockPos pos;
        private final ItemStack formItem;

        public MultiblockFormEvent(Player player, IMultiblock multiblock, BlockPos pos, ItemStack formItem) {
            super(player);
            this.multiblock = multiblock;
            this.pos = pos;
            this.formItem = formItem;
        }

        public IMultiblock getMultiblock() {
            return multiblock;
        }

        public BlockPos getClickedBlock() {
            return pos;
        }

        public ItemStack getFormItem() {
            return formItem;
        }
    }
}


























