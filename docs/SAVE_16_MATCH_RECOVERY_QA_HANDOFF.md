# SAVE-16 — Local match recovery and exact rules replay

**Date:** November 17, 2026
**Status:** source + pure JVM recovery regression tests implemented. Exact head Android CI, install-over behavior and owner phone acceptance must be recorded separately.
**Branch:** `feature/active-match-recovery-v1.4` stacked on QA-15 Draft PR #19.
**Baseline:** prior signed [QA1112](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44156394059) passed Android build, lint, signing, rules and design QA. QA1112 did **not** preserve an in-progress match when Android recreated the Activity.

## The reliability gap
Until SAVE-16, `CrossroadsApp` held `GameSession` in Compose `remember`, so Android process death, Activity recreation and subsequent launch lost all unfinished gameplay. The engine's seeded Kotlin PRNG could not safely be restored from only the visible piece positions; it also determines future dice, route tie breaks and encounter outcomes.

## Approach — exact event-sourced replay
- `MatchCheckpoint` is a **versioned pure-core JSON record**: match ID, start timestamp, initial seed, seat count, human seats, board ID, SHA-256 of board/event definitions, accepted command history and the complete authoritative snapshot for integrity verification.
- Engine records accepted **outer public commands** `ROLL`, `ROUTE`, `TARGET`, `BOT`. A bot's internal roll or route choice is **not logged twice**.
- On restore, reconstruct the same `GameSession` from the original seed and replay each command through the *normal engine*, consuming the same PRNG draws. Then compare the complete terminal snapshot (players/positions/traversal, die, phase, turn, shields, last message, event history, winner and revision).
- Incompatible format/board/events, out-of-range seats, illegal commands, modified snapshot or oversized payload reject restoration. Never partially restore or fabricate future rolls.
- Serialized snapshot is solely a **witness for comparison**; directly replacing live game-engine fields is deliberately forbidden.
- No change to die odds, existing bot decision policy, map graph, event pools or reward systems.

## Android integration
- One checkpoint is stored in the app's existing `crossroads_qa_profile_v1` local SharedPreferences, key `active_match_checkpoint_v1`, with synchronous commit after **each complete authoritative action** and at new-game start.
- On Activity creation, a valid checkpoint automatically reopens the race (including pending fork or prank target). A bad checkpoint remains quarantined and a clear message appears on home; starting a new journey overwrites it.
- The board's animated tokens start at the saved positions after restore. The entire historical trail is **not** reanimated. No stale encounter is displayed as a newly triggered effect.
- Camera re-enters Follow at the current seat; past manual zoom/pan is not part of rules state.
- `Cozy`/`Brisk` choice is retained locally; gameplay timing is presentation-only.
- Wall-clock match start timestamp survives recreation. Elapsed results include time away from the app; manually changing device time can affect this timer.
- **Completed-match accounting is idempotent**: when the final rules action is committed, the checkpoint, win/completed counts and a `last_completed_match_id_v1` identifier are written in **one SharedPreferences transaction**. Reopening a finished checkpoint cannot count the same win again.
- Intentional Leave Match or Main Menu removes the active checkpoint. **Rematch** writes a new match ID and starts fresh. On storage failure, a visible message is shown rather than pretending recovery worked.
- Only one local active session at a time, no cloud, account, LAN transport, device migration or multi-save history.

## Automated verification
- [ ] Exact-head `core:test` successfully covers save at new game, pending roll, fork with steps remaining, mischief target selection, same future dice/AI/events over 2–4 seats, finished match replay and corrupted/incompatible save refusal.
- [ ] Existing 100-seed all-bot runs and event tests pass.
- [ ] Android debug compilation, `app:testDebugUnitTest`, lint, signer/package/version validation and signed APK artifact succeed.
- [ ] Design-data validation succeeds at same source head.
- [ ] No regression to other apps or modification of main.
- [ ] Android/emulator install-over, process-kill recovery, TalkBack and touch interaction are **NOT** established by JVM tests alone.

## Owner Android acceptance
1. **Update in place** from prior signed QA build. Completed matches and wins should still be present. No existing active match could be recovered from older QA versions, since that format never existed.
2. Start a **three-seat game**, roll until a two- or three-way junction. Note **remaining steps, piece positions and routes**. Press Home, use Android Settings → Apps → Crossroads QA → **Force stop**, reopen. Same junction/steps must appear with no automatic reroll.
3. At a mischievous target prompt, repeat force-stop/reopen; choosing a target must complete the same pending prank once.
4. Start a four-seat game; let AI take some turns. Force-stop/reopen and confirm current turn, shields and last event history. Progress should continue without restarting from Start.
5. Verify elapsed match time continues across reopen. Finish a game, reopen the finished result twice, and check the **completed and win totals increment only once**.
6. From an unfinished match choose **Leave**, then relaunch: home screen, no resumed race, no extra completed/win credit. Likewise Main Menu after results.
7. Rematch begins fresh with the same player count and does not resurrect the previous race.
8. Test large font, three-way Old Forest dialog, gesture camera and options. Token movement must start from saved node, not replay dozens of spaces from Start.
9. Storage failure or a corrupt save should show a warning; it must not crash or silently invent a recovered state.

## Known limits / follow-up
- Recovery is deliberately exact for the **same checkpoint schema, rules code and board/event definitions**. Game updates that change rules or fixtures may reject old unfinished saves until a migration mechanism is added.
- `SharedPreferences.commit()` is synchronous to favor data safety on this small prototype. Check phone performance; if large transcripts cause stutter, migrate to versioned atomic file + IO dispatcher without weakening action ordering.
- Only the **authoritative state at the last fully committed action** is restored. A roll animation or pawn animation interrupted before the engine action was committed resets to the previous safe rules checkpoint.
- Local QA APK uses a **public test-only signer**, not a production security/privacy guarantee. Device data clearing, uninstall, OS storage corruption and signer-incompatible updates are not protected.
- The Android UI does not save the exact manual camera pose, recent transient animation, pending in-progress die *preview*, or open journal screen.
- Existing QA screenshots/test receipts from older milestones do not prove physical recovery or install-over survival for SAVE-16.

**No main merge, release or public listing without owner approval.**
