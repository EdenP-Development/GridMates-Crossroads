# PIECES-51 — Zoom-aware, readable numbered travelers

**Real-device screenshots November 19, 2026.** Stacked on MARKERS-50, PIECES-49, UI-48, TRAILS-47. **Draft only; no merge into `main`.**

## Finding
At close zoom the forest animal player tokens look recognizable and match the scenic boards. At fit-to-board zoom (including the Full map dialog), 2–3 animal heads plus offset numeric badges appear very small and visually crowded near Start, making seat ID harder to read. This is particularly problematic for older and low-vision players. Woven Trail screenshot shows three colored players separated around Start, but still visually noisy at the smallest magnification.

The two-route choice cards look readable after UI-48; don't revisit mechanics. The illustrated waypoint art in MARKERS-50 has not yet passed Android build QA due to external Maven repository rate limiting on its first attempt. A manual rerun failed by the repository's intentional unique-QA-version-on-first-attempt rule; a **new pull request run** is required.

## Solution
- In QA, `Pieces: Woodland (zoom detail)` means **numbered color coins at camera zoom below 1.65×**, and character portraits with smaller offset number badges at zoom >=1.65×.
- `Pieces: Standard` keeps familiar numbered coins at every zoom.
- All pieces remain at the **same original authoritative board positions**, with the same collision-group offsets and gesture hit-targets. This changes only the visual depiction, never who occupies the current node.
- With a missing or corrupt token atlas, non-debug builds, or invalid zoom values, fallback is always the readable standard coin. This works for 2, 3 and 4 seats, Full map, and portrait play.
- Only the already-packaged atlas is reused; no asset replacement, license transfer, or network download. Local preference key stays `qa_woodland_pieces_v1`.
- Existing movement animations, route choices, dice rolls, saves, achievements, and LAN remain unaffected.

## Tests
- Unit tests cover 1× fit, 1.35×, 1.64×, 1.65× threshold, 3× zoom, absent art, nonfinite zoom.
- Android compile, unit tests, lint, 15-slot art provenance and package signature checks required.

## Real-phone acceptance
1. Install over QA build 1426 *without uninstalling*. Confirm previous solo wins, preferences and saved game remain.
2. Inspect 2-, 3-, and 4-player Woven at start with the Full map open: centered numbers must be prominent, easy to distinguish; portrait detail should not obstruct Start.
3. Pinch to at least 1.65× and compare characters and offset number badges. Check cluster separation on tall, small screen and across Brookside/Cedarwood.
4. With `Pieces: Standard`, verify coins remain unchanged at all zooms. Toggle during ongoing match and confirm no movement, hit target, animation or result changes.
5. Compare illustrated vs standard markers when the MARKERS-50 build is ready. No claim of owner approval until physical visual test.

**No production release claim.**
