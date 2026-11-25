# QA1759 owner phone review

Evidence: ten screenshots supplied after QA1759 distribution, run 45534555749. Physical playthrough visible, Woven Trail two players, AI winner, three rounds, 48 seconds, fair-finish playtest. Not release acceptance or proof of LAN/install-over.

## Improvements observed
Single-layer illustrated buttons and direct text are visibly cleaner; compact count selectors and Full map use native fallback. QA label removed from board HUD. Wider live framing retains fox/owl portraits and seat numbers. Overhead environment sprites visible; encounter card readable.

## Defects observed
- Compact ornate controls (Host/Join, Close map, View course, Main menu) crowd leaf decorations; needs safe inset or simpler style.
- Course cards still depict scenic backplates rather than overhead composition.
- Overhead board has sparse isolated sprites on flat polygonal terrain. Perspective mismatch between angled sprites and top-down river/ground. River lacks banks/texture; oversized rounded bridge placeholders and bridge alignment look unnatural. River extends outside square overview artwork into dark margin; live Start area shows exposed dark band. Need clipping and intentional board edge treatment.
- Event markers tiny relative to ordinary spaces; paths still geometric parallel strokes. Maintain exact graph/hit geometry while improving legibility and environment design.
- Action tray remains substantial, styling generic relative to home; gesture hint can obscure controls. Dice and encounter treatment readable but not cohesive.
- Classic completed in 48 seconds, three rounds; previous QA1734 screenshot shows 61 seconds, three rounds. Both are tactical/fair-finish playtest results, not representative balance benchmarks. Investigate actual route lengths, movement and encounter bonuses; run repeated deterministic simulations for each ruleset and player count before changing balance.

## Next
Prioritize coherent board composition and course depth jointly: establish authored geometry and intended route pacing before expensive bespoke board paintings. Reuse original source assets, but create consistent terrain/river/bridge composition rather than scattering more sprites. Fix clipped/exposed board edges and compact text collisions. Preserve old board IDs/saves; new layouts versioned and tested. Keep QA/private beta signer identities independent. No code changes in this audit; no new APK claim.
