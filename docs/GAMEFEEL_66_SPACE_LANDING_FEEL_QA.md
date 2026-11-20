# GAMEFEEL-66 — Every landing is a real board space

**Status:** QA-only visual polish, stacked draft on GAMEFEEL-65. No change to rules or match timing.

## Goal
Owner wants the board to dominate the experience as in a real tabletop game. After enlarging painted trails and pieces (M62), integrating the fox launcher (M63), rendering wooden landing spaces (M64), and improving framing (M65), movement should visibly feel like traveling between **individual, authored spaces** rather than a smooth slide over landscape.

## Change
- Existing authoritative per-node movement uses a `hopProgress[i]` Animatable from 0 to 1 at every board graph step. `BoardMotionAccent.stepContactPulse(progress, inMotion)` adds a gentle scale/ripple during the last ~28% of **each actual hop**, peaking at 0.48 strength and returning to zero at the start and end. It does not add new animations/timers, destinations, path IDs, RNG, side effects or haptics.
- The normal arrival ripple on completing the entire move remains; contact feedback reuses existing drawing and is limited to active animated pieces, not the static full-map inspector.
- No extra UI surface, dialog, screen obstruction, or generated artwork. The enlarged fox/owl pieces, woodland backdrop, new wooden space markers, camera changes, route tap targets, current game logic, saves and LAN are unchanged.
- New unit checks cover contact during movement, idle/off state, invalid values, and exact zero endpoints.

## Verification and visual criteria
1. Exact-head GitHub Android QA: full debug build, unit tests, lint, signing and public variant safety checks.
2. Physical phone: compare QA1518/QA1522 and this build for a 4-space Steady move, a Push move, an AI move, passage through a fork, and Long-course compact spaces. Confirm subtle contact at each node and no excessive glowing/overlap.
3. Ensure camera and dock do not obscure the actual moving piece and counting spaces remains easy.
4. Verify movement duration, route results, collisions/target choices, navigation, saves and multiplayer protocol are unchanged.
5. A passing build cannot prove feel or animation: owner phone visual approval is required. No automatic merge into `main`.
