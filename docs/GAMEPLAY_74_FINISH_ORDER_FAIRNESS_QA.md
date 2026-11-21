# GAMEPLAY-74 — First mover fairness, close finishes and equal-turn decision gate

**Status:** isolated analysis-only stacked draft on v4 Lab PR #73. No gameplay rules, odds, RNG, AI, board definitions, stats, UI, save format, LAN, APK signer, or `main` change. A green CI means only that the synthetic tests completed; it does not make the experiment user-approved.

## Results motivating this milestone

M72: 864 experimental v3 simulations, mirrored human starting seats. A two-seat Quick player won **29/48 when assigned to seat 1** versus **11/48 when assigned to seat 2**. Classic (Woven Trail) produced **34/48 vs 17/48**; Long produced **33/48 vs 24/48**. These are independent human-vs-bot matches rather than two opponent totals from the same 48 races, so the two win counts are not expected to sum to 48.

M73: 3,456 v3/v4 simulations over every two- and four-player human starting seat, fixed seeds, 24 seeds and four travel/route strategies. Across 432 policy instances per version:
- Push + shortest: **16.9% (v3) -> 22.9% (v4)**.
- Push + safest: **45.1% -> 36.6%**.
- Steady + shortest: **26.6% -> 24.3%**.
- Steady + safest: **44.7% -> 40.3%**.

The v4 success-only Push hazard reward can help high-risk play but existing bots benefit too. These changes vary greatly by course and racer count; it would be wrong to assume v4 is balanced or that the reward is a simple universal improvement. v4 stays optional and unmerged.

## What this test newly answers

The previous human-vs-bot tests changed the human seat while all rivals followed a potentially different policy. To better isolate fixed turn-order effects, `FinishOrderFairnessAuditTest` makes **every player a built-in bot**. All identical bot policies receive the same preexisting RNG mechanism and visible state; there is no human policy mismatch. Compare first-seat and last-seat wins, and how close later seats are when a winner ends the race.

**2,160 fixed-seed finishes**: 3 courses x 2 Lab versions (v3/v4) x 2/3/4 seats x 120 seeds. Each sample includes:
- Actual winning seat, total die actions, total bot actions, global turn at finish.
- Minimum remaining graph spaces for nearest nonwinner.
- Number of nonwinners at most four spaces from the finish.
- Number of players with a later seat index than the winner who would normally still act before the round ends, and how many are within four minimum spaces.

The "within four spaces" signal is **not** proof those travelers could finish: four is the normal Steady distance, but hazards/route choices can change a move, and a route might not be the path chosen. Also, a last-seat winner cannot deny later seats this round because none remain; do not interpret absence of later opportunities as fairness.

Outputs from exact-head CI:
- `core/build/reports/gameplay-74/finish_order_fairness_samples.csv`
- `core/build/reports/gameplay-74/finish_order_fairness_summary.txt`
- Workflow archive `Crossroads-GAMEPLAY-74-<commit>`

## How to decide the next rule experiment

1. Confirm all 2,160 samples finish with the winner physically at the finish, no softlocks.
2. Read counts for **each** course/version/seat count, not just a pooled global seat-1 rate. Uniform 1/n win share is a reference, not a proof of exact fairness with finite RNG.
3. Examine whether earlier seats disproportionately win while later seats are within one normal Steady roll at match end. This is the candidate case where an equal-turn final round might help.
4. **Only if warranted**, build a new opt-in versioned **Final Round** prototype: on the first finish, let the remaining seats in that round receive a last move, then resolve ties with an explicitly defined, transparent rule. No ambiguous "first finisher still always wins" fake extra turn, and no retroactive changes to v1–v4 saves.
5. Alternative to test, not assume: rotate who starts each **rematch series**, which improves long-run fairness without affecting single-race starting order. Keep seat identity, LAN host authority and official Solo stats intact.
6. Check human expectation: some racing games deliberately reward first-to-finish priority. Ask physical testers whether first mover is noticeably unfair or whether an equal-turn ending would feel more confusing.
7. Do not add hidden handicaps, change dice odds, enlarge maps or stretch animations merely to make results look balanced.

## Phone gate after synthetic tests

- Verify the signed v4 QA APK from PR #73 has passed before treating it as a playable baseline.
- Play 3–5 Classic matches in two-player and at least two in Pass & Play (prefer alternating who is first); log who begins, outcome, completed rounds, real mm:ss, forks, and whether the loser was one move away.
- Look at results, rematch continuity, existing checkpoint restore and app usability on a physical Android phone.
- Full two-device Wi-Fi Host/Join remains independently unverified. This report does **not** authorize a released fairness rule.

**Scope decision:** M74 gathers the evidence required for an intentional fairness mechanic; no fairness mechanic is silently added in this milestone.
