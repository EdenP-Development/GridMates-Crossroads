# MARKERS-50 — Illustrated woodland waypoint medallions (QA preview)

**November 18, 2026.** Draft, stacked on PIECES-49 → UI-48 → TRAILS-47/46, all isolated. Never merge to `main` until user approval.

## Motivation
Real-phone Woven Trail screenshots show the illustrated roads and scenery finally forming a consistent look, while the uniform circles at crossroads and event stops remain mechanically rendered. At camera zoom they are visually dominant, and the circle+ring layering looks like app UI painted over a storybook.

## Original artwork
- `app/src/main/assets/art/v1/marker_atlas_v1.png`: exactly 256×256 RGBA PNG, four 128×128 tiles arranged 2×2: fork (amber Y), shelter (sage leaf), hazard (rust mountain), mischief (purple spiral).
- Original OpenAI image generation ID `40257970-fea9-475a-98c6-a22a9fd578c9`, Nov 18 2026. Transparency verified. 2×2 crop, downsample, indexed to 32 colors preserving alpha. **Not copied** from sibling GridMates games.
- Exact SHA-256: `2d5e32e9621b8af9699e541797c4f5b1da1385bed0e05e56de859051ad9a812a`.
- Added one generated/licensed/provenance-registry slot; owner-approval flag is explicitly QA-only. Passing data checks is not art approval.

## Presentation and fallback
- Existing node coordinates, connections, event pools, movements, turn state, hit targets, score and saves remain authoritative. **Sprite art never creates or removes a graph node.**
- For known `fork` / `event:shelter` / `event:hazard` / `event:mischief` nodes, illustrated markers replace stacked circles using the atlas; fork retains an explicit drawn '?' in a separate bottom-right medallion.
- `start`, `finish`, `plain` and unknown event types keep current rendering; no route-reward or gameplay changes.
- QA-only `⋯ > Markers: Illustrated / Standard` visual toggle, default Illustrated for comparison, stored in `qa_painted_markers_v1` only.
- Missing or corrupt atlas, Classic board look and non-QA builds automatically use the existing deterministic circles. The same choice applies to the live board and Full map.
- This is a compact, cohesive prototype, not an avatar unlock, new collectible pack, or user-facing accessibility substitute for button text.

## Automated tests
- 15-slot manifest, correct dimension and SHA, local PNG CRC, alpha and provenance.
- Pure lookup test: 4 recognized markers, unknown/plain/start/finish fallback, 2×2 atlas bounds.
- DEBUG-only vs Classic/no-art fallback.
- Android compile, unit tests, lint, signed QA artifact verification.

## Owner-device acceptance
1. Compare Illustrated vs Standard markers on Woven Trail at full map, fit, 2× zoom and 3× zoom. If the little symbols become mushy, reject this design.
2. Examine 3-way Old Forest Junction, particularly when tokens and destination highlights cover nearby nodes. No fork should become difficult to tap/understand.
3. Test Cedarwood's dense network and Brookside's tighter paths. Check number and risk legibility plus contrast on grass, river and village background.
4. Inspect screen-readability in bright daylight and with accessibility text enlargement; labels and route-description cards must remain intact.
5. Complete a race; test turn switches, AI movement, panning, save/resume and LAN if available, verifying no movement or node count changes.
6. Install over existing QA without uninstalling, then confirm local wins and in-progress progress survive. This is not physically validated by CI.

**Do not merge to main or publish a release solely because CI passes.**
