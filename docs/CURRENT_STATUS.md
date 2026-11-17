> **CAM-06B source update (Nov 16–17, 2026):** Owner's QA1057 phone screenshots confirmed initial board, follow-style close view, 1× overview and bot-turn screen. Owner chose gesture-first navigation; permanent camera controls are unwanted. On the current draft camera branch, CAM-06B now removes the floating camera bar, adds double-tap / focused-piece recentering, a saved one-time gesture hint and semantic accessibility recenter/whole-board actions. Existing pinch, drag, 1×–3× clamp and automatic following remain. A comparative UI study is in [RESEARCH_MOBILE_BOARD_GAME_UI_2026_11.md](RESEARCH_MOBILE_BOARD_GAME_UI_2026_11.md). **Source committed; new Android QA and owner's gesture acceptance pending. Prior QA1057 evidence applies only to the previous build.** No merge into main.

# GridMates: Crossroads — current status: CAM-06 / QA1057

**Verified:** Nov 16, 2026 local / Nov 17 UTC.

## Repository / app
- [`feature/dynamic-camera-v0.6`](https://github.com/PlayerEden/GridMates-Crossroads/tree/feature/dynamic-camera-v0.6) — [Draft PR #12](https://github.com/PlayerEden/GridMates-Crossroads/pull/12), stacked on [PACE-05 PR #11](https://github.com/PlayerEden/GridMates-Crossroads/pull/11), Board Motion PR #9, Turn Flow PR #8, Woven Trail PR #7 and Foundation PR #1. **Nothing merged to `main`.**
- Game: 1 local human + 1–3 AI seats; fixed Woven Trail board, 5 route choices, regional random events, shields/mischief, first-to-finish race, local stats and event journal. No actual LAN or active-match save yet.
- Last owner-provided screenshot record: **3 wins / 5 completed matches**. The app records remain on the owner's own phone; emulator tests use *separate seeded* data.

## Implemented CAM-06
- In-match camera defaults to **Follow** (~1.7x), uses currently animating token, with a smoother transition between movers. Forks widen to ~1.4x to frame immediate exits.
- **Overview** shows entire board at 1x; **Follow** recenters. Press ± or pinch (1x–3x) / drag to manually inspect; manual mode remains in place across turns until Follow is tapped.
- Gesture-centroid zoom anchoring and translation clipping are implemented as isolated camera math, independently unit-tested. Input gestures affect only the map Canvas; fixed game controls stay outside its clip area.
- Existing Inspect map dialog starts at 1x and supports zoom/pan. Camera state resets per match.
- No change to game RNG, player positions, dice, event weights, match results or SharedPreferences profile.

## Verified
- [Board data QA PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44013937468).
- [Camera + gameplay JVM tests PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44013942279), including zoom clamps, anchored pinch math, viewport bounds, fork framing and follow-the-last-mover logic.
- [**Signed QA1057 Android build PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44013937297): source commit `b30f5769659e8d08a4e7865a26d15b56ab6c3728`, versionCode 1057, `com.playereden.gridmates.crossroads.qa`, APK SHA-256 `b42d6218d389238abcc79e1f1bbd6db05d308c1e3a01e4563f21234cbeceb180`. [APK artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44013937297/artifacts/14298072589).
- [**Real Android 15 emulator 1051→1057 install-over PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44014581112), pinned APK checksums and old user-profile test XML preserved byte-for-byte; new app launched, started a 2-seat match and rolled. [Screenshots](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44014581112/artifacts/14297388959) independently inspected: Follow is magnified around Start; Overview shows whole board; Zoom In enlarges scene; static controls remain outside board clipping.
- [Immutable QA receipt](QA_RECEIPT_CAMERA_1057_2026_11_16.md); [phone camera QA checklist](CAM_06_QA_HANDOFF.md).
- The signer is a public **test-only** QA certificate, not safe for production app releases.

## Still open
1. Owner phone installs QA1057 **over QA1051**, verifies 3 wins/5 matches (if no new matches) and tries real two-finger pinch, one-finger pan when zoomed, ± controls, Overview/Follow, camera tracking humans/bots through 3- and 4-seat matches.
2. Check 3-way Old Forest chooser is fully readable, manual camera does not auto-reset without pressing Follow, and tapping dice/choices cannot be intercepted by map gestures. Device font size/portrait responsive layouts remain to be tested.
3. Confirm no camera stutter on repeated AI turns and no unintended webpage/system zoom; accept or tune follow distance (~1.7x), fork distance (~1.4x), panning/centroid anchor.
4. Match length, event-game juice, 2–4 seat balance, original woodland illustration and future Classic/Long match courses still remain.
5. LAN, pass-and-play, user accounts, server economy, production signing and real unfinished-match saves are not part of this milestone.

**No physical QA1057 phone playtest has yet been provided**. Keep Draft PR #12 until owner review.
