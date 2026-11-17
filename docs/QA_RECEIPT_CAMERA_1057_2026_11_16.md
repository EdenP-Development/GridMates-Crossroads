# Crossroads CAM-06 — QA1057 signed APK and install-over receipt

**State:** CAMERA/GAMEPLAY TESTS PASS • ANDROID BUILD, LINT AND SIGNING PASS • REAL EMULATOR QA1051→1057 UPDATE PASS • OWNER PHONE GESTURES PENDING.

## Exact binary
- [Build #44013937297](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44013937297); [GitHub artifact #14298072589](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44013937297/artifacts/14298072589), `GridMates-Crossroads-QA-1057.apk`.
- Source commit: `b30f5769659e8d08a4e7865a26d15b56ab6c3728`. VersionCode **1057**, ID `com.playereden.gridmates.crossroads.qa`.
- APK SHA256: `b42d6218d389238abcc79e1f1bbd6db05d308c1e3a01e4563f21234cbeceb180`. Verified directly against the APK bytes, adjacent `.sha256`, and embedded receipt.
- Same public QA test signer cert SHA256 `75cc3c896247d71fbd51f7a3ed41f90c8218a8a5a70df8c0f6b494b5cc23d419` as prior QA APKs. **Not a production release key.**

## Checks
- [Board-data QA PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44013937468).
- [JVM game rules + camera geometry + movement-follow tests PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44013942279).
- [Signed Android compilation, unit tests, lint and assembly PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44013937297).
- [**Actual Android 15/API35 emulator 1051→1057 update PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44014581112): SHA-pinned distinct QA1051 APK (source `9fa0836db9f3666de34bef57eb4a14f142c97943`, SHA `4a9728a48551cac471ee384ebe7fa7282c36ed86f97e4fd6e9b6457b937361d7`) and QA1057 APK. Prior profile seeded with **7 completed matches and 3 wins** and preserved byte-for-byte by `adb install -r`. New APK launched and opened a match.
- [Emulator screenshot artifact #14297388959](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44014581112/artifacts/14297388959) has menu, match, Follow, Overview, Zoom In and first-roll screenshots. Inspected: Follow is closer around Start; Overview shows full board; Zoom In enlarges with clipped viewport; Roll/Leave controls remain in the normal lower panel.

## Limits
- Tests on emulator do **not** prove natural two-finger pinching, drag/scroll competition, smooth camera handoff, large-font usability or comfort during full-length four-seat games.
- The third forest choice, human manual-pan interaction, actual product use and shield/prank targeting need further physical-device review.
- The emulator's seeded 7/3 stats are not the owner's actual 3 wins/5 completed matches previously shown.
- The build has no online match transport, active match persistence, production signer, final art or main-branch merge.
