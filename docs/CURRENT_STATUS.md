# Current Crossroads Status — Turn Flow v0.3 (QA 1026)
**Recorded:** November 16, 2026 local / November 17 UTC.

## Active development
- Source: [`feature/turn-flow-v0.3`](https://github.com/PlayerEden/GridMates-Crossroads/tree/feature/turn-flow-v0.3), [Draft PR #8](https://github.com/PlayerEden/GridMates-Crossroads/pull/8), stacked on [Woven Trail PR #7](https://github.com/PlayerEden/GridMates-Crossroads/pull/7) and [Foundation PR #1](https://github.com/PlayerEden/GridMates-Crossroads/pull/1). `main` has not been merged; no public release.
- One human vs 1–3 AI; one shared Start/Finish, 38 nodes and 5 crossroads; fixed map/curated random event pools.

## Evidence and working features
- **A complete three-player human victory was shown in owner-supplied phone screenshots** of Woven Trail. This establishes actual full-game flow, but not exact APK code or final stats persisted after relaunch. [Phone QA note](QA_PHONE_WOVEN_TRAIL_COMPLETE_MATCH.md).
- QA1026 introduces a compact header/player strip, independent scrollable board, **pinned bottom action controls**, latest-encounter panel, expandable journal and an exit confirmation for unsaved matches. End screen no longer has duplicate exit actions.
- Encounter history belongs to the Kotlin engine (bounded to 16, deterministic replay), so events can remain visible through subsequent AI and human rolls.
- [Design data validation PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44001664904); [JVM game and history tests PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44001688525).
- [Signed APK QA1026 PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44001688534), `com.playereden.gridmates.crossroads.qa`, QA signer unchanged. [Download direct GitHub artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44001688534/artifacts/14292043894). APK SHA-256: `8afa05795a0f00e4259d6d2d85e7a5f4b20a0e04df3160bc7899daceac8c4704`.
- [Actual QA1020→1026 emulator install-over PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44002151297): SHA-pinned previous and candidate APKs, Android 15, seeded 7 completed matches and 3 wins preserved byte-for-byte; upgraded app launched, 2-player match opened and first roll progressed. [Source-matched emulator screenshots](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44002151297/artifacts/14293056657).

## Still open
- Owner's **physical-phone** QA1026 visual/turn-flow/3-way route choice and actual install-over experience remain untested.
- The first real human victory **does not yet establish** that stats increment once or persist after phone restart.
- Emulator visual review suggests minor future copy polish: `You's turn` should be `Your turn`. No mandatory code change before owner testing.
- Future screen-size/large-text checks, event appeal, measured lengths/balance, AI fairness, character tokens, active-match persistence, pass-and-play and LAN are not yet complete.
- QA signer is deliberately public test-only, never for production.

## Next
1. Install 1026 **over** previous Crossroads QA build. Don't uninstall unless explicitly backing up and migrating progress.
2. Check that dice/crossroads/prank controls stay visible as you scroll the board. Check the three-route forest choice on your device and the event journal after AI turns.
3. Complete one match, return to menu and relaunch to verify counters and no duplicate victory recording.
4. Use owner playtest feedback for additional UI motion, events, balancing and modest visual identity. Avoid mass artwork until mechanics are satisfying.
