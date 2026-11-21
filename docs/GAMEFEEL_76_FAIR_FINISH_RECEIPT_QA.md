# GAMEFEEL-76 — Truthful finish-round receipt (Lab v5)

**Scope:** results clarity and deterministic read-only explanation for the existing optional equal-turn Lab v5. No additional rule, dice, probabilities, seat order, board graph, assets, database, LAN, app identity, permissions, save schema or version migrations. Draft stacked on GAMEPLAY-75; `main` untouched.

## Evidence motivating this change

The exact-head GAMEPLAY-75 report passed 2,160 identical-bot v4/v5 races. V5 improved 2-seat Quick first-seat wins from **98/120 (81.7%)** to **75/120 (62.5%)**, Classic from **76/120 (63.3%)** to **68/120 (56.7%)**, and Long from **65/120 (54.2%)** to **61/120 (50.8%)**. Quick added **0.82** average dice actions (not elapsed seconds); 34/120 Quick v5 matches had at least two finishers. Equal-turn is therefore a promising, still unapproved *playtest* rule.

Problem: `CrossroadsCourse.resultLine` still described a v5 winner as "First across" even when the winner reached FINISH later but had the most unused movement. The v5 results explanation was generic and did not say who crossed first or how many racers finished. This could look like a result bug to players.

## New read-only finish receipt

- The **v5-only** rules engine exposes a transient `FairFinishReceipt`: first finisher's name, actual winner's name, winner's unused steps, number of finishers, whether top unused-steps were tied, and whether later seats got another turn.
- Every field comes from **authoritative existing action replay** and the actual finish resolution. No new randomized decisions, recorded actions or serialized Snapshot properties are introduced. During a pending last chance the receipt is null; after resolution, it is deterministic across checkpoint restore.
- Results give one truthful explanation:
  - A sole winner after another seat's chance: "nobody else reached Finish".
  - A sole winner at the last seat: all racers already had their turn.
  - Two or more finishers: who arrived first, how many finished, who won and how many unused steps won. If the top score tied, explicitly state that earlier among the tied finishers won.
- New v5 result line: **"Fair finish on <course> · Playtest"**, not "First across". Original/v1–v4 result lines remain unchanged.
- The existing "Final-round winner" and "Reached Finish" statuses, illustrated victory seal, race time, rematch and View Course buttons stay where they were. No modal/tap, notification, music or additional animation is introduced.

## Exact-head test gates

- `DecisionLabVersion5FinishTest`: verify no pending receipt, sole winner, later-seat overtake, all-four equal-step tie, first finisher at last seat, absence in v4, plus **exact receipt equality after checkpoint replay**, not just matching Snapshot.
- `DecisionLabV5PresentationTest`: v5 result subtitle avoids mislabeling a later winner; v4 legacy copy unchanged; exact explanation for sole winner, last-seat winner, overtake and top-score ties.
- Entire core and Android suites, layout/unit, style and design-data QA, signed archive must pass at the **new M76 PR SHA**. The successful M75 APK (QA1596) does not include this copy repair.
- On-device screen sizes: 360dp/430dp portrait, landscape, largest Android font, TalkBack, results scrolling and permanently accessible buttons.
- Run both v4 and v5 from the same APK: Tactical ON / Fair Finish OFF remains v4; ON / ON runs v5. Reopen a pending last chance after force-stop, finish it, and verify the exact receipt once without duplicated stats.
- Retest v5 sole-first-finisher and two-finishers outcome. If one arrives after the other and wins, explain this from the receipt without implying the winner arrived first.

## Open questions

- M75 is promising but first-seat v5 Quick still won **62.5%** of synthetic two-seat samples, so don't claim perfect balance or change the original default mode.
- Synthetic die actions do not prove physical-phone race duration or play enjoyment.
- Prior QA receipt for QA1596 explicitly marks physical install-over, save preservation and real-device testing as not yet completed. Do not promote production readiness until these gates are fulfilled.
