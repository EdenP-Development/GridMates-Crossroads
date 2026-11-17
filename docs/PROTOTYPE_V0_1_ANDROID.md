# Crossroads Android Playable Experiment v0.1
**Status:** source + build verified, real 1008→1009 update passed on Android API35 emulator; owner physical-device/gameplay QA pending.

## Implemented in this experiment
- Small fixed 40-node, three-crossroads course from `data/boards/woodland_quick_example.json`.
- A shared starting node, a single finish, a roll-and-choose turn system, 2–4 total seats (one human and up to three local AI).
- Random seeded region events from `data/events/core_events_proposed.json`: forward boosts, backward setbacks, one-use shields, and targeted mischief. Event-induced movement cannot recursively trigger endless events.
- Simple vector-rendered 2D board with numeric colored tokens; button-based crossroads and target choices; no generated art.
- Profile stores win and completed-match counts locally, isolated from other GridMates games.
- All-bot simulation JUnit coverage for 2/3/4 seats, 100 seeds each.

## Not included yet
- Nearby Wi-Fi, multiple human players or pass-and-play.
- Persistent active match resume, cosmetic characters, new themes, alternate board shapes or deep gameplay balancing. An actual two-APK QA stats-preservation update **has now passed on an emulator**, but full save-schema migration is not yet tested.
- Released/public production app or production signing.
- Full accessibility and actual phone visual QA.

## Developer build
Requires JDK 17, Gradle 8.11.1 and Android SDK 35.
The *debug/QA test APK* uses Word Search's existing, intentionally **public test-only** signer from immutable sibling revision `183b8845b247a0ca40541f06e517999961600bc9`, SHA256 of the `.p12` file `cbe50b2b4a34f3a8433e28ddc0efd5307a1adf9aa91e324ca757f40c6aec91ee`. The GitHub workflow downloads and verifies this file before compiling. Manual local builds need to place the same file at `qa-signing/wordsearch-qa.p12`.

This public key is **not secure for production distribution**. The Crossroads QA package is `com.playereden.gridmates.crossroads.qa`, independent from Word Search's package and saves. Production signing and a separately signed production package are later tasks.

## Owner QA checklist
- [x] Signed QA APK downloaded and installed/launched on Android **emulator**, not yet the owner's physical phone.
- [ ] Main menu launches; choose 2, 3 and 4 players.
- [ ] Dice movement, crossroads choices, visible token updates, and random event text.
- [ ] AI turns auto advance, shield and prank behave as shown.
- [ ] First token to finish wins; rematch and return work.
- [ ] Completed stats remain after app restart.
- [x] Two exact separately built QA APKs (1008→1009) installed over one another on Android 15 emulator, preserving seeded stats file byte-for-byte. [Evidence](QA_RECEIPT_V0_1_2026_11_16.md). Physical handset and future save schema cases remain unchecked.

## Known issues / design limitations
The route balance and event frequency have not been tuned with human playtests. Compact mobile board might need pinch/zoom or adaptive node presentation. Work from this experiment rather than importing a large art package before gameplay validation.
