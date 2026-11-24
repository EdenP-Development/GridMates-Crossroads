# VISUAL-90 — Reusable environment art

Extends ARTREADY-38 rather than replacing the existing art system. Stacked on #89. Storybook remains the default, and current procedural Overhead scenery is preserved.

## Inventory
Nine existing packaged PNGs (three course backplates, home, shelter, hazard, trail, token and marker atlases); six previously planned slots. The new optional environmental atlas is slot 16, planned, not a delivered illustration. Its renderer is implemented.

## First production sheet
One 1024 × 1024 transparent 8-bit RGBA PNG, 4 × 4 fixed cells of 256 px. No text, borders, backgrounds, road markings, pawns, logos or gameplay symbols. Use the original Woodland cut-paper art bible: warm parchment highlights, restrained forest-green contours, muted natural greens/ochre/terracotta, lightly layered paper shading, overhead view. Each sprite is isolated, centered, with generous transparent margins. No cross-cell bleed; row 3 remains transparent.

| Row | Col 0 | Col 1 | Col 2 | Col 3 |
|---|---|---|---|---|
| 0 | broadleaf oak canopy | wildflower cluster | mossy rock | cottage roof |
| 1 | meadow flower clearing | three fir crowns | orchard quartet | bush |
| 2 | single fir crown | mushroom cluster | short fence | creek reeds |
| 3 | reserved | reserved | reserved | reserved |

The seven existing feature kinds map to cells 0–6; remaining five cells are reserved for later authored placements. The original two bridges stay graph-aligned and are not replaced by generic sprites.

## Import and acceptance
1. Create original illustrations using the existing art bible and approved visual references. Review the whole sheet together; do not claim approval of an image from a prompt.
2. Export exactly 1024 × 1024 RGBA; inspect each occupied cell and transparent margins, verify no clipping/empty used cells, and compare normal phone zoom and Full Map.
3. Add the exact PNG at the registered path, document actual origin and rights, record SHA-256 and mark manifest entry approved for QA. No sibling asset transfer without provenance/rights review.
4. Run the existing art checker and signed Android workflow. Missing or wrong-dimension image uses procedural fallback, preserving all prior landmarks.
5. On the same match, compare both art options in meadow, forest, village and both crossings. Owner decides direction before broad rollout. No graph, saved-state, LAN or rule changes.

## Runtime contract
One optional asynchronous bitmap loader for the overhead preview, fixed source cell mapping and board-relative size. Art draws below routes, nodes, pieces and interaction targets; no decorations added to Storybook or other courses. The seven mapped square footprints have half-diagonals smaller than existing graph clearances. Test registry, source cells, bounds, mapping and dimensions. Asset pixels and actual phone rendering are not tested until an illustrated PNG exists.

## Next
Produce the actual cohesive sheet; validate and import it, then capture comparable live screenshots. Do not call the pipeline milestone a completed visual overhaul. Continue to preserve QA1674 and any later verified candidates; no main merge.
