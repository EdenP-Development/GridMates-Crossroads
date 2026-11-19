# UI-56 — Floating follow-camera HUD control

Stacked on the compact immersive HUD (#56); **draft only**, no merge into `main`.

## Owner-facing improvement
Crossroads now feels like a game world. After players drag/pinch to inspect the trail, they can tap a small gold-edged, dark-green **follow** reticle at the upper-right of the playfield to resume the camera following the current traveler. They no longer need to know the double-tap gesture to regain context.

## Scope
- A 48 dp, accessible, tappable floating control is only displayed on the *live immersive world*, not read-only Full Map.
- The control calls the exact existing camera mode/follow reset, with no changes to engine position, graph, legal route selection, dice, turn order, save files, networking, artwork or signing.
- The control sits below the top HUD, away from the right thumb's primary choices and the bottom decision tray.
- Large-screen portrait, smaller landscape and font scaling need physical verification.

## Android acceptance
- Install over previous QA without uninstalling; check saved solo records.
- Drag/pinch during a match, tap the floating reticle; camera returns to cinematic player follow. Double-tap still works.
- Tap legal highlighted trails with the new control visible, verify no accidental route selection or gesture conflicts.
- Open full-map inspection: follow reticle should **not** appear in the read-only view.
- Tap map/settings HUD and use rolling, encounters, target choices, LAN turns and results; no interaction should be swallowed by the camera control.
- Test Android accessibility label **Follow active player** and 48 dp hit size.
- Require build, unit tests, lint, signed QA and release-surface preflight. No approval until owner screenshots.
