# AI2-17A — AI roster and difficulty readiness audit

## Decision
Retain six Standard personality choices in debug offline Solo Lab v5, plus Guardian Advanced preview only. Classic/LEGACY_V1 remains default. Do not promote to public UI yet or advertise calibrated difficulty. Do not add Relaxed or other Advanced choices merely to fill a matrix. Starting-player rotation distributes order exposure rather than proving per-match fairness.

## Source audit
Current BotPersonalityV1 and BotDifficultyPreviewV1 mappings are authoritative for new QA matches; BotOpponentPolicyV1 is an experimental Scout resolver, not public/QA eligibility. Historical IDs must remain replayable even if unavailable for new selection. No changes to default policy, save schema, RNG, rules, signing or LAN.

| Personality | Standard policy | QA availability | Readiness |
|---|---|---|---|
| Classic | LEGACY_V1 | Standard | Existing default; preserve |
| Traveler | ADAPTIVE_V2_R1 | Standard | Descriptive style, physical validation pending |
| Scout | ADAPTIVE_V2_R2 | Standard | Relaxed/Advanced candidates not eligible |
| Gambler | GAMBLER_V1 | Standard | Keep V1; V2 refinement did not demonstrate improvement |
| Guardian | GUARDIAN_V1 | Standard + Advanced preview | Finish candidate experimental; no three-tier ladder |
| Strategist | STRATEGIST_V1 | Standard | Competitive in recorded matrix; human readability pending |

Evidence read from repository reports (historical source claims, not independent CSV reanalysis in this milestone):
- AI2_09A_STRATEGY_PRESETS.md: old policies not monotonic strength ladder.
- AI2_11D_GAMBLER_REFINEMENT_EVIDENCE.md: G2 equal-share wins 240/480 and 240/960; no observed defensive action; retain G1.
- AI2_12B_GUARDIAN_EVIDENCE.md: Guardian 855/1440 two-seat and 840/2880 four-seat focal wins; defensive behavior evidence, not human enjoyment.
- AI2_12E_STRATEGIST_EVIDENCE.md: Strategist 883/1680 and 847/3360 focal wins; distinct target heuristic frequency not isolated.
- AI2_13D_SCOUT_DIFFICULTY_EVIDENCE.md: Relaxed beat Standard in two-seat 133/240; Standard/Advanced tied 120/240, no calibrated order.
- AI2_13N_GUARDIAN_TIER_DECISION.md: finish candidate beat Standard 128/240 and 136/480, but tied Relaxed 120/240 in two-seat. Relaxed beat Standard 131/240. Retain Advanced preview only.
- AI2_15B_STARTING_SEAT_EVIDENCE.md: 2-seat first starter 1138/1920 wins; policy-specific gaps. Shared seeds and cohorts dependent.
- AI2_15F_ROUND_START_DECISION.md: rejected intervention increased Guardian P1 wins from 204/320 to 277/320.

## Promotion gates
1. Physical QA1997 acceptance (AI2_16F_ANDROID_ACCEPTANCE.md): no regressions in setup, recovery, results, accessibility or player identity.
2. Demonstrate useful, understandable personality contrasts using observed choices, not win-rate ranking. Verify actual labels and help text against implemented behavior; avoid unsupported claims.
3. For any future strength tier, predefine per-personality comparison, mirrored/rotated starts, course and seat count strata, independent seed blocks, paired outcome reporting and behavior checks. Do not treat duplicated reciprocal rows as independent. Require evidence of intended ordering without hidden RNG or handicaps; record inconclusive outcomes honestly.
4. Public activation remains separate owner/release decision; keep Classic default and frozen policy IDs, preserve saves. Physical player feedback required before claiming enjoyment or easy/hard experience.

## Next
AI2-17B: audit actual QA selector labels/help and add concise behavior descriptions only where supported; keep difficulty labels experimental. Prioritize player-facing clarity over further speculative bot variants. No production activation or public release. No code changes in this audit.
