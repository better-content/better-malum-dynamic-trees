package com.bettercontent.bettermalumdynamictrees;

/** Stateless admission gate for Malum's native Weeping Well structure candidate. */
public final class WeepingWellDistrictPolicy {
    private WeepingWellDistrictPolicy() {}

    /**
     * Leaves terrain, cave, flatness, Y-range, and jigsaw validity to Malum after this district gate.
     * The probability deliberately remains below one so a qualifying district never promises a Well.
     */
    public static boolean admits(long seed, int x, int z) {
        double strength = OccultDistricts.occultStrength(seed, x, z);
        if (strength < .34) {
            return false;
        }
        double chance = strength >= .72 ? .18 : .07;
        return sample(seed, x, z) < chance;
    }

    private static double sample(long seed, int x, int z) {
        long value = seed ^ ((long) x * 0xD6E8FEB86659FD93L) ^ ((long) z * 0xA5A3564E27F8864DL);
        value = (value ^ (value >>> 32)) * 0xd6e8feb86659fd93L;
        value = (value ^ (value >>> 32)) * 0xd6e8feb86659fd93L;
        return ((value ^ (value >>> 32)) >>> 11) * 0x1.0p-53;
    }
}
