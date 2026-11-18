# First physical-phone screenshots — Crossroads visual QA
**Observed:** 2026-11-16, owner-supplied screenshots from a real Android handset. The exact installed APK version and device model cannot be independently established from the images. No full match victory/restart is pictured.

## Positively shown
- The app launched and rendered a main menu with 2/3/4-player match choices, local statistics and the GridMates name.
- A two-seat game displayed a shared Start/Finish route graph and human/AI tokens.
- Consecutive screenshots show an AI token advancing across the board and the current turn changing.
- A player approached a fork and saw two route-selection buttons along with remaining die movement.
- A shield state appeared under a player's name.

## P0/P1 defects and improvements
1. **Android system insets:** Under scrolling, the Crossroads heading visibly moves underneath the status-bar icons and can clip at the top; light status bar icons blend into the cream background. Apply Android-safe-area padding and request dark system icons.
2. **Map wasted space:** The original 290dp map card holds a thin horizontal ribbon of three identical diamonds. Reduce unused card height and stretch the functional path vertically; preserve the underlying coordinates/topology.
3. **Path agency:** During route selection, the map does not emphasize the available entrances. Display upper/lower lanes distinctly and add highlight rings at the exits.
4. **Inconsistent naming:** Event messages said Player 1/Player 2 while the player row said You/AI 1. Make user-facing messages consistent.
5. **Future board identity:** The course is an acceptable movement fixture, **not yet** the pencil-and-paper-style interconnected maze/race the design bible describes. Later build a genuinely winding and less symmetrical handcrafted layout rather than assuming visual tint solves this.

## Scoped first-fix patch
- Insets handled with `WindowInsets.safeDrawing` and matching system bar icon appearance.
- Board card from 290dp to 246dp; expand original Y drawing coordinates inside card without changing rules or graph.
- Region names + color-distinguished lanes + highlighted active position and fork exits.
- Matching UI labels for you and AI.
- Keep existing local stats, gameplay event rules, seeds, app ID and public test-only QA signing identity.

## Device / gameplay gates
- [x] Owner confirmed basic install/launch and showed player movement and route-choice prompt in screenshots.
- [ ] CI Android build completes and reports exact source-matched QA APK.
- [ ] Owner visually verifies safe status bar, full board, and legible controls on new build.
- [ ] A complete owner-played match and final result are observed.
- [ ] An updated APK is installed over the previous one without losing existing stats.
- [ ] Actual game of multiple routes becomes a less repetitive maze-like design.
