# Identity 01 — reusable woodland artwork

Baseline QA1696; source integration/beta-1-polish. Preserve ARTREADY-38 as the canonical art bible. Manifest has 16 slots: 10 packaged QA-preview PNGs, six planned (mischief, victory, four individual portraits). Current environment atlas has 12 code-authored 256px cells and four reserved cells. Existing generated token atlas has fox, owl, hedgehog and rabbit. No owner production-art approval inferred.

First replacement candidate: environment_v2_contract.json, same 4x4 layout and subject order as v1, 12px clear gutters, four empty reserved cells, exact 1024 RGBA. Keep v1 as fallback; do not register v2 until actual bytes are available, validated and attributed. Scenery should be overhead, simple cut-paper editorial shapes, consistent with existing palette and portrait language. No full UI mockups, text, routes or baked game markers. Individual subjects must be legible at actual small phone display sizes. A generated sheet is not proof of correctly sliced or packaged artwork.

Production order: environment sample (oak, fir, cottage, flowers) for cohesion review; full 12-cell atlas; mischief and finish banner; reusable material art; individual portraits only if justified. Preserve Sudoku/Word Search provenance and obtain explicit redistribution approval before any cross-project copying. No new PNG or gameplay feature included in this commit.

Review: verify alpha gutters, empty reserved cells, exact dimensions, SHA, provenance, existing asset pack CI, real device appearance and performance. Existing 8MiB per file and 32MiB pack limits remain. Status approved in v1 manifest means packaging eligibility, not owner visual acceptance. No public release or main merge.
