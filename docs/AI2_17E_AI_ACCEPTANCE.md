# AI2-17E — AI acceptance and transition

## Evidence boundary
AI2-17D head fe5964759a6abf84537444706ca11fdedcd9cbf4: rules run 46098498654 (#475) passed; Android run 46098498630 (#1030), job 140835327986, build/unit test/lint in progress at inspection. Prior Android #1019 failed compilation; resolver scope repair is committed but not yet verified. Do not install an unverified build or claim an APK exists until artifact and signing gates pass. QA1997 remains the documented older verified fallback, not a build containing AI2-17D changes.

## Consolidated scope
- Six immutable Standard personality mappings; Classic default; Guardian Advanced experimental only.
- Debug offline Solo Lab v5 selector, concise style descriptions and selectable rows; no calibrated easy/hard ladder.
- Starting-seat choice, random resolution once at match creation and optional cyclic rematch; ordinary rematch retains starter.
- Checkpoint/backup preserve actual seat and resolved policies; historical replay gate. Rotation distributes order exposure, not proof of fairness.
- Display-only opponent names in HUD, targets, map, journal, results and receipt; recognized narrative positions only; original recorded strings unchanged.
- Fair Finish status distinguishes winner and other finishers; guide describes QA options and existing Nearby play.

## Consolidated device acceptance
Carry forward every unchecked P01–P12 gate from AI2_16F_ANDROID_ACCEPTANCE.md, substituting the eventual verified candidate version and receipt. Export backup before upgrade; do not uninstall or clear data. Additional checks, all NOT RUN:
| ID | Test | Expected |
|---|---|---|
| A01 | Select each Standard personality and Guardian Advanced in eligible Solo Lab v5 | Seven options, accurate style hints, selected row accessible; no strength promise |
| A02 | 4-seat mixed lineup including duplicate Scout | Numbered identities distinct, colors and human seat unchanged |
| A03 | Roll, route, shield, prank and final-round events | Recognized actor/target names consistent in HUD and journal; original route text intact |
| A04 | Inspect full map and rival chooser; use TalkBack | Resolved identity and correct target seat; no duplicate radio targets |
| A05 | Finish multi-finisher Lab v5 race | Actual winner, other finishers and nonfinishers accurately distinguished in plates/results/receipt |
| A06 | Recover active and finished mixed race, ordinary and rotated rematch, export/import in backed-up QA sandbox | Resolved identities, actual starter, records and policies retained |
| A07 | Classic, Pass & Play, Nearby and public variant eligibility | No QA-only opponent/rotation controls; established behavior unchanged |
| A08 | Read guide on home and during match; narrow screen/large fonts | Accurate Classic/Nearby/QA wording, reachable footer, scrollable choices |
Record device/OS/version, PASS/FAIL/NOT RUN and reproduction steps. Physical acceptance is not inferred from CI.

## Transition decision
After repaired Android CI and artifact identity verification, freeze AI feature expansion at QA scope. Keep physical acceptance and public activation separate. No new policies or difficulty labels without evidence. Review existing broader roadmap and remaining release gates; prioritize game-first layout, board readability, action affordances, onboarding and complete journey polish over speculative features. No public release, merge, signing, network or save changes authorized by this checkpoint.
