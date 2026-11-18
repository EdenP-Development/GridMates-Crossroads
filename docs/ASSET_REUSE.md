# Shared GridMates artwork, independent inventories
- Reuse *approved source files* and family-friendly characters from Sudoku and Word Search as **flat board piece art**. Small circular token + player ring/number; no character physics or bespoke sprite animations.
- Squirrel, fox, turtle, etc. are examples of recognizable characters, not newly authorized asset imports.
- **No shared entitlement ledger:** Crossroads unlocks must be earned under Crossroads-specific achievement requirements. Never read/write Sudoku or Word Search app private saves.
- Each game requires its own unique Android applicationId, save schemas, image references, profile data and unlock keys.
- Create an asset manifest recording canonical asset ID, source repo/branch/path, license/provenance, intended usage, original dimensions, checksum, converted variant, and accessibility text.
- Only import assets after examining actual branches and file licenses. Do not download fonts/illustrations from another game/app without redistribution rights.
- Version-1 piece selection: several free default tokens; optional unlocked portraits after core gameplay works.
- Board themes should recolor backgrounds/decorations while leaving high-contrast route geometry and player state readable.
- Favor programmatically drawn paths, spaces, arrows, dice and circles to limit asset production overhead.

## Current asset-ready implementation — ARTREADY-38 (November 18, 2026)

The concrete Crossroads-only art pipeline is now described in [ARTREADY-38](ARTREADY_38_ASSET_BIBLE_AND_IMPORT_QA.md), with an exact 12-slot PNG registry at `app/src/main/assets/art/v1/manifest.json`. Four first-batch illustrations are prioritized, while existing procedural Compose art remains the fallback.

**Important:** this enables artwork to be imported; it does **not** claim any images from Sudoku, Word Search, free stock sites or AI tools have already been approved, licensed or added. Verify all source ownership/redistribution rights and actual file checksums before changing any slot from planned to approved.
