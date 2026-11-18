# BOARDS-43 — Three illustrated course backgrounds (Android QA only)

**November 18, 2026.** Stacked on THEME-42. The first owner-phone ART-PREVIEW-40 screenshots established that Woven Trail works with real generated artwork; GAMEFEEL-41 improves visibility of route choices. Do not merge into main until owner approval.

## Exact changes
- Imported `board_brookside.png` and `board_cedarwood.png` in the existing local `art/v1/` registry.
- Updated only those two manifest rows with exact RGB 1536×1536 dimensions, source generation IDs, license category `generated`, and SHA-256.
- Files are intentionally **QA-preview-only, not owner-approved**. The original generated source files are retained in the conversation export, but the packaged files are deliberately palette-reduced RGB review copies for smaller APK footprint. Inspect at full phone zoom: reject if 512px palette processing softened artwork too much.
- Existing Woven Trail, home, shelter and hazard assets unchanged. Total occupied slots: six of twelve. Four pawn images, mischief and victory banner still procedural.
- No real board paths/nodes/dice/buttons/text are embedded in the images. Existing authoritative geometry overlays and keeps rules independent of artwork.
- THEME-42's **Illustrated** appearance displays each of these local PNGs for corresponding Quick/Long courses, while **Classic map** uses procedural fallback. Same saved board IDs and match rules.

## Automated verification
Run `tools/check_crossroads_art_pack.py`; it must validate file presence, RGB PNG, CRC, dimensions, license/provenance and the exact SHA for six present assets. GitHub Android QA compiles the actual branch and produces a signed install-over QA artifact only after tests/lint/signing pass. A green build **is not visual acceptance**.

## Owner phone acceptance
1. Update *over* prior signed QA without uninstalling; verify old local solo wins and any resumed saved match.
2. Choose Quick, Illustrated: **Brookside Dash** should be cheerful and light, with routes and start/finish clearly overlaid; zoom/pan and Full map should show identical artwork.
3. Choose Classic, Illustrated: **Woven Trail** remains the existing art; routes, midgame zoom and encounters readable.
4. Choose Long, Illustrated: **Cedarwood Expedition** should read as darker cedar forest + camp but playable routes remain clearly distinct. On dense 6-fork map inspect at 1× and 3×.
5. For each, select Classic map and verify all course art reverts to procedural version without changing game outcomes.
6. Test Lab v3 route choices (2- and 3-way) on short portrait phone; ensure long text and lower choices remain reachable.
7. Note any soft/blurry boards from the compact exported PNGs, jank while loading image, unintended line illusion from scenery, or unreadable node numbers.

## Next: shared printed trail language (separate decision)
Phone screenshots showed that route network lines and nodes can look like abstract app geometry laid over art. Investigate a **single reusable trail rendering style** (layered paper/ink ribbon with restrained grain and junction medallions), drawn dynamically on top of every backplate using actual graph coordinates. Do **not** bake trail routes into course PNGs. Evaluate Android performance and 1×/3× legibility; switch to reusable texture sprites only if the procedural print treatment still looks out of place.
