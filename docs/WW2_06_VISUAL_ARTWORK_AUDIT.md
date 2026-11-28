# WW2-06 — Consolidated visual and artwork audit
2026-11-25 — reviewed implementation at a7092c5f66554335bfd6776b242d41c2b6bac362.

## Verified build
Rules 45581152204 and Android 45581152201 completed successfully. QA1857 artifact 15006113348, 20257423 bytes. This is a CI-verified QA build, not release or owner visual acceptance.

## Asset inventory
Source: app/src/main/assets/art/v1/manifest.json and CrossroadsArtAssets.kt.
17 registered slots: 10 marked approved in manifest (all QA-preview-only-not-owner-approved), 7 planned. Six individual illustrations (home, three course backplates, shelter, hazard), four reusable atlases (trail, tokens, markers, environment v1). Planned: mischief, victory, four standalone seat portraits and environment v2. Do not mark optional gaps as release blockers absent a required feature: procedural/text fallback exists. Seat atlas already provides four woodland characters, so separate portraits are not automatically needed. V2 environment atlas optional; loader tries valid V2 then V1. No new image generation or asset removal in this audit.

## Rendering
Optional artwork is decoded on Dispatchers.IO via produceState, retained per composed surface, not decoded on every frame. This is not a global cache: separate surfaces may decode again. Regional scenery and groundcover plans are remembered by board/style and artwork flags. WW2-05A hoists constant drawing palette and waypoint vectors. A 1536-square bitmap at four bytes/pixel is roughly 9 MiB before overhead; 1024-square atlas roughly 4 MiB. These are theoretical decoded sizes, not measurements. Avoid unconditional global bitmap caches and speculative changes before profiling. Sibling overhead may also load fallback backplate; measure peak memory before changing fallback. Board/route geometry remains authoritative.

## Owner test matrix
- Compare QA1857 with QA1844: Regional Journey village, forest, meadow, trailhead and destination; compact header; pan/zoom, follow camera and readable legal routes.
- Compare Brookside, Woven and Cedarwood in illustrated and simple modes; verify scenery does not obscure graph or markers.
- Complete offline match with AI, inspect encounter and results/journal, rematch.
- Test older device if available: repeated home/game/map navigation, long match, visible jank/heat; collect frame and heap profiling evidence before expensive optimization.
- Two-phone LAN separate acceptance; no inference from screenshots.
- Startup accepted in QA1844; retain app identity, signing and launcher standard. No production merge or release.
After physical review, prioritize gameplay/AI/balance, multiplayer and Beta 1 readiness; do not indefinitely expand decorative asset scope. Visual acceptance and device performance remain open.
