# HOME-11 — Unified start and match-finish experience

**Status:** source candidate on `feature/home-results-v1.1`; Android compiler / unit / lint / signer and physical-phone acceptance require evidence from exact head.
**Base:** [FEEL-10 draft PR #16](https://github.com/PlayerEden/GridMates-Crossroads/pull/16), signed **QA1078** CI pass. None of these changes are merged into main.

## Goal

Crossroads now has an illustrated woodland board, gesture-first camera, tabletop pawns, accurate dice, route choices and events. The old home was still a text-heavy QA form with three competing Start buttons, and the finish packed the winner, an ever-increasing elapsed timer, and both navigation buttons inside the same roll pane.

HOME-11 unifies these endpoints without inventing new modes or game mechanics.

## Source changes

### Home
- Extracted `CrossroadsHomeScreen` into `CrossroadsHome.kt`: warm cream/paper palette and a decorative miniature branching path using the **same original woodland drawing language**. It is a visual motif, **not a false preview of the exact board topology**.
- A single **Start journey** primary action. Select 2/3/4 total players with an explicitly selected chip; default is **2 total** (one human + one AI), with the number of bots plainly described.
- Display only actual local stats (`wins`, `completed`) using the same existing SharedPreferences keys; no profile conversion, login, mode gating, shop or inventorial rewards.
- Home remains vertically scrollable on short devices and advertises **offline solo vs AI QA** rather than claiming working LAN.

### Finish/results
- The current fixed bottom pane becomes concise after a win: the first finisher and a **View match results** action; no long static result details blocking the board.
- When the last movement presentation finishes, a **once-per-match dismissible results dialog** opens automatically. Players can dismiss to inspect the finished board, reopen the same results, Rematch with the same seat count, or return to the menu.
- The timer is frozen at the time the final piece finishes *presenting* its movement. It measures elapsed session time (including pauses / inspection) and **does not keep increasing when results are reopened**.
- `CrossroadsResultsContent` uses **actual snapshot values only**: first finisher, number of players, round count, elapsed time, and latest logged encounter if there is one. The winner is marked "Finished first"; other players are correctly marked **"On the trail"** instead of assigning invented second/third/fourth rankings.
- The existing finish match-record effect is not changed; wins and completions still update once per finished GameSession.
- No gameplay engine, event RNG, forks, route geometry, AI, match persistence, signer or package changes.

## Acceptance / tests

- [ ] Exact-head Android compile, JVM tests, lint and signer verification completed **successfully**.
- [ ] QA1078 → candidate APK installs over existing package, no uninstall, SharedPreferences `wins/completed` and camera-gesture hint preserved. New QA code strictly greater than 1078.
- [ ] Home on narrow Android portrait: title, woodland illustration, all three seat chips, Start journey, local record card reachable (scroll).
- [ ] Two seats default. Selecting 3 or 4 displays the corresponding bot count, starts actual 1 human + selected bots, without duplicate launches.
- [ ] All cards and text readable with large Android display/font scaling.
- [ ] Complete a real match with 2 seats: final token reaches finish before the results dialog opens; correct winner is shown and stored only once.
- [ ] Finished match with an AI winner: **no false You won**, no fabricated placement; unfinished AI/human seats are "On the trail".
- [ ] Inspect the finished board, reopen results several times: same frozen elapsed time, no additional completed-match count.
- [ ] From results choose Rematch and verify a **new session** with the same seat count; choose Main menu and verify local record increments exactly once.
- [ ] Verify the last recorded encounter display (if present) and empty-encounter screen (if none).
- [ ] The results dialog buttons work at large font; Android back dismisses results rather than losing the session.
- [ ] Existing dynamic camera, AI handoff, 3-way Old Forest chooser, branch risks, and Cozy/Brisk unaffected.

## Verification levels / cautions
- **Source:** Implemented in Compose only.
- **CI:** Pending at document creation; must be separately checked before promoting.
- **Emulator install-over / screenshot:** Not claimed by compilation or artifact publication.
- **Owner phone:** Requires actual device observations, not screenshots from older QA candidates.
- **Merge:** Keep stacked draft; don't merge or release without owner instruction.
