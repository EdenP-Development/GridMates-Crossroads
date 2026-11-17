# GridMates: Crossroads

**Status:** Pre-production design foundation — no playable Android build or completed LAN implementation.  
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
5. See [Roadmap](docs/ROADMAP.md) and [Playtest/QA](docs/QA_AND_PLAYTEST.md) for the next development gates.

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

> **Do not confuse planned with built.** This starter includes documentation, sample data, and validation checks only. It does NOT include game runtime, APK, tested AI, or real LAN play.

## Validation for included *design data*

Requires Node.js 20+ (no dependencies):

```bash
npm test
npm run validate
```

These checks validate proposed sample board/event data; they are **not gameplay or Android tests**.

## Source projects to review before implementation

- [GridMates: Sudoku (SudokuTogether)](https://github.com/PlayerEden/SudokuTogether) — native Android patterns, branding, profile, local network, saves and art references.
- [GridMates: Word Search](https://github.com/PlayerEden/GridMates-Word-Search) — current design master, asset pipeline and board/theme direction.

The existing games' branches may differ from `main`. **Do not copy code, artwork, identifiers or reward logic blindly.** Audit the intended stable branch and asset license/provenance first.

## Repository governance

Do not merge unfinished experiments to `main`, release publicly, change shared source repos, or publish an APK without owner approval. Use feature branches and pull requests with test evidence. See [Working Agreement](CONTRIBUTING.md).

No public license has been selected; do not assume permission to redistribute this work or other GridMates assets.