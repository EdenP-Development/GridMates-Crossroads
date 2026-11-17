# Current Crossroads Status
**Latest development branch:** `design/foundation-v0.1` (draft [PR #1](https://github.com/PlayerEden/GridMates-Crossroads/pull/1)). The branch is the current WIP source, while `main` is documentation-only until owner-approved integration.

## What exists
- Game Bible, decision log, scoped research and QA policies.
- Proposed 40-node board / 14-event fixture with deterministic Node validation.
- Android-independent Kotlin game session engine with bots, turns, dice, crossroads, region events, shields, bumps and first-to-finish logic.
- Native Android Compose board and menu for one human vs 1–3 AI, with local wins/completions and no new illustration assets.
- Consolidated unit and Android QA workflows. QA signing uses the **existing publicly known test-only Word Search key** pinned by SHA (not a private production identity).

## Status categories
- **SOURCE PRESENT:** yes (prototype implementation).
- **NODE DESIGN TESTS:** previously passed; recheck the latest commit/workflows.
- **JVM UNIT TESTS:** see exact GitHub Actions result; do not infer green from source.
- **ANDROID QA BUILD:** see [Android workflow](https://github.com/PlayerEden/GridMates-Crossroads/actions/workflows/crossroads-android-qa.yml); no automatic claim of APK availability.
- **OWNER PHONE REVIEW:** pending.
- **SAME-WIFI MULTIPLAYER:** not implemented.
- **CROSSROADS COSMETICS / THEMES:** not implemented.
- **IN-PLACE QA APK UPDATE:** not tested until a second separately built APK exists and baseline is preserved.

## What to do next
1. Inspect the latest test and Android workflow logs, repair compilation or runtime issues.
2. Produce a source-matched downloadable QA APK with package/signature/version/hash receipt.
3. Perform owner handset UI/turn-loop review, assess board legibility and AI pacing.
4. Save accepted baseline APK. Test *real older APK → newer APK* install-over and retention on an emulator and phone.
5. Improve meaningful route tradeoffs, playtest balance, then discuss existing GridMates token art.
6. Only then expand toward local Wi-Fi and themed boards.

**DO NOT** replace the public QA key with a newly generated one in an existing QA update channel, merge unfinished source to main, or claim an Android build passed merely because design validation succeeded.
