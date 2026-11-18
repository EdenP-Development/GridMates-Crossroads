# HOME-39 — Illustrated-first Crossroads landing (draft QA)

November 18, 2026. This slice is stacked on ARTREADY-38 and GAMEPLAY-37; **do not merge to main** without owner approval.

## What changed

- The existing optional woodland hero (or its procedural fallback) now appears directly under the Crossroads title and tagline, **above** the setup controls.
- The primary **Start journey** action is displayed in a persistent bottom action tray with an explicit seat/mode summary. It remains reachable on short phones and while scrolling setup; no new setup screen or required account.
- Solo vs AI remains the initial default; Pass & Play, 2–4 players, all three course lengths, v3 offline switch, nearby Host/Join, Help, and backup controls stay present and functionally unchanged.
- Important: this changes **only home presentation**. There are still zero approved standalone PNG artworks in ARTREADY-38's manifest. The three original/experimental rules, saves, player identities, LAN protocol and signed QA identity are untouched.

## What the build proves

Run Android compilation, unit tests and lint on the exact head, plus art registry validation. Do not claim phone acceptance or that artwork has been added from a green build alone.

## Owner phone acceptance checklist

1. On a small portrait phone, open the app: title, woodland visual, and Start action should be understandable without hunting through a form.
2. Start a default Solo game directly; confirm the existing game loop and saved records remain intact.
3. Scroll to choose Quick / Classic / Long, toggle Pass & Play and 2/3/4 seats, then confirm the bottom tray describes the chosen seats before starting.
4. Reach Host Nearby / Join Nearby, How to Play and About & Data by scrolling; make sure the bottom tray doesn't cover the last controls.
5. Turn large Android font scaling on and repeat; inspect the action tray, scroll space, keyboard/accessibility semantics and landscape if available.
6. When real artwork is installed later, ensure the 1600×640 home panorama crops correctly at 144dp, with a clear route to the primary action.

## Remaining critical work

The four Batch A images are still review candidates, not in the repository APK. Woven scenery needs actual overlay contrast approval; the shelter/hazard encounter pair needs independent native-resolution artwork. Also pending: a real-phone GAMEPLAY-37 playtest, real timed Quick/Classic/Long matches, and the first actual two-phone LAN session.
