# FEEL-10 — Adaptive game board and tactile movement
**Status:** SOURCE COMMITTED; exact Android QA build, install-over, gesture behavior, and owner-phone acceptance need independent evidence.

## Objective
The owner wants Crossroads to feel like a mobile board game rather than a fixed technical UI. This small presentation pass ties the scenic board, camera, piece focus, and turn status together without adding rules, screens, or monetization.

## Changes
- The playable board height is now calculated from the **actual available vertical space above the action pane** in a `BoxWithConstraints`. It can grow from 220dp to 520dp instead of staying permanently 340dp.
- On regular portrait screens the surrounding column **does not vertically scroll**, to avoid competing with direct board drags. Exception: where less than 220dp remains for the board, a scrollable fallback keeps the rest of the match accessible.
- User-readable turn status and active seat bar now reflect **the visibly moving piece** during a transition, even if the authoritative state has already advanced to the next player. At the end of movement, they return to the actual next turn.
- New original programmatic `BoardTokenArt.kt` draws layered cream-ring numbered pieces with soft shadows, legible white labels, and a focused moving-piece halo. No new external art or sprite imports.
- Pieces starting on the same space are visually fanned out using the **animated on-screen positions**, not future snapshot positions. Tap-to-recenter follows the same displaced visual location.
- Crossroads entrance highlights wait until the current movement finishes, so the route does not appear selectable prematurely.
- The map-only inspection dialog retains its existing overview path and avoids gameplay transition presentation.

## Unchanged
- No updates to `core/`, board topology, RNG, movement rules, event weights, bot strategies, save keys, stats, UI design language, public QA-only signer, LAN, or account expectations.
- Gesture-first pan/pinch/double-tap/focus modes remain; one manual camera can still override automatic following.
- Cozy/Brisk timing, route chooser, journal, event reveal and authoritative die remain as in QA1074.

## Required verification
- [ ] GitHub rules/design data QA PASS.
- [ ] Exact Android debug build, unit tests, lint, package/signature check, and **signed artifact** PASS.
- [ ] QA1074→new APK installation over existing game, no clearing data; wins, completed matches, first-use camera hint preference preserved.
- [ ] Short portrait / large Android font: no inaccessible Match options, route buttons or action pane; compact-board scroll fallback still works.
- [ ] Ordinary portrait, pinch zoom and single-finger pan on board; parent page does not steal gesture when scroll is not needed.
- [ ] 2/3/4 seats at Start: numbered pieces remain legible and distinct; tapping the visually displaced active piece recenters.
- [ ] Bot turns and turn handoff: after a human or bot rolls, the header and active seat follow the piece that's still traveling, not the next actor.
- [ ] At a fork, 2- and 3-way options appear only when the piece has settled; Cliff Track, Mill Lane and Cedar Bridge remain fully readable.
- [ ] Boost, setback and prank moving a target: focus/handover and token location remain accurate, with no phantom camera snaps.
- [ ] At Finish: completion shows once, records update exactly once and persist over restart.
- [ ] Compare real phone Follow and Overview screenshots against QA1074; validate whether tokens / scenic regions are materially clearer.

## Evidence levels
- **SOURCE:** Implemented on `feature/gamefeel-v1.0`.
- **JVM and Android CI:** record exact run only after success, never infer phone results.
- **EMULATOR and install-over:** not claimed for this new APK without a dedicated verified run.
- **OWNER-PHONE:** still pending until real screenshots/gesture observations.
- **MERGE / RELEASE:** not authorized; keep draft stacked off UX-09.
