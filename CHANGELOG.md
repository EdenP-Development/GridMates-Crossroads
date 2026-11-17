# Changelog

## [0.4.0 board motion QA 1044] — November 16/17, 2026
- QA1040 owner screenshots showed **2 wins / 3 completed games** before a new four-seat game, AI3 and human shields, a human three-space advance, and a new human victory. No post-win updated counter was shown. [Owner QA summary](docs/QA_OWNER_PHONE_1040_FULL_WIN.md).
- Added **visual-only route-following movement** over every newly traversed graph node, including reversed steps when a setback/prank moves a token back. Authoritative engine/RNG unaffected.
- Slightly enlarged numbered player pieces and active highlight, plus a read-only **Inspect map** overlay. Match-scoped animation resets on rematch.
- Added pure-Kotlin movement-frame unit tests (forward, backward, unchanged, reset), verified in [JVM QA](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44007580050).
- [Signed QA 1044 Android build PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44007530364). [Download artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44007530364/artifacts/14295396752).
- [**Real Android 15 emulator 1040→1044 install-over PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44008186807) preserving test statistics and launching a game; [screenshots](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44008186807/artifacts/14294789619).
- Owner phone motion/inspector test still pending. [QA1044 receipt](docs/QA_RECEIPT_1044_2026_11_16.md).


## [0.3.2 tabletop-feedback QA 1040] — November 16/17, 2026
- Owner QA1037 showed **1 win / 2 completed matches**, AI3 briar setback, and AI1 pranking the human back 2 spaces; the history and negative-event/Mischief cards rendered properly.
- Added a brief ~360ms visual-only dice-face animation before the **single authoritative Kotlin rules-engine roll** (no RNG or probability changes).
- Added a ~2-second nonblocking new-encounter notice in the persistent action area, while keeping the full journal/history.
- Moved Leave match outside the scrollable choice list and gave the active numbered token slightly more emphasis.
- [Design validation PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44005645444), [Kotlin rules PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44005650763), [signed QA1040 Android build PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44005645389).
- [**Actual Android 15 QA1037→1040 emulator update PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44006111587), preserving seeded stats. [Emulator UI screenshots](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44006111587/artifacts/14294641972) capture readable crossroads controls and separate Leave action.
- Owner phone QA of dice animation and event banner still pending. [Exact QA receipt](docs/QA_RECEIPT_1040_2026_11_16.md).


## [0.3.1 owner-screen polish QA 1037] — November 16/17, 2026
- Confirmed four-player AI3 victory, functioning journal, and earlier saved 1 win/1 match in owner screenshots.
- Fixed “Your turn”, singular match/win/step/encounter grammar and lower action-panel heading clipping. Actions reset to top after every game revision; route choice lists remain scrollable.
- Added subtle event-card benefit/hazard/mischief color treatment without altering the fixed board, events, dice, saved-stats keys, or signing identity.
- [Board checks PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44003804575); [JVM tests PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44003810169); [signed Android QA 1037 PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44003804869).
- [Real emulator QA1026→1037 update PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44004288480), seeded stats preserved and new route-choice UI captured.
- QA1037 owner phone review remains pending. [Full receipt](docs/QA_RECEIPT_1037_2026_11_16.md).


## [0.3.0 turn-flow QA 1026] — November 16/17, 2026
- Owner screenshots show a complete three-player Woven Trail match ending with **You won!**.
- Compact match header and player badges; independent scrolling map with persistent accessible lower action panel. Long fork choices stay inside a bounded scrollable panel.
- Bounded encounter history (latest event + optional journey journal) survives subsequent players' turns; deterministic and history-cap unit tests added.
- Unsaved match exit now requires confirmation. Victory shows Rematch and Back to menu without a duplicate Leave action.
- First crossroads route description clarified; Woven Trail topology, QA signer and private save keys unchanged.
- [Kotlin tests PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44001688525), [signed APK1026 PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44001688534), [1020→1026 emulator install-over/save retention PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44002151297).
- Phone QA1026 and human win-record retention remain open.


## [0.2.0 woven trail QA candidate] — November 16/17, 2026
- Replaced three isolated symmetric diamonds with a handcrafted **38-node maze-like board** containing five crossroads, including a three-way junction and a cross-connector.
- Added 20 proposed weighted encounters organized by region and **hazard/shelter/mischief** spaces. Choices now meaningfully change the eligible events, though match balance still needs human playtests.
- Route-specific labels, AI risk-aware routing, regional background washes, colored trail network and visible traversed paths; original v0.1 fixture retained.
- JS data validation, JVM bot/rules suite and signed QA 1020 APK build passed. The real 1018→1020 Android 15 emulator install-over preserved seeded local stats and the updated app launched.
- Captured emulator screenshots of menu/board/first roll; no owner physical-phone QA for v0.2, no full-match emulator interaction verified, no LAN or cosmetics.
- [Exact v0.2 receipt](docs/QA_RECEIPT_WOVEN_V02_2026_11_16.md).


## [0.1.1 prototype QA] — 2026-11-16/17 — First playable Android candidate

- Added Kotlin/JVM race engine, bot opponents, branch decisions and regional random events.
- Added native Android Compose board, match control, results and locally saved win/completion counts.
- Added stable public test-only QA APK build workflow using existing pinned Word Search test certificate (separate QA package).
- Built two verified QA APKs, codes 1008 and 1009, and passed actual Android 15 emulator install-over with unchanged saved stats.
- Physical game feel, LAN, cosmetic reuse and full save-schema migrations remain unverified/unimplemented.
- [Exact QA receipt](docs/QA_RECEIPT_V0_1_2026_11_16.md).


## [0.1.0] — 2026-11-16 — Design foundation

- Initial readme, living game bible, decision log, provisional rules and board/event specifications.
- Research references for Game of Life, tabletop play patterns, Android NSD, accessibility, local-network permission and game IP.
- Visual asset reuse policy, QA plan, technical architecture and staged roadmap.
- Sample graph/event JSON and structural validation with dependency-free Node tests.

**Not included:** gameplay implementation, an Android app, multiplayer transport, imported character art or APK.