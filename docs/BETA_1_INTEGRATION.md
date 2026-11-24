# Beta 1 Polish — unified Woodland Experience, November 24, 2026

**Latest verified QA1696:** [signed Android archive](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45443761764/artifacts/14945831693), [Android workflow](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45443761764). All workflow steps passed. Source 289c29cbb4c13b51eeaf87a5ef03c10ee07926d8; built merge commit 881cdfb6fc962fd7031df8ee4dfd520f4c3a36f1. Archive expires December 8, 2026. Dedicated integration/beta-1-polish fast-forwarded from verified QA1695 source by four commits, no divergence. Original integration/beta-1 and main unchanged.

## Current scope
- POLISH 01–03: adaptive portrait framing, optional overhead groundcover, matte live trails and intersections.
- POLISH 04: 1,080 baseline and 480 expanded-course all-bot simulations passed. Original two-seat Woven median rounds 5, expanded prototype 9; median forks 8 in both. Prototype remains outside shipped registry. No human time or enjoyment claim.
- POLISH 05–06: parchment home, scroll cue, existing art reused in course cards, compact HUD, themed match options.
- Existing original three courses, experimental rules, saved progress and LAN behavior retained. No public release.

## Acceptance gates
1. Physically install QA1696 over existing QA (export backup first), verify record and saved race.
2. Compare Storybook and Overhead across courses, check actual thumbnail crops, scroll, action tray, route sheet, camera, touch targets, performance and large text.
3. Complete Solo and Pass & Play, inspect results and rematch. Record durations rather than assume simulated rounds predict human minutes.
4. Two physical phones: trusted Wi-Fi, host/join, wrong code, disconnect/rejoin, complete match and host rematch. Plaintext TCP remains a release blocker.
5. New asset library: establish coherent authored sprite dimensions, transparent boundaries, provenance and reuse; no generated concept image may be represented as packaged art or actual screenshot. Prioritize board scenery and shared UI over cosmetic expansion.
6. Expanded Woven: separately versioned authored layout, route tradeoffs and encounter exposure measured; no automatic promotion of midpoint prototype.
7. Public distribution remains separate: owner approval, production signing, privacy/security and distribution review.

Use integration/beta-1-polish for subsequent verified polish work. Existing historical baseline follows. Physical acceptance and complete integrated screenshot gate still open.

---

# Beta 1 candidate — November 24, 2026

**Latest verified signed QA1688:** [APK archive](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45335337623/artifacts/14894101997), [Android QA](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45335337623), [rules and design](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45335337627). Both passed. Source head 955bd52aa640237f8c80da2aea1718a985669733; built PR merge commit e139d6797d14683e37166baf43b96d50f19d8ea6. Integration branch fast-forwarded from f531c0cc3d0900b33a9fbdb61b4547a32aa7f0e6 with 27 additional commits and no divergence. No main merge. QA archive expires December 8, 2026.

## Implemented and automatically verified
- Three distinct optional overhead scenes with shared atlas, original Storybook remains default (#96).
- Discoverable Nearby/help, measured home footer, selected-course summary and shared results controls (#97).
- Exact-config rematches after completed checkpoint restore, including six historical rules versions (#98).
- Original-rules minimum distance comparison (#99).
- AI graph-distance cache with legacy equivalence tests (#100).
- Pending-join cancellation and original-socket-bound queued writes (#101).

## Physical beta acceptance — still open
1. Install QA1688 over existing QA without clearing data; confirm record and active race recovery.
2. Solo Quick, Classic, Long: complete races; record rough time, confusing decisions, readability, camera and perceived fairness. Inspect both Storybook and optional Overhead under Match options.
3. Pass & Play: two people, full finish and rematch; check turn ownership.
4. Two physical phones on trusted household Wi-Fi: host/join, incorrect code, correct code, disconnect/rejoin, full race and host rematch. Nearby TCP remains plaintext; no host process-death recovery or public-network readiness.
5. Large text, TalkBack, short screen and device performance. Visual-92 whole-map difference evidence exists but complete screenshot gate still unresolved.
6. Public release separately requires private production signing, privacy and security decisions, distribution compliance and explicit owner approval. No public release.

## Next development rule
Use integration/beta-1 as the current source. Do not add speculative features until physical findings or a reproducible integration issue justify them. Historical records below are not current status.

---

# Beta 1 — integrated development candidate

This is the complete active game source in one review branch, not a public release. Source parent: VISUAL-95 #95 b38aac34af1f8e657ec93c864a130902cf650318. GitHub comparison against main before this checkpoint: 682 commits ahead, zero behind, 267 changed paths. Alternate HUD #55 is divergent (four commits absent from the active lineage); do not merge it. Preserve historical PRs and do not merge main without owner approval.

## Evidence as of this checkpoint
- QA1680, VISUAL-94: signed Android build/unit/lint/public variant validation passed, artifact 14885933423 from run 45316635525.
- QA1679, VISUAL-93: signed Android build passed, artifact 14885553423 from run 45316069272.
- QA1678, VISUAL-91: signed Android build passed, artifact 14884620025 from run 45309598813.
- QA1680 has the Woven terrain and full environment library, not the subsequent VISUAL-95 control changes.
- VISUAL-95 Android run 45316973684 pending at branch creation.
- VISUAL-92 run 45315961076 captured six real Android screenshots, preserved checkpoint and appearance preference on QA1677→QA1678, whole-map atlas differences observed; final assertion failed because opening live view was identical. Retry 45316542594 blocked by Pixel Launcher ANR. The corrected #92 head c69ecd8a4a1ed76c5c38088fbdefcdadaf3b9295 is a later sibling of the inherited workflow revision; reconcile its script before rerunning. No full screenshot QA pass claimed.
- Prior RC1 evidence remains valid for its specific builds: QA1607→QA1613 install-over, 144-match cross-version replay stress, LAN loopback. No new physical acceptance inferred.

## Beta scope and gates
1. Original rules, three authored courses, Solo AI, two-to-four local Pass & Play, local resume/export/import. Tactical/Fair Finish stay QA research unless approved. Preserve default Storybook and optional Woven overhead.
2. Finish Brookside and Cedarwood visual review against their actual course geometry. Do not copy Woven art or automatically promote experimental visuals.
3. Full journey review: home/start, each turn phase, map, rules, results, rematch, quit/resume, large fonts, TalkBack. Actual human pace and readability required.
4. Real two-phone trusted household Wi-Fi host/join/rejoin/rematch including wrong-code rejection, turn ownership and disconnect behavior. Existing TCP is plaintext; code does not encrypt. Public release requires a deliberate secure transport or exclusion decision.
5. Exact integrated source build/unit/lint/design validation and signed QA package, followed by device install-over preserving old checkpoint/statistics. QA signer is not production signer.
6. Public distribution is a separate later gate: privacy, permissions, naming/IP, production signing, explicit export/import migration between package IDs, release artifact. No public deployment or main merge authorized.

## Working agreement
Use this integration branch as the current review base; small fixes can target it. Do not silently merge abandoned variants or close old draft evidence. Track verified artifacts separately from hypotheses, generated concept art and pending CI. Prioritize complete playable experiences over incremental decoration.
