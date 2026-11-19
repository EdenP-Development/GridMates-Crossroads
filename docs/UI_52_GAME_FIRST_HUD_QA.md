# UI-52 — Game-first Crossroads HUD prototype

**November 19, 2026.** Stacked on draft PIECES-51, MARKERS-50, PIECES-49, UI-48 and artwork work. This changes **presentation only**, with no merge into main.

## Why
User's real-phone feedback: the illustrated Crossroads board still feels like *a page with buttons*, not an interactive game. The old Android screen had an app header, large player cards, a board card, explanatory paragraphs, and a second full-width app card for actions. The visual hierarchy treated the board as content in a form.

### First-pass target: the board is the playfield
- **Playfield first:** full-screen dark tabletop environment; square illustrated board no longer buried inside a scrolling page.
- **On-board top HUD:** match/course, player identities, map access, pace, options; compact and persistent while the camera follows motion.
- **Contextual action dock:** warm printed tabletop panel with a clear current turn/phase, concise action information, and big colored decisions at the bottom. Long text lists scroll inside the dock on smaller phones / large fonts instead of pushing the board offscreen.
- **Three-route forks:** anchored bottom tactical choice sheet rather than a centered system-style full dialog. The three legal routes and exact descriptions still scroll, and Inspect full map remains reachable.
- The HUD uses drawn native Compose components with accessible descriptions. No webviews, HTML, online art assets, account systems, or remote requirements.

## What remained unchanged
- Core route graph, camera graph coordinates, pan/pinch gestures, tap to refocus, roll odds, AI, encounter choice effects, game turns, LAN messages, saved checkpoints, resumes, and results screens.
- All illustrated board/route/token/marker art and their SHA records; the HUD borrows colors from the existing ARTREADY-38 visual contract.
- Both high and low-risk route detail text and the actual encounter action callbacks. The Steady/Push odds are merely shortened in presentation.
- The previous build remains the easy comparison/rollback. This is a new **draft** stacked on PR #51.

## Verification gates
- CI: Kotlin/Android compile, unit tests (including phase HUD labels), lint, art contract, signing, and APK archive integrity.
- Real phone: check small portrait and landscape on 2/3/4 players, both AI and pass-and-play; board remains unobstructed enough for orientation; no hidden final route buttons; dock scroll with increased font size; map/pacing/options accessible.
- Camera: pinch/drag/double tap still works outside HUD touch regions; zoom does not distort the board or move tokens; hover menu does not swallow gestures.
- If action dock obstructs too much board on smaller devices, lower its max fraction / introduce temporary collapse affordance; do not remove decision descriptions without a discoverable way to inspect them.
- Verify entire 3-way fork chooser can scroll to third route and still reach map button. UI-48's text overflow fix must remain.
- Verify local saves, finished-match records and install-over progress; CI cannot prove physical save preservation.

## Known limitations of this first pass
- Some turn panels still contain lengthy explanatory copy. The final game-quality pass will replace remaining long-form form controls with icons / concise contextual labels **without eliminating accessible details**.
- This is a layout prototype, not final approved visual design. Large/full-screen screenshots required before a release decision.
