# TRAILS-44 — Printed path prototype versus simple lines

**November 18, 2026. QA-only visual experiment stacked on BOARDS-43. Do not merge into main.**

## Why
The real phone screenshots confirmed Woven's new illustrated scenery, but the authoritative graph still looked like straight abstract GUI lines placed over a hand-drawn world. The owner asked for actual visual assets for board trails, **unless** the route renderer itself can be made convincing. This milestone tests that less expensive approach before creating dozens of path-image pieces.

## Technical work
- New reusable `CrossroadsTrailArt.kt` draw function accepts source/destination graph coordinates, actual route type and density; produces deterministic layered printed route ribbons. Stroke order: soft forest ink, colored edge, warm parchment deck, muted colored dotted stitch center.
- Small printed medallions frame ordinary event/junction nodes. **Actual circle centers, risk colors, labels, token rings, hit targets and camera math remain authoritative.**
- Same scene can A/B old simple trails vs printed trails by opening **⋯ > Trail style: Printed / Simple** in the Android QA build. This option is only in `BuildConfig.DEBUG`; no new production-facing theme complexity. Visual preference stored separately as `qa_printed_trails_v1`, never in saved matches.
- The full-map overview and live board use the same selection. Classic map appearance keeps the existing simple rendering; printed trails are only used with Illustrated appearance.
- No asset sprites, pre-baked roads, extra maps, AI changes, RNG, lane graph modifications, save codec, route previews, LAN protocol or monetization.

## Android QA
- Exact-head Android compile, tests, lint, signing and art manifest must pass.
- Test Quick (Brookside), Classic (Woven), Long (Cedarwood) using the same Illustrated board, switching the trail style in the ⋯ menu. Compare at fit/1× and maximum manual zoom.
- At forks, colored route distinction must survive the new parchment deck. Check the narrowest Long board: nodes and 2/3-way choices must not become crowded or deceptively connected.
- Test panning, double-tap recenter, edge clipping, rapid AI turns, game pause/recovery, results and board overview.
- Watch for visual repetition, coarse pixel details from compact Batch B backplates, and thermal jank on older phones.
- If the style is still too digital: move to dedicated alpha-backed **tile/sprite** art for straight and angled stretches plus junction medallions, drawn along actual graph edges. Never embed route geometries in the board background.
- Do not claim a green CI is proof of improved visual fidelity. Final route aesthetic remains owner review.

## Future: real trail assets if needed
Sprites would be **reusable shared primitives**, not one pre-drawn map per course. Draw at the route vector's angle and scale; expose safe/risky/mischief colored trim; keep the paper texture and in-game touch region independent. Export with transparent alpha and asset provenance registry. Only build them after confirming the printed vector prototype cannot meet the visual target.
