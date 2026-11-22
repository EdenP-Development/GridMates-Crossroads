# BALANCE-28 — Repeatable race-length and seat-fairness audit

**November 17, 2026.** Stacked on the unmerged RC0 review. This milestone instruments the existing engine; it does not adjust its dice, AI, events, board geometry, checkpoint fingerprints, LAN or Android interface.

## Why

Quick Brookside and Classic Woven Trail are different handcrafted courses, but route-count differences alone don't establish that Quick consistently feels shorter. AI opponents may favor shortcuts, encounters can advance or retreat, and the first seat rolls first. This is a quantitative cross-course baseline before changing rules.

## What runs

The new BalanceAuditTest generates **1,440 complete deterministic races** (240 fixed seeds × 2 boards × 2/3/4 seats), all seats controlled by the existing AI. A test failure identifies a hang, a winner not at Finish, or missing encounters. It writes:

- **crossroads_balance_samples.csv**: one row per race with course, seats, seed, winning seat, dice rolls, AI actions, encounter draws and effect categories
- **crossroads_balance_summary.txt**: each course/seat group's median/P90/min/max dice rolls, mean encounters, winner counts and Quick-vs-Classic mean dice-roll ratio

The GitHub rules workflow uploads a **Crossroads-BALANCE-28** artifact with these files. Reproduce locally with: gradle :core:test --tests com.playereden.crossroads.core.BalanceAuditTest

## Interpret carefully

- **Dice rolls aren't elapsed seconds.** The UI adds movement animations, reading and human think time.
- **AI decisions aren't human decisions.** Bots choose routes by distance and announced risk; humans may play differently.
- **Winner seat shares are descriptive**, not proof of competitive fairness. All racers are bots with the same strategy, and their starting order is fixed.
- The samples are deterministic and repeatable; unusual event chains outside the selected seeds may exist.
- A Quick/Classic duration inversion, extreme seat-1 advantage or long-play outliers are design-review findings, **not permission to quietly alter board/event definitions**. Save fingerprints would reject changed content.
- Human testing is still needed for visual clarity, pacing, and enjoyment.

## Acceptance

- [ ] Core test suite completes, including all 1,440 simulated races.
- [ ] Summary and CSV are uploaded as a GitHub Actions report artifact.
- [ ] Inspect Quick/Classic rolls and seat-win distribution; record actual results before any balance decision.
- [ ] Android app compilation remains green.
- [ ] Physical play and two-phone LAN test remain separate; no merge into main or production distribution.

Consolidated RC0 source: https://github.com/PlayerEden/GridMates-Crossroads/pull/27

This BALANCE-28 draft is stacked on RC0, not merged into it.
