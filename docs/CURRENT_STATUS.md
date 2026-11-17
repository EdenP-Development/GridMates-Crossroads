# Current Crossroads Status — QA prototype
**Last reviewed:** 2026-11-16 (US Central) / early 2026-11-17 UTC.

**Current development branch:** `design/foundation-v0.1` — [Draft PR #1](https://github.com/PlayerEden/GridMates-Crossroads/pull/1). `main` remains unchanged until owner approval.

## What is genuinely verified
- Source includes pure Kotlin engine, 40-node three-crossroads board, 14 region event definitions, bot action support, and native Compose single-human/AI gameplay UI.
- JS board/event integrity checks passed.
- Kotlin unit + simulator tests passed ([rules run](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43995157738)).
- **Signed QA APK 1008** built and identity/preflight verified ([Android run](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43995150638)); 1009 also built separately ([Android run](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43995157650)).
- The **actual two distinct QA APKs** were installed 1008→1009 on an API35 Android emulator with `adb install -r`. The seeded profile save XML containing 7 completed games and 3 wins was byte-for-byte unchanged. The upgraded app launched and stayed running. [Successful regression run](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43996214349).
- One test APK SHA-256: 1008 `6cb6e89128d7f3a5ed123f38ebd5878f2775e200898f6ee8a4379dbbdf01354d`. [Full receipt](QA_RECEIPT_V0_1_2026_11_16.md).

## Not verified / not built
- **No owner phone install or match-play acceptance**, no screenshot layout audit, no measured game balance.
- **No real Wi-Fi multiplayer**, second human, pass-and-play or cloud.
- No cosmetic collection, unlocks, profile customization, active-match save/resume, themed boards or alternative match durations.
- Update regression verifies the current *statistics file* only: not future schema migrations, unfinished match saves, or paid/free cosmetics.
- Production build and signing not configured. QA signer is an existing publicly known **test-only** key from the Word Search project, pinned by SHA; never use it for a public production release.

## Next actions
1. Owner installs QA1008 on Android, runs full 2/3/4-seat matches, reports screenshots and gameplay issues.
2. Tune distinct risk levels of the upper/lower paths, match lengths and mischief, using test evidence.
3. Protect owner QA progression with backup/export before new save-schema changes; test another **actual older→newer** APK for each accepted milestone.
4. Add local Wi-Fi 2–4 human seats after solo gameplay acceptance; then reuse approved GridMates artwork as simple token pieces.
5. Keep evidence in this repo, not only in conversations.
