> **Current standards (November 24, 2026):** [Eden Development Standards](https://github.com/PlayerEden/Eden-Development-Standards) adopted. Read [Crossroads signing, recovery and release audit](docs/EDEN_STANDARDS_ADOPTION.md) first for QA1700 identity and pending custody/upgrade gates. Existing packages/signers unchanged; no production signing or public release authorized. Historical status below is retained.

# GridMates: Crossroads

**Current status (November 22, 2026):** A playable native Android **QA game**, built from a continuous unmerged draft chain. Three illustrated courses (Quick, Classic, Long), Solo AI, 2–4-person local Pass & Play, the original first-to-Finish rules, optional *experimental* Tactical v4/Fair Finish v5, action-replay local saves, manual progress transfer and a same-Wi-Fi two-phone **prototype** are implemented. No mandatory account or cloud.

**Verified automated baseline:** signed **QA1613**, [Android build 45027345072](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45027345072) and [rules tests 45027345005](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45027345005) passed, including [144 seeded cross-version recovery races](docs/SAVE_78_CROSS_VERSION_RECOVERY_STRESS_QA.md). [QA-79 Android emulator install-over 45031430316](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45031430316) **passed QA1607 → QA1613**, preserving a genuine one-roll Woven Trail saved match and existing official statistics. It has **not** passed owner-phone installation, two-phone Wi-Fi play, or public-release security/signing/permissions review.

**RC1 development phase:** consolidation and focused release-candidate review—not permission to merge, publish or silently adopt the experimental finish rules. See [Current Status](docs/CURRENT_STATUS.md), [RC1 Integration Review](docs/RC1_INTEGRATION_REVIEW_2026_11_22.md), and [Start Here](docs/START_HERE.md). All existing draft PR history is preserved and `main` is untouched.

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

> **Current implementation:** deterministic Kotlin/JVM rules for 2–4 racers and **all three authored courses** (Quick, Classic, Long); Solo AI, Pass & Play, replay-safe local saves and verified manual backup transfer; optional offline v4/v5 research modes; illustrated Android UI; host-authoritative Nearby prototype. [QA1613 + QA-79 Android emulator upgrade](docs/CURRENT_STATUS.md) have passed, but physical two-phone LAN and public release acceptance have **not**. See [RC1 gates](docs/RC1_INTEGRATION_REVIEW_2026_11_22.md).

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