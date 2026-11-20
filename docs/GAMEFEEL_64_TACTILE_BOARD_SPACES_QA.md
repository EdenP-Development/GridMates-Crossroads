# GAMEFEEL-64 — Tangible board spaces (visual-only QA prototype)

Status: isolated stacked draft on verified BRAND-63 QA1518. Do not merge to main or ship as a public release without device approval.

## Why this change exists
The owner wants Crossroads to **look and feel like a board game**, not merely a beautiful landscape with miniature lines and pieces. The next visual priority, after M62's larger trail/piece scaling, is that each real movement space is recognizable as a landing place along the printed board route.

## Implementation
- **Only QA live illustrated board:** draw each authored `plain` and `merge` node as a layered wooden tabletop disk (shadow, wood-grain-toned raised rim, cream face, tiny highlight), rather than a flat white node dot. No new fictional spaces are invented between engine nodes.
- **Density-aware:** one cached nearest-node distance per author-defined node (including forks/events) caps disk radius at 33% of closest center distance in painted world pixels. Comfortable Classic/Quick nodes enlarge up to 1.45x over their already-M62-sized radius; tightly packed Cedarwood Long areas retain smaller disks to prevent overlaps.
- Existing **fork/event/shelter/hazard/mischief** medallions, `S`/`F` labels, choice rings, scenic backplates, texture atlas, trail geometry, camera, and pawns remain untouched.
- Full-map inspector and non-illustrated board styles keep prior flat node rendering. Current Android QA build only; release build intentionally retains existing visuals until owner approval.
- No changes to game graph, movement counts, rolls, encounters, AI, saved data, networking, app identity/signing, or the approved fox launcher icon.

## Test receipts and gates
1. Exact-head Android debug QA compile, unit tests, lint and signing.
2. `CrossroadsBoardSpacesTest`: nearest spacing counts special nodes, Classic spaces grow, dense Cedarwood nodes cap disk size, full-map never changes, degenerate inputs do not blow up.
3. Real phone side-by-side with **QA1518**, same Woven Trail start, open route, close fork, arrival and finish. Confirm the **spaces themselves** are plainly visible and help count travel one node at a time.
4. Capture Cedarwood Long and Brookside in gameplay: confirm no intersections turn into clusters of coins, signs remain distinguishable and individual trail locations read.
5. During a route choice, tap both roads and the route-choice dock. Enlarged artwork must not change correct hit targets. Pinch/pan/recenter and 2–4 pawn overlap still work.
6. Continue ordinary save/rematch/reinstall-over and signed APK acceptance. Require actual screenshots/owner acceptance; source QA alone cannot validate illustration density.

## Release rule
Keep GAMEFEEL-64 isolated and draft; treat a failed visual check as grounds to adjust or revert just the waypoint rendering, not a reason to rewrite the graph, game rules, or scenery.
