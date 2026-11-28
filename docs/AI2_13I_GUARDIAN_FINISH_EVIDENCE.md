# AI2-13I — Guardian finish candidate competitive evidence

Source: rules run 45961604660 (success), artifact 15181063442, guardian_finish_competition.csv opened and parsed locally. 9600 actor rows, 2880 unique match keys (course,seats,seed,guardian_seat), each with correct seat count, unique actor seats, exactly one winner and one candidate. Android 45961604703 in progress at inspection.

| Course | Candidate wins / 192 two-seat | Candidate wins / 384 four-seat |
|---|---:|---:|
| Brookside | 105 | 102 |
| Woven | 99 | 105 |
| Cedarwood | 104 | 106 |
| Regional | 102 | 108 |
| Convergence | 97 | 103 |
| Total | 507 / 960 | 524 / 1920 |

Rates 52.8125%, 27.2917%, above equal-share 50%,25% by 2.8125 and 2.2917 percentage points. Seed-cluster percentile bootstrap, 20000 resamples of 96 seeds (all courses and focal seats retained in each seed), NumPy default_rng(42) reset per seat count: 95% intervals 51.5625–54.1667% and 26.5104–28.125%. Conditional on this course set and simulator; not a general guarantee. Candidate focal seat wins /480: two-seat 291,216; four-seat 126,144,151,103. Seat effects still substantial. Candidate steady/push 3693/2073 two-seat and 4883/6017 four-seat. Standard opponent totals 3841/1925 and 15031/17669 respectively (three Standard actors per four-seat match). 

Decision: retain candidate for eventual Advanced Guardian preview, positive modest effect across every tested course and both player counts. Do not activate the current generic difficulty resolver: Scout route-only variants previously failed intended ordering, and no Relaxed Guardian exists. Next add a separate calibrated-preview eligibility/resolution contract, then investigate Relaxed using targeted, bounded decisions; no intentional illegal actions or gameplay advantages. Physical QA and mixed-roster generalization outstanding. No runtime behavior change in this evidence checkpoint.
