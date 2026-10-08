package com.bettercontent.bettermalumdynamictrees;

import com.sammy.malum.common.worldevent.ActiveBlightEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import team.lodestar.lodestone.handlers.WorldEventHandler;

/** Starts Malum's native finite blight event once at rare stable district sites. */
@Mod.EventBusSubscriber(modid = Dtmalum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BlightLocusBootstrap {
    private static final PendingBlightLoci<ServerLevel> PENDING = new PendingBlightLoci<>();

    private BlightLocusBootstrap() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !Level.OVERWORLD.equals(level.dimension())) {
            return;
        }
        var chunk = event.getChunk();
        var site = BlightLocusPolicy.siteForChunk(level.getSeed(), chunk.getPos().x, chunk.getPos().z);
        if (site.isEmpty()) {
            return;
        }
        // Forge Load can run before the FULL future is published. Metadata only here:
        // even a same-chunk level read can wait on the future enclosing this callback.
        PENDING.enqueue(level, site.get());
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)
                || !Level.OVERWORLD.equals(level.dimension()) || !level.getServer().isSameThread()) {
            return;
        }
        // Native END tick is outside the Load callback; lookup does not request or await chunks.
        PENDING.drain(level,
            candidate -> level.getChunkSource().getChunkNow(candidate.chunkX(), candidate.chunkZ()),
            (candidate, chunk) -> admit(level, chunk, candidate.site()));
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PENDING.unloadChunk(level, event.getChunk().getPos().x, event.getChunk().getPos().z);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PENDING.unloadLevel(level);
        }
    }

    private static void admit(ServerLevel level, net.minecraft.world.level.chunk.ChunkAccess chunk,
            BlightLocusPolicy.Site selected) {
        BlightLocusData locusData = BlightLocusData.get(level);
        BlockPos source = findEligibleBase(level, chunk);
        if (source == null) {
            return;
        }
        // Commit the once-only claim only after a valid ground location is found.
        if (!locusData.claim(selected.key())) {
            return;
        }

        ActiveBlightEvent blight = new ActiveBlightEvent()
            .setPosition(source)
            .setBlightData(2, 4, 4);
        WorldEventHandler.addWorldEvent(level, blight);
    }

    private static BlockPos findEligibleBase(ServerLevel level, net.minecraft.world.level.chunk.ChunkAccess chunk) {
        int centerX = chunk.getPos().getMinBlockX() + 8;
        int centerZ = chunk.getPos().getMinBlockZ() + 8;
        // Search outward deterministically inside this chunk so water, leaves, or obstructed centers
        // do not discard a rare locus when another suitable surface is nearby.
        for (int radius = 0; radius <= 7; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    int x = centerX + dx;
                    int z = centerZ + dz;
                    int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                    BlockPos base = new BlockPos(x, surfaceY, z);
                    if (surfaceY > level.getMinBuildHeight()
                            && level.getBlockState(base).isSolidRender(level, base)) {
                        return base;
                    }
                }
            }
        }
        return null;
    }
}
