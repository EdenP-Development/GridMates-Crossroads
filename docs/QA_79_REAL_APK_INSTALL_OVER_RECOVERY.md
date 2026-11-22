# QA-79 — Real Android emulator install-over: QA1607 → QA1613

**Purpose:** verify an actual APK-to-APK upgrade with a **genuine saved in-progress match**, not just successful app builds or unchanged synthetic SharedPreferences. This is an Android 15 (API 35) emulator-only acceptance gate. It does not imply physical handset compatibility or permission to ship.

## Exact immutable inputs

| Field | Previous | Candidate |
| --- | --- | --- |
| QA build | 1607 | 1613 |
| GitHub Actions run | 44971128199 | 45027345072 |
| Artifact | Crossroads-Android-QA-1607-324cf2b4232df4d36287c4134ae0ae143c172b4f | Crossroads-Android-QA-1613-4924a8862d23e0ac9ad53f2357a9bac69043fb8e |
| APK SHA-256 | b926e01bc0066a5e71140f9241f4b21f49e6aa799acaca2fef180b0679086dca | c42054e446c35e0b805ac69950e1ee478c39eb0058d32ff0290e8389f0f94779 |
| Package ID | com.playereden.gridmates.crossroads.qa | com.playereden.gridmates.crossroads.qa |
| Signer certificate SHA-256 | 75cc3c896247d71fbd51f7a3ed41f90c8218a8a5a70df8c0f6b494b5cc23d419 | same |

Both archives were already produced by passing *separate* rules/Android workflows. QA1607 is the LAN-77 source and QA1613 is the SAVE-78 source. The QA signer is a public **test-only** key, not a private production signing key.

## Emulator verification procedure

The existing `.github/workflows/crossroads-upgrade-qa.yml` now uses these exact already-built artifacts as its normal pinned defaults and runs the existing Python emulator helper.

1. Download both original GitHub artifacts directly by run and immutable artifact name. Verify **both APK files' actual SHA-256** and JSON receipt values, package, versionCode, QA-only channel and matching certificate digest.
2. Install QA1607 into a clean Android 15 emulator and initialize its real private SharedPreferences. Stop the app, then seed existing official `completed=7` and `wins=3` stats as a controlled fixture.
3. Launch QA1607, tap its actual **START RACE** action and **ROLL THE DIE**. Wait for the official rules action to finish, then force-stop. Require that the actual app committed a version-1 active-match checkpoint with a nonempty action log, `FINISHED` false, and intact official stats.
4. Perform **`adb install -r`** with QA1613. Never uninstall or clear data. Confirm stats **and exact serialized checkpoint bytes** have not changed during installation, and that the package version is now 1613.
5. Launch QA1613. Require the process to survive, a recognizable game HUD to appear instead of START RACE setup, then stop and check that the checkpoint and official counters are still unchanged.
6. Upload the resumed-screen screenshot and an independently inspectable JSON receipt with source SHAs, signer equality, checkpoint hash, action count, course, previous/new code, and explicit `owner_phone_verified=false`.

## Verified exact-head gates — November 22, 2026

**Result:** [QA-79 source head `ae114fd218e0c9323c4827bf540cfc71591bd8da`](https://github.com/PlayerEden/GridMates-Crossroads/pull/79) completed [emulator workflow 45031430316](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45031430316) **successfully**. [Design-data QA 45031430364](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45031430364) passed at that same SHA.

- [x] Source-level design validation passes on QA-79 head.
- [x] Android 15 emulator install-over workflow passes on QA-79 head.
- [x] Uploaded JSON receipt reports `status=PASS`, `resumed_in_game=true`, **7 completed / 3 wins**, and **one real saved action**.
- [x] Source-matched emulator screenshot of an in-progress Woven Trail match under QA1613 is available in the run artifact.
- [x] Test-only changes did not alter racing rules, save format, APK ID/signer, Android permissions or `main`.

**Evidence artifact:** `Crossroads-Upgrade-Emulator-1607-to-1613-ae114fd218e0c9323c4827bf540cfc71591bd8da` from the passing workflow. Earlier red/cancelled SHA runs reflected UIAutomator coordinate parsing and the system-owned fullscreen permissionless `Got it` overlay; do not confuse those superseded test-harness failures with the final verified run.

## Critical limits

- Emulator UIAutomator can depend on accessibility semantics and current controls. If the test cannot find the required controls, mark **inconclusive or failed** and correct the test/UX; do not downgrade it to a static XML-only check.
- The result does **not** validate every old checkpoint, other phones' GPU/OEM behavior, long-running persistence under storage pressure, or physical QR/NSD Wi-Fi networking.
- It tests one genuine Classic match after one roll, not a rare encounter or pending Lab v5 final round; SAVE-78's 144-match deterministic JVM stress covers those rules mechanics separately.
- All production APK promotion, real owner-device install-over, two-phone LAN play, production signing, privacy/IP/store work and owner review remain mandatory.
- Don't silently merge draft PRs, publish artifacts as a release, or touch `main`. The workflow references immutable prior QA artifacts, not an automatic latest-build detector.
