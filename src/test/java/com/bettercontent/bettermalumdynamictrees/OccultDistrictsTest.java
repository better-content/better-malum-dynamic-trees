package com.bettercontent.bettermalumdynamictrees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OccultDistrictsTest {
    private static final long[] FIXED_SEEDS = {1L, 42L, 991L, 84291L, 0x5EED1234L, -73129L, 8675309L};

    @Test void fixedSeedIsStableAcrossSerialParallelAndReorderedQueries() {
        var points = new ArrayList<long[]>();
        for (int x = -4096; x <= 4096; x += 97) {
            for (int z = -4096; z <= 4096; z += 113) {
                points.add(new long[] {x, z});
            }
        }
        for (long seed : FIXED_SEEDS) {
            List<Double> expected = points.stream()
                .map(p -> OccultDistricts.occultStrength(seed, (int) p[0], (int) p[1])).toList();
            assertEquals(expected, points.parallelStream()
                .map(p -> OccultDistricts.occultStrength(seed, (int) p[0], (int) p[1])).toList(),
                "parallel sampling changed field for seed " + seed);
            Collections.reverse(points);
            for (int i = 0; i < points.size(); i++) {
                assertEquals(expected.get(expected.size() - 1 - i),
                    OccultDistricts.occultStrength(seed, (int) points.get(i)[0], (int) points.get(i)[1]),
                    "reordered sampling changed field for seed " + seed);
            }
            Collections.reverse(points);
        }
    }

    @Test void fixedWorldSeedsRetainGapsAndBroadIrregularBands() {
        for (long seed : FIXED_SEEDS) {
            int[] counts = new int[OccultDistricts.Band.values().length];
            int transitions = 0;
            int comparedEdges = 0;
            OccultDistricts.Band[][] grid = new OccultDistricts.Band[129][129];
            for (int ix = 0; ix < grid.length; ix++) {
                for (int iz = 0; iz < grid[ix].length; iz++) {
                    OccultDistricts.Band band = OccultDistricts.band(seed, (ix - 64) * 128, (iz - 64) * 128);
                    grid[ix][iz] = band;
                    counts[band.ordinal()]++;
                    if (ix > 0) {
                        comparedEdges++;
                        if (band != grid[ix - 1][iz]) transitions++;
                    }
                    if (iz > 0) {
                        comparedEdges++;
                        if (band != grid[ix][iz - 1]) transitions++;
                    }
                }
            }
            double total = grid.length * grid[0].length;
            assertShare(counts[OccultDistricts.Band.ORDINARY.ordinal()], total, .45, .58, "ordinary", seed);
            assertShare(counts[OccultDistricts.Band.FRINGE.ordinal()], total, .23, .30, "fringe", seed);
            assertShare(counts[OccultDistricts.Band.INTERIOR.ordinal()], total, .15, .21, "interior", seed);
            assertShare(counts[OccultDistricts.Band.CORE.ordinal()], total, .025, .055, "core", seed);
            double boundaryRate = (double) transitions / comparedEdges;
            assertTrue(boundaryRate > .40 && boundaryRate < .55,
                "districts should have irregular boundaries for seed " + seed + ": " + boundaryRate);
        }
    }

    private static void assertShare(int count, double total, double minimum, double maximum,
                                    String band, long seed) {
        double share = count / total;
        assertTrue(share >= minimum && share <= maximum,
            band + " share outside measured range for seed " + seed + ": " + share);
    }
}
