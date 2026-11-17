# Changelog

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