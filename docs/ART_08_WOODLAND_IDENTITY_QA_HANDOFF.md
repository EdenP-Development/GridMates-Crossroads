# ART-08 — Woodland Crossing region identity
**Status:** Draft/source implemented; compilation, Android QA build, camera and owner phone acceptance to be confirmed by exact CI evidence.

## Aim
Take the user-visible Woven Trail board from a flat three-panel route diagram toward a memorable **cozy tabletop journey**, without making it difficult to understand route risk, where pieces are, or which junction is currently important.

The board data remains 38 fixed nodes, one Start, one Finish, and five crossroads. No course geometry changes.

## Source design (procedural original illustration)
The new `WoodlandBoardArt.kt` paints an original and deterministic illustration layer into the same Compose board canvas **before** any paths, fork markers, player progress trails, tokens, or landing effects.

- **Meadow** (left): soft leafy meadow wash, fine grass silhouettes, and tiny wildflowers. Open-air visual identity.
- **Forest** (middle): pale shaded canopy, evergreen tree clusters and a winding cool-blue stream passing behind the Cedar Bridge sector. Denser environmental identity than Meadow.
- **Village** (right): small friendly cottages, orchard treetops, a short rustic fence, and warm earth tones.
- **Whole board**: warm parchment ground and gentle curved contour-like lines. Colors intentionally low contrast, subordinate to paths and tokens.
- **Future boards**: course-specific motifs only render for `woodland_woven_trail_v02`; unknown board IDs get neutral paper instead of incorrectly positioned scenery.
- **Performance/safety**: no background network access, no image decoding, no random draw, no extra Gradle dependencies and no external art/font/license imports. Current visuals are code-owned and versioned in the repo.
- **Camera**: the existing gestures, 1×–3× zoom, center/tracking, and route choice interactions remain unchanged. Scenery scales with the same board, not with the surrounding action UI.

## Deliberate limitations
This is **first-pass illustrated scenery**, not a final hand-painted high-resolution board, 3D scene, character set or a new scenery-generation system. Labels and route colors retain their original meaning. The new decorations do **not** represent events, rule actions or interactable hit targets.

## Visual/behavior acceptance checklist
- [ ] Android QA code compile, lint, JVM tests and signed exact-version APK pass.
- [ ] New signed APK installs over QA 1067 with existing completed games/wins and first-use gesture help flag intact; signing checks must be on the actual APK.
- [ ] At whole-board view (1×), Meadow, Forest, and Village look noticeably distinct, but the path network/38 spaces, fork rings, region labels, finish and player numbers remain readable.
- [ ] At Follow (~1.7×) and 3× zoom, scenery does not cover route strokes, tokens, endpoint rings or text labels; no clipping/hard block edges.
- [ ] Camera pinch/pan and double-tap recenter still work; manually exploring does not snap back during bot turns.
- [ ] Cozy/Brisk movement timings, dice result, bot motion and 3-choice Old Forest selector unchanged.
- [ ] Short phone portrait and larger Android font size: fixed bottom panel has room and route chooser is fully selectable.
- [ ] Inspect/Board Overview still shows entire course, including the bridge area, while return to match is natural.
- [ ] On real-device screenshots, revise any muddy art vs colored road contrast before investing in larger art production.

## Art provenance/asset registry
- Artwork origin: **original programmatic Compose primitives**, `app/src/main/java/com/playereden/gridmates/crossroads/WoodlandBoardArt.kt`.
- Third-party file imports: **none**.
- Sibling GridMates image/token imports: **none**; separate inventories remain untouched.
- No art asset IDs or bitmap manifests were added; source path itself is the provenance.
- Follow future [ASSET_REUSE.md](ASSET_REUSE.md) before importing external / sibling image assets.

## Evidence boundary
Automated Android compilation or screenshots cannot prove comfortable *manual* gesture behavior or aesthetic preferences. This milestone is accepted only after an owner phone playtest. Keep source changes in the Draft PR, no merge or production release without explicit user approval.
