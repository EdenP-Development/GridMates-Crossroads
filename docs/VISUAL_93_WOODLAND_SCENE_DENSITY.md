# VISUAL-93 — Woodland scene density and atlas reuse

QA1678 is the most recent verified playable build at the start of this pass. VISUAL-92's first emulator job failed before launching Android because sdkmanager was not on PATH. The workflow was repaired on #92 using the established Android SDK lookup; a new result is required.

## Actual changes
Seven fixed, atlas-only secondary ornaments are placed around Woven: a bush, two fir crowns, two reed clusters, mushrooms and a fence near the village outskirts. All five previously unused sprite families are now referenced by authored scenery. The 23 existing ornaments plus seven additions target 30 scenic features, with the two existing graph-aligned bridges unchanged.

Every new center must be at least .067 normalized units from any legal edge/node, .077 from existing and accepted new ornament centers, and its square sprite footprint must fit the board. Tests cover exact authored placement, deterministic output and suppression if future geometry conflicts. These are geometric safeguards, not artistic acceptance.

The same positions appear in live and Full Map Overhead views. No new asset bytes, game rules, interactions, state, permissions, camera, or Storybook changes. Missing atlas still draws the existing procedural scene. No main merge.

## Next acceptance
Check the repaired VISUAL-92 emulator comparison and signed Android candidate build. On actual phone assess the richer scenery density at normal zoom, fence/creek placement, board readability, and whether the current art needs a more hand-illustrated second edition. Do not treat density alone as a completed illustration overhaul. Physical LAN and install-over acceptance remain outstanding.
