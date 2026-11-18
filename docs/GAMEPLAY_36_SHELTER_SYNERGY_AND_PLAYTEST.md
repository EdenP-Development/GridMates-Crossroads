# GAMEPLAY-36 — Shelter strategy and decision clarity (Lab v2)
**November 17–18, 2026 · Experiment, not release approval**

## Why another lab version

The owner can button-mash Crossroads; gameplay redesign is ahead of cosmetic polishing. In AGENCY-34, shortest-route strategies outperformed shelter-first; in GAMEPLAY-35, the initial Steady/Shelter policy won only **7.5%** of 4-seat Long races over one 120-seed sample, versus **29.2%** for the random human policy. These were fixed synthetic player-seat-1 trials, not human playtime or skill evidence.

Board/event inspection found a specific imbalance: the Long course's low-risk branches generally add **two spaces each** relative to the risky routes. Their shelter event pool is weighted 4/9 for +2 steps, 2/9 for +1 and 3/9 for a shield, yielding approximately **1.11 immediate extra spaces** per entered shelter. Across six junctions, the fixed detour penalty compounds; shield cards often don't translate into progress. This does not mean every player must take a shortcut, but it explains why repeated safe/Steady play struggles.

**Lab v2 hypothesis:** when a player deliberately chooses **Steady (4 guaranteed spaces)** AND commits to a **low-risk shelter route**, grant **+1 additional movement space**, once per turn, in addition to the normal single shelter draw. Push still offers higher upside and more risk; High routes still draw hazards. The bonus is a publicly labeled tactical interaction, not a hidden catch-up dice modifier.

## What actually changes
- Home **Gameplay 2.0 · Playtest v2** remains OFF by default. Existing original Quick/Classic/Long are untouched, as are old experimental Lab v1 rules.
- Every new Lab match uses the unique board identity `<base id>__decision_lab_v2`. Old Lab matches use `__decision_lab_v1` and retain their original code paths, random draws, records, and replays.
- Normal game modes, underlying board nodes, event definitions, LAN packet formats and production identifiers remain unchanged.
- Under Lab v2 only, selecting **Steady** and then entering **Shelter** adds **one movement step** in addition to the normal shelter encounter (a benefit, a shield or another reward). Route-entry encounters remain at most one per turn.
- If players instead select Push, or a high/medium risk route, the extra movement is **not** granted. Same seeded Push actions remain identical under Lab v1 and Lab v2.
- Decision panels now show **minimum remaining path spaces**, risk type and guaranteed **on-entry encounter**. For an eligible current Steady/Shelter combination, they explicitly promise **Steady trail bonus +1**. This is a shortest graph distance, **not** a prediction of dice/events or match time.
- Course banners and result titles recognize both Lab versions. Existing local wins/finished-match totals do not increment for either experimental version. Rematch from an old Lab v1 opens a new v2 experiment; the original v1 checkpoint is always recoverable *before* starting a replacement match.

## Evidence/compatibility gates
- `DecisionLabVersion2Test`: distinct board fingerprints; v1 and legacy checkpoint replay unchanged; a synthetic fork with a guaranteed shield rewards exactly one extra space to v2 Steady; Push and risk behavior remain identical; 3 courses × 2/3/4 seats × 30 seeds finish and replay.
- Android recovery tests: previously saved Lab v1 opens under v1 rules, Lab v2 checkpoints export/import and restore; normal completed/win records stay unchanged.
- `DecisionLabVersion2BalanceTest`: 3,456 synthetic races, representing 1,728 paired v1/v2 conditions across all possible human starting seats, 24 seeds, 3 courses, 2- and 4-seat lobbies, four policies (random, fast, cautious, adaptive). Measurements include human wins and dice/route counts. This removes the original audit's seat-1-only comparison, but the sample is **not sufficient to claim fairness**.
- All existing game/rules CI, Android unit tests, lint and signed QA install-over pipeline must pass on the **exact head** before an APK is called verified.

## Owner-phone playtest questions
1. Does the 2-player Quick/Classic opening now make you choose a tactic and a route for a reason, rather than automatically choosing the same button?
2. Does the bonus clearly show **why** Steady + Shelter helped? Is the route preview legible on small Android screens and three-route junctions?
3. Can the player choose Push into a risky shortcut when trailing, then Steady into a safe detour to protect a lead? Are either options obviously dominant?
4. Does Long remain engaging, or does 6 forks × repeated roll screens become tedious?
5. Does a previously saved QA1312 Lab v1 match resume unchanged? Does an experimental match leave established stats unchanged?
6. Does a high-dice move still perform its *one* advertised encounter without duplicate effects or lost steps after background/foreground?
7. How long were the real games? The original 34-second Quick playtest and simulation roll counts cannot substitute for new physical timings.

**Acceptance decision:** If cautious Long remains substantially behind random play or if first-action strategy is still unclear, **do not promote this lab**. Run another isolated versioned experiment instead of quietly editing the shipping modes. Real two-phone LAN is still separately pending. No `main` merge or public release without owner approval.
