# GridMates: Crossroads

**Current status (November 19, 2026):** A playable native Android **QA prototype** with three illustrated woodland courses (Quick / Classic / Long), Solo AI, local Pass & Play, experimental offline encounter decisions, full-screen camera, reusable painted trails and woodland travelers, saved offline matches, and a same-Wi-Fi Host/Join **prototype**. Milestone #58's **QA1479** Android checks passed and the owner supplied real-device screenshots; the latest #59 movement-effects Android checks also passed. **EXPERIENCE-60 is the active one-turn UI integration candidate, NOT yet accepted or publicly released.** See [Project State — November 19](docs/PROJECT_STATE_2026_11_19.md) and [Experience-60 QA](docs/EXPERIENCE_60_COMPLETE_TURN_QA.md). All game code remains in **stacked draft PRs**, with no approved merge to `main`. Real two-phone LAN, physical save/update, accessibility and release/security/privacy acceptance remain outstanding.

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
5. See [Roadmap](docs/ROADMAP.md), [Current Status](docs/CURRENT_STATUS.md), [QA1051 receipt](docs/QA_RECEIPT_1051_2026_11_16.md), and [Playtest/QA](docs/QA_AND_PLAYTEST.md) for build and testing evidence.

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

> **Current implementation:** Kotlin/JVM deterministic engine, 2–4 participants, Quick and Classic boards, Solo AI, Pass & Play, local action-replay saves, and an Android Nearby Host/Join prototype with host-authoritative gameplay. Automated Android and rules checks are passing on the latest feature drafts, but full two-phone LAN and physical release acceptance remain pending. See [Course QA](docs/COURSE_24_QUICK_BROOKSIDE_QA.md) and [Release gates](docs/RELEASE_25_PUBLIC_SURFACE_AND_QA_GATES.md).

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