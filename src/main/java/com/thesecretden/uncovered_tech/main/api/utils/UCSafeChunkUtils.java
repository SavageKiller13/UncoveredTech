package com.thesecretden.uncovered_tech.main.api.utils;

import com.google.common.collect.ImmutableSet;
import com.thesecretden.uncovered_tech.main.UncoveredTech;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = UncoveredTech.MODID)
public class UCSafeChunkUtils {
    private static final Map<LevelAccessor, Set<ChunkPos>> unloadingChunks = new WeakHashMap<>();

    public static LevelChunk getSafeChunk(LevelAccessor accessor, BlockPos pos) {
        ChunkSource provider = accessor.getChunkSource();
        ChunkPos chunkPos = new ChunkPos(pos);
        if (unloadingChunks.getOrDefault(accessor, ImmutableSet.of()).contains(chunkPos))
            return null;
        return provider.getChunkNow(chunkPos.x, chunkPos.z);
    }

    public static boolean isChunkSafe(LevelAccessor accessor, BlockPos pos) {
        return getSafeChunk(accessor, pos) != null;
    }

    public static BlockEntity getSafeTE(LevelAccessor accessor, BlockPos pos) {
        LevelChunk chunk = getSafeChunk(accessor, pos);
        if (chunk == null)
            return null;
        else
            return chunk.getBlockEntity(pos);
    }

    @Nonnull
    public static BlockState getBlockState(LevelAccessor accessor, BlockPos pos) {
        LevelChunk chunk = getSafeChunk(accessor, pos);
        if (chunk == null)
            return Blocks.AIR.defaultBlockState();
        else
            return chunk.getBlockState(pos);
    }

    public static int getRedstonePower(Level level, BlockPos pos, Direction dir) {
        if (!isChunkSafe(level, pos))
            return 0;
        else
            return level.getSignal(pos, dir);
    }

    public static int getRedstonePowerFromNeighbours(Level level, BlockPos pos) {
        int power = 0;
        for (Direction dir : UCDirectionUtils.VALUES) {
            int neighbourPower = getRedstonePower(level, pos.relative(dir), dir);
            power = Math.max(power, neighbourPower);
            if (power >= 15)
                break;
        }
        return power;
    }

    public static void onChunkUnload(ChunkEvent.Unload event) {
        unloadingChunks.computeIfAbsent(event.getWorld(), level -> new HashSet<>()).add(event.getChunk().getPos());
    }

    public static void onTick(WorldTickEvent event) {
        if (event.phase == Phase.START)
            unloadingChunks.remove(event.world);
    }
}
























