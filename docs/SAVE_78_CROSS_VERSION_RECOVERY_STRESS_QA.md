# SAVE-78 — Cross-version checkpoint, restore and progress-transfer stress

**Milestone status:** draft-only on top of LAN-77 / QA1607. Only intended production change is a narrow validation correction in `CrossroadsProgressCodec.verify`; original game rules, Lab v1–v5 dice/events, LAN protocol, Android file format, artwork, package/signing, and `main` are unchanged.

## Evidence-backed bug: Lab-only first match could not be backed up

`MatchRecoveryStore.persist` **intentionally excludes experimental Lab matches from the official `completed` and `wins` counters**. It *does* store the finished checkpoint, its match ID and completion time, allowing reopening the actual result. However, `CrossroadsProgressCodec.verify` previously required `completed > 0` whenever `activeMatch.expected.phase == FINISHED`, even when that match was experimental. This prevented exporting the progress bundle for a new profile that had finished only a Lab race, because `MatchRecoveryStore.exportProgress` verifies the bundle it just created.

**Fix:** The strict completed-match receipt and matching completion timestamp remain mandatory for **every** finished match, including all Lab variants; the `completed > 0` invariant is now required **only when the restored finished match uses original rules**. Lab v1–v5 can legitimately have zero official races. No counter is silently increased, no backup format is changed, and no identity/odds migration occurs.

## Automated test coverage

### Core / pure JVM
- `CrossroadsProgressCodecTest`: for each Lab v1–v5, fully finish an identical-bot Quick match, create a complete backup with **zero official wins and completions**, verify decoding/restore and v5 finish receipt, and reject mismatched/missing finished-match sentinel. Separately reject a finished original-rules backup with zero official completed races and verify the recorded variant.
- `CrossVersionRecoveryStressTest`: **144 completed synthetic races** = 3 authored courses × 6 fixed rules identities (original, v1/v2/v3/v4/v5) × 2/4 participants × Solo/Pass & Play × 2 fixed seeds. Each game has a synchronized future-state mirror; repeatedly serialize and decode a checkpoint, restore the engine and compare the complete snapshot. The mirror and original are then given the same legal actions and MUST keep matching dice/event RNG, routes, pieces, encounter notes, winner and pending/end-of-match v5 receipts. Interrupted states include pending route, encounter, target, final chance, completed game. Report phase coverage instead of claiming all stochastic events must appear in every sample.
- `core/build/reports/save-78/cross_version_recovery_summary.txt` and `.../cross_version_recovery_samples.csv`, uploaded as `Crossroads-SAVE-78-<SHA>`.

### Android persistence (local JVM unit tests)
- `MatchRecoveryStoreTest`: a new zero-stat profile completes Lab v1–v5 and **exports an actual valid backup** to a second simulated app preferences sandbox. Import restores exact final snapshot and Lab v5 receipt, and repeated finished persists never invent official results or change the freeze timestamp.
- Another test pauses v5 mid-final-round after Player 1 has crossed, persists and exports the still-unfinished match, restores it in a second preference sandbox, finishes with an equal-unused-step tie and checks correct first finisher/winner and idempotency. The same fixture on v4 must continue to finish immediately, preserving version isolation.

## Non-goals / limitations

- **Not** an Android force-stop or true QA install-over demonstration. `MemoryPreferences` mimics committed SharedPreferences state at the unit-test level; actual filesystem, OS process timing, disk failure and two-device Wi-Fi remain untested.
- **Not** a guarantee of rollback to older QA builds: a v5 checkpoint still requires a build that registers its immutable v5 board ID.
- Experimental matches remain excluded from official completed/win statistics, even when their final result is saved and exported. Existing official counters are never manufactured.
- Real finished physical-device receipts, accessibility, portrait/landscape screen scroll, LAN host rejoin after an OS kill and production signing remain release gates.

## Exact-head acceptance

- [ ] New app/core tests and all preexisting tests pass on the SAME final PR SHA.
- [ ] Android test/lint/QA signing succeeds and produces a build archive at the matching SHA.
- [ ] Design-data QA passes; no artwork, route data, permissions, package or key rotation.
- [ ] Inspect SAVE-78 artifact summary/CSV for exactly 144 completed cases and positive restore counts. Mark any missing expected phase coverage honestly.
- [ ] Keep this PR **DRAFT**; don't merge the stack to `main` or claim physical test acceptance.

## When real-device tests become possible

1. On the same signed QA app, note old `completed`/`wins` and current course. Force-stop/reopen with a pending fork, target and v5 hazard/shelter encounter.
2. Force-stop/reopen after the first v5 finisher while a later seat has a final turn. The original winner/candidate must not change.
3. Finish the pending v5 match and reopen repeatedly; result receipt should be identical, no duplicate stats, and frozen elapsed time should not grow.
4. Export a profile with **zero official completed races and a saved finished Lab v5 match**. Import it into a separately installed, compatible app build, and verify exact result and zero official counters.
5. Install-over without clearing Android data; verify original-era QA saves, stats and manual progress transfer remain intact. A true older-version downgrade remains unsupported.

**Release decision:** This milestone repairs backup consistency and increases automated confidence, not approval of the experimental fair-finish rules or the overall app.
