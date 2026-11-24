# VISUAL-88 — Adaptive fork framing

Stacked on VISUAL-87 (#87), whose Android run 45303806646 passed and published QA1674 artifact 14880827515. Preserve that independently downloadable candidate.

## Why
Earlier owner feedback (QA1614) asked for a closer ordinary travel camera. Keep that established framing rather than reverting it globally. The latest owner screenshots show isolated strips of road and insufficient surrounding context. At a fork, the fixed zoom and origin-weighted anchor do not account for the real spread of exit paths.

## Implementation
Only immersive animated route decisions for the focused current player use the adaptive preview. Compute bounds of the hub and first 60% of each outgoing graph edge; target their midpoint and limit the preset zoom by available viewport width and vertical decision area. Clamp zoom to 1.65–2.16 to preserve illustrated animal portraits. The 36% screen-height anchor targets the upper visible area. Very wide forks may exceed this approximate envelope at the portrait-preserving zoom floor; no promise of full endpoint visibility. Manual pan/pinch and route choice buttons remain available. Ordinary rolls, encounters, targets, non-immersive views and Full Map are unchanged. No new routes, art replacement, movement, network or save changes.

## QA
JVM tests cover narrow versus compact forks, aspect-ratio differences, empty exits and unmeasured viewport fallback. Exact candidate Android checks must pass before distributing another APK. On actual phone compare F1–F5 Woven Trail, including Old Forest Junction and the stream crossings, in both Storybook and Overhead. Check visible hub and opening branches, pawn detail, no HUD/tray obstruction, legal taps, manual pan and return to follow. Also check compact portrait, landscape, Brookside and Cedarwood. This is a camera experiment, not approval of either artwork. No main merge. 

## Next
Review the actual paired screenshot artifact from #85 before choosing the art direction. Add meaningful authored landmarks and local detail where normal travel framing still exposes large empty terrain; do not replace the Storybook default until owner approves. Real-phone install-over saves, LAN and race pacing remain separate release gates.
