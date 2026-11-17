# Changelog

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