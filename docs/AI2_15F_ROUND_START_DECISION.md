# AI2-15F — Round-start awareness decision

Direct evidence: rules run 46054811743, job 140694606060, step 13 validated JSON. Android run 46054811852 passed.

320 matched seeds/course pairs, plus 320 native controls. Baseline reproduced original Guardian wins 204/116; round-start candidate wins 277/43. Player 1 share rises from 63.75% to 86.5625% (+22.8125 percentage points). Paired transitions (live winner, candidate winner): (1,1)=182, (1,2)=22, (2,1)=95, (2,2)=21.

| Course | Live P1 wins /64 | Candidate P1 wins /64 |
|---|---:|---:|
| Brookside | 37 | 64 |
| Cedarwood | 31 | 37 |
| Woven | 47 | 56 |
| Convergence | 54 | 56 |
| Regional | 35 | 64 |

Live rolls per seat 1937; pushes 160/1170. Candidate rolls per seat 2010; pushes 25/175. Candidate differed from contemporaneous live recommendation 1131 times. Equal rolls retained. Candidate changes only travel assessment, retaining final-round live awareness and fallback on own-distance change. This intervention worsened balance in this sample; it is not proof of causal mechanism for all policies, courses or human play. Lower push rates are associated with worse balance here. Do not register or promote this candidate; retain test and original frozen policies.

Next: audit starting-player identity assignment and replay/rematch assumptions. Consider explicit opt-in offline QA starting-order rotation or randomization, preserving human identity, color, statistics and replay. This distributes rather than eliminates per-race order advantage. Examine finish ties and route/encounter effects separately. No engine, save or release changes.
