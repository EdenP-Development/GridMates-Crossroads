# Crossroads current status — QA 1040 tabletop feedback

**Last verified:** November 16, 2026 local / November 17 UTC.

## Where work lives
- Source: [`feature/turn-flow-v0.3`](https://github.com/PlayerEden/GridMates-Crossroads/tree/feature/turn-flow-v0.3), [Draft PR #8](https://github.com/PlayerEden/GridMates-Crossroads/pull/8), stacked on Woven Trail Draft PR #7 and Foundation Draft PR #1. No merge to `main`; no public release.
- One human vs 1–3 AI seats; 38-node handcrafted Woven Trail, one Start and Finish, five crossroads with route-dependent region/event pools, dice and winner determination.
- Native Compose Android UI includes fixed actions, recent event and history; stats saved locally under `crossroads_qa_profile_v1`. No actual LAN or persistent unfinished-match state yet.

## Real owner-phone QA
- A prior three-player owner **win** and a four-player **AI3 win** were both confirmed by screenshots.
- The newest screenshots of QA 1037 display **1 win and 2 completed matches** on the menu; one new four-player match shows normal turn movement, AI3 encountering Tangled briars (back 2), and AI1 triggering Woodland prank against the human (back 2). This confirms visible adversarial mechanics and event presentation, but not a new final result.
- Copy, singular/plural labels, player headers and regular route choice buttons are improved. Gameplay presentation still needs richer feedback and visual identity.
- [QA1037 phone findings](QA_OWNER_PHONE_1037_EVENTS.md). Screenshot bytes remain private in conversation and are not published in GitHub.

## QA1040 source and verification
- A brief local-only dice cycling presentation appears on human Roll before the game's single deterministic RNG roll. No engine changes or new game data.
- A short (2.1 s) temporary **encounter reveal** now appears in pinned action panel for both AI and human effects, with benefit/setback/Mischief tints.
- Leave match moved outside scrollable action list; active numeric piece receives modest emphasis.
- [JS board/data QA PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44005645444), [Kotlin/JVM QA PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44005650763).
- [**Signed Android QA1040 build PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44005645389), [APK ZIP artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44005645389/artifacts/14293982913): `GridMates-Crossroads-QA-1040.apk`, source commit `7fb37fcb2621ac48ca6cc828c6f3bc6f40bdd4c6`, SHA256 `531ed9fade8d8ede415d5bc3789664bebbaa348d801f98c0c668fba79c515473`.
- [**Real 1037→1040 Android 15 emulator install-over PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44006111587), two separately built SHA-pinned APKs, seeded 7 games/3 wins profile preserved byte-for-byte, updated app launched and opened match/first roll. [Captured screenshots](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44006111587/artifacts/14294641972).
- [Immutable details](QA_RECEIPT_1040_2026_11_16.md).
- Same app identity `com.playereden.gridmates.crossroads.qa` and public test-only signer. Never use that signer for production.

## Open gates
1. Owner installs QA1040 over QA1037 and evaluates whether die reveal and encounter banner feel better without being distracting.
2. Physically test 3-way Old Forest Junction, multi-fork same roll, visible Leave control, replay and stats persistence.
3. Test unfinished-match exit confirmation and large-text accessibility.
4. Do measured route balancing and match timing before setting Quick/Classic/Long durations.
5. Then add optional original GridMates animal art and coherent woodland presentation; postpone ambitious LAN and permanent multiplayer until solo fun is established.

Do not claim a full QA1040 phone match, timed dice animation or user acceptance without owner feedback.
