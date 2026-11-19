# UI-55 — Compact game HUD and optional information

**2026-11-19.** Draft stacked on the immersive full-screen branch UI-54 (and prior INPUT-53). Never merge into `main` without physical game-flow review.

## Owner's design goal
Crossroads must not borrow Sudoku/Word Search's page-like layout. Keep the illustrated world on-screen and let a compact HUD provide only the information relevant to playing.

## Changes
- Remove the extra "CROSSROADS · QA / Pace" title row from the match HUD. Keep a 2-row, rounded translucent overlay instead of an opaque full-width application header.
- Make each player's seat indicator a deep-green translucent HUD chip with high-contrast player name, seat color dot, focus and shield icon. Preserve the complete accessible seat/status description.
- Keep only the course title, Cozy/Brisk control, Map and options in the second row. Remove the slogan from the gameplay viewport. All original actions remain available.
- In the bottom action dock, show the real turn/phase, step count and actionable buttons by default. One discoverable `Info` / `Less` action expands/collapses the last action and full odds/instructions without altering the match.
- Encounter choices and their outcome descriptions remain visible because these are safety-critical gameplay decisions. Recovery/save failures and encounter notifications are **never hidden**.
- This is UI state only. It does not change the engine, route action IDs, player movement, network behavior, sound, core screens, art assets, or saved games.

## Android and physical acceptance
- Android compile, tests, lint, signing and manifest verification on stacked head.
- Woven, Brookside and Cedarwood, 2/3/4 players and landscape/portrait. Ensure all names/shields remain legible; chips must not occlude the active player.
- During a roll: Steady, Push and Die controls remain obvious. Info reveals exact Steady/Push odds, which must remain correct.
- At a fork: highlighted graph-legal routes stay tappable; Compare sheet remains an accessible alternative; Info must not hide or change a legal path.
- During shelter/hazard/mischief: all choices and exact effects remain visible even with Info collapsed.
- On long-action text, large Android font and small portrait devices: action dock scrolls, but never hides the **only** way to complete a turn.
- Match after pause/restore, installed over previous QA, records/saves and nearby LAN remain intact.
- Compare with UI-54's larger overlay; if this hurts touch readability or looks visually empty at zoom, revise before release.

**Owner phone QA required; do not infer art or usability acceptance from CI.**
