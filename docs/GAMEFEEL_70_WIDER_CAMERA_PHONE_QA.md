# GAMEFEEL-70 — Wider live-camera framing / visible fork

**Status:** isolated Android QA experiment stacked on GAMEFEEL-69. Source commit does not imply a passing APK, real-device acceptance, or a merge to `main`.

## Owner evidence — November 20, 2026

The owner's QA1547 phone screenshots show a materially stronger woodland tabletop: large animal pieces, wooden roads and hubs, illustrated encounter card, green/cream/gold controls. **Preserve them.** Some active-board screenshots, however, devote most of the phone to an enormous grass texture or one road segment. At route decisions, the fork can sit too low behind the large bottom route-choice sheet, making the player choose from text rather than the physical board.

The owner's full-map screenshot shows **RETURN TO ROUTE CHOICES** in the context of an active junction. This is expected copy; it is not evidence of a postgame bug. The code checks the live ROUTE phase before showing that CTA.

The owner's Woven Trail Solo vs AI Playtest ended in 3 rounds / 57 seconds. This is **real play evidence for that one run only** and flags a separate, important human pacing/balance investigation; no dice or course geometry is changed in this visual milestone.

## Changes (presentation only)

- Reduce full-screen live follow zoom by approximately 11–15%, without collapsing to old tiny-piece prototypes:
  - Opening roll: 2.15x -> **1.92x**
  - Subsequent roll: 2.36x -> **2.02x**
  - Route decision: 2.12x -> **1.82x**
  - Encounter: 2.48x -> **2.12x**
  - Target interaction: 2.32x -> **2.04x**
- During live ROUTE phase, move the focused fork to **30% viewport height** (previously the normal 43%) so it sits *above* a tall choice sheet, rather than behind it.
- Keep unchanged: 1.91x illustrated animal size, 1.58x wooden trail thickness, 1.55x marker scale, landing disks, authored junctions, all artwork, player follow/manual pan/pinch, read-only 1x full-map, dice, game engine, AI, saves, host/client, app identifiers and signing.
- Unit tests pin every preset and explicitly check that route focus stays above an approximately 40% screen-height sheet. These tests do **not** replace screenshot or touch-gesture QA.

## Phone acceptance / rollback conditions

1. Exact-head Android CI: Kotlin/Compose compilation, unit tests, lint, content/provenance and public-config checks, debug signing / signed QA archive. Do not call it green until the workflow finishes.
2. Install over QA1547 without clearing records. Compare the **same** Woven Trail positions on M69 vs M70: first start, round 2, Meadow Crossing (two-way fork), Old Forest Junction (three-way fork), and shelter/hazard entry.
3. At a fork, verify the **pawn + wooden hub + two or three exit roads** are recognizable in the visible area above the bottom route sheet. If the 30% anchor is too high, clipping the HUD or hiding exit directions, revise the anchor rather than changing graph nodes.
4. At ordinary rolls, player and next 1–3 authentic landing spaces should remain legible. Keep fox/owl tokens distinguishable when stacked; no new motion jitter, manual-pan steals or lost player position.
5. Pinch, drag, double-tap to follow, reticle recenter, direct legal route taps, Full Map and postgame View Course retain their prior behavior.
6. Quick Brookside and Long Cedarwood: check path density, edge-of-map focus and tall portrait phones. Check larger system font and landscape orientation.
7. Verify full match and rematch; the outcome should still be identical for the same inputs/seed. Two physical-phone LAN test remains a separate gate.
8. **Reject or adjust** if the wider angle makes animals or branch choices too small, or puts any active pawn underneath the top HUD or bottom action tray.

## Follow-on priorities (not bundled)

- **P0 gameplay pacing**: log 3–5 real Classic races: seconds, rounds, route decisions, encounter decisions, why the first racer finished, and how often Steady vs Push wins. Avoid artificial delays or untested balance changes.
- **P1 presentation**: tabletop die feedback and a brief victory celebration/results treatment; preserve accessibility and fast rematch.
- **P1 UX**: make postgame map-to-results return intuitive if phone testers report confusion (current CTA logic is phase-aware).
- **P0 release gates**: two-phone LAN, resume/install-over, large-font accessibility, formal PR integration only with owner approval.
