# UI-81 — Owner QA1614 visual audit (phone screenshots)
**Date:** November 22, 2026  
**Source:** nine owner-supplied screenshots captured from the installed Crossroads QA1614 Android app. This is **manual visual evidence**, not a complete device functional, usability, persistence or network acceptance report.

## What the screenshots actually show

- **Home / first viewport:** the illustrated woodland hero, Solo versus AI, two-player default, Classic/Woven Trail selected and pinned START RACE are visible. The collapsed options button is partly cut off/hidden by the dock on the initial viewport; its long string wraps to two lines.
- **Expanded secondary options:** Board appearance, Tactical choices, Lab v5 Fair Finish, Nearby Host/Join, guide, local record and the persistent Start CTA. Fair Finish ON is clearly green, but the **OFF** track/thumb shown in the companion screenshot is lavender/grey-purple despite the rest of the screen using cream/green/gold.
- **Live in-game camera and HUD:** a woodland painted world, readable wooden routes and junction discs, player-color strip, recenter button, contextual dice/route/encounter dock. The user has reached a crossroads with one remaining step and has an interactive shelter choice with SUPPLIES and COVER. Separate screenshots display the START pair of animal tokens and much closer map sections during play. **These show rendering and specific in-game states**, not completion of a full match.
- **Full-map dialog:** complete Woven Trail miniature map with start/finish/players, a hazard/shelter/mischief legend, explicit read-only copy and two return/close affordances. The overview is useful for geographic understanding even while the live camera shows a smaller region.
- **Stats:** screenshots display **7 Solo wins and 9 finished matches** at capture time. Treat this as on-screen user-device evidence only, not independently validated save/store transactions.

## Highest-value corrections made

1. **Home expand affordance:** replace the crowded `Nearby play · Appearance · More options ↓` button caption with **`More options ↓`**. The expanded caption stays `Hide more options ↑`. This reduces unwanted wrapping/clipping and keeps the advanced section discoverable without moving Start or adding another toolbar.
2. **Switch colors:** apply one explicit, coherent woodland material palette to the Tactical and Fair Finish switches in **both states**. Forest-green/cream ON, cream and muted bark/gold OFF. This eliminates the default Material lavender off-state visible on the actual phone.
3. **Toggle accessibility:** give the two experimental switches explicit screen-reader descriptions. The options remain optional, exactly the same state/setting keys and rules; no modal onboarding or extra step.

## Open visual/playtest questions — do not mistake these for confirmed bugs

- **Live camera context:** closeups can fill the screen with one junction, one piece, or isolated scenery. The current preset deliberately zooms about 1.82–2.12× because a prior 1.15–1.28× view made pieces illegible. Screenshots alone cannot establish whether this is *automatic Follow* framing or a deliberate pinch/pan. **Do not reduce camera zoom blindly** and risk making token selection and route markers harder to read. On next device playtest compare automatic Follow in ROLL, ROUTE, and ENCOUNTER against manual pinch; check that the focus indicator and full-map escape remain easy to find.
- **Board topology readability:** some colored trails and node badges are fairly fine in the overview. Check whether first-time players can point to the two meaningful alternate routes and understand the amber/green/purple legend without already knowing the map.
- **Full-map panel:** both top `Close map` and bottom `BACK TO THE TRAIL` dismiss. This is minor redundancy, not a gameplay blocker; the lower action is reachable for one-handed use. Defer removal until real usability feedback.
- **Gameplay copy:** `Lab v5` is appropriate while experimental, but avoid exposing internal version numbers on a future first-run public surface if Fair Finish becomes an approved product choice.
- **Screen-scale QA:** first screen shows one more-options control just under the visible fold. Recheck 320/360/411 dp widths, Android font scale 1.0/1.3/1.5, gesture and TalkBack actions after the short-caption change. Other device sizes are not proven by these nine screenshots.

## Exact scope and acceptance

- Scope: **home options caption, two switch themes, two switch semantics** + this audit. No Android package, signer, game engine, RNG, board coordinates, camera/math, LAN, checkpoint, progress schema, or user profile changes.
- Gate: design data workflow and Android build/tests/lint at exact UI-81 SHA; visual screenshot still needs owner confirmation. Do not claim a new test APK until a real signed artifact is available and verified.
- Build lineage: QA1614 / consolidated [RC1 #80](https://github.com/PlayerEden/GridMates-Crossroads/pull/80) as last verified signed owner build; UI-81 draft stacked on it, `main` untouched.
