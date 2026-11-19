# PIECES-49 — Optional Woodland traveler tokens (QA preview)

**November 18, 2026.** Draft stacked on UI-48 (which stacks on TRAILS-47 and prior artwork milestones); nothing is merged to `main`.

## Owner-phone evidence and intent
The Woven Trail route artwork now looks increasingly cohesive at ordinary zoom, but numbered red/blue/green/gold circular player pieces still feel visually separate from the woodland illustration. This experiment tests **actual original local transparent artwork** behind the required player numbers.

## Art provenance
- `app/src/main/assets/art/v1/token_atlas_v1.png`: 512 × 128 RGBA, four adjacent **128 × 128** sprite cells (left to right: fox, owl, hedgehog, rabbit).
- Original OpenAI image generation on November 18, 2026, ID `c9ff8c19-2f97-4b06-adf9-e1dde9123d52`; **new icons for Crossroads**, not reused/copied Sudoku or Word Search assets.
- The rendered source contained RGB checkerboard (not a true alpha channel). Converted to alpha by isolating the external neutral-color backdrop, cropped and downsampled to a compact 24-color local atlas. Exact packaged SHA-256: `ca2f8dceb3679dff75e24870f4a5b993458b3f853b69df48b6c28a43bed570dc`.
- Pinned in the approved-slot manifest with explicit `qa-preview-only-not-owner-approved` review status; passing CI does NOT imply approval for release.

## Runtime behavior
- DEBUG / QA-only `⋯ > Pieces: Woodland / Standard` control, preference `qa_woodland_pieces_v1`. Default Woodland for first phone comparison; may switch to Standard at any point in an existing match.
- The portrait icon occupies the center of the existing *same-colored* tactile token. Mandatory player **number is redrawn in a separately outlined bottom-right medallion**, so it remains legible and the portrait is not hidden by a central numeral.
- All four pieces use identical hit targets, movement positions, local group offset, camera, scale, and layer order as the original.
- When artwork is disabled, absent/corrupt, or a non-QA build is running, the standard numbered tokens remain the fallback. Future per-seat portrait slots remain separately planned and untouched.
- The same choice is visible on live board and Full map. No network request, character unlock, selectable avatar system, new game rule, save codec, or nearby LAN change.

## Tests and acceptance
- `tools/check_crossroads_art_pack.py`: dimensions, PNG integrity/alpha, SHA and packaged budget.
- Unit tests: four canonical source cells, slot registry counts, debug-only permission, missing-art fallback and manual Standard selection.
- Android: compile/tests/lint/signing, and physical-device screenshot comparison of Woodland vs Standard at fit and 2×–3× zoom.
- Test 2, 3, 4 players on the start node; confirm all seat **numbers** are distinct, not obscured or confused with the underlying route nodes, with no overlap causing accidental route selection.
- Check Woven Trail, Brookside, and Cedarwood: individual seat colors must remain distinguishable on grass, forest, water, and pale paper. Compare progress markers and match completion.
- Screenshot the smallest normal-zoom board: reject the concept if 18–24 px painted animal faces are muddy or too visually busy. We may instead use simpler wooden pawn silhouettes.
- Install over the previous verified QA, never uninstall, and confirm historical win counts and any local saved game.

**Owner visual approval pending. All branches isolated.**
