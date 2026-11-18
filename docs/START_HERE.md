# Start here — GridMates: Crossroads (November 17, 2026)

**CURRENT:** Three hand-authored race lengths (Quick / Classic / Long), Solo AI, Pass & Play, nearby two-phone LAN *prototype*, deterministic saved offline races and manual JSON backup, Android QA1270. Subsequent draft JUNCTION-32 Android build succeeded; verify the actual phone flow. A fully completed Long human session, real two-phone LAN match, public signing and production launch remain **unverified/not done**. The older camera/LAN notes below are historical.

**Start here for the active game-feel project:** [GAMEFEEL-33 Experience Bible & Roadmap](GAMEFEEL_33_EXPERIENCE_BIBLE_AND_ROADMAP.md). It incorporates previous research, current screenshots and an implementation/QA sequence. Existing detailed vision and constraints remain in [Game Bible](GAME_BIBLE.md) and [GridMates Charter](GRIDMATES_CLASSIC_GAMES_CHARTER.md).

---

# Start here — Crossroads foundation
**Status:** CAM-06 QA1057 built and verified updating over QA1051 with saved statistics preserved on an Android emulator. Dynamic camera Follow/Overview and manual zoom/pan await owner physical-phone approval. LAN is not yet implemented.

0. [Current Status](CURRENT_STATUS.md) — exact implementation/checkpoint and blockers.
1. [Game Bible](GAME_BIBLE.md) — authoritative product vision and decided-vs-proposed distinctions.
2. [Decision Log](DECISION_LOG.md) — dated user-approved choices and unresolved mechanics.
3. [Core Rules](CORE_RULES_SPEC.md) — *proposed* playtest rules for the first match.
4. [Board/Event System](BOARD_AND_EVENT_SYSTEM.md) — handcrafted routes and controlled random variation.
5. [Gameplay Research](RESEARCH_GAMEPLAY.md) / [Android Research](RESEARCH_TECH_AND_ACCESSIBILITY.md).
6. [Roadmap](ROADMAP.md) / [QA](QA_AND_PLAYTEST.md) — milestones and proof required.
7. [Asset Reuse](ASSET_REUSE.md) — independent unlocks, shared visuals.
8. [Prototype Android](PROTOTYPE_V0_1_ANDROID.md) — first engine history. [Woven Trail v0.2](BOARD_WOVEN_TRAIL_V02.md) and [Verified QA Receipt](QA_RECEIPT_WOVEN_V02_2026_11_16.md) — current source and evidence.
9. [Sibling Repo/APK Audit](GRIDMATES_SIBLING_REPO_APK_AUDIT_2026-11-16.md) / [APK Update Policy](APK_UPDATE_POLICY.md) — tested precedents and proposed install-over safety rules.

The sample data under `data/` and Node validation scripts test design integrity, not gameplay. Preserve all accepted decisions; write proposed changes to the decision log; build on separate branches; seek owner approval before merging or releasing.

**Companion repos:** [Sudoku](https://github.com/PlayerEden/SudokuTogether) and [Word Search](https://github.com/PlayerEden/GridMates-Word-Search).
