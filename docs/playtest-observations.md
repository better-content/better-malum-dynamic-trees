# Bounded playtest observations

## Authority and feature scope

This repository owns the observations at `BlightLocusBootstrap`. Authoritative source remains the
behavior owner; the local full gate tests native behavior. Logging source/privacy contracts
are not pack runtime, performance or visual acceptance.

Rare-site queue, no-ground defer, already-claimed rejection and native event submission remain separate. Every queue/defer/reject/submission carries the existing selected packed cell claim plus dimension: the key is packed native cellX/cellZ, not a world seed, private-content hash or new site ID. Admission runs on the native END boundary and includes game tick; Load can be off-thread and uses only the already-admitted Overworld identity, never a new clock/chunk/player query. Projection stays behind enabled guards and retains no world references. submitted means WorldEventHandler admission returned after the once-only claim, not observed blight block placement. No retry/reroll or saved-claim policy changes for diagnostics.

## Controls, bounds and lifecycle

The existing loader SLF4J backend owns `bc.playtest.better_malum_dynamic_trees.*`; no runtime provider
is bundled. Startup reports real mod version, effective loaded instrumentation and own-artifact
source metadata when available, otherwise UNKNOWN. Metadata is bounded and read only from this
mod's exact Forge ModFile at initialization. Normal standalone logging stays INFO; an explicitly
selected DEBUG profile enables causal records. One process-local instance shares 120 detail
records/minute, burst20, bounded256 aggregation keys and 2048-character single-line records.
Native END-server-tick boundaries coalesce monotonic30-second summaries; server stop clears
keys without replenishing the bucket or rearming TRACE. Actual failures preserve existing
normal severity/stack traces independently of detail suppression.

Focused TRACE remains default off and requires `bc.playtest.trace.better_malum_dynamic_trees.dimension`
and exactly one `.subject` UUID or `.pos=x,y,z`; the once-per-process window begins on the first
matching detail and expires at60seconds/1000records, also constrained by the common bucket.
These feature boundaries currently emit causal DEBUG, not per-tick/per-frame TRACE. The helper
supports focused details but a focus property does not invent an uninstrumented native observer.
Hot diagnostics guard before native context/field creation; no extra inventory/chunk scan,
RNG draw, entity construction, packet or persistent ledger is added.

## Native source fixture boundary

A fresh retained `run-gametest/attempts/<UUID>` directory is selected through the actual Forge
RunConfig. Old directories/evidence stay untouched; absent-only native properties preparation
preserves existing bytes. The pure setup guard checks defaults/preservation and ERROR-FATAL
negative input; native verification requires exactly2 authored required cases and strict
error-free logs, not merely Gradle exit0. The new projection/fixture still needs that full gate.

## Verification boundary

The unchanged shared helper contract covers off/lazy behavior, escaping/truncation,
budget/cardinality/overflow/fakeclock/reset/focus and own-metadata validation. The owner-specific
source contract asserts actual event seams/privacy markers, not native success. Existing native
feature tests and the owning documented full gate remain required before publication. Exact
candidate gameplay, paired native UI review where relevant and performance acceptance are
separate; do not infer them from a causal log or a source-only pass.
