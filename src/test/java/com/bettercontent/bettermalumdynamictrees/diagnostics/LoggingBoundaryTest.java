package com.bettercontent.bettermalumdynamictrees.diagnostics;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/** Supplementary wiring/privacy invariant, not the actual emitter or native behavior test. */
final class LoggingBoundaryTest {
    @Test void authoritativeFeatureBoundaryAndPrivacyRemainExplicit() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/bettercontent/bettermalumdynamictrees/BlightLocusBootstrap.java")) +
            Files.readString(Path.of("src/main/java/com/bettercontent/bettermalumdynamictrees/diagnostics/FeatureObservation.java"));
        assertTrue(source.contains("FeatureObservation.submitted"), "FeatureObservation.submitted");
        assertTrue(source.contains("blight.queue"), "blight.queue");
        assertTrue(source.contains("already_claimed"), "already_claimed");
        assertTrue(source.contains("claimed_native_event"), "claimed_native_event");
        assertFalse(source.contains("fields(\"nbt\""));
        assertFalse(source.contains("fields(\"payload\""));
        assertFalse(source.contains("fields(\"seed\""));
        assertTrue(source.contains("enabled("));
    }
}
