# VISUAL-87 — Board-first full map and continuation checkpoint

## Recovered state (2026-11-23)
- VISUAL-84 / #84: Storybook remains default; optional QA-only Woven overhead experiment. QA1672 was the owner-tested build.
- VISUAL-85 / #85: same-match Storybook/Overhead Android screenshot harness. Run 45077010249 passed and artifact 14775173621 was uploaded. Screenshot art approval is still open.
- VISUAL-86 / #86: carved roads narrowed to 79%, warmer wood, reduced seams and compact fork tray. Android run 45254574542 passed; signed QA1673 ZIP artifact 14857967736. Owner-phone test and install-over save verification still open.
- VISUAL-87 is stacked on #86. No merge into main or production release.

## Owner direction
Make Crossroads feel like a handcrafted digital board game, not a small illustration under oversized controls. The owner should see the art at normal play zoom, not have to pinch in to appreciate it. Preserve walnut framing, woodland player pieces, parchment, authoritative road geometry and touch targets.

## Implemented in VISUAL-87
The read-only Full Map dialog uses 4 dp outer horizontal padding instead of 12, 6 dp inner horizontal padding instead of 12, and 3 dp board-frame padding instead of 8. This returns 38 dp of horizontal board space on typical phone widths. The board takes a square canvas of the actual available width; the outer dialog is capped at 680 dp on larger displays to avoid stretching the board. Short displays retain scrolling; close and return controls remain outside the scrolling content. The legend stays visible in the scroll content, and the map explains existing inspection gestures.

No route callback, graph, saved state, live camera, artwork, signer, or network change. Full Map remains read-only.

## Acceptance checklist
- Android unit/build/lint and design checks on exact candidate revision.
- Open Full Map on compact and typical portrait phones and in landscape: artwork is square, all routes present at initial fit, close/return available; scroll if needed.
- Verify pinch/pan and returning to an active junction leave snapshot, remaining steps and legal choices unchanged.
- Compare both art variants on the same Woven race; owner selects preferred treatment only after physical-phone review.
- QA1673 (or later verified install-over candidate): owner confirms save retention, thinner roads, visibility, comfortable route choices and larger scene readability.
- Real two-phone LAN host/join, play and reconnect remain open.
- No public release until these gates and release packaging checks pass.

## Next visual work
1. Compare Storybook and Overhead using matched screenshots; evaluate actual scenery scale, not just roads or token size.
2. Use normal-zoom evidence from open fields, village, bridge and junctions to tune follow framing and local landmarks; preserve graph and legal path visibility.
3. Refine selected art style consistently across Woven and other courses, with owner approval before replacing original assets.
4. Revisit main-menu board-game identity and UI cohesion after gameplay scene is approved.
