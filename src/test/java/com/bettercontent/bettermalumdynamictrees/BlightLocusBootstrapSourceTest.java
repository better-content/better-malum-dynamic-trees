package com.bettercontent.bettermalumdynamictrees;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BlightLocusBootstrapSourceTest {
    @Test
    void bootstrapUsesPersistedClaimsAndMalumsNativeFourBurstBlightEvent() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        String bootstrap = Files.readString(root.resolve("src/main/java/com/bettercontent/bettermalumdynamictrees/BlightLocusBootstrap.java"));
        String data = Files.readString(root.resolve("src/main/java/com/bettercontent/bettermalumdynamictrees/BlightLocusData.java"));

        assertTrue(bootstrap.contains("Level.OVERWORLD"));
        int eligibleSite = bootstrap.indexOf("BlockPos source = findEligibleBase(level, chunk)");
        int persistedClaim = bootstrap.indexOf("locusData.claim(selected.key())");
        int supportCheck = bootstrap.indexOf("level.getBlockState(base).isSolidRender(level, base)");
        assertTrue(eligibleSite >= 0 && persistedClaim > eligibleSite && supportCheck >= 0,
            "terrain must be found before the one-time event claim is consumed");
        assertTrue(bootstrap.contains("for (int radius = 0; radius <= 7; radius++)"),
            "the candidate chunk should search nearby eligible ground before abandoning its rare locus");
        assertFalse(bootstrap.contains("SOULWOOD_GROWTH"), "blight sites must not place the tree starter");
        assertFalse(bootstrap.contains("level.setBlock("), "blight site bootstrap must not place extra blocks");
        assertTrue(bootstrap.contains("new ActiveBlightEvent()"));
        assertTrue(bootstrap.contains(".setBlightData(2, 4, 4)"));
        assertTrue(bootstrap.contains("WorldEventHandler.addWorldEvent(level, blight)"));
        assertFalse(bootstrap.contains("SoulwoodTreeFeature"), "do not directly place a Soulwood forest");
        assertTrue(data.contains("computeIfAbsent(BlightLocusData::load, BlightLocusData::new, DATA_NAME)"));
        assertTrue(data.contains("tag.put(CELLS_KEY, cells)"));
    }

    @Test
    void pinnedMalumEventDelegatesSoulwoodGrowthToItsNativeBlightGenerator() throws IOException {
        String event = pinnedClassBytes("com/sammy/malum/common/worldevent/ActiveBlightEvent.class");
        String feature = pinnedClassBytes("com/sammy/malum/common/worldgen/tree/SoulwoodTreeFeature.class");

        assertTrue(event.contains("SoulwoodTreeFeature"));
        assertTrue(event.contains("generateBlight"));
        assertTrue(feature.contains("SOULWOOD_GROWTH"));
        assertTrue(feature.contains("BLIGHT"));
    }

    private static String pinnedClassBytes(String resource) throws IOException {
        try (var input = BlightLocusBootstrapSourceTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, "pinned Malum class missing from test runtime: " + resource);
            return new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }
}
