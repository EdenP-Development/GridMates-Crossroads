# AI2-10D — Gambler competitive evidence

Source: rules run 45948087404, artifact 15176076243, actual ZIP inspected using pandas. gambler_competition.csv has 12000 actor rows, 3600 GAMBLER_V1 rows. exact_strategy_calibration.csv has 720 rows. Android run 45948087192 passed, QA1914 artifact 15175577813 (20280346 bytes).

## Gambler wins (two-seat / four-seat)
Each opponent provides 240 / 480 matches.
- V1: 113 / 100
- R1: 112 / 90
- R2: 117 / 122
- R3: 117 / 122
- R4: 117 / 122
Total 576/1200 (48.0%) and 556/2400 (23.17%). Course wins (240/480 each): Brookside 106/110, Cedarwood 133/129, Woven 98/107, convergence 111/105, Regional 128/105. Gambler seat wins two-seat 309/267 (600 each), four-seat 146/141/134/135 (600 each).
Gambler choices: steady 3053, push 17444, low/medium/high 3976/1513/11820, supplies/cover 2901/1075, gamble/brace 9449/0, target 76, pass 0. Push 85.1%; all observed hazard decisions gamble. Opponent exposure and match progress differ; repeated seeds and shared RNG limit statistical inference. Do not claim calibrated difficulty or human enjoyment.

## Prior exact homogeneous calibration
Columns steady,push,low,medium,high,supplies,cover,gamble,brace,target,target_pass:
CLASSIC 385,1031,895,177,534,363,206,412,7,52,0
TRAVELER 399,1003,897,172,527,362,206,405,7,45,0
SCOUT 357,818,341,207,864,122,219,562,1,20,0
GUARDIAN 353,794,341,204,841,128,213,547,1,19,0
STRATEGIST 353,794,341,204,841,128,213,547,1,19,0
Guardian/Strategist aggregate counts coincide; no demonstrated personality separation here.

## Decision
Keep G1 immutable. Test a pure G2 candidate with a deliberate defensive exception when leading, not on final round, no steady protection, and meaningful retreat risk; use visible movement and race gap only. This is a heuristic, not an optimal probability calculation. Do not activate until mixed competition and exact behavior comparison. No difficulty claim.
