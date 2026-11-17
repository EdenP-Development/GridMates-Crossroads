# Handcrafted board + controlled random events
**Accepted concept:** players can learn a fixed landscape while each match has different encounters.

## Board graph
- Each board has a globally unique ID/version, a single Start and Finish, drawable positions, nodes, directed edges, and a list of valid outgoing choices at forks.
- Routes can diverge and converge; all legal branches must eventually reach Finish. V1 example uses no true dead ends or backward loops.
- Each themed region (e.g. meadow / forest / village) has a risk label and a curated set of eligible event IDs.
- Code stores geometric positions separately from rule semantics so new skins do not alter core movement.

## Per-match randomness
- One match seed feeds a deterministic stream of dice and event draws. This supports LAN synchronization and reproducible debugging.
- Randomize *content* by region/eligibility, not Start/Finish or hidden route connectivity.
- First prototype: event spaces remain marked '?' and events are chosen when landed on. This avoids high setup complexity.
- Option later: precalculate a per-match map of special event placements using bounded weights and host-shared seed.
- Avoid overly repetitive setbacks and negative chains via eligibility filters and bounded cooldowns, subject to playtest.

## Risk communication
Show upcoming branch labels such as 'Longer / safer' and 'Shorter / unpredictable' plus visible route length. Do not make one branch universally best. Do not deceptively hide blocked paths.

## Board variations
- Quick, Classic, Long use the same movement system. Tune course geometry/branch count rather than varying fundamental rules.
- Multiple themed boards should use different paths **only when intentionally designed**, not because a theme asset forces gameplay code changes.
- Reserve alternative shapes and modular/seed-generated courses for later.

## Example design data
See `data/boards/woodland_quick_example.json` and `data/events/core_events_proposed.json`. These are **design fixtures**, not approved balance or production assets.
