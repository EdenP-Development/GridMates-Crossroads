# GAMEFEEL-62 — Board-first legibility on a real Android phone

**2026-11-19 · stacked draft on EXPERIENCE-61. Not merged or released.**

## Owner evidence
Real screenshots of signed QA Build 1509 show that game entry, world, movement and results are much better. The outstanding problem is structural: **roads/trails, fork nodes and miniature characters still feel too small against large high-detail scenery**. The player reads the landscape first, not the board. At the route decision the outgoing roads and game piece must be unmistakable; do not make scenery larger merely to hide this by excessive camera zoom.

## Changes
- Live board only: painted trail atlas and fallback trail strokes **1.32× → 1.67×** of base artwork (about **27% wider** relative to QA1509). Graph routes, road coordinates, sprite atlas and backgrounds unchanged.
- Live board only: player token drawing scale **1.42× → 1.91×** (**35% larger**). This also enlarges animal artwork, number medallions and clustered-offset spacing together. Preserve unique seat numbers and colors.
- Live board only: junction/node medallions **1.18× → 1.55×** (**31% larger**).
- Legal fork choice rings grow proportionately; the restrained color cues continue on just the initial section of existing painted roads, avoiding white UI tape over the artwork.
- Follow camera: opening roll 2.00× → 2.15×, later rolls 2.40× → 2.48×, junction 2.15× → 2.20×, encounter 2.55× → 2.60×, target 2.30× → 2.40×. Keep changes modest because geometry itself carries the major readability increase. Junction camera retains 79% weight on fork origin instead of 72% to avoid focusing empty midpoint terrain.
- Full-map inspection remains unchanged with 1.0× trail, marker and token multipliers; camera manual/pan/zoom controls, hit-test math, saved records and rule engine are unchanged.

## Test gates
1. Automated Android source/gradle unit/lint/signing/public-surface; presentation tests assert the exact new scales and untouched overview.
2. Owner's **Build 1509** baseline vs new signed QA APK: capture same start, moving, two-path junction and finish at the same resolution. Verify **fox/owl art and seat medallions are obviously larger** while other players remain individually identifiable.
3. Look for path crowding/crossing intersections and seams in the repeating atlas, especially Cedarwood Long. Ensure colors remain distinguishable and fork signs/risk categories legible.
4. Tap both legal fork branches by touching the rendered road and endpoint. With camera zoom/pan and route sheet, no inaccurate path selection or blocked taps.
5. Check action dock never conceals a player at start/finish, and camera reticle returns to the piece. Test tablet/landscape and small portrait/large font, 2–4 players.
6. Verify full-map still shows entire course with small markers, all player pieces, and readable geometry. No board IDs, RNG, turn history, save version or LAN message change.
7. Device screenshots/approval required; a green CI is **not** proof that larger painted sprites look correct or don't overlap on a handset.

## Governing release rule
This is a coherent **scale and focus** pass. Stay stacked on PR #61; no automatic merges. Keep the approved original woodland background image exactly as provided.
