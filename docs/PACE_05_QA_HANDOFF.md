# Crossroads PACE-05 — Cozy turn rhythm QA handoff

**State:** Development candidate on `feature/pace-gamefeel-v0.5`, [Draft PR #11](https://github.com/PlayerEden/GridMates-Crossroads/pull/11). **Do not mark the phone feel as accepted until the owner plays it.**

## Why this milestone exists
The owner tested QA1044 on Android. Screenshots confirmed **3 wins / 4 completed matches** before play, an accessible Old Forest Junction with three routes but partially hidden Cedar Bridge button, a visible Inspect map, a later AI1 victory and **3 wins / 5 completed matches** afterward. The owner reports the game feels too fast and wanted research into mobile dice/board-game pacing and game juice.

[Competitor/game-feel research](RESEARCH_MOBILE_BOARD_GAME_PACING_AND_FEEL_2026_11.md) and [PACE-05 issue](https://github.com/PlayerEden/GridMates-Crossroads/issues/10) provide design foundation.

## Source changes (test hypotheses; no rule changes)
- **Cozy / Brisk** toggle in the in-match header. Default Cozy uses **205ms per traversed node** plus ~220ms landing pause and ~480ms for a new encounter. Brisk uses 95ms/node, 80ms landing and 110ms encounter pause. AI has a small additional 180ms or 90ms breathing interval. The human die-face cycle is 10×45ms Cozy, 4×25ms Brisk. These are tuning starting points, not measured ideal values.
- **Game-authoritative movement**: a player/bot action executes once, a snapshot diff computes actual moved nodes, and new input/next bot action waits for the on-screen movement. No new RNG, no rerolls, no automatic human path decisions.
- **Landing highlight**: a short subtle pulse when numbered token reaches destination. Original recent-event reveal and journey journal remain.
- **Three-way route chooser**: an expanded, scrollable AlertDialog opens for three-choice forks, with pinned junction title and complete route descriptions. Back or “View board” hides it; the persistent action panel has a **Choose a path · 3 routes** button to reopen. The regular 2-way chooser remains in the lower action panel.
- **QA match timing**: results show wall-clock minutes/seconds since starting the session and the round count. Timing includes thinking/reading and any background interruptions; it is NOT a performance benchmark or a true active-play-only stopwatch.

## What this does NOT do
- No new artwork, new boards, new events or weights, paid dice/resources, LAN, matchmaking, sound or haptics.
- No change to 38-node Woodland Woven Trail topology, 16–22 steps per route, 2/3/4 seats, dice distribution, first-to-finish win condition, pranks/shields, or local save keys.
- No active-game save/resume. Stats remain existing `SharedPreferences` and QA signer is test-only.
- This helps **perceived** pacing only. The current Woven Trail remains structurally a short game; actual Classic and Long courses are separate future work.

## Owner test plan
1. Install candidate QA APK **over** QA1044. Verify menu still shows **3 wins / 5 completed matches** if no additional matches intervened.
2. Start a three-player game, reach **Old Forest Junction** and ensure **Cliff Track, Mill Lane, Cedar Bridge** can all be seen, selected, and reopened. Check with Android large text if practical.
3. Play **Cozy**, watch piece move every actual node and wait at destinations. Verify another click during travel cannot roll or choose routes twice. Observe AI, shield and event cards.
4. Run a comparable game in **Brisk**. Compare whether it feels comfortable, overlong, or too rushed. Timing on the result screen helps, but compare similar player counts and route decisions.
5. Trigger **Inspect map**, close it and continue the same session. Confirm Leave match remains usable and confirmation preserves game if canceled.
6. Finish a match, return to menu, and verify the completed-match counter rises by exactly one and a win rises only if human wins. Restart and check again.
7. Optional: compare two- and four-seat games, note approximate rounds/elapsed duration and event counts, and rate feel 1–5.

## Known limitations of this version
- The path’s final board geometry is still a minimalist topology drawing; larger polished artwork comes later.
- There is no universal speed/animation-off accessibility setting yet; Brisk is the faster option.
- The 3-way chooser uses a larger dialog with a scrollable middle for tiny screens; the actual user handset QA is decisive.
- Emulator screenshots prove layout/state only, **not** animation smoothness or human enjoyment.
- Maintain Draft until Android build/lint, rules tests, actual QA1044→candidate install-over test and owner phone review pass.
