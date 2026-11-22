# UI-55 — Compact overlaid match HUD

**Stacked on draft PR #54; do not merge into main without owner device approval.**

## Change
- Remove the app-like CROSSROADS / Pace toolbar from active matches.
- Keep player seats in one compact overlay row; keep the course title and Full map / match-options actions in a second compact row.
- Move Cozy / Brisk presentation speed to the match-options dropdown, preserving the existing preference key and speed behavior.
- Preserve match controls, direct route tapping, saved games, game engine and LAN protocol.

## Why
The game-world artwork should occupy the maximum visible area. A permanent branded header and tagline add height but do not help players make decisions.

## Device acceptance
- The first thing visible is the illustrated board, not a page header.
- Player identity and map/options remain immediately accessible.
- Movement speed is discoverable from the match options.
- Large-font layouts and 4-player score rows do not clip.
- Compare path, encounter, map inspection and pass-and-play still work.
- QA build, unit tests and lint must pass before distribution.
