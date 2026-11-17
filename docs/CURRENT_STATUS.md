# Current Crossroads Status — Woven Trail v0.2 QA candidate

**Last verified:** November 16, 2026 (America/Kentucky/Louisville); CI results recorded early November 17 UTC.

## Repository state
- Active source: [`feature/woven-trail-v0.2`](https://github.com/PlayerEden/GridMates-Crossroads/tree/feature/woven-trail-v0.2), [Draft PR #7](https://github.com/PlayerEden/GridMates-Crossroads/pull/7), stacked on [foundation Draft PR #1](https://github.com/PlayerEden/GridMates-Crossroads/pull/1).
- `main` is not yet the gameplay code baseline. No owner-approved merge or public release.
- This document supersedes the earlier v0.1 handoff on this branch. Historical v0.1 receipts remain.

## Source and test evidence
- Android-independent Kotlin game engine, AI, dice, player-choice routing and first-to-finish results.
- **Handcrafted Woven Trail** with 38 nodes, 5 named crossroads (one three-way), interconnected route alternatives, one shared Start/Finish.
- 20 proposed weighted events grouped by board area and hazard/shelter/mischief pools; topology remains fixed between matches.
- Native Android 2D Compose game, colored/numeric pieces, region washes, journey trails, crossroads choice descriptions, local wins/completions.
- [JS board/event validation PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43999321091); [Kotlin/JVM tests PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43999346658).
- [Signed QA 1020 APK build PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43999320965). [Download artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/43999320965/artifacts/14291053254).
- [1018→1020 actual Android 15 emulator **install-over PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44000042757): exact SHA-checksummed APKs; 7 completed games/3 wins seeded profile XML byte-for-byte preserved; upgraded app launched. Emulator menu, board and first-roll screenshots captured [here](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44000042757/artifacts/14292176348).
- [Full immutable test receipt](QA_RECEIPT_WOVEN_V02_2026_11_16.md).

## Owner QA and limitations
- The owner previously supplied **real-phone screenshots of the old three-diamond build**, confirming initial menu, token movement and crossroads prompt. See [first owner phone audit](QA_OWNER_PHONE_SCREENSHOTS_2026_11_16.md).
- **Owner has not yet verified new Woven Trail QA 1020 on a phone.** Emulator captured a move and an AI shield; it did not complete a full automated gameplay run through a winning end-state on Android.
- Event weights, relative route win rate and Quick/Classic/Long match times are not balanced. 38-node board is a candidate, not a finished themed collection.
- No local Wi-Fi multiplayer, pass-and-play, active-match persistence, unlockable pieces, external network/cloud, production key or signed public beta.
- QA 1020 retains the old stable **public test-only signer**, package `com.playereden.gridmates.crossroads.qa`. Do not use for production.

## Next work
1. Owner installs QA 1020 over their current Crossroads QA version. Validate status-bar safe areas, 5-fork layout, readable text, human path choices, 2/3/4 AI matches and local win count.
2. Tune hazard/shelter weights and costs with match statistics. Do not promise choices are perfectly fair until simulations/human testing.
3. Fix readability/scale based on owner screenshots and test increased font sizes; consider zoom/pan only if necessary.
4. After first complete human playtest, develop same-Wi-Fi nearby play with careful state synchronization and update-safe active-match persistence.
5. Only then selectively import approved GridMates art as optional animal tokens and themed board decoration.
