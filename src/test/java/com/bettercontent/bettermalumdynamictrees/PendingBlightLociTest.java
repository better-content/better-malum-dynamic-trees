package com.bettercontent.bettermalumdynamictrees;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PendingBlightLociTest {
    private static BlightLocusPolicy.Site site(int x, int z) {
        return new BlightLocusPolicy.Site(Math.floorDiv(x, 40), Math.floorDiv(z, 40), x, z);
    }

    @Test void enqueueDeduplicatesMetadataWithoutExecutingLookupOrAdmission() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object();
        queue.enqueue(level, site(1, 2));
        queue.enqueue(level, site(1, 2));
        assertEquals(1, queue.pendingCount(level));
        AtomicInteger actions = new AtomicInteger();
        assertEquals(0, queue.drain(level, candidate -> null, (candidate, chunk) -> actions.incrementAndGet()));
        assertEquals(0, actions.get());
        assertEquals(1, queue.pendingCount(level));
    }

    @Test void unavailableChunkStaysPendingAndNeverInvokesTerrainOrClaimCallback() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object();
        queue.enqueue(level, site(-1, -2));
        AtomicInteger lookups = new AtomicInteger();
        assertEquals(0, queue.drain(level, candidate -> {
            assertEquals(-1, candidate.chunkX());
            assertEquals(-2, candidate.chunkZ());
            lookups.incrementAndGet();
            return null;
        }, (candidate, chunk) -> fail("unpublished chunk must not admit terrain/claim/event work")));
        assertEquals(1, lookups.get());
        assertEquals(1, queue.pendingCount(level));
    }

    @Test void readyLookupPassesActualChunkAndAdmitsExactlyOnceOutsideQueueLock() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object(), fullChunk = new Object();
        var selected = site(3, 4);
        queue.enqueue(level, selected);
        AtomicInteger actions = new AtomicInteger();
        assertEquals(1, queue.drain(level, candidate -> {
            assertFalse(Thread.holdsLock(queue));
            return fullChunk;
        }, (candidate, chunk) -> {
            assertFalse(Thread.holdsLock(queue));
            assertSame(fullChunk, chunk);
            assertEquals(selected, candidate.site());
            assertEquals(0, queue.pendingCount(level), "old entry must detach before native admission");
            actions.incrementAndGet();
        }));
        assertEquals(1, actions.get());
        assertEquals(0, queue.drain(level, candidate -> fail("already processed"), (candidate, chunk) -> fail()));
    }

    @Test void actionEnqueueIsDeferredUntilNextStableSnapshot() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object();
        queue.enqueue(level, site(1, 1));
        List<Integer> visited = new ArrayList<>();
        assertEquals(1, queue.drain(level, candidate -> new Object(), (candidate, chunk) -> {
            visited.add(candidate.chunkX());
            queue.enqueue(level, site(2, 2));
        }));
        assertEquals(List.of(1), visited);
        assertEquals(1, queue.pendingCount(level));
        assertEquals(1, queue.drain(level, candidate -> new Object(), (candidate, chunk) -> visited.add(candidate.chunkX())));
        assertEquals(List.of(1, 2), visited);
    }

    @Test void sameChunkReenqueueDuringActionSurvivesOldEntryCompletion() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object();
        var selected = site(1, 1);
        queue.enqueue(level, selected);
        AtomicReference<PendingBlightLoci.Candidate> old = new AtomicReference<>();
        queue.drain(level, candidate -> new Object(), (candidate, chunk) -> {
            old.set(candidate);
            queue.unloadChunk(level, 1, 1);
            queue.enqueue(level, selected);
        });
        assertEquals(1, queue.pendingCount(level));
        assertEquals(1, queue.drain(level, candidate -> {
            assertEquals(old.get(), candidate, "replacement has identical value");
            assertNotSame(old.get(), candidate, "replacement must have fresh identity");
            return new Object();
        }, (candidate, chunk) -> {}));
    }

    @Test void unloadAndEqualReplacementDuringLookupCannotBeWipedOrAdmittedAsOldEntry() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object();
        var selected = site(5, 6);
        queue.enqueue(level, selected);
        assertEquals(0, queue.drain(level, candidate -> {
            queue.unloadChunk(level, 5, 6);
            queue.enqueue(level, selected);
            return new Object();
        }, (candidate, chunk) -> fail("stale lookup cannot admit replacement")));
        assertEquals(1, queue.pendingCount(level));
        assertEquals(1, queue.drain(level, candidate -> new Object(), (candidate, chunk) -> {}));
    }

    @Test void replacingLaterSnapshotEntryDuringEarlierActionDoesNotRunItInSameDrain() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object();
        queue.enqueue(level, site(1, 1));
        queue.enqueue(level, site(2, 2));
        assertEquals(1, queue.drain(level, candidate -> {
            assertEquals(1, candidate.chunkX(), "old second entry must skip even its lookup");
            return new Object();
        }, (candidate, chunk) -> {
            queue.unloadChunk(level, 2, 2);
            queue.enqueue(level, site(2, 2));
        }));
        assertEquals(1, queue.pendingCount(level));
        assertEquals(1, queue.drain(level, candidate -> new Object(), (candidate, chunk) -> assertEquals(2, candidate.chunkX())));
    }

    @Test void levelUnloadDuringLookupInvalidatesSnapshotAndKeepsFreshReplacement() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object();
        queue.enqueue(level, site(7, 8));
        assertEquals(0, queue.drain(level, candidate -> {
            queue.unloadLevel(level);
            queue.enqueue(level, site(7, 8));
            return new Object();
        }, (candidate, chunk) -> fail("old world queue snapshot is invalid")));
        assertEquals(1, queue.pendingCount(level));
        assertEquals(1, queue.drain(level, candidate -> new Object(), (candidate, chunk) -> {}));
    }

    @Test void unloadRemovesTransientsWithoutLookupAndWorldKeysAreIdentityScoped() {
        var queue = new PendingBlightLoci<String>();
        String first = new String("overworld"), second = new String("overworld");
        queue.enqueue(first, site(0, 0));
        queue.enqueue(second, site(0, 0));
        queue.unloadChunk(first, 0, 0);
        assertEquals(0, queue.pendingCount(first));
        assertEquals(1, queue.pendingCount(second));
        queue.unloadLevel(second);
        assertEquals(0, queue.drain(second, candidate -> fail("unloaded world"), (candidate, chunk) -> fail()));
    }

    @Test void signedChunkCoordinatesRemainDistinct() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object();
        queue.enqueue(level, site(-1, 0));
        queue.enqueue(level, site(0, -1));
        queue.enqueue(level, site(-1, -1));
        queue.enqueue(level, site(0, 0));
        assertEquals(4, queue.pendingCount(level));
        queue.unloadChunk(level, -1, 0);
        assertEquals(3, queue.pendingCount(level));
        assertEquals(3, queue.drain(level, candidate -> new Object(), (candidate, chunk) -> {}));
    }

    @Test void nativeAdmissionFailureIsNotSilencedOrMadeIntoSuccessAndFreshEntrySurvives() {
        var queue = new PendingBlightLoci<Object>();
        Object level = new Object();
        queue.enqueue(level, site(1, 1));
        RuntimeException failure = new RuntimeException("native failure");
        assertSame(failure, assertThrows(RuntimeException.class, () -> queue.drain(level,
            candidate -> new Object(), (candidate, chunk) -> {
                queue.enqueue(level, site(1, 1));
                throw failure;
            })));
        assertEquals(1, queue.pendingCount(level));
    }

    @Test void originalSurfaceSearchBoundsStayInsidePositiveAndNegativeCandidateChunks() {
        for (int chunk : new int[]{-41, -1, 0, 1, 40}) {
            int center = chunk * 16 + 8;
            for (int radius = 0; radius <= 7; radius++) {
                for (int delta = -radius; delta <= radius; delta++) {
                    assertEquals(chunk, Math.floorDiv(center + delta, 16));
                }
            }
        }
    }
}
