# UI-54 — Immersive Crossroads game world (QA prototype)

**November 19, 2026. Stacked draft on INPUT-53 (#53), UI-52 (#52) and earlier visuals. Keep `main` safe.**

## Direction
Unlike GridMates Sudoku and Word Search, **Crossroads is a graphical board-game experience**, not a vertically scrolling mobile productivity screen. The square illustrated board is the WORLD; HUD/controls are overlays over it.

## Implementation
1. During a match, Android status and navigation bars are hidden with transient swipe-to-show behavior. The activity returns them when leaving the match. Home, nearby lobby and system UI retain normal behavior.
2. The game board now fills its viewport behind top and bottom overlays; it is **not** a square card below a page header. Full-screen presentation crops the real 1536×1536 illustration uniformly, in portrait and landscape, rather than stretching it.
3. `BoardWorldViewport.frame` owns that source-space geometry. The rendered art, graph edges, player pieces, following camera, gesture bounds, and direct legal-route taps share the same square crop. Existing Full map inspection retains its old whole-board geometry.
4. Top scoreboard is a translucent dark HUD overlay with course/seat data and existing map/settings/pace controls.
5. Contextual actions float as a smaller rounded parchment control island (not a full-width app section). Dynamic maximum height scales with turn type; action text remains scrollable and existing choices untouched.
6. UI-53 remains available: player selects highlighted legal paths directly on the world; Compare paths opens the accessible fallback sheet. All route selection still passes through the existing game rules and host authority.

## Non-goals
No engine rules, RNG, route graphs, AI, LAN, match timing, saved data structures, family of GridMates apps, or image assets have been changed. Production release is not implied by a passed CI build.

## Automated gates
- App compiles, unit tests and lint pass; signed QA artifact and unchanged asset manifest validate.
- Square aspect and crop positions at 400×800 portrait and 800×400 landscape, classic square preview unchanged.
- Direct route hit target and screen rendering use the SAME frame with zoom/pan.
- Existing camera and input tests pass.

## Device QA / design acceptance
1. Install over the prior QA (do not uninstall) and verify saved wins and any ongoing match. Start 2-, 3-, 4-player games.
2. Confirm entire *active-match* screen is scenic world with a floating HUD and action dock; no visible page header/background bands. Home menu retains normal Android status/nav.
3. At fit, follow, and 2×–3× manual zoom, artwork is never tall-stretched; paths, node circles and pieces remain on the same landmarks.
4. Drag/pinch/double-tap-follow, tap-highlighted legal route, open Compare paths and inspect Full map, then return to game. Confirm that third routes are reachable on small portrait screens.
5. Follow AI and human travel; verify HUD controls do not obscure every fork exit. If so, adjust follow framing and dock height before approval.
6. Check screen-reader labels, larger Android text sizes, light/dark systems, transient system-bar swipe and leave-match restoration.
7. Check tapping map/options in the HUD does not accidentally select routes behind it.
8. Compare against UI-52 Build 1442 for real-device feeling, frame-rate, and legibility. Android CI does not establish visual acceptance.

**Do not merge into main or call this release-ready before owner device approval.**
