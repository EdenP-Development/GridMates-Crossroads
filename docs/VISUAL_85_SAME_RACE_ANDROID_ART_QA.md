# VISUAL-85 — Exact APK, same-game-state woodland art review

**Status:** Draft Android 15 emulator screenshot capture. Not a production build or human art approval.

## Why this gate matters

A new backdrop should not be judged only as an isolated 1536×1536 picture. The actual game includes 38 graph nodes, carved roads, animated traveler markers, a full-screen follow camera, static walnut rim, player status plates, action tray and a Full Map inspector. The owner explicitly wants the **actual scenery and playable spaces to look larger and feel like a digital tabletop game**. An overhead scenery test is only meaningful when those real elements are layered over the board.

The signed **QA1672 APK** contains the code-drawn Woven overhead prototype and the previous visual changes. Its exact tested bytes are SHA256 `f4a27d23a7ac9ba9a8efef6b5c1fdb441c7b236bf2200398ebe95edac44ffeba`, signed as public-test-only package `com.playereden.gridmates.crossroads.qa`, derived from the source tree of VISUAL-84 head `8dcca15a72fe6e4157cc7bd930b94fedaceb9e22`. Read the exact source-matched artifact from [Android run 45072993375](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45072993375) rather than constructing an APK with a different camera or random seed.

## Evidence that the automated capture must produce

1. Install **one** exact QA1672 APK into an Android 15 Pixel 4 emulator. Reject a missing APK, mismatched receipt, versionCode, signer or SHA. Never fetch a mutable latest build.
2. Start a real default Solo versus AI **Woven Trail** game. Record the exact saved active-match checkpoint hash; do not fabricate a game board, manually draw roads, or rewrite the state for screenshots.
3. Capture the **normal live follow camera** and **Full Map** with the original Storybook art.
4. Through the in-game accessible **Match options** menu, enable `Woven board art: Overhead (QA)`. Reuse **the same game and traveler position**, no rolling, no route move and no new match. Verify the active-match checkpoint is byte-identical.
5. Capture the **normal live follow camera** and **Full Map** again. Verify the menu now says Overhead, and the two same-view PNG files are not byte-identical. Save four real Android screencaps, their dimensions, hashes, actual APK source hash and a machine-readable comparison receipt.
6. The reviewer compares route legibility, landmark size, seam/bridge placement, static walnut border, matching action card, small-screen crop and whether the artwork feels truly overhead. This is not automatically decidable from a green JUnit test or different PNG checksums.

## Limitations and follow-up

- These screenshots are **emulator graphics at one device profile**. They do not establish touch quality, physical owner-phone performance, UI readability at larger fonts, actual match pacing or approval of artistic direction.
- One game checkpoint confirms identical source state between art views. It does not retest older saves' install-over compatibility; that is a separate upgrade-QA gate.
- Do **not** change the default artwork or mark the old perspective assets obsolete without owner visual acceptance. Woven is the only opt-in experimental course; Brookside and Cedarwood remain as authored.
- New documentation/visual automation only; no changes to board nodes, rules, saved match data, rendering code, Android package ID, signing key, or permissions.
- The artifact is retained 14 days. Compare visually promptly; avoid unnecessary recurring CI runs or extra app-build minutes.

**Owner acceptance questions:** Can you see and enjoy the top-down woodland visual detail at automatic zoom? Do the crossings read like little bridges instead of accidental overlap? Are the paths/markers easily distinguished from decorations? Does the complete composition feel like a physical board game?
