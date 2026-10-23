package com.bettercontent.bettermalumdynamictrees.diagnostics;
import java.util.function.Supplier;
/** Pure owner-specific projection of already computed native outcomes; no gameplay writes or RNG. */
public final class FeatureObservation {
    private FeatureObservation() {}
    public static void submitted(PlaytestLog log, Supplier<PlaytestLog.Context> context, long claim) {
        if (!log.enabled("worldgen")) return;
        log.debug("worldgen", "blight.admit", context.get(), "submitted", "claimed_native_event", () -> PlaytestLog.fields("claim", claim));
    }
    public static void site(PlaytestLog log, Supplier<PlaytestLog.Context> context, String event, String result, String reason, long claim) {
        if (!log.enabled("worldgen")) return;
        log.debug("worldgen", event, context.get(), result, reason, () -> PlaytestLog.fields("claim", claim));
    }
    public static void decision(PlaytestLog log, String category, String event, String result, String reason) {
        if (!log.enabled(category)) return;
        log.event(category, event, "server", result, reason, () -> "");
    }
}
