# Crossroads APK Update & Delivery Policy — Proposed
**Status:** proposed long-term policy; first implementation prototype now has an Android QA Gradle configuration and build workflow, but install-over/save migration remains untested.
**Evidence:** [Sibling repository audit](GRIDMATES_SIBLING_REPO_APK_AUDIT_2026-11-16.md) and official Android/GitHub docs therein.

## Goal
The owner should be able to tap a link on their Android phone, download one Crossroads QA APK, tap **Update**, and keep existing settings, statistics, unlocked board pieces, and any restorable active match state. Build/package checks must fail instead of silently publishing an incompatible APK.

## Channels and identities (candidate)
- **QA prototype (implemented configuration):** `com.playereden.gridmates.crossroads.qa`. Debug APKs use the immutable, *public test-only* QA signer already used by Word Search; the workflow pins its binary SHA and APK certificate fingerprint. It is **not confidential and not safe as a production identity**. Anyone with the public key could sign a forged QA update, so only install exact verified GitHub test artifacts. Local development with that identity is for disposable testing, not sensitive user data.
- **Production (placeholder only):** `com.playereden.gridmates.crossroads`. A **new private production signing key must be generated and protected separately** before any real public beta/store release; the public QA key must never sign production.
- **Future recommendation:** after initial testing, evaluate migrating owner QA to an independent private signing identity before long-lived/unbacked-up progression is accumulated. Changing QA signer requires controlled export/restore; don't silently make incompatible updates.
- The QA and production package IDs above are already specified in `app/build.gradle.kts`, but production signing/release is not ready.

## Version discipline
- `versionCode` is Android's ordering identifier; use a monotonically increasing **integer**, centrally allocated by *one* owner-QA workflow. No feature branch hardcodes a conflicting new code for a delivered test APK.
- `versionName` is human-readable, e.g. `0.1.0-qa.7`; no compatibility promises from the name alone.
- A workflow run reserves a new code, records its exact source SHA, and rejects reruns that attempt to publish the same code with different bytes. An unsuccessful reservation can leave a gap: **gaps are fine**.
- Keep an immutable manifest/receipt per accepted APK with signer cert hash, exact APK checksum, package ID, code, min SDK, source ref, release/tag/run URL, and QA verdict.
- Only the owner-QA workflow may produce the officially downloadable owner update. Ordinary local `assembleDebug` must not be described as compatible.

## Build gates (mandatory before giving a candidate link)
1. Test Android-free `core` engine, test bot moves and event-data schema. Run lint and Android app compile/tests.
2. Restore the *pinned public QA-only test signer* from its immutable reviewed Word Search source and verify both file SHA-256 and expected certificate SHA-256; private production keys remain separately secret. Use the same QA application ID and SDK compatibility.
3. Build exactly selected Git commit; verify built APK package, signer, `versionCode`, `minSdk`, APK hash and source commit. Fail closed on mismatch.
4. Upload APK, SHA-256 and structured receipt to GitHub Actions (temporary artifact, suggested retention 14–30 days subject to repo policy). Expose one clear download location and build number.
5. **Before endorsing an install-over:** on an Android emulator install the **last accepted actual APK**, seed profile plus one unlocked/equipped character and a resumable match, install the candidate using `adb install -r`, reboot the app and verify saved bytes or normalized state. Also prove the new APK launches and responds to gameplay inputs.
6. **Before claiming physical readiness:** owner runs representative install/update tests on their phone; local Wi-Fi multiplayer requires real 2–4 device QA.
7. Keep at least one exact previous accepted APK and source tag as a fallback. Android generally blocks straightforward downgrade and the QA data schema may have migrated: a rollback may require forward-fix or verified export/restore. Do not promise that installing an older APK will safely roll back data.

## Download experiences
**During active development:** Github Actions provides a ZIP containing the APK and receipt; signed-in GitHub access is needed. Label artifacts with game, QA code and short source SHA.

**After owner approves a real test milestone:** optionally create a GitHub **pre-release** with a versioned tag and attach a directly downloadable `.apk` and checksum/receipt. Unlike temporary Actions artifacts, release attachments persist until removed. A pre-release is still an experimental QA binary, not the Google Play production build. Keep it separate from production releases and do not auto-publish every PR.

**At a public beta:** choose appropriate production distribution (e.g. itch.io, later Play). Cross-channel install-over requires same package and compatible app signing identity, so plan this *before* the first public install. A QA `.qa` APK is an independent installation and does not install over production; export/import would be needed to move data between them.

## Simple handoff for each downloadable APK
```text
GridMates: Crossroads — QA build <code>  (<versionName>)
Source: <full SHA>  |  Test: <PASSED/FAILED/BLOCKED>
Package: com.playereden.gridmates.crossroads.qa
Signer: <certificate SHA-256>
Download: <Actions artifact or approved prerelease direct link>
SHA-256: <exact APK checksum>
Update tested: <baseline version> -> <candidate version> on <device/API>
Saved state: <what was verified>
Known limitations: <e.g. physical LAN test pending>
Install: back up if needed; install over existing QA app; do not uninstall/clear storage
```

## Scope boundaries
- No automatic update checker, Play Store enrollment, cloud accounts, server or telemetry for V1.
- No private **production** signing keystore in source, chat attachments, Actions artifacts, screenshots or pull request comments. The public test-only QA key reused here is intentionally not private.
- Don't copy another game's keystore, application ID, preferences or backup files.
- Never claim 'install-over compatible' merely because tests/build were green: verify the *final artifact* and an actual baseline-to-candidate update.
- Keep the first version enjoyable and stable before adding new boards, events or cosmetic collections.

## Implementation acceptance checklist
- [ ] Crossroads app project and unique package declared.
- [x] Stable **public test-only** QA certificate pinned to reviewed Word Search source; **not** a privately owned production/QA key.
- [ ] One canonical QA build workflow with centrally controlled version codes.
- [ ] Final APK preflight (package/signature/code/hash) implemented.
- [ ] Actual last-good APK install-over test with seeded Crossroads saves passing.
- [ ] Verified owner phone install; repeated update without clearing data.
- [ ] Artifact/receipt download works; publicly shareable prerelease only after explicit owner approval.
- [ ] Documentation points to a single latest accepted QA version and prior fallback.
