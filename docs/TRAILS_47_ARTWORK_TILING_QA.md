# TRAILS-47 — Painted road cap-and-center tiling (QA-only)

**November 18, 2026. Isolated draft stacked on TRAILS-46; not approved or merged into main.**

## Problem from owner-phone visual direction
The first real trail atlas in TRAILS-46 replaced a geometric-looking printed strip with illustrated moss and stones. However, the 128×32 road sprite was stretched horizontally from its fixed source width to every actual graph edge length. That distorts the small stones and moss on long branches, and could make the whole surface look like a resized sticker.

## Implementation
- Reuse the same verified transparent artwork and 13-slot manifest from TRAILS-46. No additional image, license, or theme permissions required.
- 16px left/right source cap slices maintain their faded end detail.
- The central 96px source area repeats at approximately native horizontal-to-vertical texture proportion. All segments share the same original route height.
- A deterministic pure layout function returns source intervals and precise destination ranges, plus a short-edge fallback and a **24-center-stamp cap** for older phones.
- Canvas rotates the resulting road pieces around the original graph edge, with route-specific colored ink underlay. Junction art, original circles/numbers, hit targets, movement, random rolls, saved progress, LAN, and course graphs remain unchanged.
- DEBUG-only Artwork / Printed / Simple switch remains; no new player-facing option. Classic map continues to use Simple.

## Automated review
- Unit tests cover short edges, long edges, dense boards, gap-free endpoint coverage, capped stamp budgets, and invalid lengths.
- Android QA must compile, run tests/lint, verify signed APK, and validate unchanged art checksums.

## Physical Android QA
1. Install over QA 1413; verify prior solo records and saved-match state survive (do not uninstall).
2. Compare Artwork on Woven, Brookside, Cedarwood at fit and maximum zoom.
3. Inspect long links for unstretched pebbles, seams, abrupt repeated stone patterns, and half-transparent cap gaps; try forks that converge from different angles.
4. Check route type distinction with accessibility needs in mind (safe green, risky amber, mischief purple). Also compare Printed and Simple on the same map.
5. Inspect animation while moving rapidly, panning/zooming, Full map, pause/resume, and a match ending.
6. Reject tiled artwork if repetition/aliasing is more conspicuous than the original stretch. In that case, actual **two or three hand-authored central motifs** and a nine-slice sprite pack will be needed.

**No claim of visual approval until user screenshots.** Review the APK on a device before deciding on further token/menu assets.
