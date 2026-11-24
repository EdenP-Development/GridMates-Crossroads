# VISUAL-89 — Regional landmarks

Stacked on VISUAL-88 #88; prior QA1674 (VISUAL-87) remains available. The overhead Woven prototype is an opt-in QA art direction; Storybook remains the default and is not modified.

## Authored landmarks
Three small, spatially fixed scenes complement the original 20 ornaments and two graph-aligned bridges:
- Meadow wildflower clearing at (.28, .49), with warm straw/cream blooms and leafy ground.
- Forest fir grove at (.60, .68), with layered dark-green radial conifer crowns.
- Village orchard at (.94, .83), with four cultivated crowns and muted fruit accents.

Use the existing normalized board coordinate frame. Each placement is conditional on at least .09 normalized clearance from every real graph edge/node and .085 separation from prior ornament centers. All drawings are under the existing playable graph. No new nodes, interactions, rewards or route changes. Geometry, saves, art selector, signing, and network unchanged.

## QA
Authored 38-node Woven course should yield 23 ornaments, including two cottages and the three distinctive landmarks. Tests assert placement, clearance, deterministic reproduction and removal when a hypothetical playable node occupies a landmark. Existing bridge and preview gate checks remain. Exact-candidate Android unit/build/lint required before distributing a candidate.

## Visual acceptance
On a physical phone compare the overhead and Storybook options during normal travel, at junctions and in Full Map. Check that new features are recognizable, not mistaken for legal nodes, and do not distract from highlighted choices. Compare local terrain near meadow, forest, village, and both stream crossings. These vector scenes are a prototype, not final illustrated asset approval. Do not replicate to other courses until the Woven art direction is chosen. Next: evaluate actual rendered screenshots and decide on coherent authored illustration assets, then complete device/installation/LAN acceptance.
