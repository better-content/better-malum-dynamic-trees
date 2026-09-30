package com.bettercontent.bettermalumdynamictrees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class BlightLocusPolicyTest {
    @Test
    void candidateCellsAreStableAcrossQueryOrderAndChunkBoundaries() {
        long seed = 991L;
        var cells = new ArrayList<int[]>();
        for (int x = -160; x <= 160; x += 3) {
            for (int z = -160; z <= 160; z += 5) {
                cells.add(new int[] {x, z});
            }
        }
        List<BlightLocusPolicy.Site> expected = cells.stream()
            .map(cell -> BlightLocusPolicy.siteForCell(seed, cell[0], cell[1]).orElse(null)).toList();
        for (int i = 0; i < cells.size(); i++) {
            int[] cell = cells.get(i);
            assertEquals(expected.get(i), BlightLocusPolicy.siteForCell(seed, cell[0], cell[1]).orElse(null));
        }
        Collections.reverse(cells);
        for (int i = 0; i < cells.size(); i++) {
            int[] cell = cells.get(i);
            BlightLocusPolicy.Site site = BlightLocusPolicy.siteForCell(seed, cell[0], cell[1]).orElse(null);
            assertEquals(expected.get(expected.size() - 1 - i), site);
            if (site != null) {
                assertEquals(site, BlightLocusPolicy.siteForChunk(seed, site.chunkX(), site.chunkZ()).orElseThrow());
                assertTrue(OccultDistricts.occultStrength(seed, site.chunkX() * 16 + 8, site.chunkZ() * 16 + 8) >= .72);
            }
        }
    }

    @Test
    void sparseSitesAreNotGuaranteedForStrongDistricts() {
        long seed = 42L;
        int selected = 0;
        int total = 0;
        for (int x = -256; x <= 256; x += 2) {
            for (int z = -256; z <= 256; z += 2) {
                total++;
                var candidate = BlightLocusPolicy.siteForCell(seed, x, z);
                if (candidate.isPresent()) selected++;
            }
        }
        assertTrue(selected > 0);
        assertTrue(selected < total / 12, "selected loci must remain sparse across world cells");
    }
}
