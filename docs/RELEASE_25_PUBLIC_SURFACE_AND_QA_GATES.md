# RELEASE-25 — Public-facing Android identity and data notice

**Status:** Draft PR; automated QA and owner device approval are separate. This is NOT a production release, not a privacy policy, and does not authorize publishing.

## What changes

- Android's **public/release** app label is `GridMates: Crossroads` rather than `Crossroads QA`. The **debug/QA** label remains `Crossroads QA` so test installs are distinguishable.
- An original adaptive launcher icon uses GridMates woodland green and intersecting cream/gold paths; no copied external art or licensed font.
- The home screen keeps its current single-tap Solo default, course choice, Pass & Play, and Host/Join; adds an optional **About & data** dialog for transparent local-save and LAN handling.
- The in-match QA identifier and the home/nearby QA footers are **debug-only**, not public UI. The incompatible-host message now says both devices must use the same app version.
- Public setup does not claim matchmaking, cloud sync, dedicated servers, encrypted LAN or more than two real phones.

## What does NOT change

- Game rules, event probabilities, boards, dice, AI, camera, active-save checkpoint format, statistics, room protocol, LAN behavior, profile preference filename or existing QA signer.
- No production signing key is created or added to GitHub.
- No `main` merge, production APK distribution, Google Play or itch.io upload.

## Important: QA-to-production data boundary

- Existing QA application ID: `com.playereden.gridmates.crossroads.qa` using the existing public **test-only** QA signer.
- Planned production application ID: `com.playereden.gridmates.crossroads`, requiring a **private new production signer**.
- Android treats them as **separate apps**. Installing the production app **will not update** the QA app or automatically import QA wins, progress or unfinished games.
- The current `crossroads_qa_profile_v1` SharedPreferences filename is deliberately unchanged in source. It protects **QA-to-QA install-over** updates; it does not overcome distinct package sandboxes.
- Before an actual public release, decide whether QA saves need an explicit **user-initiated export/import migration**; do not silently copy data between unrelated apps, promise transfer, or wipe the QA installation.

## Honest local data disclosure

- Offline current match checkpoints and local wins/matches are stored in the app's private data.
- Nearby Host/Join uses same-Wi-Fi direct network traffic and shares host game state, including deterministic seed/action history, with the joined phone.
- The present LAN prototype is **plaintext** and suitable only for a trusted household network during testing, not public/open Wi-Fi. A room code is not strong encryption.
- There is no app account/cloud sync in the current implementation. Clearing app data or uninstalling can erase local progress.
- The optional About dialog explains this; a standalone, reviewed privacy policy and required distribution disclosures **remain outstanding**.

## Automated gate

The new `scripts/check_release_surface.py` is a static source check executed in the Android QA workflow. It fails if:
- the launcher label or icon is reverted to a QA literal/default;
- the production/QA application IDs or QA signing relationship are changed without a deliberate review;
- public build accidentally references the public test signing key;
- QA-only home/nearby/match labels cease to be scoped to debug;
- the existing preference key is renamed, risking old QA progress.

This preflight is **not** a complete Android security audit, artifact signer check, app-store compliance review, trademark clearance, two-phone test, or physical UI verification.

## Remaining actual release gates

1. Run exact-head core/Android QA, lint, icon/resource compilation and signer checks.
2. Install the latest QA APK over an older QA version; confirm that existing saved stats and unfinished Classic/Quick races are still present, then inspect launcher icon/label and all new screens at normal/large font.
3. Physically verify LAN on two real phones, including join/rejoin, turn ownership, rematch and reconnect. Repair or disable Nearby in a public release if it cannot pass.
4. Provide and review a public privacy policy, app-store information and permissions disclosure; check originality/name rights and support/contact details.
5. Configure and protect a real private production signing key. Set a deliberate production versionCode, review min/target SDK for store submission, and run a separate production package/installer test.
6. Decide on QA-to-release data migration. Production and QA package names differ, so user progress cannot magically follow a release install.
7. Consolidate the stacked draft PR history into a verified release branch only when the owner approves; no unilateral merges into `main`.
