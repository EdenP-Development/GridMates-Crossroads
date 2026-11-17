# TURN-21 — Pause automatic turns while the app is not active

**Date:** November 17, 2026
**Status:** committed source and revised JVM gate tests; exact-head Android CI and owner-phone checks must be verified separately.
**Branch:** `feature/classic-pass-play-v1.5` · [Draft PR #21](https://github.com/PlayerEden/GridMates-Crossroads/pull/21).
**Prior verified baseline:** signed [QA1154 Android CI](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44175612318), [artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44175612318/artifacts/14369178664) passed build/unit/lint/signer/rules/data checks. QA1154 was not tested on owner's phone.

## Source finding
TURN-20 correctly held the AI during in-app Help, Journal, Overview, results and menus. But the AI effect did not know whether Android had paused the Activity. As a result, an AI breath could finish and call `game.botStep()` while the user had switched apps. Similarly, a decorative roll preview could complete and commit a roll while Crossroads was no longer active.

For a cozy offline board game, backgrounding the app must not advance an unseen turn.

## Implementation
- Observe the existing `ComponentActivity.lifecycle` with `LifecycleEventObserver` and a Compose `DisposableEffect`; `ON_RESUME` enables automatic actions and `ON_PAUSE` disables them. Remove the observer on dispose. No new Android permissions or external library added.
- The existing `TurnFlowGate` now also requires `appInForeground` for AI progression. This **cancels** a pending AI breath when paused, and restarts the ordinary breath after resuming.
- The decorative die `LaunchedEffect` is keyed to foreground state. If the phone is backgrounded before the authoritative `GameSession.roll()`, no real die roll is consumed; returning to the app restarts its **visual-only** preview, followed by one legitimate rules roll.
- Just before invoking the authoritative `roll()` or `botStep()`, check that the Activity is currently **RESUMED** as a last safety guard against cancellation scheduling races.
- A pawn's already committed movement can finish its presentation timer; no new AI command is started until foreground returns.
- No edits to core RNG/dice odds, bot route policy, board, saves, win counting, or game rules. Pass & Play remains manual.

## Automated checks
- [ ] `TurnFlowGateTest` existing cases still pass: ready vs busy, in-app overlay, finished match, human turns (including Pass & Play), bot route/target.
- [ ] Added case verifies background pauses a pending AI action, returning foreground permits the same pending action, and menu overlay takes priority.
- [ ] Android debug build, app unit tests/lint, signed QA identity and artifact upload succeed at exact PR head.
- [ ] Core rules tests and design data pass on the same source SHA.

## Owner Android acceptance
1. Install signed new QA **over QA1154 without uninstalling**. Local wins/completed stats and saved journey should remain.
2. Solo with 3–4 participants: just before or during an AI turn, press **Home** or switch to another app and leave Crossroads backgrounded. Reopen: no unexpected board position/turn advancement; AI continues after the normal short breath.
3. Start tapping **Roll**, press Home during the decorative dice animation, then resume. If the engine roll had not committed, a fresh visual preview should run and commit **one** actual roll; if it had committed, no duplicate roll should occur.
4. Open **How to Play / Board Overview / Journal** on an AI turn and background the app; returning with the modal open should keep the AI paused until it is closed.
5. While a 2- or 3-way human crossroads choice is pending, background and reopen: keep the same remaining steps and legal route options.
6. Pass & Play (2–4 humans) remains manual; no automatic other-player turn after backgrounding. Try reopening saved match from Android **Force stop** separately to test full recovery, not merely Activity resume.
7. Check results once-only, frozen match time, accessibility large fonts, and no new permission prompts.

## Evidence boundaries
Automated JVM tests of a pure gate do not prove real Android Activity timing; CI compilation does not prove process-death recovery, install-over, or performance. The separate owner phone test is still required. Keep PR DRAFT and do not merge to `main` or release publicly until approved.
