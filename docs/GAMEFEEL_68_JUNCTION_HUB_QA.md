# GAMEFEEL-68 — Carved tabletop crossroads hubs

**Status:** stacked draft above #67; no merge to `main`, no public release. Screenshot approval required.

## Visual defect identified on real owner screenshots
On Woven Trail, a 2- or 3-way fork can look like multiple giant wooden strips crisscrossing: the painter independently draws all incoming/outgoing roads right up to their common node. Even after M65 reduced painted road scaling, the direction is legible but the physical intersection reads like stacked timber instead of a tabletop board **decision point**. The route graph and touch geometry are already correct. Do **not** re-author paths to solve a surface overlap.

## Changes
- **Live illustrated, painted-road board only:** draw a recessed wooden junction cap precisely at each existing author-defined `fork` or `merge` node **after** route art and **before** route/event symbols and player tokens.
- The hub is a quiet layered dark-forest/wood/brass/cream disk, not a new event/choice, fake space or particle effect. The original painted fork medallion, ? badge, selected route cue, and overlapping traveler portraits remain visible above it.
- The hub defaults to a modest radius (29 draw pixels normal, 24 on dense course) and caps to **35% of the minimum distance to any other authored graph node**. If too crowded to clarify (<8px), it is omitted entirely rather than adding another tiny coin.
- **Full-map inspector, printed/simple route styles, non-illustrated board, text/action dock:** unchanged.
- Graph node IDs, graph edges, AI/dice/encounter/turn logic, network protocol, persistence, app ID, launcher icon and art atlases: unchanged.

## Target QA
1. Exact-head Android compile, unit tests, lint, public variant validation, and signed QA archive. M67's reusable control treatment is inherited but should still be run.
2. At Woven Trail 2-way fork and 3-way fork, inspect whether the center reads as a **single actual physical intersection**, with branches obviously emerging from it instead of four textures piled together.
3. On Brookside Quick and Cedarwood Long, verify density cap does not hide nearby movement nodes or event markers.
4. Select every highlighted branch by touch; compare route metadata sheets, pan/zoom/recenter and finish; no change in legality/choice positions.
5. Check enlarged fox/owl pieces and per-space landing effects remain legible above the hub. Observe scene in motion rather than only stills.
6. Check full map still retains small original junctions; no visual switch carried into saved games; successful install over QA1532.
7. On a small Android phone with larger text, top HUD and action dock remain accessible. Confirm event choices are unchanged, with no stalls or cut-off buttons.

## Design lesson
A tabletop game can have substantial painted roads without making every crossing a pile of texture. **Separate the road surfaces, the authored intersection, and the pawn/decision layer.** The benefit must be validated from device screenshots, not asserted from CI.
