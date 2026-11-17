# GridMates: Crossroads — current status: PACE-05 / QA1051

**Last verified:** November 16, 2026 local (GitHub CI results November 17 UTC).

## Repository / app
- Current source: [`feature/pace-gamefeel-v0.5`](https://github.com/PlayerEden/GridMates-Crossroads/tree/feature/pace-gamefeel-v0.5), [Draft PR #11](https://github.com/PlayerEden/GridMates-Crossroads/pull/11), stacked on [Board Motion PR #9](https://github.com/PlayerEden/GridMates-Crossroads/pull/9), Turn Flow PR #8, Woven Trail PR #7 and Foundation PR #1. **None merged to `main`.**
- Android QA only (`com.playereden.gridmates.crossroads.qa`, intentionally public test signer). One local human vs 1–3 AI; Woven Trail has 38 nodes, 5 crossroads, 14 event nodes, 24 possible routes (16–22 spaces Start-to-Finish). Dice, route choice, first-to-finish results, region event pools, shield/prank, journal and local wins/completions work.
- Real owner screenshots show 3 wins / 4 matches before a three-seat round that AI1 won, followed by **3 wins / 5 completed matches** on the menu. Three-way forest chooser was partly cut off; owner reports the flow feels too quick. [Mobile genre research and graph audit](RESEARCH_MOBILE_BOARD_GAME_PACING_AND_FEEL_2026_11.md).

## QA1051 new behavior
- **Pace: Cozy / Brisk** in match header. Cozy candidate: 205ms/space, 220ms landing, 480ms new encounter; Brisk: 95ms/space, 80ms landing, 110ms new encounter. Bot thinking pauses and die-face animations are also tuned. These are initial design hypotheses, not measured ideal values.
- Input/bot pacing uses *snapshot path differences* to wait for actual traveled steps. Game engine remains sole authoritative source of dice, events and winners. Brief landing ripple marks arrival.
- At a three-way fork, **Cliff Track, Mill Lane and Cedar Bridge** appear in a bigger scrollable chooser, with risk colors and descriptions. Dismissing reveals the board; an accessible action button reopens it. Two-option forks keep their original buttons.
- QA result panel reports elapsed wall-clock match time and rounds. It includes real-world pauses and is meant for owner comparison, not production analytics.

## Verified independently
- [Board/event-data QA PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44011268994).
- [Kotlin gameplay + pacing tests PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44011277561).
- [**Signed QA1051 Android build/lint/test PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44011268890). [GitHub artifact ZIP](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44011268890/artifacts/14295839798), containing `GridMates-Crossroads-QA-1051.apk`. Source SHA `9fa0836db9f3666de34bef57eb4a14f142c97943`, APK SHA-256 `4a9728a48551cac471ee384ebe7fa7282c36ed86f97e4fd6e9b6457b937361d7`, Android versionCode 1051.
- [**Actual Android 15 emulator QA1044→1051 install-over PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44011821841), with two independently SHA-pinned APKs, prior stats XML unchanged byte-for-byte, new app launched and board/first roll captured. [Screenshots artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44011821841/artifacts/14297176384). First-roll image visibly confirms Cozy header and both Meadow route buttons accessible.
- [Immutable QA1051 receipt](QA_RECEIPT_1051_2026_11_16.md); [phone QA checklist](PACE_05_QA_HANDOFF.md).

## Open acceptance
1. **Owner installs QA1051 over QA1044** and confirms their own stats (last observed 3 wins/5 matches if no intervening games). Test expanded Old Forest three-way chooser with all three routes, including scrolling and reopening after viewing the board.
2. Compare Cozy vs Brisk in 2/3/4-seat matches, record real elapsed time/rounds and felt speed. Ensure bots don't continue while pieces are mid-motion; confirm no double roll or hidden human choice.
3. Check prank retreat and shield feedback, map inspection during choices, leave-match confirmation, app restart/stat retention, and large-text phone behavior.
4. Research issue [PACE-05 #10](https://github.com/PlayerEden/GridMates-Crossroads/issues/10) remains open for structural Classic/Long board lengths, audio/haptics/reduced motion, plus themed art/board atmosphere. Current route length still makes Woven Trail a short match.

No physical-phone QA1051 test has been performed by owner. No LAN, active-match persistence, new mode layout, production release signer or main merge.
