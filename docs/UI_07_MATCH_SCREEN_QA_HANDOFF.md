# UI-07 — Board-first match screen refinement QA handoff
**Status:** SOURCE COMMITTED on `feature/match-ui-v0.7`; Android automated build, real-device acceptance and install-over data preservation must be checked independently.
**Base:** CAM-06B `feature/dynamic-camera-v0.6` (gesture-first camera, signed QA1065 CI pass).
**Scope:** UI-only. This milestone does not change dice, RNG, game rules, route graph, local stats keys, APK package/signing, AI, LAN or board artwork.

## Why
Owner screenshots from QA1057 showed (1) static camera controls obstructing the board, (2) Latest Encounter/route legend beneath the fixed turn-action area, and (3) "AI 2's turn" paired with unlabelled "AI 1 rolled 3." Research points to a board-first layout with only the active game decision made visually dominant; see [comparative UI research](RESEARCH_MOBILE_BOARD_GAME_UI_2026_11.md).

## UI-07 source changes
- The in-match `Woodland Crossing` heading remains, but always-visible `Inspect map` text is replaced with one small, accessible **Match options** overflow. It contains: **Board overview**, **Journey journal (N)** (disabled when there are no encounters), and **Leave match** (still requests confirmation).
- The central board is **340dp** high rather than **315dp**. No overlays are added atop the map, and all CAM-06B gestures remain.
- The duplicate Latest Encounter card and journal list below the map are removed. Immediate encounter presentation still appears in the turn sheet when triggered; full history is in a scrollable Journal dialog.
- The fixed turn panel still displays who is actively playing and the round; prior events now say **Latest action · ...** so an AI turn does not look like the other bot's die roll. First-turn guidance is short.
- Removal of the persistent **Leave match** footer makes the bottom sheet shorter and gives the board more breathing room.
- AI-waiting message simplified; interactive dice and route-selection controls unchanged.

## Android testing required
- [ ] Exact debug APK from latest UI-07 source is signed with existing public **QA-only** key. Record APK SHA, package and versionCode.
- [ ] Existing user test APK installs over QA1065 (and older compatible QA) without erasing prior completed-match/win records or the gesture-hint dismissal.
- [ ] Portrait 2/3/4 seat games: board starts at follow view and remains visible; no permanent camera bar; UI does not clip on small phones.
- [ ] Small-font and large-font devices: Match options menu is reachable; each item works; board overview and back to match work while AI turns are advancing.
- [ ] After a recent event, transient reveal remains visible, while permanent Journal shows correct turn, title and outcome; scrolling never hides a required action.
- [ ] With AI 2 active after AI 1 roll, current turn stays clearly labelled separately from "Latest action".
- [ ] Old Forest Junction shows **Cliff Track**, **Mill Lane**, **Cedar Bridge** entirely, with usable selection at largest normal Android font size.
- [ ] Roll button works once per press; Cozy/Brisk, bot timing, camera handoff, save stats, results and rematch unchanged.
- [ ] Android Back dismisses menu/dialog before accidentally leaving; physical multi-touch and double tap remain functional.
- [ ] Landscape and accessibility/TalkBack are exploratory, **not** claims of passing.
- [ ] No merges to `main`; await owner device feedback.

## Verification labels
- **SOURCE-ONLY:** latest UI-07 Kotlin code and this handoff.
- **JVM/ANDROID-CI:** update only when linked GitHub Actions runs finish successfully.
- **EMULATOR:** update only with exact proven Android instrumentation or screenshots.
- **OWNER-PHONE:** pending until user tests a concrete QA APK.
