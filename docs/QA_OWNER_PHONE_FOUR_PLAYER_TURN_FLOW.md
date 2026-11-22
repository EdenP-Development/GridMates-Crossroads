# Owner phone QA — Turn Flow v0.3 (four-player match)
**Observed:** November 16, 2026, owner-supplied Android screenshots (9 captures) in the Game Design chat. Screenshots are not committed to this public repository. Exact package/versionCode cannot be read directly from screenshots; UI matches the QA1026 turn-flow build.

## Confirmed by phone screenshots
- The game was installed and opened at main menu with **1 win / 1 completed match**. This is consistent with the previous photographed three-player win remaining in the local record after the app update. Separate emulator 1020→1026 data-preservation test passed.
- A **4-player match** started (one human plus AI1/AI2/AI3). All four colored/numeric pieces were displayed and diverged along the handcrafted Woven Trail.
- Dice rolls moved players. **Meadow Crossing** and **Woodland Crossroads** route-choice buttons were shown inside the lower action panel.
- Positive encounters displayed in **Latest Encounter** (e.g. AI1 Rabbit's shortcut +2 spaces). The **Journey Journal** expanded to display recorded events for human and AI turns, and stayed populated through subsequent turns.
- A complete four-player race ended in **AI3 reaching the finish**. The ending displayed 'AI 3 won!', Rematch and Back to menu. This adds a visible *AI-win/loss* result to the earlier human victory evidence.
- The player header and bottom action region remained visible with the board/journal separately scrollable.

## Issues revealed
1. **P1: Copy**: 'Round 1 · You's turn' should be 'Round 1 · Your turn'.
2. **P1: Copy**: '1 wins • 1 completed matches' should use singular grammar.
3. **P1: Copy**: '1 steps remaining' should use singular grammar; also note event outcomes with 1 space.
4. **P1: Lower-panel scroll**: After a roll/fork transition, action text can appear scrolled partway up and partially cropped by the panel boundary. Keep status/title above the scrollable route options and reset list scroll when turn/phase/player changes.
5. **P2: Tokens**: Tokens and small event nodes remain visually tiny and rely heavily on color/number; larger numeric/pattern-backed pieces and board inspection/zoom need a separate gameplay polish pass. Do not indiscriminately enlarge them if crowding intersections.
6. **P2: Event feedback**: Latest encounter and journal function, but the board-game moment still lacks dice/space landing animation, event card reveal and optional subtle sound. This is a subsequent gameplay-feel milestone.

## Addressed in source after this feedback
- Turn heading pinned outside the scrolling list.
- Action-list position reset on phase/turn/current player change; 3-way decision hint.
- Pluralization corrected on home stats, remaining movement, and encounter messages.
- Encounter cards softly tinted by positive/setback/mischief type.

## Still to verify after next signed QA APK
- New layout and scroll behavior on owner's real phone, including three-way forest intersection.
- Updated QA install-over preserving real user statistics.
- Whether the main menu records the **four-player AI loss** as a completed match (wins unchanged), and whether it survives app relaunch. Pre-game screenshot cannot establish this.
- Physical accessibility on smaller screens and larger font settings; match duration and event-balance study.
- No game rules, network transport, active-match resume or cosmetics changed in this pass.
