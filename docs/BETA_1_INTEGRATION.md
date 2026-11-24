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
