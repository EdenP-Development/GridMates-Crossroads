# TRANSFER-26 — Portable, user-controlled Crossroads progress

**November 17, 2026 — draft QA implementation, not public release authorization.**
Branch `feature/progress-transfer-v0.1`, stacked on RELEASE-25. Neither production signing nor merges to `main` are part of this milestone.

## Problem

Existing tester installs are `com.playereden.gridmates.crossroads.qa`. A future public app is `com.playereden.gridmates.crossroads`. Because Android isolates each package's private preferences, a public install **does not** automatically carry the QA app's saved race or results across. A user with long-played progress needs a portable, intentional bridge.

## Player experience

- **Export local progress:** From the main menu's **About & data**, or from **Match Options** while playing an *offline* Solo/Pass & Play race, choose an Android document destination. The app writes `GridMates-Crossroads-backup.json` using Android's system document picker.
- **Import a backup:** From the main menu **About & data**, choose a JSON backup using Android's document picker. The app validates the file and **previews its finished-match count, solo-win count and presence of a saved race** before asking for **Replace & import**. Dismissing **Keep my data** changes nothing.
- After successful import, the local record is refreshed. If the backup includes an unfinished offline race, it is restored directly with its exact course/seed/accepted actions.
- There are **no accounts or mandatory servers**. The file is plain JSON and **not encrypted**; players should keep it somewhere private. This is explicitly an **opt-in replacement**, not a merge or online leaderboards.

## Data transferred

- `completed` match count and `wins` solo count.
- Optional active offline match as its existing `MatchCheckpoint` (board ID, start timestamp, seed, accepted actions, expected snapshot).
- Last completed match sentinel and finish timestamp. Keeping both prevents a finished recovered match from incrementing counts twice.
- **Not transferred:** LAN rooms, live multiplayer state, Wi-Fi credentials, room tokens, server data (there is none), cache, theme preferences, animation speed or unrelated apps.

## Safety and compatibility

1. Strict format/version/kind check. Input limited to **512 KiB** and a serialized length cap. No arbitrary file paths or storage permissions are requested.
2. Counters must be nonnegative; `wins <= completed` and both bounded. Files with missing courses, changed event fingerprints, corrupted checkpoints or unexpected JSON fields fail import.
3. Every included race is reconstructed using `GameSession.restore`, replaying every action and comparing the final authoritative snapshot, **before** any preference writes.
4. Finished match backups need a matching last-finished sentinel/time; old QA saves missing the timestamp are frozen on export.
5. Import is a **single synchronous SharedPreferences commit**; failed validation does not mutate local data. A canceled picker/confirmation does not mutate local data.
6. The existing `crossroads_qa_profile_v1` preference filename, app package IDs, QA signing key, board definitions, rules odds and save checkpoint version remain unchanged.
7. Export is allowed during an offline game, but **not** a live LAN match; import only from home, when there is no active match. Users explicitly confirm overwrite.
8. This is a self-managed backup, **not authenticated**. A person can edit their own finished/win counts in JSON; therefore exported records must not become ranked leaderboards or paid entitlements.

## Automated acceptance

- [ ] JVM tests: Classic and Quick backups with 2/3/4 seats, correct checkpoint replay, records-only backups, corrupt/missing-course/mismatched-receipt rejection and maximum-size cap.
- [ ] Android unit tests: separate SharedPreferences instances simulate QA→production transfer, preserve count + last completion sentinel and current saved match; invalid import leaves old data untouched; records-only import intentionally clears old active match.
- [ ] Android Compose/Resources compilation, Kotlin lint and signed QA artifact on the **same PR commit**.
- [ ] Verify release-surface preflight still passes and an **unsigned** public preview still compiles with the correct name/package but is **not** distributed or signed with the QA key.

## Physical-device acceptance (one phone is enough for this milestone)

1. Install the newly signed QA APK **over** the existing QA installation, never uninstall it. Check existing statistics and saved match before doing anything.
2. During an unfinished Classic race, open Match Options → **Export progress**, save JSON to a chosen local/downloads folder. Return and confirm the game has **not** been discarded or restarted.
3. Finish a match and export records. Start a Quick match and export a separate backup; ensure the two files are distinguishable and saved privately.
4. From Home → About & data → **Import a backup**, select one. Read the preview. Choose **Keep my data** first; check that nothing changes.
5. Repeat and confirm **Replace & import**. Confirm exact counts, correct Classic/Quick board, dice/route state, and no duplicate completed results.
6. Try a malformed/wrong game/oversized file. Confirm a readable error and **unchanged** local records.
7. Verify the Android system file picker works across the target Android devices and increased font sizes.
8. Only after we produce a production-signed candidate, repeat the **QA export → production import** test. Package isolation means this cannot be inferred from two preference objects in unit tests.

## Remaining release blockers

Real two-phone LAN acceptance and LAN security, production signing, published privacy policy, accessibility/installer checks, art/IP/name rights, reliable public APK/AAB/release channel, plus explicit owner approval to merge the stacked drafts. Nothing is published and `main` remains untouched.
