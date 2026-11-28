# AI2-07B — Tournament evidence and next validation

Source: exact-head rules run 45683192102, artifact 15053909983, mixed_tournament.csv and encounter_samples.csv. Android run 45683191929 passed, QA1886 artifact 15054214634 (20273510 bytes).

## Two-seat tournament
960 completed games, 384 appearances per policy:
- LEGACY_V1 163 wins (42.45%)
- ADAPTIVE_V2_R1 168 (43.75%)
- ADAPTIVE_V2_R2 205 (53.39%)
- ADAPTIVE_V2_R3 212 (55.21%)
- ADAPTIVE_V2_R4 212 (55.21%)

Seat 1 won 574/960 (59.79%); seat 2 386/960. Equal-turn rules do not eliminate this observed advantage. The mirrored design controls aggregate policy exposure but does not establish human fairness. R3 vs R4 head-to-head 48/48. Seed reuse and shared RNG mean samples are not independent; no statistical superiority claim.

## Encounter comparison
288 homogeneous races per arm: R2/R3 actions 13522/13534, rolls 5471/5486, supplies 778/767, cover 334/388, brace 72/85, gamble 2366/2327, targets 24/24, shielded targets 9/9, summed per-match distinct hazard IDs 937/940. All 24 target opportunities per arm occurred on Brookside; other three courses zero. No catalogue coverage or player fun claim. R4 improvement therefore requires multiplayer-seat and mischief-focused evidence.

## Decision
No difficulty presets or public activation yet. Add four-seat cyclic lineup comparison with every policy omitted once and each remaining policy occupying each seat, on four V5 boards and 12 seeds. This tests larger-room competition, seat exposure and actual target frequency. Not a full permutation design; lineup interactions remain. Preserve default V1 and all replay identities. Owner visual review, Android install-over, LAN compatibility and Beta acceptance outstanding.
