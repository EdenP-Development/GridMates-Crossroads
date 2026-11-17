# TURN-20 — Pause AI while reading or inspecting the board

**November 17, 2026 · Source and three test cases committed; exact-head QA and physical-phone acceptance pending.** On the stacked `feature/classic-pass-play-v1.5` branch, Draft PR #21. Last proven signed Android baseline: QA1146 ([run 44171258124](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44171258124), [artifact 14368585938](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44171258124/artifacts/14368585938)).

## The problem

The Android presentation effect combined (a) completing a moving pawn's animation with (b) automatically running a bot after a short breath. It did **not** check whether the human had opened How to Play, Journey Journal, Board Overview or other menu/dialog. AI could advance unseen while someone was reading, even in a casual game meant to be comfortable to stop and inspect. Simply restarting the original combined effect whenever a modal opened would also repeat the movement delay and risk odd camera transitions.

## Scope of fix

- Keep movement completion in a `LaunchedEffect(game, revision)` keyed to the authoritative revision alone.
- Put the AI action in a *separate*, cancellation-aware `LaunchedEffect(game, revision, turnReady, readingOrMenuOpen)`.
- The AI may act only if the snapshot is on a bot seat, match is not finished, the previous movement is settled and none of the existing reading/inspection/leave/options/results surfaces is visible.
- Opening a menu during the AI's breath cancels that pending bot step, and closing it begins the short breath again. No skipped turns, hidden bot moves while reading or repeated pawn movement delay.
- Pending human rolls, fork choices and prank targeting remain manual; no additional confirmation modal or button.
- Preserve exact engine, RNG, board, event catalogue, chosen pace, local saving/replay and statistics. No LAN, account or profile system.

## Automated checks

- [ ] `TurnFlowGateTest` verifies bot seat + ready + no overlay advances, paused/busy turns do not, finished matches do not, and human turns—including Pass & Play—never advance automatically. Bot route/target phases may continue after pause.
- [ ] Android compilation, all JVM unit tests, lint, QA signer identity and APK artifact succeed for current head.
- [ ] Existing pure-core and design-data tests pass on same revision.

## Android owner checks

- [ ] Install new signed QA *over* QA1146 without uninstalling. No losses to local wins, match records or unfinished save.
- [ ] In Solo with 3–4 seats, open **Match Options → How to play** while AI is on its turn; leave the guide open longer than the Cozy AI breath. The match should stay at the same authoritative turn.
- [ ] Repeat with **Board overview**, **Journey journal** (once enabled by an encounter), and an open **Match Options** menu. Neither visible bot positions nor journal should advance while these are open.
- [ ] Close the reading screen and watch AI continue after its normal brief pause—no need to tap Roll for the bot, no stale movement animation from Start, no stuck spinner.
- [ ] Open a menu during a pawn movement: the movement finishes, AI awaits the menu closing; no duplicate dice, event or completed-match increment.
- [ ] In Pass & Play, all 2–4 human players continue with deliberate manual roll/route/prank actions and handoff reminder; this fix must not accidentally autoplay other humans.
- [ ] Back-to-game, 3-way route dialog, long-font layouts, result reopening/frozen time and force-stop/recovery still operate as before.

## Evidence limits

Automated tests are **not** a simulation of real Android Compose overlay timing. The pause behavior and physical gestures require owner or emulator acceptance. Previous QA1130 phone screenshots do not prove TURN-20. Retain Draft PR #21, no `main` merge/production release.