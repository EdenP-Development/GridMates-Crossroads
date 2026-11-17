# QA-15 — Gameplay reliability and accessibility hardening

**Date:** November 17, 2026
**Status:** source + JVM regressions committed; this head's Android compilation, lint, signing, and real-device verification need separate evidence.
**Branch:** `feature/reliability-accessibility-v1.3`; stacked on [RESULT-13 + GUIDE-14 Draft PR #18](https://github.com/PlayerEden/GridMates-Crossroads/pull/18).
**Last verified before this work:** signed [QA1107](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44151632306), build/unit/lint/signing/design QA PASS. User phone screenshots are from older QA1082; there is no QA1107 physical-phone acceptance yet.

## Audit findings by severity

| Severity | Finding | Evidence | Disposition |
| --- | --- | --- | --- |
| P1 | Android system Back had no explicit in-match guard despite the visible Leave Match confirmation | `MainActivity.kt`: no Compose `BackHandler` in QA1107 | **Fixed in source:** unfinished match Back now requests Leave confirmation; finished match Back returns home; dialogs/popup still own their Back |
| P1 | Unfinished game state is ephemeral | `session` is Compose `remember { GameSession? }`; no restoration payload or persisted PRNG state | **OPEN.** Activity recreation / process death can lose active game. Do **not** imply resume, install-over active-game survival, or background process death safety |
| P2 | Current/animated player marked with color/bold only | player strip `Surface` and `Text` | **Fixed in source:** seat semantics announce seat number, name, current/waiting/moving/finished state and shield |
| P2 | Board canvas announces gestures but not inspected player or follow state | board `semantics` | **Fixed in source:** announce focused piece name and region, whether following or manual exploration; preserve semantic custom actions to recenter / whole-board |
| P2 | Roll button's decorative pip image could be spoken as an actual 1 before rolling | `DieFace` inside Roll button | **Fixed in source:** no spoken preview-face result in Roll action; the *actual* result die remains labeled |
| P2 | No explicit dynamic turn heading announcement | action pane `Text` | **Fixed in source:** `LiveRegionMode.Polite` on turn header only, not on rapidly cycling pips or animation frames |
| P2 | Match options icon had a small 40dp visual size | `IconButton.size(40.dp)` | **Fixed in source:** 48dp target |
| P2 | Core JVM tests did not explicitly lock invalid actions/terminal snapshot/mutable snapshot isolation | GameSession tests covered illegal forks but not wrong-phase actions, mutation isolation and post-win lock | **Three new pure JVM tests** |

## Behavior and limitations
- `BackHandler` is enabled only during an active match with no visible dialog/menu. With a junction chooser, overview, journey journal, help guide, results, leave confirmation, or options popup, that surface handles Back first.
- After a completed race, Back returns to the main menu (existing match record stays); for an unfinished race, Back opens the existing **Leave this match?** prompt.
- No new camera toolbar: custom accessibility actions remain non-visual. The active player strip uses spoken labels, not just color.
- All player location semantics are derived from **current snapshot and board definition**, not manually invented coordinates.
- No alteration to random draws, rules, AI choices, movement timing, button behavior, match record keys, token art, graph or events. No new dependencies.
- Android official guidance: [Compose semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics), [accessibility principles](https://developer.android.com/guide/topics/ui/accessibility/principles). Semantic descriptions and `liveRegion` are implemented source code; actual TalkBack usability cannot be established by compilation alone.

## Automated acceptance
- [ ] New exact-head JVM tests: mutable `Snapshot` cannot mutate live game; wrong-phase actions do not mutate game; all 2/3/4-seat winners remain terminal after illegal further actions.
- [ ] Existing seeded bot races, forks, event bounds and movement pacing tests unchanged, pass.
- [ ] Android QA build, unit tests, lint, signing/version verification and artifact upload succeed at exact head.
- [ ] Design-data QA succeeds at same source head.
- [ ] PR diff contains only intended app semantics/Back and new tests/docs; no `GameSession` production code modification.

## Owner Android / TalkBack acceptance
- [ ] Install **over QA1107** without uninstall, and check saved completed/wins and dismissed camera hint.
- [ ] Start a 3- and a 4-player game; turn on TalkBack and focus each seat: hear player name/number and current/moving/waiting status, shield when present.
- [ ] Move from manual board zoom to automatic Follow using board custom accessibility action; verify spoken location/mode and no extra floating controls.
- [ ] Focus Roll: hears action, not an arbitrary pre-rolled die result. After rolling, real die and remaining steps are understandable.
- [ ] Let AI act: turn change announces politely, without announcing every rapidly changing frame.
- [ ] Press Android Back during an unfinished match: confirmation appears, Keep playing resumes match, Leave discards unfinished match only.
- [ ] Android Back when a 3-way route choice dialog or other dialog/menu is open should close that surface; it must not unexpectedly leave the match.
- [ ] After finished match with saved stats, Back goes to home without an unsaved-match warning.
- [ ] 200% Android font or large display: screen still reachable; inspect labels/buttons at 2–4 seats.
- [ ] Background/resume while process is *alive* and full activity recreation/process death are separate scenarios: unfinished-match recovery is NOT implemented.

## Verification labels
- **SOURCE**: code committed on this draft branch.
- **JVM**: not claimed until exact unit workflow succeeds.
- **ANDROID-CI**: not claimed until signed artifact passes.
- **OWNER-PHONE / TalkBack**: pending physical checks.
- **LAN**: not implemented or tested.
- **MERGE**: do not merge to `main` or release without owner approval.
