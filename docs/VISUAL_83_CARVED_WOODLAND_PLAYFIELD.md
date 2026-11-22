# VISUAL-83 — Carved Woodland Playfield

**November 22, 2026 · source prototype · stacked DRAFT after VISUAL-82**  
**Scope:** first in-game board-art pass, *not* new core mechanics or a public launch

## Real-device problem

Owner's QA1614 screenshots and explicit follow-up make the requirement clear: the *actual board scenery, trails and travelers* need to be large, enjoyable and integrated as a digital tabletop game, not a miniature full-map picture with platform UI layered on top. UI-81 enlarges the painted scene and interactive visuals, and VISUAL-82 provides a coherent walnut/brass/parchment player-facing frame and control tray.

The specific new art mismatch we found in the live renderer: many wooden route sections were drawn from a **128 × 32 sprite strip** and magnified at the higher camera zoom. The 1536px original painted landscape is considerably more detailed than those route strips. The result can feel like scaled-up digital lines floating over the scenery rather than wooden pieces belonging to an illustrated game board.

## Implemented first pass

- **High-resolution vector carved roads in live illustrated mode:** when the QA art trail atlas is installed and the `ARTWORK` trail style is chosen, only the *immersive live playfield* now draws in-layer carved-wood road sections at the actual Canvas resolution. They use ground shadow, dark wood edge, color-coded green/amber/purple inlay, a beveled warm-wood surface, fine woodgrain and sparse cross-section joins. The renderer does not require a road sprite to paint the new actual sections, although the existing atlas still decides the QA art-style availability.
- **Engraved construction detail, not fake game nodes:** tiny join lines are calculated deterministically from each existing line segment's length and scale, capped at seven per edge and omitted for short/dense edges. They never receive input, label a new location or change the legal graph.
- **Carved Start and Finish sockets:** raised honey-gold and green wooden rings anchor the already-authoritative start/finish nodes; their center stays exactly at the original graph coordinates. Their size is bounded against neighboring nodes on dense maps. Existing labels and actual player coins draw over them.
- **Unchanged overview:** the independent whole-course Full Map uses the existing route atlas and compact markers, not the large carved pieces. A full overview remains readable after the previous 20% scenic world enlargement.
- **Comparisons remain possible:** original QA `Printed` and `Simple` trail styles, and Classic Map, remain available for A/B review. Normal game progression and previous saved visual preferences are not migrated or rewritten.
- **No generated bitmap replacements yet:** this pass is deliberately source-rendered and scalable. We still need side-by-side visual inspection and eventual editorial polish of actual scenic backplates, large landmarks, and token portraits rather than accepting every existing QA-preview PNG automatically.

## Exact technical guardrails

The new `drawCrossroadsCarvedTrail` is called under `BoardCanvas` only for `immersiveWorld && illustrated && effectiveTrailStyle == ARTWORK && trailAtlas != null`. All render points come from `position(board.node(id))`. Start/Finish use `nearestSpaceSpacing`, shared for the other route-space rendering. The existing tap hit test and camera transforms do not reference decorative joins or rendered colors. The board backplates, stored board IDs, RNG, turn structure, match snapshots, local progress, installed package ID, and LAN protocol are unchanged.

JUnit tests cover deterministic seam placement, maximum decorative joints, short/dense edges, invalid input, safe/risky/mischief inlay distinction, and start/finish radius on dense and normal courses. Existing app geometry, JNI-free core, save/recovery, and rules checks must remain green on the **exact** VISUAL-83 branch head before calling a new APK verified.

## Actual owner-phone comparison required

Check **Classic/Woven Trail first** in the next exact-head signed QA APK: start area at default Follow; one long safe/risky route; the first 2-way junction; a shelter/hazard; Finish; full map. Compare against QA1614 at the same screen positions and zoom. Then check Quick/Brookside (fewer, longer segments) and Long/Cedarwood (many tight nodes) at large Android text and all two-to-four seats.

Accept only if wooden roads look sharp rather than upscaled, illustrated terrain still dominates, traveler pieces can be distinguished, risk colors remain accessible, fork exits are not buried below the action dock, special sockets are not confused with additional game spaces, and zoom/pan can return to the active traveler. The new board rim and control tray from VISUAL-82 should be visible but not compete with the board.

### Not yet proven

This is **source code plus unit plans**, not verified owner-phone art acceptance. A green CI run proves compilation, checked rules and fixture validity but cannot prove style, legibility, feel, fun, or that the decorative join lines are visually tasteful. If the new scene is over-detailed, reduce wood joinery before sacrificing route clarity; if the scenery still feels too small, investigate a cohesive large-landmark/backplate redraw, not ever-increasing camera zoom.

Existing signed QA1614 remains the last explicitly owner-playtested baseline until a newer verified build is installed. **No merge, signing change, public release, game mode, or asset provenance approval is authorized.**
