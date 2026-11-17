# GridMates: Crossroads

**Status:** First full Woven Trail human victory observed on phone; turn-flow QA **1026** signed and emulator update **1020→1026 passed**, saved stats preserved in automated test. **QA1026 owner phone review pending.** Current draft source: `feature/turn-flow-v0.3` ([PR #8](https://github.com/PlayerEden/GridMates-Crossroads/pull/8)), built upon [PR #7](https://github.com/PlayerEden/GridMates-Crossroads/pull/7). LAN not implemented.  
**Design baseline:** 0.1.0 · 2026-11-16  
**Owner:** PlayerEden  
**Name:** *GridMates: Crossroads* is the current preferred **working** name; app-store/trademark clearance has not been performed.

> **Same destination. Different journeys.**  
> A cozy, competitive, match-based mobile board game. Everyone shares one start and one finish; route choices at crossroads, dice, and controlled region-specific events make each race different.

## Where to begin

1. Read [START_HERE](docs/START_HERE.md) for status and document ownership.
2. Read the [Game Bible](docs/GAME_BIBLE.md) for the accepted vision and explicit proposals.
3. Read [Decision Log](docs/DECISION_LOG.md) before changing rules.
4. Read the [Research Notes](docs/RESEARCH_GAMEPLAY.md) and [Platform Research](docs/RESEARCH_TECH_AND_ACCESSIBILITY.md) for evidence and references.
5. See [Roadmap](docs/ROADMAP.md), [Woven Trail v0.2](docs/BOARD_WOVEN_TRAIL_V02.md), [Verified v0.2 QA Receipt](docs/QA_RECEIPT_WOVEN_V02_2026_11_16.md), [Current Status](docs/CURRENT_STATUS.md), and [Playtest/QA](docs/QA_AND_PLAYTEST.md) for build/testing gates.

## Core pillars

- **Recognizably GridMates:** welcoming, accessible, polished, familiar, good for solo and friends in the same room.
- **A real board game:** a complete, repeatable race with a clear winner, not a persistent town or endless energy grind.
- **Decisions matter:** crossroads are the signature mechanic, not merely decorative forks.
- **Learnable boards, unpredictable encounters:** fixed handcrafted routes; events are drawn from controlled pools based on board areas.
- **Build the gameplay first:** few reusable visuals, simple pieces, no large asset-production prerequisite.
- **Independent games:** reuse approved GridMates art from Sudoku/Word Search only as source assets; Crossroads has separate saves, stats and unlock criteria.

## Initial goals / explicit non-goals

| In scope for product plan | Out of scope for initial playable |
| --- | --- |
| Android APK, offline vs AI, 2–4 seats, family-friendly play | Remote internet matchmaking, accounts/cloud |
| 2–4 players same Wi-Fi as later verified milestone | Procedural maze generation |
| One shared start and finish, visible paths, forks, events | Resource economy, property, life simulation |
| Quick / Classic / Long length options **as design targets** | Pay-to-win, energy/time gates, forced daily chores |
| Token cosmetics, theme possibilities, local achievements | Cross-app unlock sync |

> **Current implementation candidate:** Kotlin/JVM deterministic race engine with bots + Android Compose board/UI, 2–4 total seats, dice and crossroads choice. It also includes proposed region-specific events, a QA build workflow and locally saved win counts. **Owner confirmed an earlier full Woven Trail match on a physical phone; new QA1026 screen still needs owner testing. LAN, final visuals and release readiness remain unfinished.** See [Prototype status](docs/PROTOTYPE_V0_1_ANDROID.md) and [Current Status](docs/CURRENT_STATUS.md).

## Validation for included *design data*

Requires Node.js 20+ (no dependencies):

```bash
npm test
npm run validate
```

These Node checks validate proposed board/event data only. JVM game tests run through `gradle :core:test`; an Android QA workflow is available separately.

## Source projects to review before implementation

- [GridMates: Sudoku (SudokuTogether)](https://github.com/PlayerEden/SudokuTogether) — native Android patterns, branding, profile, local network, saves and art references.
- [GridMates: Word Search](https://github.com/PlayerEden/GridMates-Word-Search) — current design master, asset pipeline and board/theme direction.

The existing games' branches may differ from `main`. **Do not copy code, artwork, identifiers or reward logic blindly.** Audit the intended stable branch and asset license/provenance first.

## Repository governance

Do not merge unfinished experiments to `main`, release publicly, change shared source repos, or publish an APK without owner approval. Use feature branches and pull requests with test evidence. See [Working Agreement](CONTRIBUTING.md).

No public license has been selected; do not assume permission to redistribute this work or other GridMates assets.