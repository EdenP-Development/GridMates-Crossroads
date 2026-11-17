# Woodland Crossing — Woven Trail v0.2
**State:** Source prototype on `feature/woven-trail-v0.2`. Game-balance, visuals, and phone playability need human evaluation. Do not mistake this board for a finished illustration.

## Fixed geography
- One shared Start and one shared Finish; map coordinates are hand-authored, not generated each match.
- 38 playable nodes over Meadow, Forest and Village; 5 distinct crossroads: Meadow Crossing (2 routes), Old Forest Junction (3 routes), Woodland Crossroads (2 routes), Village Crossing (2 routes), and Last Crossroads (2 routes).
- A Cedar Bridge connector from the upper forest road rejoins the lower Woodland Crossroads. All paths are one-way and eventually reach Finish; no dead ends or infinite loops in this candidate.
- Depending on decisions, the direct shortest path is about 17 nodes (including Start) and the longest is about 23. Actual match duration depends on dice, encounters and players, not raw node count.
- Both future theme variants and all play sessions must retain this board topology unless releasing a separately identified handcrafted board. **Randomness affects encounters, not route geometry.**

## Choices and eligibility pools
Each event-marked space assigns a **fixed region and pool**. The event is drawn when a player *lands* on that space, not when merely crossing it.

| Pool | Typical route | Event logic | Intended feeling |
| --- | --- | --- | --- |
| `hazard` | Short/fast | Probabilistic small advance or setback (1–3 spaces, region-dependent) | Tempting shortcut, may cost the race |
| `shelter` | Longer | Advance +1/+2 or one-use shield; no retreat in initial pool | Reliable, with useful protection |
| `mischief` | Forest mill path | Opponent prank, progress boost, or minor self-setback | Social, tactical interference |

- Exactly 20 authored event definitions across three local area-specific sets with integer weights. Weights are *starting hypotheses*, not tuned percentages.
- An event move never triggers another landing event in the same turn; this keeps the chain bounded.
- Bots evaluate visible route length and disclosed risk category. When ahead they tend to prefer safety; when behind they weigh the shortest route more. Bots are not allowed to inspect the next random event before choosing.
- Region-specific pools are public design data. Once a player knows the board they can learn route characteristics. A match's seeded RNG changes the actual encounter.

## First implementation and UI
- App loads `data/boards/woodland_woven_trail.json` and `data/events/woven_events_v02.json`.
- Generic `forks[].choices` drives route button titles and subtitles; removes the fake "upper always better" hardcoding. Three-way branches are supported without new Kotlin screen logic.
- Board drawn in warm paper style, with 3 softly separated regions, colored route markings, highlighted candidate entrances at intersections and an individual journey trace.
- Numeric player token identity remains while we postpone optional GridMates animal artwork.

## Criteria to prove the mechanic works
1. Every proposed fork offers two or three truly valid routes leading eventually to the same finish.
2. A shorter high-risk branch must actually expose hazard events; safer trails must have shelter events, not the same random deck.
3. No deterministic strategy always wins (measure win rate, route selection and effects before asserting balance).
4. Human can understand why a route is risky **before** pressing it.
5. Four player pieces, 200% text scaling and controls stay distinguishable on a physical Android phone.
6. Owner completes multiple full matches and judges whether choices feel like they affect winning/losing.

## Deferred
Additional custom board lengths, truly hand-illustrated Woodland art, LAN and pass-and-play, active match restoration, collections and AI difficulty tiers. The original three-diamond fixture remains preserved as `woodland_quick_example.json` for regression/comparison; it is no longer intended as the launch screen course.

[Parent design issue #6](https://github.com/PlayerEden/GridMates-Crossroads/issues/6).
