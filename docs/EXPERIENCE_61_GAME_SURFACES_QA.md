# EXPERIENCE-61 — Game entry, full map and results presentation

**November 19, 2026 — STACKED DRAFT on EXPERIENCE-60. Do not merge to `main`.**

## Why
Owner's physical Android screenshots: woodland board, zoomed travelers and camera now read well, but menu and dialogs still feel like a tall app/form. Gameplay should feel like a polished *digital tabletop*. These refinements change the presentation surfaces around an active match, not the rules.

## Changes in this milestone

### Home = entry into the game, not configuration page
- Preserve the original Home hero illustration as a real packaged asset. Expand it to a ~202dp cover with **Crossroads · Every path is a choice** over the art.
- The first visible choices are Solo vs AI / Pass & Play, seat count, and Quick / Classic / Long course. A persistent **START RACE** button remains at the bottom.
- Move less-used presentation, Lab v3 experimental-rule switch, nearby Host/Join, record, help, data backup and QA notes behind the explicitly labeled **Nearby play · Appearance · More options** expander. No controls are removed, no new menu steps are required for the default solo race.
- On scroll, preserve the current selected course, seat count and modes; toggling the expander must not mutate them.

### Map = large in-game course inspection
- Replace Android's small default AlertDialog with a width-flexible, darkened, full-screen **read-only map stage**.
- Preserve the original full-board zoom/no-animation painter, painted markers and authentic positions. Maximize square map at available width and put the legend beneath it.
- Header identifies the actual course. **BACK TO THE TRAIL** remains pinned and tappable even if the legend must scroll; on a route sheet, close returns to that decision as before.
- Dismissing map cannot select a route or change GameSession; separate route-choice sheet still pauses AI appropriately.

### Result = race moment, not a generic form
- Keep exact existing Snapshot / race outcome / timing / encounter summary data; do not invent rankings for unfinished players.
- Dark evergreen victory header and game-stage parchment with a scrollable match receipt.
- **RACE AGAIN**, **View course**, and Main menu are pinned below the receipt. **View course opens the actual full-map inspector**, not simply the underlying camera. Retain host-only rematch authority, LAN guest disabled state and prior result persistence.

## Explicitly unchanged
Original artwork bitmaps and manifests, app signing/IDs, saved data, engine RNG/rules, AI, networking/authority, route hit geometry and active-match camera. The map is still inspection-only. No game is considered officially finished by CI alone.

## Android gates
- Release-surface preflight, app/core tests, lint, signed QA artifact, artwork manifest and provenance.
- Test Solo vs AI, Pass & Play, Quick/Classic/Long, nearby Host/Join buttons, old QA local stats, Lab v3 selection and Classic/Illustrated style.
- Test 2-, 3-, 4-player setup and return from a finished race; do not reset wins or checkpoint by expanding options.
- Force-stop/restart and install over the previous QA without uninstalling. Test larger Android text, small portrait and landscape; Start and back/rematch always reachable.
- Inspect map at each fork and during an encounter; it must never make a turn or become an accidental route-tap target.
- At Finish: verify winner, elapsed time, encounter outcome, players, rematch, view board, main menu. LAN guest cannot rematch.
- Verify correct UI typography/contrast and the original Home artwork displays on real phone.

## Project governance
Do not merge dozens of stacked draft PRs automatically. Maintain an isolated integration stack until the owner approves screenshots and physical gameplay, then perform a separate release readiness audit. Current state source: `docs/PROJECT_STATE_2026_11_19.md`.

**Device acceptance is pending. This is not an app-store-ready release.**
