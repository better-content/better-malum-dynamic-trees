package com.bettercontent.bettermalumdynamictrees.diagnostics;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
/** Actual scalar production emitters; not whole-native gameplay/RNG equivalence. */
final class FeatureObservationTest {
    public static void main(String[] args) { new FeatureObservationTest().actualEmitterStagesPrivacyAndObservationalPurity(); }
    @Test void actualEmitterStagesPrivacyAndObservationalPurity() {
        var records = new ArrayList<String>(); boolean[] enabled = {false};
        var log = new PlaytestLog("better_malum_dynamic_trees", new PlaytestLog.Sink() {
            public boolean enabled(String category, String level) { return enabled[0]; }
            public void write(String category, String level, String text) { records.add(text); }
        }, () -> 0L, new Properties());
        var reads = new AtomicInteger();
        Supplier<PlaytestLog.Context> tick = () -> { reads.incrementAndGet(); return PlaytestLog.Context.server("minecraft:overworld", 10); };
        Supplier<PlaytestLog.Context> load = () -> { reads.incrementAndGet(); return PlaytestLog.Context.server("minecraft:overworld", -1); };
        for (boolean active : new boolean[]{false, true}) {
            enabled[0] = active;
            FeatureObservation.submitted(log, tick, 42);
            FeatureObservation.site(log, load, "blight.queue", "queued", "rare_native_site", 42);
            FeatureObservation.site(log, tick, "blight.admit", "deferred", "no_eligible_ground", 43);
            FeatureObservation.site(log, tick, "blight.admit", "rejected", "already_claimed", 42);
            if (!active) { assertTrue(records.isEmpty()); assertEquals(0, reads.get()); }
        }
        assertEquals(4, records.size()); assertEquals(4, reads.get());
        assertTrue(records.get(0).contains("result=submitted reason=claimed_native_event"));
        assertTrue(records.get(1).contains("result=queued reason=rare_native_site"));
        assertTrue(records.get(2).contains("result=deferred reason=no_eligible_ground"));
        assertTrue(records.get(3).contains("result=rejected reason=already_claimed"));
        assertTrue(records.get(0).contains("claim=42")); assertTrue(records.get(1).contains("claim=42"));
        assertTrue(records.get(2).contains("claim=43")); assertTrue(records.get(3).contains("claim=42"));
        assertTrue(records.stream().allMatch(s -> s.contains("dimension=minecraft:overworld")));
        assertTrue(records.get(0).contains("tick=10")); assertFalse(records.get(1).contains(" tick="));
        assertTrue(records.stream().noneMatch(s -> s.contains("seed=") || s.contains("placement_succeeded=") || s.contains("nbt=") || s.contains("player=")));
    }
}
