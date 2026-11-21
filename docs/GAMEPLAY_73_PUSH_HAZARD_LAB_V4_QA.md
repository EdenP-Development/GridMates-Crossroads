# GAMEPLAY-73 — Opt-in Lab v4: Push + successful Hazard Gamble

**Status:** isolated source/QA experiment built on GAMEPLAY-72's fully passing Android and rules audit. This document alone is not evidence of a passing new APK, 2-phone Wi-Fi test, approved rule balance or release readiness. `main` remains untouched.

## Decision and evidence

The 864 fixed-seed v3 simulations in GAMEPLAY-72 finished with no softlocks. Across all three courses and both 2- and 4-seat conditions, the four policy wins were:

| Human policy | Wins / 216 |
| --- | ---: |
| Steady + safest route | 96 |
| Push + safest route | 97 |
| Steady + shortest route | 67 |
| Push + shortest route | 35 |

These pools mix very different course lengths, seat counts and a small 12-seed sample, and each compares with the game's unchanged, state-aware AI. **These are diagnostic, not validated fair-play expectations**. The variation across courses matters: v3 Woven 2-seat Steady+shortest performed well (18/24), whereas Woven 2-seat Push+shortest won 5/24. One owner phone match at 57 seconds / 3 rounds is not a representative average.

Static board audit: every authored fork presents a risk-versus-shortest-distance conflict, but this does **not** mean each available branch is competitive. At Old Forest Junction on Woven Trail, Cliff Track is high risk with at least 9 spaces left, while Mill Lane is medium with at least 8. The current forest hazard Gamble is a weighted 30% +3, 60% retreat 2, 10% retreat 1. A Push + high-risk route can therefore combine an unattractive road with a volatile negative consequence. V3 also lets a player Brace, trading unused movement for a shield.

## One precise hypothesis

**If** a player deliberately chooses Push, enters a high-risk route, chooses Gamble, and the existing regional hazard draw is an **advance**, the successful risk should have a more distinctive upside. Under v4 only:

- On a successful hazard advance: add **2 extra movement steps**, exactly once, on top of the existing event's advance (e.g. +3 becomes +5). Event success chance, draw weights and original effects stay unchanged.
- On retreat, failed gamble, Brace, Steady, shelters or mischief: **no v4 extra bonus**. Steady's existing setback protection and shelter synergy are unchanged.
- Existing once-per-turn route encounter cap stays; no extra event, reroll, free turn or backdoor action.
- Built-in bots continue the existing publicly defined heuristic; this experiment does not change AI policy.

The extra reward is only an experiment. It may help, be insufficient, or overcorrect. Do **not** promote it until measured against v3 and accepted in real Android play.

## Compatibility and player honesty

- The new immutable board ID suffix is `__decision_lab_v4`; older v1/v2/v3 IDs, checkpoints and game effects are unchanged. Base board JSON and event JSON do not change.
- When **Tactical choices · Experimental** is ON for a **new offline** match, launch v4. OFF continues launching original rules; Nearby Host/Join continues using unmodified original Classic.
- Previously saved v1/v2/v3 matches remain registered and restorable under their exact old rules. V4 checkpoints encode and replay the new bonus from the same seed and action sequence; no schema fields added. Existing test wins remain excluded from official counters.
- Route previews say `Push + successful Gamble: +2 extra` only on eligible v4 high-risk forks with an encounter remaining. The card says `Success: +2 extra` rather than promising it on every Gamble; actual encounter output shows the complete gained amount and bonus.
- Saved-game downgrade warning: older QA builds do not recognize active v4 checkpoints. Do not downgrade with an active Lab v4 race. Preserve/backup any important progress before changing QA versions.

## Exact-head automated QA

1. Core/Android compile, unit tests, lint, source art provenance, signing identity and archive. Existing data QA must pass.
2. New `DecisionLabVersion4Test` checks all five version fingerprints, rejected cross-version restores, a pending v4 hazard, successful Push +2 vs same-seeded v3, Steady without bonus, failed hazard, Brace, and bit-exact v4 checkpoint restore.
3. New `DecisionLabV4PresentationTest` verifies result labels and honest conditional card text.
4. `DecisionLabVersion4BalanceTest`: **3,456** v3/v4 simulations = 3 boards x (2+4 human starting seats) x 24 seeds x 2 travel styles x 2 route styles x 2 versions. Holds human encounter response at Supplies/Gamble. Outputs individual CSV and summary of win shares, roll counts and observed bonuses; reports unexpected regressions rather than hiding them.
5. The rules workflow publishes `Crossroads-GAMEPLAY-73-<SHA>`; exact-head status and artifact must be checked before using the statistics.

## Real-device acceptance (separate)

1. Install the signed QA APK **over QA1558**, not after uninstalling. Confirm previous Solo wins/completed match counts and existing checkpoint accessibility.
2. With Tactical OFF, run ordinary Quick: no Steady/Push and no bonus. With Tactical ON, start a new v4 Woven race and check the version subtitle and Playtest result.
3. Try Push -> High -> Gamble, both a success and retreat. Only a successful v4 Push Gamble adds the advertised +2. Try Steady -> High -> Gamble: setback may be blocked but no +2 win bonus. Try Brace: shield/no extra movement.
4. Shelter Supplies/Cover, two and three-way route choices, no double encounter after a long die, and the actual winning pawn at finish.
5. Force-stop during pending v4 encounter; replay exactly once, no duplication; complete match and verify no official win/stat increment.
6. Test 2- and 4-seat Solo, Pass & Play, Quick/Classic/Long, high font scaling and both portrait/landscape orientations. Confirm the bottom encounter card remains readable without clipping.
7. Measure several full matches in actual elapsed time and ask if choosing a risk **feels** worthwhile. Avoid increasing animation delays to fake longer play.

**Decision:** Keep v4 opt-in and unmerged unless the paired audit and owner playtest show better, understandable risk/reward without a fresh dominant choice, plus all compatibility and LAN/release gates remaining clear.
