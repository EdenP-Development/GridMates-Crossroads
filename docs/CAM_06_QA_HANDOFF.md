# CAM-06 — Dynamic board camera owner QA handoff
**Status:** implementation on [Draft PR #12](https://github.com/PlayerEden/GridMates-Crossroads/pull/12); build and Android install-over receipts must be checked independently. No production release authorized.

## Purpose
The owner reports Crossroads is too fast and the board/numbered pieces are tiny. A map that zooms, follows the active traveling piece and can be panned manually supports the next visual-art pass without expanding the gameplay rules.

## Implemented camera behavior (candidate)
- Initial game camera: **Follow**, ~1.7x around the traveling piece. Before a route decision, frame the junction and all its immediate exits at ~1.4x.
- During a turn transition, stay on the previous mover until travel completes. A prank can move the target rather than attacker: prioritize the actual moved piece.
- **Overview** control: zoom all the way out to 1x and show the complete handcrafted course.
- **Manual**: two-finger pinch around the gesture centroid, drag-to-pan, plus/minus zoom buttons. Press **Follow** to resume automatic focus.
- Camera zoom is clamped to **1x–3x**. Translation is bounded to avoid leaving an empty board completely offscreen.
- Existing **Inspect map** is the larger read-only inspection overlay; its board viewport can also be zoomed/panned, starting in Overview.
- The board is clipped to its own viewport: the fixed Roll, crossroad choices, Leave controls and header must never scale with the map.
- Match-scoped state: a new match starts with a fresh Follow camera. No cloud accounts, sync or save format modifications.

## Device acceptance checklist
- [ ] Install the new QA APK **over** QA1051. Don't uninstall or clear app data. Last owner record observed was **3 wins / 5 completed matches** (if no additional matches).
- [ ] Start a 3-player match. Confirm the board starts closer and can be zoomed out fully with **Overview**.
- [ ] Pinch smoothly using two fingers on the board. The point beneath fingers should stay approximately in place; the screen itself must not enlarge.
- [ ] Drag the zoomed board horizontally/vertically. It should never vanish permanently, and the game page outside the board should still scroll.
- [ ] Tap **+ / −** several times, then **Follow** to return to automatic camera. Confirm the current moving piece recenters.
- [ ] Roll Cozy or Brisk and watch the token take its actual path. The camera should remain on the moving token even if the next player becomes active.
- [ ] Reach Old Forest Junction: the camera widens around available exits; the three-choice modal still shows **Cliff Track, Mill Lane, Cedar Bridge** completely.
- [ ] Pan away manually while a bot moves. Check the app respects user control and does not snap back until Follow is tapped.
- [ ] Press **Inspect map**, pinch/drag there, close and continue the same game.
- [ ] Play through a positive boost, setback and prank/target movement if encountered; no camera animation may affect dice, player order or effects.
- [ ] Finish and check records update exactly once and persist through app restart.
- [ ] Check with four nearby tokens at Start, and ideally with larger Android font size for legibility.

## Limitations
- This is a 2D magnification/pan system, **not** free-world 3D, a minimap, or landmark artwork.
- The camera follows the actively moving *piece* using Compose animation states; still screenshots alone cannot prove smooth following or pinch response.
- Manual gestures inside a vertically scrolling page may require physical-phone tuning. Owner evaluation is essential before merging.
- Pinch/drag and zoom buttons are local camera-only; no network connections, new sprites, rule changes or new long match courses.
- Current **QA1051** is the last independently verified update until the new exact APK, test run and SHA receipt are complete.
