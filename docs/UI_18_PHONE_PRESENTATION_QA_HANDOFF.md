# UI-18 — QA1130 owner phone evidence and visual consistency

**November 17, 2026. Status:** screenshot-driven fixes committed on `feature/classic-pass-play-v1.5`; exact new Android CI and physical verification of the updated UI are pending.

## What the owner’s eight QA1130 Android screenshots actually show
1. Home title and large original woodland trail image; the home **Solo vs AI / Pass & Play** and **2 / 3 / 4 players** selectors, Start Journey, How to Play and existing local record (5 Solo wins, 7 completed at screenshot time). Selection backgrounds were **lavender** rather than woodland green.
2. A Pass & Play match with visibly separate **Player 1** (red) and **Player 2** (blue) at Round 2, and a human Meadow Crossing 2-choice route prompt. This establishes that the 2-human mode opens and displays turns and route choices. It is **not** yet proof of a full 2–4 human match, 3-way route choice by Player 2+, rematch, or process-death recovery.
3. Solo-vs-AI opening board, movement, 2-way Meadow route, 3-way Old Forest Junction choice, and finished result with winner, elapsed time, 3 seats, actual last encounter and travelers. The revised cream results card is legible and its last encounter card is now within view.
4. The Old Forest 3-choice dialog is still the default **lavender Material AlertDialog**, visually inconsistent with the cream-and-forest results screen and in-app help.
5. How to Play opens and can be scrolled to its last sections while **Got it** remains accessible, but the risk section says “Your AI rivals choose their own routes”, which is not appropriate in Pass & Play.

## UI-18 source changes
- Replace only the three-way route `AlertDialog` with a responsive cream-surface `Dialog` matching the existing results/help shell. Header and View board footer stay visible; all three route buttons scroll as needed, with a 68dp minimum and muted woodland border. No change to available route IDs or actions.
- Apply cream container colors to remaining Journey Journal, Board Overview and Leave Match `AlertDialog` instances; no extra screens or mechanics.
- Color selected home `FilterChip` selections muted sage green rather than lavender; Solo remains the default.
- Make How to Play’s risk explanation valid for **both** Solo and Pass & Play.
- No changes to board layout, dice odds, saved records, PRNG, animation sequencing, AI or LAN status.

## Acceptance checklist
- [ ] Exact latest-source Android build, JVM tests, lint and QA APK identity/signing verification passed.
- [ ] Update QA1130 **in place**, no uninstall; Solo wins, completed matches, active-match state and gesture hint stay intact.
- [ ] Solo route chooser opens all three Old Forest routes and View board stays visible; select Cliff, Mill, Cedar in separate attempts, verifying one legal route action per tap.
- [ ] In Pass & Play with Player 2+ at three-way Old Forest Junction, popup is cream instead of lavender and player can select a route.
- [ ] Large font/short screen: scroll route choices without cutting off third option or View board; hardware Back dismisses modal without leaving match.
- [ ] Home game-mode/player-count selection chips are **green/sage** and readable; other game palettes unchanged.
- [ ] Journey Journal, Board Overview and Leave Match dialogs use cream backgrounds instead of lavender.
- [ ] How to Play risk guidance describes Solo and Pass & Play accurately, closes normally.
- [ ] Finish result, saved-record counting, force-stop/recovery, rematch and solo-bot match remain unaffected.

## Evidence limit
The uploaded phone screenshots verify display of **the older QA1130 candidate**, not the new UI-18 revision. A passed Android CI workflow is not a physical device acceptance. No merge to `main` or production release without owner approval.
