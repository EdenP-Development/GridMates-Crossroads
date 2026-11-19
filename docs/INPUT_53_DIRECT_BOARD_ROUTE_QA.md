# INPUT-53 — Directly choose a legal route on the game board

**November 19, 2026.** Draft stacked on game-first HUD #52. No changes to `main`.

## Why
Owner reported that even with illustrated artwork, Crossroads feels like an app page with buttons rather than an actual mobile game. HUD #52 moved the board into the playfield. INPUT-53 makes that playfield useful at a crossroads.

## Experience
- When a **local human** is currently selecting a route and the previous movement is complete, **only the engine's current `availableRoutes()`** are marked with a restrained white/green, amber, or purple outline on their *actual graph edges* and exit nodes.
- Tap the highlighted road segment or its endpoint: `BoardRouteHitTest` maps the screen touch into rendered board coordinates using canvas padding, camera zoom and translation. The selected exact route ID is passed back through existing `localAction(LanAction(ROUTE, id))`. There are no new game rules, route shortcuts, or direct snapshot mutations.
- Taps close to the shared fork center, outside reach, or ambiguous between close routes do **nothing**. The alternative list of choices remains tappable and screen-reader accessible.
- The same **opt-in route comparison bottom sheet** serves two-way and three-way forks. Choose directly from the highlighted board or press `Compare 2 paths` / `Compare 3 paths` in the compact bottom HUD dock to inspect risk, distance, and the detailed descriptions before selecting a listed route.
- No player is forced to use screen-space path tapping. All legal route buttons remain accessible in the sheet, with scrolling on small phones.
- Outside the route phase, in AI turns, while an animation is moving, or when the local seat doesn't have authority, no path targets are exposed. Existing tap-to-recenter, pan, pinch and double-tap continue.
- The full-map inspection remains **read-only** and does not enable accidental path selections. The host/guest local authority checks and core engine validation are unchanged.

## Tests
- Exact destination-node hits for two legal routes.
- A segment tap between origin and exit, not just the destination dot.
- Zoom/pan coordinate mapping, fork-origin rejection, no nearby route, ambiguous overlapping routes, and invalid/non-finite camera data.
- Android compile/test/lint/QA signing + art registry checks must pass.
- QA actual handset: 2/3/4 players; two- and three-way junctions; Woven/Brookside/Cedarwood; fit zoom, double-tap follow, pinch & drag; both slow and brisk animation pacing.
- Ensure the bottom HUD never blocks route points the player must select; if it does, retain the legal choices from the sheet and adjust the camera framing in a later milestone.
- Test route sheet menu (opening, dismissal, select) plus safe click behavior during rapidly advancing AI turns and LAN host/guest.
- Install over earlier QA without uninstalling and verify saves/local records. CI cannot verify physical install-over.

## Not changed
The game engine, route graph, legal move list, dice probabilities, computer AI, events, saves, achievements, LAN protocol, artwork assets, and download requirements remain intact.

**Design QA only; nothing merged into main until physical visual approval.**
