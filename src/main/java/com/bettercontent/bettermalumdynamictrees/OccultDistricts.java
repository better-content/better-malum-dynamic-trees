package com.bettercontent.bettermalumdynamictrees;

/** Stateless district field for later Malum flora and Well placement. */
public final class OccultDistricts {
    private static final int CELL = 640;

    private OccultDistricts() {}

    /** Returns 0 for ordinary gaps, then a continuous fringe/interior/core strength in [0, 1]. */
    public static double occultStrength(long seed, int x, int z) {
        int cellX = Math.floorDiv(x, CELL), cellZ = Math.floorDiv(z, CELL);
        double best = 0.0;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                long cell = mix(seed ^ (((long) cellX + dx) * 0x9E3779B97F4A7C15L)
                    ^ ((long) (cellZ + dz) * 0xC2B2AE3D27D4EB4FL));
                double centerX = (cellX + dx) * (double) CELL + CELL * (.20 + unit(cell) * .60);
                double centerZ = (cellZ + dz) * (double) CELL + CELL * (.20 + unit(cell >>> 21) * .60);
                double radius = CELL * (.30 + unit(cell >>> 42) * .19);
                double distance = Math.hypot(x - centerX, z - centerZ);
                best = Math.max(best, 1.0 - distance / radius);
            }
        }
        return Math.max(0.0, Math.min(1.0, best));
    }

    public static Band band(long seed, int x, int z) {
        double strength = occultStrength(seed, x, z);
        return strength >= .72 ? Band.CORE : strength >= .34 ? Band.INTERIOR : strength > 0 ? Band.FRINGE : Band.ORDINARY;
    }

    public enum Band { ORDINARY, FRINGE, INTERIOR, CORE }

    private static double unit(long value) {
        return (mix(value) >>> 11) * 0x1.0p-53;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }
}
