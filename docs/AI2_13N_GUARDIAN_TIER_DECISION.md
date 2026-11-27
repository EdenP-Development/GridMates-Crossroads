# AI2-13N — Guardian three-tier decision

Verified directly from GitHub Actions rules job 140390312643, run 45964646619, step 10 "Validate and display Guardian tier results". Both rules and Android run 45964646684 passed. JSON log confirms 12960 actor rows and 3600 matches.

| Focal | Opponent | Two-seat wins / 240 | Four-seat wins / 480 |
|---|---|---:|---:|
| Relaxed candidate | Standard | 131 | 113 |
| Standard | Relaxed candidate | 109 | 131 |
| Relaxed candidate | Finish candidate | 120 | 95 |
| Finish candidate | Relaxed candidate | 120 | 148 |
| Standard | Finish candidate | 112 | 104 |
| Finish candidate | Standard | 128 | 136 |

Two-seat reciprocal rows refer to the same matches; four-seat rows represent distinct focal-versus-three configurations. Equal-share descriptive benchmarks 50%/25%. Relaxed is stronger than Standard in two-seat (54.58%) and slightly below equal-share in four-seat (23.54%). Finish candidate exceeds Standard in both (53.33%/28.33%), but ties Relaxed in two-seat (50%). No calibrated three-tier ordering.

Decision: retain Standard and the existing offline Advanced Guardian preview; keep Relaxed excluded. Do not expand unproven difficulty variants indiscriminately. Prefer shipping an optional QA personality selector with Standard choices first, with Advanced Guardian separately marked experimental; investigate starting-position fairness and collect physical feedback before pursuing full difficulty matrix. No production activation.

Evidence provenance: this report is based on directly retrieved CI log text, not just artifact metadata. Earlier numerical reports are historical claims not independently reverified here. Do not treat them as independently confirmed unless reproduced from CSV or logs. No confidence intervals inferred from aggregate results.
