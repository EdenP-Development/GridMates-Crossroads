# EXPERIENCE-60 — One connected board-game turn

**November 19, 2026 · DRAFT, stacked on GAMEFEEL-59. Do not merge to `main` or publish.**

## Owner device problem
Actual Build 1479 screenshots confirm the illustrated world, route geometry, zoomed travelers and fork framing are much more legible. Yet match interactions still feel like a normal mobile form: a tall scrolling parchment panel duplicates phase, latest action, instructions and rules; route comparison can slip below the bottom of the visible panel; enormous white/color path stripes conceal the actual illustrated road; the map inspector and results are conventional dialogs. The app is native Android/Compose, but its **visual hierarchy** feels webpage-like.

## First integrated pass (this PR)
- The **active gameplay action** is always positioned before lengthy rules and narration. A shortened floating parchment action dock retains the dark-green turn strip, a single-line latest-action cue, and the authoritative die/remaining-step indicator.
- ROLL: bold **STEADY / PUSH** with concise honest outcomes for Lab v3; a large dice button for classic/offline and nearby races. The existing deterministic 2/5/1/6/3/4 decorative dice preview now appears briefly **over the board world**, with zero changes to the actual die/roll RNG.
- ROUTE: the active junction title and an explicit **COMPARE OR CHOOSE PATHS** control remain on-screen. Tap existing graph-legal illustrated routes directly, or use the existing accessible comparison sheet for complete risk/remaining-distance descriptions.
- ENCOUNTER: short, high-contrast side-by-side options with both their named action and concrete effect text. Full rules still accessible behind **Rules & last action**.
- TARGET: direct opponent buttons, no extra repeated explanation, and shields remain identifiable.
- AI/travel/network waits: no misleading human action buttons; a readable motion/waiting indicator is used instead.
- Rule explanations, latest-action detail and encounter replay may be expanded, but use only spare scrollable space; decision controls are never nested in a scrollable text region.
- Top HUD: replace large app-like cream seat boxes with smaller dark/cream game chips; show the **actual course name** instead of a clipped `CROSSROADS · QA · Wove...` line. QA identity is retained visually and in accessibility metadata; release-surface checks must still pass.
- Legal route visual cues: remove the global translucent rectangle (hard-edged dimming) and the oversized cream strips. Tint only the first 14–70% of each outgoing legal edge, with restrained endpoint rings. The hit-testing engine still accepts the full original graph segments; geometry, board nodes and road atlas aren't modified.

## Explicitly unchanged
Core rules, graph/boards, AI, RNG, saved checkpoints, results stats, application ID/signing, LAN protocol, tap hit radius, zoom camera, three illustrated backplates, animal atlases, original painted roads, inventory and monetization. The current Lab v3 rules remain experimental.

## Source/automated gates
- Core and app unit tests, lint, QA/public-identity preflight, optional art provenance/hash checks, signed APK and immutable certificate on the **exact final PR head**.
- New pure tests assert truthful encounter options, compact panel height presets, and partial painted-route cue geometry.
- Existing camera/touch/animation, save replay, core board and LAN tests must remain green.

## Phone acceptance checklist
1. Install over the previous QA app; never uninstall/clear app data. Verify existing local record and any saved journey remain.
2. Woven Trail, 2 AI players: make a full sequence `ROLL → traveler moves → choose a highlighted path → Shelter/Hazard option → AI turn`. The **primary button must never be below the viewport**, including while the history/details is expanded.
3. Roll while zoomed 2×: the preview die should be visible **over the world** and disappear once authoritative movement begins. Rolling does not add a second outcome or skip a player's turn.
4. Fork choice: compare paths via the now-visible button, inspect full map, return; selection must match the exact legal route. Painted road geometry must remain visible underneath accents; no rectangular shadow or oversized white bands.
5. Shelter/Hazard: read **SUPPLIES +2**, **COVER +1 & shield**, **GAMBLE**, **BRACE**. Compare explicit consequences to the original Lab v3 core rules, not just appearance. Accessible rule details remain discoverable.
6. While AI is playing/another human holds the device/LAN guest waits, verify disabled or absent action controls and clear wait identity. No AI decisions should be labeled as yours.
7. Test 3–4 seat, Brookside Quick, Cedarwood Long, small portrait + larger font, pinch/pan and follow reticle. Check dark/bright background contrast and notched devices.
8. Finish a real match: results must remain reachable and statistics correct. Test pause/resume/force-stop and save integrity separately.
9. Owner may reject visual direction despite CI passing; do not auto-merge or ship.

## Later work, intentionally NOT piled into this milestone
- Dedicated animated Results and Full Map design instead of conventional dialogs.
- Sound/haptic options, improved camera ease, accessibility-reduced-motion behavior.
- Final home/setup screen conversion from long form to simple illustrated game menu.
- Real two-device Wi-Fi acceptance and release signing/privacy/IP review.

This is a cohesive **one-turn** presentation pass, not a claim that the entire game is finished.
