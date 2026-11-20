# GAMEPLAY-72 — Tactical decisions, route tradeoffs and race pacing audit

**Status:** source-only, stacked draft on GAMEFEEL-71. Exact-head JVM/Android CI and on-phone review required. No change to the authoritative rules, save fingerprint, board art, players, or LAN. `main` unchanged.

## Why

The owner's Woven Trail tactical phone race (two participants) ended in **3 rounds / 57 seconds**, with the AI winning. This is one completed real-device result, not a representative duration estimate. Earlier deterministic all-AI samples showed first-seat advantages and far fewer total rolls on Quick/Classic than Long, but those are synthetic, not human attention or time. The existing v3 policy comparison mixes travel choice and route preference; it cannot cleanly isolate each axis.

Crossroads should deliver interesting route tradeoffs, not artificially lengthen the die animation or insist that a set number of minutes is 'correct'.

## What this measurement adds

`TacticalFactorialAuditTest` runs **864 deterministic Lab v3 race samples**:

- 3 unchanged handcrafted boards (Quick, Classic, Long).
- All human starting seats in both two-seat and four-seat races: 2+4 mirrors per board.
- 12 fixed seeds for each configuration.
- Independent travel styles: **always Steady** vs **always Push**.
- Independent route styles: **shortest route** vs **safest route**. Shortest breaks ties toward safety; safest breaks ties toward distance.
- Human encounter response is always **Supplies** or **Gamble** so defensive encounter behavior does not change when the travel style changes. Rival seats use the unchanged existing bots.

The audit records actual human wins, all-players dice-roll counts (not time), human dice rolls, route decisions, high/low-risk commitments, encounter decisions, cases with no human fork, and the number of crossroads where no available route is simultaneously shortest AND lowest-risk.

This simple **distance-versus-risk conflict** is only one necessary ingredient of a strategic tradeoff. It is not evidence of balanced odds, hidden hazard severity, expected value, or human comprehension. Same initial seed does not guarantee the same future RNG sequence after divergent event draws.

CI writes:

- `core/build/reports/gameplay-72/tactical_factorial_samples.csv`
- `core/build/reports/gameplay-72/tactical_factorial_summary.txt`

The existing rules-test workflow uploads `Crossroads-GAMEPLAY-72-<SHA>`. The exact commit must pass before treating these as measured evidence; the test is not a new in-app analytics service.

## Android / human playtest protocol (no backend)

Use the latest approved QA build, **Tactical Choices / Experimental ON**, Woven Trail Solo vs AI. Try 3–5 separate completed matches on Woven Trail, then at least one Quick and Long match, recording:

| Session | Course | 2/3/4 seats | Win? | Real mm:ss | Rounds | Steady choices | Push choices | Forks faced | Shelter/Hazard decisions | Was the best route obvious? | Fun / frustration note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Woven Trail | 2 | | | | | | | | | |
| 2 | Woven Trail | 2 | | | | | | | | | |
| 3 | Woven Trail | 2 | | | | | | | | | |

At the first fork, ask: **What are you giving up if you take the faster path?** If the player cannot tell from the risk/distance labels and the map, improve the presentation before altering rules. Observe how much of the match consists of useful choices versus passive movement; 'time' by itself is a weak measure.

Repeat selected courses with Tactical Choices OFF to compare the simpler original rules, **but do not treat unrelated seeded results as a controlled A/B test**. Don't uninstall the app or clear saves. Experimental runs are distinct from canonical records.

## Decision gates

1. **Functionality:** all 864 synthetic matches complete within the action cap; every winner is at the actual finish. Core tests and Android QA must pass at exact PR head.
2. **Analysis:** read the uploaded CSV/summary; investigate strategy or seat gaps, zero-fork prevalence, and how many actual observed forks have a distance/risk conflict. Do not collapse across seat counts or call confidence intervals without supporting statistical work.
3. **Potential balance changes:** if Push+Shortest dominates across multiple boards and mirrored seats, or safer routes rarely win, propose a **new, versioned, opt-in experimental rules variant**, with separate checkpoint identity and regression suite. Keep current v1/v2/v3 and legacy saves unmodified.
4. **Accessibility/game feel:** verify actual 2-, 3-, 4-player turns, speed settings, readability, background/resume, and full match on a physical Android phone. CI alone cannot prove pacing or fun.
5. **Release:** two-phone same-Wi-Fi Host/Join and production signing remain independent blockers; this audit makes neither ready.

## Scope protection

No new rewards, currencies, matchmaking, camera, artwork or progress systems. No edits to `GameSession`, event weights, die probabilities, AI, handcrafted routes, network messages, app metadata, or public release. Stacked draft PR only. Intent: **find out where the decisions fail before redesigning them**.
