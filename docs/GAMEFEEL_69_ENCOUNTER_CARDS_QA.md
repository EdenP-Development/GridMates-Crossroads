# GAMEFEEL-69 — Integrated illustrated encounter cards

**Status:** staged Android QA draft on GAMEFEEL-68; no merge to `main` or public distribution.

## Why this change
Owner screenshots show the gameplay world's woodland illustration, carved routes, and large fox/owl tokens have improved, but the **shelter and hazard decision panels** still look like an ordinary app: a 44dp image, disconnected title and two Material buttons. A real board-game encounter should resemble a small printed card **without covering more of the board** or adding a new compulsory page.

## Implementation
- Use the already packaged `event_shelter.png` / `event_hazard.png` through the existing async optional art slot. **No replacement or generated file**.
- Replace thumbnail+heading+buttons with **one compact illustrated parchment event card**: 62dp framed existing artwork, warm-gold accent, short title and honest context, two clear raised decision buttons.
- The existing legal choice list from `GameSession.availableEncounterChoices()` is passed through exactly unchanged. Original `localEncounterChoice(option)` callback resolves game state and host authorization. Copy from `encounterActionCopy(option)` continues to show:
  - Shelter: SUPPLIES / +2 spaces; COVER / +1 & shield.
  - Hazard: GAMBLE / Face hazard; BRACE / Stop & shield.
- The card adds only press-scale visual feedback matching GAMEFEEL-67. No shake, modal, force delay, RNG or decorative game-state action.
- While an active human is choosing, hide the **redundant two-line latest-action label** immediately above the card, reclaiming screen space. The exact action note is still available under **Rules & last action**. For AI/remote/waiting states, retain existing status messaging.
- If existing art is absent/corrupt, a stable typographic fallback is shown. The image is decorative and the title/action copy remains accessible text.
- Preserve die/remaining-steps indicator, fixed action dock, camera, road geometry, three courses, LAN, state snapshots, and all original assets.

## Verification
1. Exact-head GitHub Android QA build/unit/lint/public-validation/signing with no skipped failures; design data QA.
2. Phone compare M68 vs M69 at Woven Trail shelter and hazard. The card should feel printed and connected to the game art, but **both full action labels and effects must be visible without scrolling or clipping**.
3. Test at maximum practical Android font settings and short portrait heights, including two buttons on the bottom edge of the screen. Rules & last action remains reachable.
4. Verify that tapping each decision once yields **the same result** as earlier QA: SUPPLIES, COVER, GAMBLE, BRACE. Host/guest authorization and waiting states remain correct.
5. Test Quick/Classic/Long, AI encounter resolution, pass-and-play, pause/resume during an encounter, and full-map inspection. Do not invent extra choices from presentation.
6. Check event art decode fallback, cropped image alignment, color legibility, accessibility and subtle press feedback. No new haptic/audio loops or graphics performance regression.
7. Keep work in draft until actual phone acceptance; two-phone LAN and production signing remain separate release gates.

## Next targeted visual priorities
After phone acceptance, unify in-match dice choice buttons and results celebration styling, then add judicious optional wooden tap/roll sound cues. Avoid overwhelming the player with repeated effects in Long matches.
