# AI2-16F — Consolidated Android acceptance (QA1997)

## Verified candidate
- Branch head: 6a65edccee70e83d722ad54e1d97bd5b86fbc33c.
- Actual PR #120 synthetic merge checkout: 36a3d4c836b8563e43ab8b1bdbcc2802db5cb1a1 (base 1957dae16779edca49c9d464408f1f8ceae79ff7). This is not a main merge.
- Rules run [#455](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/46085455315): JVM rules tests, design fixtures, pinned historical replay comparison and fairness validation passed.
- Android run [#997](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/46085455388): core/app unit tests, lint, debug APK, unsigned public variant validation, branding and signed APK identity gates passed.
- [QA1997 artifact](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/46085455388/artifacts/15238793637), 20,314,205 bytes ZIP, expires 2026-12-12 UTC. ZIP SHA-256 f464e6b652623890cc115c1adfe0f6aec5375fbbf021feac6454ad307f086794. APK file GridMates-Crossroads-QA-1997.apk; package com.playereden.gridmates.crossroads.qa, versionCode 1997, versionName 0.1.0-qa. Signer certificate SHA-256 75cc3c896247d71fbd51f7a3ed41f90c8218a8a5a70df8c0f6b494b5cc23d419 verified in job 140793883389. Archive bytes not independently downloaded; APK hash is in the included receipt and .sha256 file; do not confuse archive digest with APK hash.
- 108 core flexible-start race scenarios and 36 Android JVM recovery scenarios covered; these are not physical Android tests.

## Owner acceptance — all unchecked
Use an existing QA installation only; do not uninstall or clear data. Export a private backup first, note completed/wins and current match, and verify installed version is below 1997 before upgrading. If Android rejects the update, stop rather than uninstall. Backup import replaces local progress; use a disposable QA profile/device or a backed-up QA installation, never production data.

| ID | Check | Expected |
|---|---|---|
| P01 | Install QA1997 over compatible older QA; reopen existing match | Records and match retained; branding and board render |
| P02 | Solo, More options, tactical rules + Fair finish Lab v5; choose 2/3/4 seats | Starting player and AI controls visible only in eligible setup; cycle You, active players, Random; inactive choice normalized |
| P03 | Start with a bot explicitly and with Random; note initial seat | Correct opening actor; human remains Player 1; random resolved once (not guaranteed different each race) |
| P04 | Background, force-stop, relaunch mid-race | Same match, active actor, positions and pending choice; no extra roll; resume AI only when foreground |
| P05 | Complete a Lab v5 race; choose ordinary Race again | Same initial starter and roster; new match |
| P06 | Complete races using rotate starter action, including last-seat wrap, at 2/3/4 seats | Next cyclic starter; same identities/policies; no official stat changes |
| P07 | Force-stop/reopen finished race then rotate; force-stop/reopen new rotated race | Finished state and resolved next starter preserved |
| P08 | Export local backup during a randomized/rotated match; import into backed-up QA sandbox after inspecting confirmation | Exact active match, policies and stats restored; no random redraw |
| P09 | Classic Solo and Pass & Play results; inspect Nearby where available | No experimental rotate action; ordinary rematch unchanged |
| P10 | Narrow phone, larger font, TalkBack; home/options, race results | Labels legible, actions reachable and distinct, no overlap or clipped footer |
| P11 | Consolidated woodland/branding smoke: splash, launcher, course cards, board, pawn movement, route dialog, map/results | Consistent artwork and readable tabletop UI |
| P12 | Recheck original backup and records; record device/OS/build and observed failures | No loss, no duplicate records; restore original test backup only if intended |

Record PASS/FAIL/NOT RUN with device, OS, screenshot when useful and reproduction steps. Prioritize P01–P08 and P10; P09 Nearby requires a second device if testing actual multiplayer. Do not report physical acceptance based on CI. No public release, main merge or signing change. After acceptance, review remaining AI difficulty/fairness evidence and broader player-experience/release gates before promoting experimental controls.
