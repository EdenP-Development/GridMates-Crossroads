# RESULT-13 — Compact results screen QA handoff
**November 17, 2026 · Status:** SOURCE IMPLEMENTED. Exact-head Android QA and physical phone acceptance must be verified independently.
**Branch:** `feature/results-polish-v1.2` stacked on the DICE-12 `feature/home-results-v1.1` draft.
**Source files:** `CrossroadsResults.kt`, `MainActivity.kt` (UI only).

## Why
The owner supplied QA1082 physical Android screenshots. The match end dialog was an oversized pale-purple system-looking card, leaving the **"Last recorded encounter"** header at the bottom of the content viewport while the actor/title/outcome were not visible without scrolling. Rematch was visually disconnected from View Board / Main Menu beneath it. Core gameplay and completion stats work; this is a presentation problem.

## Changes
- Replace only the match-end `AlertDialog` with an original cream/paper-colored Compose `Dialog` / `Surface`, visually consistent with the home menu, board and action panel.
- Use a compact winner banner (horizontal star/name treatment), reduced padding, and small three-column summary tiles.
- Move the **complete last recorded encounter** above the travelers section; show actor, title, and outcome together. Do not show a stranded section heading with hidden details. If no encounter exists, no empty card is rendered.
- Put completed first finisher and other players under **Travelers**; never fabricate placement rankings for those still racing.
- Keep **Rematch**, **View board** and **Main menu** below the scrollable content, always accessible when the dialog is open.
- Let the receipt scroll as necessary on short phones and large system fonts, but prefer an in-view compact summary on typical portraits.
- Preserve the actual session's frozen elapsed time, match rounds, seat count, winner, and log. No new rewards, achievements or game logic.

## Required QA
- [ ] Exact source Android build / unit / lint / signer & unique QA version passes.
- [ ] Install over QA1090 without uninstalling; existing local wins/completed and gesture hint preferences remain.
- [ ] In a 3-player game, complete a match and verify winner card, rounds/time/seats, latest encounter (whole detail) and every traveler are visible or scrollable.
- [ ] With 4 players and larger Android fonts, content scrolls without forcing buttons offscreen.
- [ ] On a game with no encounter, absence of event card leaves no empty gap.
- [ ] Finish dialog appears after piece movement, only once per match; opening/dismissing it repeatedly does not update records or elapsed time again.
- [ ] Test physical Back, View board, Main menu, and Rematch; unfinished matches are not silently saved.
- [ ] Retain risk-choice dialogs, camera gestures, AI handoffs, Cozy/Brisk dice timing and 1x-3x board zoom.
- [ ] Compare with owner QA1082 screenshot `7483.png`; confirm background and hierarchy look part of the same game.
- [ ] No change to signer, package ID, permissions, saves, gameplay code, RNG, LAN capabilities or sibling GridMates apps.

## Evidence
Previous **QA1090** passed [Android build and signer verification](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44144297702), [rules tests](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44144297650), and [design-data checks](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44144287617). These were for the previous branch. They do not verify RESULT-13. This handoff does not claim emulator or new device tests.

**No merge or release until owner approval.**
