# AI2-15D — Two-seat tactical exposure evidence

Direct source: rules run 45972486928, job 140416034678, validated JSON in logs. Android run 45972486932 passed. 1920 observed matches and paired controls; 23520 travel decisions.

| Policy | Rolls per seat | Push seat 1 | Push seat 2 | Trailing seat 1 | Trailing seat 2 |
|---|---:|---:|---:|---:|---:|
| Classic | 2061 | 727 | 1512 | 727 | 1512 |
| Traveler | 2050 | 701 | 1458 | 764 | 1515 |
| Scout | 1929 | 641 | 1367 | 701 | 1419 |
| Gambler | 1871 | 1253 | 1446 | 685 | 1289 |
| Guardian | 1937 | 160 | 1170 | 636 | 1868 |
| Strategist | 1912 | 560 | 1419 | 647 | 1448 |

All policies Push more frequently from seat 2. Guardian exhibits the largest disparity. Seat 1 never observes final-round-pending during roll; seat 2 does. These are descriptive associations, not causal estimates. Current-position comparison includes the first player's current-round movement; a trailing classification is not necessarily a deficit at an equal-turn boundary. Direct finish odds are not win probabilities.

Decision: preserve existing v5 policies and rules. Next isolate round-start awareness in a test-only comparison or separately versioned rules candidate; do not alter frozen policy IDs. Starting-player randomization/rotation can distribute the advantage across identities but does not remove the underlying order effect. Audit checkpoint/replay and Android identity assumptions before any rotation implementation. No public or LAN changes.
