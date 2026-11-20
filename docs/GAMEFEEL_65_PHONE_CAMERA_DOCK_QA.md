# GAMEFEEL-65 — Real-phone camera and trail-crowding corrections

**Status:** Stacked QA draft based on #64, following owner phone screenshots of QA1518. No public release and no merge to main.

## Evidence from real device
- Approved fox launcher icon is visibly installed in the device's Dev folder: BRAND-63 asset transfer and launcher resources now have device evidence.
- The large colored fox and owl portrait tokens look substantially more like tabletop pieces than the tiny earlier build. **Preserve M62 tokenScale = 1.91x**.
- Some route intersections appear as a wide stack of large painted roads, and a Round 2 ROLL screenshot shows no active piece at the visible junction. The latter could be manual exploration, so do not claim a camera engine defect without reproducing it.
- Bottom action summaries such as an encounter at High Ridge are ellipsized mid-sentence while there appears to be unused panel space.

## Deliberately limited fixes
1. Live trail scale 1.67x -> **1.58x** (still ~20% wider than M61's 1.32x). Character/number scale 1.91x and node scale 1.55x remain unchanged.
2. Follow camera: first roll stays 2.15x; subsequent rolls 2.48x -> 2.36x, route 2.20x -> 2.12x, encounter 2.60x -> 2.48x, target 2.40x -> 2.32x. The goal is to preserve visible painted paths and active piece without huge intersections dominating the viewport.
3. A new ready-roll turn (not yet seen by the camera) returns a panned/zoomed live board to FOLLOW **only after the preceding travel animation finishes**. The player can still freely drag and pinch during the current turn; camera does not keep stealing manual control. Full-map and static view are excluded.
4. Give the short latest-action summary **two lines** so encounter guidance isn't pointlessly cut off; existing pinned buttons stay in place.
5. Preserve M64's experimental raised wooden spots, current fox launcher, background images, route hit-test graph and rules, all multiplayer and saved records.

## QA acceptance
- Android exact-head debug compilation, unit tests, lint, security/public configuration and signed APK archive.
- Follow-policy tests: new turn resets, current manual exploration preserved, transition animation never interrupted, read-only/full-map unchanged.
- Camera/scale presentation regression tests, earlier M64 density/spacing tests, route click tests unchanged.
- Owner comparison vs signed QA1518: Round 1 start, Round 2 ready roll after panning, two- and three-branch crossroads, hazard text, compact or larger Android font, and Cedarwood Long dense intersections. Confirm large animal pieces remain big and recognizable.
- Test tap, recenter control and pinch/drag plus touch target. Capture screenshots of any remaining offscreen traveler at ready roll.
- Stacked draft; do not merge or publish without owner approval.
