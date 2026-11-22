# UI-81 — Owner QA1614 visual audit (phone screenshots)
**Date:** November 22, 2026  
**Source:** nine owner-supplied screenshots captured from the installed Crossroads QA1614 Android app. This is **manual visual evidence**, not a complete device functional, usability, persistence or network acceptance report.

## What the screenshots actually show

- **Home / first viewport:** the illustrated woodland hero, Solo versus AI, two-player default, Classic/Woven Trail selected and pinned START RACE are visible. The collapsed options button is partly cut off/hidden by the dock on the initial viewport; its long string wraps to two lines.
- **Expanded secondary options:** Board appearance, Tactical choices, Lab v5 Fair Finish, Nearby Host/Join, guide, local record and the persistent Start CTA. Fair Finish ON is clearly green, but the **OFF** track/thumb shown in the companion screenshot is lavender/grey-purple despite the rest of the screen using cream/green/gold.
- **Live in-game camera and HUD:** a woodland painted world, readable wooden routes and junction discs, player-color strip, recenter button, contextual dice/route/encounter dock. The user has reached a crossroads with one remaining step and has an interactive shelter choice with SUPPLIES and COVER. Separate screenshots display the START pair of animal tokens and much closer map sections during play. **These show rendering and specific in-game states**, not completion of a full match.
- **Full-map dialog:** complete Woven Trail miniature map with start/finish/players, a hazard/shelter/mischief legend, explicit read-only copy and two return/close affordances. The overview is useful for geographic understanding even while the live camera shows a smaller region.
- **Stats:** screenshots display **7 Solo wins and 9 finished matches** at capture time. Treat this as on-screen user-device evidence only, not independently validated save/store transactions.

## Follow-up clarification from owner

The close-up images **were produced by manual pinch-to-zoom**. The owner clarified emphatically that the problem is **actual on-screen scale**: painted scenery, trails and traveler pieces look physically too small to enjoy, even when the camera is following. This requires larger rendering of the unified live board, **not merely moving or retargeting a camera**. The gameplay experience should showcase the art, with a separate whole-course overview when needed.

## Highest-value corrections made

1. **Home expand affordance:** replace the crowded `Nearby play · Appearance · More options ↓` button caption with **`More options ↓`**. The expanded caption stays `Hide more options ↑`. This reduces unwanted wrapping/clipping and keeps the advanced section discoverable without moving Start or adding another toolbar.
2. **Switch colors:** apply one explicit, coherent woodland material palette to the Tactical and Fair Finish switches in **both states**. Forest-green/cream ON, cream and muted bark/gold OFF. This eliminates the default Material lavender off-state visible on the actual phone.
3. **Toggle accessibility:** give the two experimental switches explicit screen-reader descriptions. The options remain optional, exactly the same state/setting keys and rules; no modal onboarding or extra step.

4. **Bigger default Follow camera:** original Follow magnification was about **1.82–2.12×**, which motivated repeated manual pinching. Current test candidate is **2.42× on first ROLL, 2.52× on later ROLL, 2.62× at ENCOUNTER, 2.48× on TARGET, and deliberately wider 2.16× at a ROUTE fork**. Phase-preserving unit tests are in place.
5. **Bigger ACTUAL illustrated world:** the live board now renders its complete square painted backdrop and all route coordinates into a **1.20× larger square** via the shared `BoardWorldViewport.frame`, rather than squeezing the whole art scene into the viewport's long axis. Wooden roads, numbered animal traveler tokens and event/route markers use that same 1.20× scale. The enlarged art, camera focus, route taps and painted waypoints share the *same* coordinate frame. No saved route geometry changes. The independent unanimated Full Map is the old compact 1× view.
6. **More close inspection:** the existing pinch zoom ceiling increases from **3× to 4×**, with unchanged centroid-anchored pan and translation mathematics. Tests verify clipping/bounds, identical legal-route tap selection at maximum manual zoom, 1× Full Map, and corresponding token/trail scales.

## Open visual/playtest questions — do not mistake these for confirmed bugs

- **Closeup acceptance:** the owner confirmed previous art was genuinely too small. The new source enlarges both painted scenery and interactive visual symbols (plus follow zoom). **This combined increase is not yet verified on the owner's phone**. During the next test check artwork readability, character and route marker sizing, 2-/3-way fork awareness, start/finish visibility, recenter and manual 4× pan without losing the route-choice dock. If excessively cropped at some phases, adjust the scenic scale independently from the fork overview; do not secretly revert the art-size correction.
- **Board topology readability:** some colored trails and node badges are fairly fine in the overview. Check whether first-time players can point to the two meaningful alternate routes and understand the amber/green/purple legend without already knowing the map.
- **Full-map panel:** both top `Close map` and bottom `BACK TO THE TRAIL` dismiss. This is minor redundancy, not a gameplay blocker; the lower action is reachable for one-handed use. Defer removal until real usability feedback.
- **Gameplay copy:** `Lab v5` is appropriate while experimental, but avoid exposing internal version numbers on a future first-run public surface if Fair Finish becomes an approved product choice.
- **Screen-scale QA:** first screen shows one more-options control just under the visible fold. Recheck 320/360/411 dp widths, Android font scale 1.0/1.3/1.5, gesture and TalkBack actions after the short-caption change. Other device sizes are not proven by these nine screenshots.

## Exact scope and acceptance

- Scope: **home options caption, two switch themes/semantics, live-world scene/art/roads/pieces/markers scale, presentation-only Follow zoom presets, manual zoom limit**, targeted geometry/unit tests and this audit. No Android package, signer, game engine, RNG, stored normalized graph coordinates, gesture centroid math, LAN, checkpoint, progress schema, or user profile changes.
- Gate: design data workflow and Android build/tests/lint at exact UI-81 SHA; visual screenshot still needs owner confirmation. Do not claim a new test APK until a real signed artifact is available and verified.
- Build lineage: QA1614 / consolidated [RC1 #80](https://github.com/PlayerEden/GridMates-Crossroads/pull/80) as last verified signed owner build; UI-81 draft stacked on it, `main` untouched.
