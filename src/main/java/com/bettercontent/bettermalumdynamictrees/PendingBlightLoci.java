package com.bettercontent.bettermalumdynamictrees;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** Server-thread transient admission metadata only: never retains chunks or claims durable sites. */
final class PendingBlightLoci<L> {
    private final Map<L, LinkedHashMap<Long, Candidate>> levels = new IdentityHashMap<>();

    record Candidate(BlightLocusPolicy.Site site) {
        int chunkX() { return site.chunkX(); }
        int chunkZ() { return site.chunkZ(); }
    }

    synchronized void enqueue(L level, BlightLocusPolicy.Site site) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(site);
        levels.computeIfAbsent(level, ignored -> new LinkedHashMap<>())
            .computeIfAbsent(chunkKey(site.chunkX(), site.chunkZ()), ignored -> new Candidate(site));
    }

    /** Lookup must be nonblocking. A null result leaves the candidate pending without any action. */
    <C> int drain(L level, Function<Candidate, C> readyLookup, BiConsumer<Candidate, C> action) {
        // No queue lock is held across world lookups/actions or nested load/unload notifications.
        List<Candidate> snapshot = snapshot(level);
        int processed = 0;
        for (Candidate candidate : snapshot) {
            if (!isCurrent(level, candidate)) continue;
            C ready = readyLookup.apply(candidate);
            if (ready == null || !removeCurrent(level, candidate)) continue;
            // Detach BEFORE admission so a reentrant unload/reload can enqueue a fresh generation.
            action.accept(candidate, ready);
            processed++;
        }
        return processed;
    }

    synchronized void unloadChunk(L level, int chunkX, int chunkZ) {
        var pending = levels.get(level);
        if (pending == null) return;
        pending.remove(chunkKey(chunkX, chunkZ));
        if (pending.isEmpty()) levels.remove(level);
    }

    synchronized void unloadLevel(L level) {
        levels.remove(level);
    }

    synchronized int pendingCount(L level) {
        var pending = levels.get(level);
        return pending == null ? 0 : pending.size();
    }

    private synchronized List<Candidate> snapshot(L level) {
        var pending = levels.get(level);
        return pending == null ? List.of() : List.copyOf(pending.values());
    }

    private synchronized boolean isCurrent(L level, Candidate candidate) {
        var pending = levels.get(level);
        // Identity, NOT record equality: an equal-valued unload/reload replacement is a new entry.
        return pending != null && pending.get(chunkKey(candidate.chunkX(), candidate.chunkZ())) == candidate;
    }

    private synchronized boolean removeCurrent(L level, Candidate candidate) {
        if (!isCurrent(level, candidate)) return false;
        var pending = levels.get(level);
        pending.remove(chunkKey(candidate.chunkX(), candidate.chunkZ()));
        if (pending.isEmpty()) levels.remove(level);
        return true;
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }
}
