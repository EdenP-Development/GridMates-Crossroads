# GAMEFEEL-71 — Tabletop die and finish-line payoff

**Status:** stacked draft above passed GAMEFEEL-70. Source edits are not a verified Android APK until exact-head CI passes. `main` untouched. Owner visual approval remains open.

## Player benefit

The roll and result are the beginning and end of each Crossroads race. The game already offers real dice preview, movement animation and a correct finished-match receipt, but the die still reads as a flat white square and the winner plaque as a plain result row. Give both moments one cohesive woodland tabletop identity **without slowing the game or changing who wins**.

## Changes

1. **Ivory wood-rim die:** reuse the original `DieFace` across die preview, roll button and in-game remaining-steps indicator. Draw a carved-looking offset shadow, pale parchment interior, outlined rim and inset forest-green pips in Canvas. Existing deterministic preview sequence, die tilt, visual timing and real roll remain unchanged.
2. Extract the exact six pip layouts into pure `TabletopDiePips.positions(face)`, clamped to 1–6. Unit tests verify 1–6 counts, uniqueness, bounds, odd center pips and clamping. **No random numbers added.**
3. **Finish seal:** replace the plain text sparkle in the existing winner card with an original, compact flag-and-rim medallion. Keep all actual winner labels, route, time and remaining-player truthfulness. The winner banner gets a restrained ~270–320 ms non-blocking alpha/scale entrance, using Compose animation handling (respects Android animator scale). No new page, mandatory wait, sound or haptics.
4. Fix the winner card's *decorative tint only* to identify a local `You` win from the reported name rather than assuming `winnerIndex == 0`. This is particularly relevant to multi-seat or remote scenarios. Winning logic and local records are unchanged.
5. Result and die copy stay accessible text, and the decorative medal itself adds no redundant screen-reader message. No new art files or modified source assets.

## Scope / exclusions

No engine, die probabilities, `Steady` or `Push` payouts, board definition, handcrafted paths, encounter/AI, network, save/replay, stats, room join, app icon, version identity, signer, new permissions, or mainline changes. M70 camera experimentation remains inherited and owner-phone pending.

## Verification matrix

- **Source / tests:** `TabletopDiceFinishTest` covers 1–6 pip coordinates, preview clamping, and winner headline/tint identity. Existing turn presentation, results and rules tests must still pass.
- **Exact-head Android QA:** compile, unit, lint, artwork/data validations, signing and archive must succeed. The PR being created is **not** evidence of a green workflow.
- **Phone acceptance:** compare Milestone 70 against 71 on 360–430dp portrait phones, large font, and landscape. At 25dp, the die's face and pips still need to read; at 66dp, the medallion/wood relief should look intentional, not muddy.
- **Full turn:** run classic single die; run experimental Steady (fixed 4) and Push (risk) once each; confirm zero additional taps/latency beyond existing preview, a single authoritative action and no duplicate haptics.
- **Finish:** win as You and lose to AI. Verify actual winner headline, winner identity, last event, exact elapsed time freeze, all traveler statuses and the Race Again / View Course / Main menu buttons immediately respond.
- **Lifecycle:** reopen results after dismissal, rotate, background/resume, reduced animations and screen reader. Check no re-counted finish receipts and no entrance animation that disables controls.
- **Offline / LAN:** repeat saved result and Pass & Play locally; two-physical-phone Wi-Fi playtest remains a separate release gate.

## Next development focus

Do **not** spin up unrelated cosmetic micro-milestones. The owner's observed Woven Trail tactical playtest finished in 57 seconds / 3 rounds (one observation). Before altering Quick/Classic/Long balance, collect several full physical matches per mode with route decisions, encounters, Steady/Push choice frequencies and winner finish causes. Evaluate if the safest or shortest lane dominates. Crossroads is intended to be a fun branch-choice race, not a tap-through timer.
