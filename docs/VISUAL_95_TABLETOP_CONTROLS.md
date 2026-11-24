# VISUAL-95 — Tabletop navigation cohesion

The existing shared tactile primary action and choice components already establish the green, parchment and brass material family. This change extends that vocabulary to five navigation controls: live Full map, route sheet Back to board and Full map, inspector Close map and inspector return action.

The new TabletopUtilityButton uses a minimum 48dp height, native clickable Surface semantics, readable text, restrained 95ms contact animation, thin edging and either dark forest or parchment face. The inspector return reuses TabletopPrimaryActionButton. Existing callbacks, accessibility labels, route and checkpoint behavior are retained. No game logic, networking, artwork or camera changes.

## QA
Compile, unit-test, lint and package the exact candidate. On compact portrait, landscape and large-font phones, verify the top HUD fits alongside its title/options, route-sheet utilities do not clip, and map close/return remain visible and correctly labeled. Verify opening and dismissing Full Map during a fork preserves legal choices, remaining steps and saved state. Inspect visual hierarchy: secondary actions must not compete with roll or route decisions.

VISUAL-92 retry 45316542594 failed on the system Pixel Launcher ANR before START RACE; it does not invalidate the six captures and successful preceding assertions from run 45315961076. It is not a game visual approval. Keep emulator infrastructure separate from real-phone acceptance.

No main merge. Next: review the actual candidate on a phone and consider distinct Brookside/Cedarwood improvements rather than copying Woven composition.
