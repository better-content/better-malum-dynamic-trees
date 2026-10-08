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
    void forgeLoadOnlyQueuesMetadataAndNativeEndTickOwnsReadyAdmission() throws Exception {
        String bootstrap = Files.readString(Path.of("src/main/java/com/bettercontent/bettermalumdynamictrees/BlightLocusBootstrap.java"));
        String load = methodBody(bootstrap, "public static void onChunkLoad(");
        assertTrue(load.contains("BlightLocusPolicy.siteForChunk(level.getSeed()"));
        assertTrue(load.contains("PENDING.enqueue(level, site.get())"));
        for (String forbidden : new String[]{"findEligibleBase(", "BlightLocusData.get(", ".claim(",
                "getHeight(", "getBlockState(", "getChunkNow(", "new ActiveBlightEvent", "addWorldEvent("}) {
            assertFalse(load.contains(forbidden), "pre-FULL Load callback must not interact with the level: " + forbidden);
        }
        String tick = methodBody(bootstrap, "public static void onLevelTick(");
        assertTrue(tick.contains("TickEvent.Phase.END"));
        assertTrue(tick.contains("event.level instanceof ServerLevel level"));
        assertTrue(tick.contains("level.getServer().isSameThread()"));
        assertTrue(tick.contains("PENDING.drain(level"));
        assertTrue(tick.contains("getChunkNow(candidate.chunkX(), candidate.chunkZ())"));
        assertTrue(tick.contains("admit(level, chunk, candidate.site())"));
        assertTrue(methodBody(bootstrap, "public static void onChunkUnload(").contains("PENDING.unloadChunk("));
        assertTrue(methodBody(bootstrap, "public static void onLevelUnload(").contains("PENDING.unloadLevel(level)"));
        for (String forbidden : new String[]{".execute(", ".join(", "managedBlock(", "addRegionTicket(",
                "setChunkForced(", "level.getChunk(", "catch (", "level.setBlock("}) {
            assertFalse(bootstrap.contains(forbidden), "admission must not force/await/repair native work: " + forbidden);
        }
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

    private static String methodBody(String source, String signature) {
        int start = source.indexOf(signature);
        assertTrue(start >= 0, "missing Forge handler: " + signature);
        int open = source.indexOf('{', start), depth = 1, end = open + 1;
        while (end < source.length() && depth > 0) {
            char value = source.charAt(end++);
            if (value == '{') depth++;
            else if (value == '}') depth--;
        }
        assertTrue(depth == 0, "unterminated handler: " + signature);
        return source.substring(open + 1, end - 1);
    }

    private static String pinnedClassBytes(String resource) throws IOException {
        try (var input = BlightLocusBootstrapSourceTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, "pinned Malum class missing from test runtime: " + resource);
            return new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }
}
