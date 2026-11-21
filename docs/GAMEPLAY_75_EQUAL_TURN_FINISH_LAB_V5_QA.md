# GAMEPLAY-75 — Equal-turn final round (opt-in Lab v5)

**Status:** draft-only experiment stacked after passing GAMEPLAY-74. Do not merge or promote as default without full exact-head Android + JVM QA and human acceptance. `main` untouched.

## Why this specific change

GAMEPLAY-74's 2,160 identical-bot races found a large first-seat advantage under v3/v4, most pronounced in Quick (98/120 first-seat wins in both v3 and v4 two-player races). Classic was 85/120 (v3) and 76/120 (v4), Long 68/120 and 65/120. At the moment a racer wins, later racers within one regular 4-space Steady roll were present in only ~28-34% of Quick two-player matches and ~4-7% of Classic/Long two-player matches. Thus an equal-turn finish **may reduce a last-action disadvantage** but cannot be expected to cure all Quick asymmetry. These are synthetic, not human enjoyment or formal inferential claims.

GAMEPLAY-73's v4 Push + successful Gamble reward remains unapproved: under pooled policies Push + shortest improved but several other strategies became less successful relative to the state-aware AI.

## V5 rule: one final chance, not a prolonged extra round

- The existing **Tactical choices · Experimental** toggle starts an offline Lab v5 race for *new* matches; the default OFF still starts the original unmodified rules. Nearby Host/Join remains the original classic race.
- The *first* traveler who reaches the real FINISH starts the **Final Round** only when later seat indices have not yet had their turn in that round. That traveler stays at FINISH and does not roll again. Remaining later seats get exactly their current-round turn, with unchanged dice, encounters and routes.
- If a later traveler also reaches FINISH, compare **unused movement steps on the finishing roll or route move**: the most unused wins. If tied, the traveler who reached FINISH first wins. This is visible before starting and on the final receipt, not a hidden catch-up effect.
- If no later racer finishes, the first traveler wins normally. If the first finisher is the last seat, the match ends immediately. The game does **not** start another entire round or let previous seats roll again.
- V5 inherits v4's Push + successful hazard Gamble +2 conditional success reward, Steady + Shelter synergy and v3 encounters. All old variants and the original rules are untouched.
- An in-match 'FINAL CHANCE' turn header and nonblocking action copy keep players oriented. No extra dialog, animation delay, purchases or surprise rewards.
- Results show the **final-round winner**, plus other travelers who also reached FINISH. No invented podium places.

## Persistence and compatibility

- Immutable new board suffix: `__decision_lab_v5`. Old v1/v2/v3/v4/legacy IDs and content fingerprints remain unchanged.
- The pending final-round leader, unused movement and contenders are **transient, deterministically reconstructed** from existing recorded actions; no changes to serializable `Snapshot`, checkpoint schema, action types or LAN protocol. V5 checkpoints always restore with the same source board, seed, moves and events.
- Final-round winner is set only when the last remaining seat has concluded its legal turn; results and official Solo win counts are still isolated from all Lab modes.
- Do not downgrade a device with an active v5 checkpoint to an older QA build. Preserve backups/install over; phone tests must inspect recovery at the pending final chance.

## Exact-head automated QA

- `DecisionLabVersion5FinishTest`: pending final round, no extra round, first arrival tie, a later finisher winning with greater excess, four seats each finishing, final-round snapshot replay, legacy v4 immediate finish, uniqueness of six board version fingerprints and no cross-version restores.
- `EqualTurnFinishComparisonTest`: **2,160 identical-bot v4/v5 races** = 3 courses x 2/3/4 seats x 120 seeds x 2 versions. Record winner seat, dice and bot actions, whether the final round triggered, the number of extra rolls and the count of finishers. Compare per-course seat 1 versus last seat, time-proxy changes and multi-finisher prevalence. No assumption of improved balance is baked into the assertions.
- `DecisionLabV5PresentationTest`: precise v5 result title and old version labels; conditional hazard reward remains honest.
- Existing v1-v4 checkpoint, engine, network, Android UI/accessibility, art-data, lint, signer and release-surface checks must pass. Archive `Crossroads-GAMEPLAY-75-<SHA>` contains the synthetic results. No new APK is declared ready until exact-head QA finishes.

## Physical Android playtest

1. From successful v5 QA, install over QA1571 **without deleting data**. Verify existing stats/backup and an older v4 checkpoint still resume; do not create a v5 active checkpoint before installing an older APK.
2. Start original Quick with experimental OFF: race still ends on first arrival. In new v5 Quick with toggle ON: first-seat finisher triggers the final chance for later seats with a clear turn title.
3. Two-player Pass & Play: have seat 1 finish and seat 2 fail; then have both finish in a round and verify unused-step tie rule. On four-player Pass & Play, each later seat gets a chance, no earlier seat gets another turn.
4. Save, force-stop, reopen during pending final chance. The same active seat and prior first finisher must return; playing through produces a single result and no duplicate official stats.
5. Confirm the result receipt distinguishes `Final-round winner` and `Reached Finish`, including when the later seat wins.
6. Test Cozy/Brisk animations, small portrait and landscape, large Android font scale, route sheet, illustrated encounters, result buttons, replay, spectator camera and speed.
7. Compare human session time and whether waiting for an additional finishing move feels fair, engaging, or irritating. Avoid making Quick slower just to match a target duration.
8. Separate release blockers remain: two physical Wi-Fi phones, local persistence install-over, screen-reader verification, and non-QA signing.

## Decision after QA

If first-seat bias remains high or the new rule confuses players, don't silently adopt v5. Test a transparent rotating starter in a series/rematch, or explore why the opening 1-2 rolls dominate Quick. Do not use hidden handicaps, adjust dice weights without a versioned experiment, or merge this test into `main` until approved.
