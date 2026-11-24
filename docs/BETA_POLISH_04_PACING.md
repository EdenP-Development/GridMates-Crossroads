# Polish 04 — Race pacing baseline

Graph enumeration on exact existing authored course definitions, counting edges from Start to Finish (not all nodes):
- Brookside: 24 nodes, 2 forks, 7 event nodes, 6 complete routes, 13–15 steps.
- Woven: 38 nodes, 5 forks, 14 event nodes, 24 complete routes, 16–22 steps.
- Cedarwood: 101 nodes, 6 forks, 28 event nodes, 144 complete routes, 54–66 steps.

The shortest Classic route is just three steps longer than Quick. More total nodes need not imply more travel. This ignores all events and movement rolls. Owner observed one Classic match of roughly one minute; not a population estimate.

New BetaPacingAuditTest runs 1,080 seeded all-bot matches: three courses × three existing rules variants (original/v4/v5) × two seat counts (2/4) × 60 seeds. Records actual rounds, rolls, fork decisions, interactive encounters and target choices, excluding animation. Reports grouped medians, CSV and summary via existing rules workflow. All bots finish legally within cap. Results pending exact CI; do not convert actions to real minutes or claim human enjoyment.

Provisional two-player design targets: Quick 2–4 min, Classic 5–8 min, Long 10–15 min, subject to owner preference and human tests. Prioritize meaningful travel and decisions, not artificial waiting. Any redesigned course requires a distinct ID and explicit save-compatibility review. Do not replace original definitions, alter dice, change gameplay code, or promote experimental rules in this audit. No main merge.

Polish 03 Android run 45438908303 was in progress at start. Latest verified QA1690 from Polish 02. Next use measured baseline to decide whether a new longer Classic layout is appropriate, then menu overhaul and HUD polish.
