# AI2-12E — Strategist competition evidence and roster selection

Actual ZIP artifact 15178202767, rules run 45954691900, inspected using Python zipfile and csv. 16800 actor rows, 5040 Strategist focal appearances.

| Opponent | Two-seat wins / 240 | Four-seat wins / 480 |
|---|---:|---:|
| V1 | 141 | 127 |
| R1 | 139 | 125 |
| R2 | 127 | 115 |
| R3 | 121 | 118 |
| R4 | 121 | 118 |
| Gambler V1 | 124 | 113 |
| Guardian V1 | 110 | 131 |
| Total | 883 / 1680 | 847 / 3360 |

Exact focal decisions: steady 9198, push 19453, low 6698, medium 1746, high 15785, supplies 4322, cover 2376, gamble 11917, brace 394, target 164, target_pass 0. Push 67.896%; brace 3.2%. Competitive viability demonstrated within this matrix, not difficulty ordering or human enjoyment. No counterfactual measurement of unique target heuristic frequency.

Proposed initial roster mapping: Classic V1, Traveler R1, Scout R2, Gambler V1, Guardian V1, Strategist V1. Preserve experimental BotStrategyPreset and historical cohorts unchanged. Introduce separate roster resolver for new offline tactical matches only; do not wire UI or change default policy until subsequent integration. Persist resolved policy IDs; no reinterpretation of saved matches. Difficulty is separate future work.
