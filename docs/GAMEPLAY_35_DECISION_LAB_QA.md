# GAMEPLAY-35 — Decision Lab v1 (first playable tactical rules experiment)
**November 17, 2026 · Draft, isolated QA experiment. Not an approved balance overhaul.**

## Why this exists

The owner's observed experience: Crossroads can be button-mashed and a four-racer Quick game finished in **34 seconds**. The AGENCY-34 audit confirmed that simple shortest-route play beat safest-first and random play in all tested seat/course groups. The current rules roll and move automatically through encounter nodes, only triggering chance events when landing directly on them. More artwork or board spaces won't repair weak player agency.

This milestone experiments with *two real tradeoffs*, while preserving fully functioning original modes. Data is evaluated before any adoption decision.

## How to playtest on Android

From the home screen, choose Quick, Classic or Long, and turn **Gameplay 2.0 · Playtest** **ON**. Start an offline Solo or Pass & Play game. The home toggle is OFF for ordinary games by default.

On each human turn choose:
- **Steady · 3** — move a predictable **3 spaces**; the first hazard setback from this turn is blocked, but otherwise advances are slower.
- **Push · risky** — the six-sided die gives **1 step on a roll of 1, 2 or 3** (a stumble), or **6, 7 or 8 steps on rolls of 4, 5 or 6**. No setback protection.

At a **crossroads**, entering a high-risk branch draws one hazard encounter immediately; a low-risk branch draws one shelter benefit; a medium-risk branch draws a mischief encounter if that region has an eligible pool. There is at most one encounter per turn, so landing on an encounter after a route commitment will not trigger a second surprise. Old modes retain the original landing-only rule. Encounter descriptions and live piece movement reflect the consequences.

Steady's protection blocks a *setback/retreat*, not another player's prank; stored shields still protect against a prank. Both styles are test mechanics: do not assume the expected movement always favors one of them.

## Exact technical isolation

- Every prototype course is a **copy of the established BoardDefinition with only its `id` changed** to `<original>__decision_lab_v1`. Its actual nodes, forks and artwork are unchanged.
- The existing engine's `roll()`/landing-only event logic remains the only behavior on original board IDs. A lab board rejects `roll()` and exposes `rollSteady()` and `rollBold()`.
- **New replay actions** are appended as `STEADY_ROLL` and `BOLD_ROLL` without adding any field to previously stored `MatchAction`. Old checkpoints still decode/replay identically.
- The checkpoint's existing board ID + fingerprint reliably separate lab and legacy. The Android recovery and optional portable JSON backup know all original + lab board IDs.
- A lab finish stores its completed match receipt and frozen elapsed timestamp, but **does not increment** the established Solo wins or finished-match counters. It remains available for immediate rematch and visual QA.
- Nearby Host/Join constructs the exact original Classic board, and networking's `LanAction` types are not extended. **Lab is offline-only**. Real 2-phone LAN remains a separate acceptance gate.
- No event definitions, weights, original board JSON, RNG algorithm, paid feature, account requirement or production signer changed.

**Downgrade warning:** Old QA builds do not understand the lab board ID/actions. Never downgrade *with an active lab checkpoint* unless you've exported any important compatible legacy save first. Returning to an older QA version while the app has a lab save may show a recovery warning. Do not uninstall or clear app data.

## Automated gates

- [ ] Legacy tests still pass, including unchanged board/rules balances and saved checkpoint recovery.
- [ ] Core test rejects tactical actions in ordinary games and old-style `roll()` in lab games.
- [ ] Lab & original content fingerprints differ even though layouts are the same; no silent cross-restore.
- [ ] Both new dice styles checkpoint and replay exactly (including after app restart).
- [ ] Each initial high/low route commitment triggers an eligible encounter from the correct pool, which cannot be skipped by high die faces.
- [ ] All 3 courses × 2–4 seats × 55 seeds finish without hanging; early checkpoints replay bit-for-bit.
- [ ] 5,760 simulated legacy/lab races compare **random, fastest, safest and adaptive** policies; publish per-seed CSV and summary; do not assume positive outcome.
- [ ] Android compile, unit tests, lint, public-preview identity preflight, signed QA APK artifact (same `.qa` package). Do not publish public build.

## One-phone functional and fun test

1. **Before installation**, export your current QA backup if it contains valuable progress. Install the new signed QA APK over the previous QA app; verify established wins/completed games remain unchanged.
2. Start **Quick with Gameplay 2.0 OFF**. Confirm original Roll button, same race and event rules, and unchanged records. This is essential baseline.
3. Return Home. Turn **Gameplay 2.0 ON** and start Quick with two racers. Confirm the **Steady / Push** buttons, action explanation and experimental course label.
4. Use Steady until reaching a fork and choose the high-risk route. Even when a die would skip hazard spaces, this experimental route must show a hazard encounter immediately. Check shield-like setback protection is readable.
5. Use Push for several turns. Note both one-step stumbles and six-to-eight-space bursts. The game must show the actual progress, not falsely report the raw die as number of traveled spaces.
6. Enter a low-risk route: a shelter benefit triggers. Enter a medium-risk choice on its eligible woodland course: mischief applies or prompts an opponent choice.
7. **Force-stop/reopen** during a human Steady/Push turn, while a fork is pending, and while an event target is pending. No lost or doubled move; current board and players must match.
8. Complete a lab match and inspect results. The regular local **Solo wins** and **Finished matches** numbers should be the same as before the experimental match. Reopening results must not advance the timer.
9. Export and import a lab checkpoint through Android's system document picker with explicit replacement consent; verify seed/replay state. Test using a disposable QA record/backup, not your only valuable progress.
10. Compare 4-player Quick vs Classic/Long in real seconds, informed choices versus mindless taps, first-fork decision delay, and willingness to rematch.

**The decision to promote experimental mechanics into the regular game is contingent on measurable improvement and human fun.** If shortest+Push or safest+Steady dominates every situation, the prototype is not ready. No silent `main` merge or store publication.
