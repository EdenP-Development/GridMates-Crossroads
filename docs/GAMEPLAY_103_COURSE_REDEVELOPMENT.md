# GAMEPLAY-103 — Course redevelopment baseline

## Evidence (2026-11-24)
QA1759 physical review: 48 seconds, 3 rounds, 2 seats, AI winner. QA1734: 61 seconds, 3 rounds, 2 seats, local winner. Tactical fair-finish playtest; not representative human timing. See QA1759_OWNER_PHONE_VISUAL_AUDIT.md.

Direct graph traversal of current JSON (edge counts, not expected dice rolls):
| Course | Nodes | Min/max start-finish edges | Forks | Event nodes |
|---|---:|---:|---:|---:|
| Brookside | 24 | 13/15 | 2 | 7 |
| Woven | 38 | 16/22 | 5 | 14 |
| Cedarwood | 101 | 54/66 | 6 | 28 |
| Expanded Woven prototype | 81 | 32/44 | 5 | 14 |

Previous documented baseline: 1080 all-bot samples, two-seat median rounds original 4/5/16 and V5 3/3/12 for Quick/Classic/Long. See BETA_POLISH_04B_EXPANDED_WOVEN.md and BetaPacingAuditTest. Expanded Woven adds plain midpoint nodes only; no additional forks, no new events, no authored geography. Existing 480-sample experiment remains separate. Do not treat simulation actions as human minutes.

## Proposed initial design targets (not accepted release thresholds)
Quick 3–5 min, Classic 8–12 min, Long 15–25 min. Validate physical human timing, decision exposure and enjoyment; never inflate animation delays to meet targets. Distinct courses should differ in character and strategic depth, not only length.

## Next experimental workflow
1. Build graph/decision audit for all three seat counts, original and V5, using fixed seeds and existing bot policy. Record round distributions and per-seat roll/fork/encounter exposure, wins and zero-decision prevalence. Distinguish automatic events from player choices. Avoid claims of fairness from all-bot samples alone.
2. Author a new Woven prototype outside runtime registry with meaningful regional forks, clear risk-distance tradeoffs, encounter cadence, sufficient spacing for board art. Compare existing and midpoint-expanded Woven; do not silently overwrite immutable original IDs.
3. Test human decision policies (Steady/Push, shortest/safest), then physical runs. Choose geometry before commissioned board art. Keep old layouts and save identities intact.
4. Design coherent forest/meadow/village composition around approved graph; fix river clipping, oversized bridges and compact button leaf collisions as recorded in QA1759 review.
5. Only after evidence and approval register versioned course. Preserve network compatibility, QA vs private beta signer identities, and existing checkpoints.

No rules, runtime course or release changes in GAMEPLAY-103. Active draft PR120; main untouched.
