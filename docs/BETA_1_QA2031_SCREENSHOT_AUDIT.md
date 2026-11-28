# BETA-ITCH-02B — Owner screenshot audit, QA2031

Owner confirmed both batches complete. 16 submitted images, including one Sudoku reference and a repeated Balance Preview overview; 15 Crossroads image submissions, not necessarily unique. Test target QA2031 per prior handoff; screenshots do not independently show version code. Review is visual evidence, not full physical acceptance. Latest documentation head cc23a87 rules #481 (46100075656) and Android #1036 (46100075675) passed.

## Evidence and priorities
| ID | Observation | Classification | Priority |
|---|---|---|---|
| V01 | Woven overhead river bridges appear as oversized capsule platforms; paths overrun or split across them | visible composition defect; inspect geometry/renderer | High |
| V02 | Woven and Regional terrain has broad flat color fields, sparse isolated scenery and small repetitive grass stamps | visible art cohesion issue | High |
| V03 | Several round-one frames show no pawns; Regional start frame has left pawn close to edge; recenter control visible | observed framing, not proof of automatic follow bug; reproduce untouched start, manual pan, recenter and movement | High |
| V04 | Collapsed Steady/Push dock has substantial lower blank area and separate details row; expanded dock takes more board space | visible layout opportunity; preserve minimum touch targets | High |
| V05 | Home banner is attractive; ornate gold/green buttons, flat player chips and course cards mix styles | design consistency; use restrained tabletop hierarchy informed by Sudoku reference, not copy | Medium |
| V06 | Full-map dialog has thick dark frame/padding and tiny region labels; Regional topology fits and is recognizable | improve usable map area, maintain gesture/footer accessibility | Medium |
| V07 | QA options occupy long home scroll and match menu; experimental labels visible | expected QA scope, audit public exclusion and grouping rather than remove tools | Medium |
| V08 | Balance Preview bare background and angular network | expected prototype, keep out of public registry | Release gate |
| V09 | Numbered woodland pawns, cream/forest palette, course cards, clear Steady/Push labels and map legend | retain strengths | Retain |
| V10 | Regional Journey longer serpentine topology with meadow/forest/village | promising QA course; no release promotion without balance and art acceptance | Later |

## What screenshots establish
Home, course selection, expanded options, live tactical roll interface, match options, board exploration, full map, experimental course launch and dice presentation are visible. No screenshot evidence of complete match, actual pacing, route choice/encounter outcomes, rematch, backup restoration, install-over, two-device LAN or accessibility acceptance. Local record shown 0/0 does not establish loss or preservation. No crash reported. Do not infer actual automatic camera behavior or build version solely from images. Sudoku screenshot is a reference, not Crossroads evidence.

## Beta 1 fix sequence
1. Inspect camera viewport and action dock constraints, reproduce follow vs manual, fix only demonstrated visibility/layout issues; regression tests.
2. Inspect bridge rendering against actual Woven river and edge geometry; reduce visual footprint and align to traversed edge without changing board graph.
3. Cohesive terrain/scenery pass with existing approved assets, preserve route contrast and pawn visibility; compare actual same-race screenshots.
4. Simplify home and map hierarchy, group QA options separately, keep public scope and release gate decisions separate.
5. Build one consolidated QA candidate; request focused owner screenshots and full normal Solo/Pass & Play race, recovery and pacing feedback before acceptance.
6. Public surface/security, private signer integration, privacy and itch packaging remain open per BETA_1_ITCH_RELEASE_GATE.md.

No new runtime changes in this checkpoint. No public release, merge, signing, RNG, save or rule changes. Original screenshots are conversation uploads, not committed binary assets. This document records the observations so subsequent chats can resume even if images are unavailable.
