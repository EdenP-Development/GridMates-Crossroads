# AI2-13D — Scout difficulty evidence and revised experiment

Source: actual scout_difficulty_competition.csv, ZIP artifact 15178694083, rules run 45957389443. Inspected using Python zipfile/csv; 14400 actor rows and 4320 focal matches. Android 45957389394 in progress at inspection.

| Focal | Opponent | Two-seat wins / 240 | Four-seat wins / 480 |
|---|---|---:|---:|
| Relaxed | Standard | 133 | 135 |
| Relaxed | Advanced | 133 | 135 |
| Standard | Relaxed | 107 | 110 |
| Standard | Advanced | 120 | 120 |
| Advanced | Relaxed | 107 | 110 |
| Advanced | Standard | 120 | 120 |

Standard versus Relaxed and Advanced versus Relaxed matched on course, seats, seed, focal seat and actor seat: all 2400 corresponding rows equal for winner flag, match actions and all eleven decision counts. This does not establish identical full action sequences, but no difference on these measured outcomes. Reversed ordered two-seat configurations duplicate lineups and are not independent. Relaxed focal against Standard: low/medium/high counts 627/69/521 (two-seat), 758/191/1318 (four-seat); Standard focal against Relaxed 426/84/690 and 370/227/1654. Exposure differs. Relaxed's stronger performance and safer observed route mix suggest the adjustment may improve risk management; causality not isolated. No calibrated difficulty ordering. Do not expose these variants as strength labels.

Next experiment: use actual visible travel mechanics. Steady moves four, Push has outcomes 1,1,5,6,7,8 with equal probability; mean 4.6667, no hidden RNG. A pure finish probability helper for uninterrupted corridors can support future Advanced choices and quantify risk; not sufficient alone to rank difficulty. Future integration must verify corridor encounter/landing assumptions and equal-turn excess movement before using probabilities to select actions. Preserve existing IDs, standard, rules and saves.
