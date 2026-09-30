package com.bettercontent.bettermalumdynamictrees;

import java.util.Optional;

/** Stable sparse-site selector for native Malum blight events. */
public final class BlightLocusPolicy {
    private static final int CELL_CHUNKS = 40; // 640 blocks, matching OccultDistricts.CELL.
    private static final double SITE_CHANCE = 1.0 / 12.0;

    private BlightLocusPolicy() {}

    public static Optional<Site> siteForCell(long seed, int cellX, int cellZ) {
        long key = seed ^ ((long) cellX * 0x9E3779B97F4A7C15L)
            ^ ((long) cellZ * 0xC2B2AE3D27D4EB4FL);
        if (unit(mix(key ^ 0xD1B54A32D192ED03L)) >= SITE_CHANCE) {
            return Optional.empty();
        }
        int chunkX = cellX * CELL_CHUNKS + bounded(mix(key ^ 0x94D049BB133111EBL), CELL_CHUNKS);
        int chunkZ = cellZ * CELL_CHUNKS + bounded(mix(key ^ 0xBF58476D1CE4E5B9L), CELL_CHUNKS);
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        if (OccultDistricts.occultStrength(seed, blockX, blockZ) < .72) {
            return Optional.empty();
        }
        return Optional.of(new Site(cellX, cellZ, chunkX, chunkZ));
    }

    public static Optional<Site> siteForChunk(long seed, int chunkX, int chunkZ) {
        int cellX = Math.floorDiv(chunkX, CELL_CHUNKS);
        int cellZ = Math.floorDiv(chunkZ, CELL_CHUNKS);
        return siteForCell(seed, cellX, cellZ)
            .filter(site -> site.chunkX() == chunkX && site.chunkZ() == chunkZ);
    }

    private static int bounded(long value, int bound) {
        return (int) Long.remainderUnsigned(value, bound);
    }

    private static double unit(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    public record Site(int cellX, int cellZ, int chunkX, int chunkZ) {
        public long key() {
            return ((long) cellX << 32) ^ (cellZ & 0xffffffffL);
        }
    }
}
