# First completed owner-phone Woven Trail match — usability QA
**Observed:** screenshots supplied by the owner in ChatGPT on 2026-11-16 (US Eastern). This report records what is visibly supported, not a full instrumented playtest. Phone screenshot bytes are **not** published to this public repository.

## Direct observations
- The owner launched the Woven Trail board, chose **three players** (one human, two AI), rolled the die, saw the Meadow Crossing and Old Forest Junction decision screens, and completed a match.
- The player at the finish is labeled "You" and the victory screen displays **"You reached the finish and won!"**, alongside a Rematch and Back to menu button.
- Coloured movement paths and human/AI positions visibly update; the winding board is substantially more meaningful than the old three-identical-diamond fixture.
- A long scroll moved the header off-screen. In one screenshot the three-way route chooser started below the visible viewport; users would need to scroll to see all choices.
- Victory layout duplicated a second **Leave this match** button below **Back to menu**.
- Screenshots show a main-menu record of 0 wins/0 matches **before the match**. They do NOT prove the win record was saved after exit/relaunch; that remains a test.
- Game events are not showcased in the supplied screenshots and cannot be inferred to be compelling or legible. The game was not timed with a reliable clock.

## v0.3 UX proposal (PR #8)
1. Compact in-match header and player strip; separate scrollable board/encounter panel from a bounded **always-visible action area** containing Roll / Choose path / Target.
2. Keep route choice button names and explanations; show three-way choices through scrolling **inside** the bottom action area, not by scrolling the entire game away.
3. Track a bounded encounter journal in the Kotlin rules engine. Show the most recent event independently of short-lived roll messages, with optional recent history.
4. Remove duplicate finished-state exit control. Add confirmation before leaving an unfinished match, because active game state is **not** persisted.
5. Slightly slow AI automatic actions for readability without modifying random outcomes or dice.

## Required future verification
- [x] Human three-player Woven Trail match reaches a win screen on a physical phone (visual evidence).
- [ ] The new v0.3 action panel, especially three-way choices, remains reachable across small screen/large text settings.
- [ ] Latest encounter history shows AI and human event results correctly.
- [ ] Match record increments once after victory and survives app restart **and APK install-over**.
- [ ] Player can dismiss Leave confirmation and continue the same game.
- [ ] 2/3/4 player results and loss paths tested, not just a three-seat human win.
- [ ] Measured durations and route choices recorded for balance.
- [ ] Design art / token collection phase postponed until these screens behave reliably.

## Scope caution
No release decision, merge to main or public production signing is authorized. Continue from [Woven Trail QA receipt](QA_RECEIPT_WOVEN_V02_2026_11_16.md) and [UX PR #8](https://github.com/PlayerEden/GridMates-Crossroads/pull/8).
