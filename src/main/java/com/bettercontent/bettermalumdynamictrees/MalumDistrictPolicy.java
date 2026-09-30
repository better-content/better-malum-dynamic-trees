package com.bettercontent.bettermalumdynamictrees;

/** Placement policy for the Dynamic Trees selector seam. */
public final class MalumDistrictPolicy {
    private static final float AZURE_MAX_TEMPERATURE = 0.40F;

    private MalumDistrictPolicy() {}

    public static boolean shouldSelectRunewood(long seed, int x, int z, boolean azure, float temperature) {
        if (azure && temperature > AZURE_MAX_TEMPERATURE) {
            return false;
        }
        double strength = OccultDistricts.occultStrength(seed, x, z);
        double chance = strength >= .72 ? .16 : strength >= .34 ? .08 : strength > 0 ? .015 : 0;
        return sample(seed, x, z, azure) < chance;
    }

    private static double sample(long seed, int x, int z, boolean azure) {
        long value = seed ^ ((long) x * 0x9E3779B97F4A7C15L) ^ ((long) z * 0xC2B2AE3D27D4EB4FL);
        if (azure) {
            value ^= 0xD1B54A32D192ED03L;
        }
        value ^= value >>> 30;
        value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27;
        value *= 0x94d049bb133111ebL;
        return ((value ^ (value >>> 31)) >>> 11) * 0x1.0p-53;
    }
}
