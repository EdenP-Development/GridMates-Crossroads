# GAMEPLAY-106 — Regional prototype comparison

V4 prototype design/prototypes/woodland_woven_regional_v04.json is outside runtime registry. 95 nodes, 102 edges, 8 forks, 32 event nodes, 46–62 start-finish edges. Each of eight forks presently offers four interior risky nodes versus six interior safe nodes before convergence. Repeated topology is a first-pass baseline, not accepted strategic variety.

CourseDecisionExposureTest now compares five configurations, original and V5 rules, 2/3/4 seats, 60 seeds each: 1800 races, 5400 seat observations. Exact new CI results pending. Previous 1440-race report verified in Android run 45544024393, artifact 14990039984. No human timing inference.

Geometry inspection: minimum pairwise normalized Euclidean distance .011767693 (R3_4/R4_1); S6_6/S7_1 .014541155; S6_4/S7_3 .015232367; S6_5/S7_2 .019518079. These are crowded areas, not accepted layout. Next prototype should reduce near-overlaps and avoid unrelated edge crossings; use sufficient visual spacing and deliberate regional composition. Preserve all existing board IDs.

Before promoting: compare actual V4 simulation results; assess repetitive serial diamonds and two-space risk delta; design varied tradeoffs with optional routes and distinct risk profiles, rather than changing rules or increasing encounter density blindly. Verify physical readability and human pacing. No new artwork or runtime registration in this checkpoint.
