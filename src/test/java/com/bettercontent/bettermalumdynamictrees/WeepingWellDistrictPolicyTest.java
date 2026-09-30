package com.bettercontent.bettermalumdynamictrees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeepingWellDistrictPolicyTest {
    @Test
    void candidateAdmissionIsStableAcrossSerialParallelAndReorderedQueries() {
        long seed = 441L;
        var points = new ArrayList<int[]>();
        for (int x = -8192; x <= 8192; x += 128) {
            for (int z = -8192; z <= 8192; z += 128) {
                points.add(new int[] {x, z});
            }
        }
        List<Boolean> expected = points.stream().map(p -> WeepingWellDistrictPolicy.admits(seed, p[0], p[1])).toList();
        assertEquals(expected, points.parallelStream().map(p -> WeepingWellDistrictPolicy.admits(seed, p[0], p[1])).toList());
        Collections.reverse(points);
        for (int i = 0; i < points.size(); i++) {
            int[] point = points.get(i);
            assertEquals(expected.get(expected.size() - 1 - i), WeepingWellDistrictPolicy.admits(seed, point[0], point[1]));
        }
    }

    @Test
    void onlyInteriorAndCorePointsCanBeAdmittedAndNeitherBandGuaranteesAWell() {
        long seed = 118L;
        int admittedInterior = 0, rejectedInterior = 0, admittedCore = 0, rejectedCore = 0;
        for (int x = -8192; x <= 8192; x += 32) {
            for (int z = -8192; z <= 8192; z += 32) {
                OccultDistricts.Band band = OccultDistricts.band(seed, x, z);
                boolean admitted = WeepingWellDistrictPolicy.admits(seed, x, z);
                if (band == OccultDistricts.Band.ORDINARY || band == OccultDistricts.Band.FRINGE) {
                    assertFalse(admitted);
                } else if (band == OccultDistricts.Band.INTERIOR) {
                    if (admitted) admittedInterior++; else rejectedInterior++;
                } else {
                    if (admitted) admittedCore++; else rejectedCore++;
                }
            }
        }
        assertTrue(admittedInterior > 0 && rejectedInterior > 0);
        assertTrue(admittedCore > 0 && rejectedCore > 0);
    }
}
