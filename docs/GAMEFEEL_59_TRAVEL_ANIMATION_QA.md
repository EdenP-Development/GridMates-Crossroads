# GAMEFEEL-59 — Traveler hops and arrival feedback

**November 19, 2026.** Draft stacked on GAMEFEEL-58 (visual scale/camera) and the existing isolated Crossroads PR chain. Never merge to `main` without owner approval.

## Why
The owner's real-device shots show tiny, hard-to-follow travelers and an overly passive playfield. GAMEFEEL-58 brings them into the player's view. This next stage ensures the pieces feel **animated game objects** during actual movement, not static icons teleporting from node to node. Inspiration is the *legibility and energy* of polished mobile board games, not reused images or sounds.

## Changes
- Animate a short **vertical hop** while each piece moves along the same existing graph-edge segment. A separate 0→1 progress animatable uses the **same `stepMillis`** as the existing position animatable; no extra move delay is added, and both must finish before starting the next step.
- The hop curve is `sin(πt) * 5.2 * gamePieceScale` source-canvas pixels. At t=0 and t=1 it is exactly on the painted path, with its highest rise at the midpoint. This is **visual only** and never changes legal routes, piece history or checkpoint positions.
- At the end of each *movement batch*, keep the existing 260ms landing fade but add a pale two-ring ripple around that **player's actual drawn marker**, plus a maximum 16% temporary piece overshoot that settles back to its normal size.
- The effect automatically scales with existing immersive token size and stays minimal or absent on read-only Full Map. The original board background, numbered traveler art, seat colors and graph remain unchanged.

## Risks / constraints
- Extra `Animatable` per player and two circles during the brief landing phase. No unbounded particle allocation, sound, image downloads or new files on the user's device.
- The same segment timing and turn-transition controls are preserved. Future cancellation/rematch must reset animation state with match-scoped `remember` keys and `LaunchedEffect`.
- Hit testing for board nodes and taps continues to use original unbounced graph coordinates. No effect may modify authoritative BoardDefinition, Snapshot, RNG, save or LAN packets.
- The new pure `BoardMotionAccent` tests cover hop start/end, symmetry, scale factor and invalid input, plus arrival clamp/fade behavior.

## Physical QA
1. Compare with QA 1460 and upcoming GAMEFEEL-58: a 3-player Woven Trail match with Cozy and Brisk. Each path segment should produce a subtle piece lift and land exactly on its node.
2. Travel through a fork/encounter and view the glowing landing pop: seat number, player color, trail link and nearby node must stay legible; no flicker or detached shadow.
3. Watch AI move and cancel/quit/reopen mid-animation; resume must not spawn a piece in the wrong place.
4. Try 2/4-player shared Start, Brookside and dense Cedarwood; effects must not hide nearby routes.
5. Pan/zoom while pieces travel. Camera should follow the *actual* route position rather than chase the cosmetic bob.
6. Verify existing route tap, Full Map, nearby LAN, saves, results and app pause/resume. CI cannot prove physical-device visual smoothness.
7. Android preflight, compile, unit tests, lint, signing and checksum validation must pass.

**No gameplay or release approval is claimed merely because the code was committed.**
