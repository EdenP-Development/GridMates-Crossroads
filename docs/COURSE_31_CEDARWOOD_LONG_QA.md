# COURSE-31 — Cedarwood Expedition (Long Race)

**November 17, 2026 — new draft course, not release-approved.**
Stacked on VISUAL-30. This does not merge anything into `main`.

## Why a longer course

The owner's QA1247 screenshot showed a **four-racer Brookside Quick match completed in 34 seconds** and four rounds. Quick works as a short race, but the game bible also calls for distinct Quick, Classic and Long experiences. Simply slowing animations would not add decisions. The new Long course provides a genuinely longer route graph.

## What is implemented

- Offline **Solo vs AI** and **Pass & Play** can now choose Quick, Classic or **Long** from one compact horizontal selector. Nearby LAN remains Classic-only until its two-phone verification.
- **Cedarwood Expedition** is a third handcrafted woodland route: **101 graph nodes, six decision crossroads**, two three-way forks, and 144 possible fork-choice combinations.
- Directed route-step lengths are **54–66 edges** from Start to Finish (depending on choices). For comparison, the unchanged Woven Trail Classic is **16–22 edges**, and Brookside Quick is **13–15 edges**.
- Routes include risky shorter shortcuts, safer detours with shelter encounters and two optional mischief lanes. Event pools reuse the existing, independently tested regional event catalogue.
- Distinct, original printed-atlas graphics show forest, river, small cottages and autumn-gold details. The dense route has thinner trail lines and smaller ordinary markers to keep critical crossroads recognizable.
- Course title, map view, results screen and save/backup recognition include Cedarwood.
- **No new odds, gameplay rules, saved-data format or mandatory account/server features.**

## Save compatibility

Existing Woven Trail and Brookside board JSON and IDs are unmodified. The app's existing known-board registry now includes Cedarwood, so the saved checkpoint's original board ID still controls restore. The manual JSON progress export/import and event fingerprint validation also recognize the new board. An older QA build will not know Cedarwood, so **do not downgrade an active Cedarwood save** unless you exported a compatible backup first.

The current public release is **not** signed or published; QA remains a separate package. No automatic migration or store readiness claim.

## Automated gates

- [ ] Run the CLI data validator on **all three** boards (previously it checked Woven only), and reject empty event pools.
- [ ] Long graph is finite, non-cyclic, has unique positions/ID routes, all forks correctly map high/low/medium risk to actual route colors and all event spaces have at least two eligible event definitions.
- [ ] 55 seeded Long matches per 2/3/4-player configuration finish without softlocks; checkpoint replay reproduces exact state. Long checkpoints must not restore against Quick or Classic.
- [ ] Existing Classic/Quick saves still resume unchanged; Long saves, export/import and zero-win records round-trip in a separate preference-store simulation.
- [ ] Repeat the balance audit over 240 fixed seeds × 3 courses × 3 seat counts = **2,160** simulated races. Inspect **mean/median dice rolls**, not imaginary wall-clock minute targets, and record the first-seat winning share.
- [ ] Signed Android QA APK compiled, unit tests/lint run, unsigned public-variant identity checked on this exact commit.

## One-phone owner test

1. Install the new signed QA build **over** your existing QA app; verify existing local wins/matches and any Classic/Quick save still appear.
2. On Home, tap **Long** and confirm the full Cedarwood name/description appears without pushing Start Journey out of sight on a normal-size screen.
3. Play a 2-person Solo and a four-player match. Test all six forks; risk explanations should be readable and paths should be visually distinct.
4. Use **Full map**, pan/zoom and double-tap Follow. The large two-loop board should be understandable even though its 101 nodes are denser than Quick.
5. Force-stop/reopen in the middle of a Long route decision, then resume. Saved route choice, player positions, counters and seed-driven next events should match.
6. Export a Long backup, import it through Android's file picker after explicit confirmation, and compare the restored position and records. Don't overwrite valuable data without first saving a separate backup.
7. Finish a Long race and compare actual minutes, number of meaningful decisions, event frequency and enjoyment against the earlier 34-second Quick run and Classic.
8. Check LAN Host/Join still explicitly uses the established Classic course; the new Long race is **not** yet available over LAN.

## Acceptance boundary

This is a new **Long playtest candidate**, not proven balanced, not guaranteed to reach the game's earlier aspirational 25–40 minute target and not an excuse to lengthen every race. The prior first-player advantage in simulated games also remains under review. Real human match length, mobile presentation, fun, accessibility and two-phone LAN remain physical test gates.

**Do not merge or publish without the owner's approval.**
