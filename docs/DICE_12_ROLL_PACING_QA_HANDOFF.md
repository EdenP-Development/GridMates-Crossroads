# DICE-12 — Slower die roll based on QA1082 owner phone feedback
**Status:** source + focused timing tests committed; new Android QA and physical acceptance pending.
**Branch:** `feature/home-results-v1.1` / Draft PR #17. No merge to `main`.

## Owner observations (November 17, 2026)
The owner installed and photographed QA1082 on a physical Android phone. Eight provided images show:
- Home menu, match opening and large board viewport, first-use camera tip, Meadow Crossing choices, all three Old Forest Junction options, later player turns, and match-end results.
- Critically, the owner reported: **"The dice 'rolls' really fast."**
- The older visualization used fixed-speed face changes for **10 × 45ms = 450ms Cozy**, and **4 × 25ms = 100ms Brisk**. These were blink-speed animations.
- The result dialog screenshot exposes the "Last recorded encounter" heading but its details are below the immediately visible scroll area. This is logged as separate RESULTS polish; avoid smuggling that UI redesign into DICE-12.
- Screenshots substantiate Android display and readable route choices but do **not** establish multi-touch responsiveness, all-time install-over safety, or automated event reliability.

## Implementation
- `PresentationSpeed.COZY`: 11 preview faces; each frame delays `55 + 2 * frameIndex²` ms, totaling **1375ms**.
- `PresentationSpeed.BRISK`: 8 preview faces; each frame delays `50 + 2 * frameIndex²` ms, totaling **680ms**.
- The increasing spacing gives a short lively opening and a slower finish instead of a flash.
- A subtle Compose tilt and squash effect accompanies the preview frames; resets to normal at rest.
- The preview uses a deterministic six-face appearance sequence; **no additional PRNG calls** and the actual die is still rolled **exactly once** with `GameSession.roll()` after the preview.
- The chosen pace is captured at the start of each animation so toggling Cozy/Brisk mid-roll cannot alter the remaining preview cadence or cause duplicate rolls.
- No changes to gameplay, routes, bot logic, dice outcomes, events, saved records, APK package identity or signing key.

## Tests / acceptance criteria
- [ ] New exact-head Android compile, JVM tests, lint and signed APK identification.
- [ ] PresentationPacingTest confirms frame counts, durations, monotonic deceleration, and Brisk shorter than Cozy.
- [ ] Real phone Cozy roll lasts visually about 1.4 seconds, with recognizable changing pips rather than a flash.
- [ ] Brisk still feels quick but readable at about 0.7 seconds.
- [ ] Only one authoritative roll per tap. During a roll, button remains disabled and no extra rolls occur.
- [ ] If the die lands at a junction, displayed pip result matches remaining steps / engine state. Other turns may complete immediately and show last action instead.
- [ ] During rolling, small wobble looks natural, legible and not distracting; no rapid flashing or stuck tilted die.
- [ ] Large fonts / 2–4 seats: roll action and panel remain readable. Three-way route chooser, AI handoff and bot movement unchanged.
- [ ] QA1082-to-new QA APK installs on top of the existing test app; verify wins, completed matches, and gesture-tip dismissal persist.
- [ ] Screenshot-based results encounter clipping is a follow-on UI quality issue, not fixed or claimed fixed here.

## Evidence boundary
Phone screenshots verify that QA1082 displayed the home, board, choices, AI, and results; they do not prove new DICE-12 animation. Android CI success is not real-device acceptance. Do not merge/release from draft without owner direction.
