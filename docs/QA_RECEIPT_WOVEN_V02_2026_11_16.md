# Crossroads Woven Trail v0.2 — QA APK and emulator update receipt
**Status: PASS for source tests, signed QA build and Android emulator 1018→1020 update. Physical-phone v0.2 review OPEN.**

## Source / download
- Handcrafted course: [`data/boards/woodland_woven_trail.json`](../data/boards/woodland_woven_trail.json). Event pool: [`data/events/woven_events_v02.json`](../data/events/woven_events_v02.json).
- Verified Android source: `9f12c6101bf50829e4935bc17ab55720969fba7a`.
- Signed APK: **`GridMates-Crossroads-QA-1020.apk`**, GitHub [Actions build #43999320965](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43999320965), [Artifact #14291053254](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43999320965/artifacts/14291053254).
- APK SHA-256: `f528d0b722363e3df8043faa4344427afad506ec29b79b212231813499dcad40`.
- QA application ID: `com.playereden.gridmates.crossroads.qa`, versionCode **1020**.
- Signer SHA-256: `75cc3c896247d71fbd51f7a3ed41f90c8218a8a5a70df8c0f6b494b5cc23d419` — a pinned **public test-only** certificate, not a production signing key.

## Test results
| Check | Result | Evidence |
|---|---|---|
| Static board/event schema and route integrity | PASS | [Design data workflow](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43999321091) |
| Kotlin game engine, bot simulation, event-pool tests | PASS | [Rules workflow](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43999346658) |
| Android lint, compile, signed APK identity and checksum | PASS | [Android workflow](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43999320965) |
| Actual `adb install -r` of distinct previous 1018 → new 1020 APKs on Android 15 emulator | PASS | [Upgrade workflow](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44000042757) |
| Local stats preservation: seeded completed=7, wins=3 file byte equality after update | PASS | Upgrade workflow |
| Updated APK launching, main menu displayed, two-seat match opened, first roll movement seen | PASS for captured emulator interaction | [Screenshots artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44000042757/artifacts/14292176348) |
| Owner phone install/visual/play-balance/complete match | **PENDING** | Requires owner device feedback |

## Previous baseline
- Verified prior QA 1018 SHA-256: `5b70cca909d6154bb5fa5cd227df6abf7a8480424da6e45cdf4776813c395d2d`.
- Baseline download [GitHub Actions #43997729223](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43997729223).
- The actual old APK was downloaded from its separate completed build artifact; not recompiled from the current source, ensuring test genuinely covered install-over identity.

## Limitations
- The test preserves the existing **stats XML**. It doesn't prove migration of cosmetics, unfinished matches or profiles that don't exist yet.
- Emulator screenshots are visual observations, not full accessibility testing or human validation.
- No LAN test or production-release readiness.
- The current event pools are hypotheses. Choice consequences are coded, but route fairness/strategy is not yet measured.
- GitHub artifacts have limited retention; retain approved long-term test APKs separately with checksums before expiry.
