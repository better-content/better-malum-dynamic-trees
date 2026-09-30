package com.bettercontent.bettermalumdynamictrees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class MalumDistrictPolicyTest {
    @Test
    void policyIsStableAcrossSerialParallelAndReorderedQueries() {
        long seed = 83124L;
        var points = new ArrayList<int[]>();
        for (int x = -4096; x <= 4096; x += 71) {
            for (int z = -4096; z <= 4096; z += 83) {
                points.add(new int[] {x, z});
            }
        }
        List<Boolean> expected = points.stream()
            .map(p -> MalumDistrictPolicy.shouldSelectRunewood(seed, p[0], p[1], false, .8F)).toList();
        assertEquals(expected, points.parallelStream()
            .map(p -> MalumDistrictPolicy.shouldSelectRunewood(seed, p[0], p[1], false, .8F)).toList());
        Collections.reverse(points);
        for (int i = 0; i < points.size(); i++) {
            int[] point = points.get(i);
            assertEquals(expected.get(expected.size() - 1 - i),
                MalumDistrictPolicy.shouldSelectRunewood(seed, point[0], point[1], false, .8F));
        }
    }

    @Test
    void azureRequiresActualColdSuitability() {
        long seed = 99L;
        int[] selected = findSelected(seed, true, .2F);
        assertTrue(MalumDistrictPolicy.shouldSelectRunewood(seed, selected[0], selected[1], true, .2F));
        assertFalse(MalumDistrictPolicy.shouldSelectRunewood(seed, selected[0], selected[1], true, .41F));
    }

    @Test
    void ordinaryGapsNeverSelectAndFringesRemainSparseClues() {
        long seed = 121L;
        int ordinary = 0;
        int fringeSelections = 0;
        for (int x = -8192; x <= 8192; x += 32) {
            for (int z = -8192; z <= 8192; z += 32) {
                OccultDistricts.Band band = OccultDistricts.band(seed, x, z);
                boolean selected = MalumDistrictPolicy.shouldSelectRunewood(seed, x, z, false, .8F);
                if (band == OccultDistricts.Band.ORDINARY) {
                    ordinary++;
                    assertFalse(selected);
                } else if (band == OccultDistricts.Band.FRINGE && selected) {
                    fringeSelections++;
                }
            }
        }
        assertTrue(ordinary > 100);
        assertTrue(fringeSelections > 0, "fringes should expose occasional Runewood clues");
    }

    private static int[] findSelected(long seed, boolean azure, float temperature) {
        for (int x = -8192; x <= 8192; x += 16) {
            for (int z = -8192; z <= 8192; z += 16) {
                if (MalumDistrictPolicy.shouldSelectRunewood(seed, x, z, azure, temperature)) {
                    return new int[] {x, z};
                }
            }
        }
        throw new AssertionError("sample must contain an eligible district placement");
    }
}
