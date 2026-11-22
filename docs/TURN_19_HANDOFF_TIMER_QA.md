# TURN-19 — Pass & Play handoff and finished-match timer integrity

**November 17, 2026 · Source and tests committed; exact-head CI and owner phone acceptance pending.**
**Branch:** `feature/classic-pass-play-v1.5` within [Draft PR #21](https://github.com/PlayerEden/GridMates-Crossroads/pull/21). No merge to `main`.

## Problem and scope
The most recent verified build was **QA1138** (Android compile/unit/lint/signer, rules, design data all passed). User screenshots are of older **QA1130**, confirming basic Pass & Play human seats and a completed Solo result, but not finished Pass & Play or force-stop recovery.

Two remaining small usability/reliability issues were confirmed in source:
1. On shared-phone Pass & Play, the incoming person could miss the change of player, even though the Round/Player header changed. A fullscreen handoff modal would add needless steps to a classic game.
2. SAVE-16 persisted the completed match and its statistics atomically, but **not the match's completion time**. After activity death or reopening a finished saved game, `finishedElapsedSeconds` was recalculated from the original start and could grow while the app wasn't being played.

## Changes
- On an all-human match, after the first turn, at the beginning of the next human **ROLL** and when presentation is ready, show one short reminder: **Pass the phone to Player N**. No modal, additional tap, account or hidden dice. It disappears during rolling or crossroads choices. Solo continues unchanged.
- Store `last_completed_at_epoch_millis_v1` in **the same SharedPreferences commit** as finishing checkpoint, wins/completed count and last completed ID. On reopening, display the duration derived from the stored completion timestamp, not from current clock time.
- Already-finished saves from older QA versions have no completion timestamp. The first open on this updated build captures a fallback timestamp once and reuses it; historical exact finish moment cannot be reconstructed. Do not claim that old elapsed durations are perfectly recovered.
- No change to score/wins accounting, RNG, move/turn engine, board data, route choices, APK ID, signers, LAN, or permissions.

## Automated QA acceptance
- [ ] Exact-head Android build, `app:testDebugUnitTest`, lint, signer identity and APK upload pass.
- [ ] New `MatchRecoveryStoreTest` proves a 3-seat Solo race and all-human race each store only one completed result, preserve the finished timestamp across repeated save/restore, and do not count a Pass & Play winner as a Solo win.
- [ ] Legacy finished-save test verifies a missing completion timestamp gets frozen once, rather than increasing across every new activity.
- [ ] Existing pure-core all-seat, save/replay, bot and board-data tests pass.
- [ ] PR source diff remains UI, `MatchRecoveryStore`, tests and documentation. No core production rules changes.

## Real Android phone checklist
- [ ] Install new QA over QA1138 with data intact. Confirm current 5 Solo wins / 7 completed as of older screenshots are retained or legitimately increased by new real matches, and existing match save remains intact.
- [ ] Start Pass & Play with 2 people; after Player 1's turn, a brief **Pass the phone to Player 2** label is visible above roll area; the person can roll with one tap without another confirmation.
- [ ] Repeat with 3/4 people; no duplicate AI action or wrong player handoff. If a fork is pending, no misleading handoff message should obscure the available routes.
- [ ] Finish a Solo match, record displayed time; Android force-stop/reopen the finished saved game and verify identical result time, winner and records.
- [ ] In Pass & Play, finish, force-stop/reopen, rematch and return to menu; completed matches count once, Solo wins never increase because a shared-phone human won.
- [ ] Check large Android font, compact portrait controls, TalkBack, gesture camera and 3-way Old Forest chooser.
- [ ] Real device smoothness/storage/installation remains unverified until the owner confirms.

**Design guardrail:** This is a small quality fix, not new game complexity or a new progression layer.
