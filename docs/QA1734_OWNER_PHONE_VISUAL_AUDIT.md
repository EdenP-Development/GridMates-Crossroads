# QA1734 owner-phone visual audit — 2026-11-24

Evidence: ten owner-provided phone screenshots in the Game Design conversation. Build QA1734, run 45530635961. Owner reaction: 😬. This is not visual acceptance. One completed two-player Woven Trail playtest is visible: 3 rounds, 1m 1s, local winner, fair-finish playtest. No LAN or install-over claim.

## Observed
- Illustrated controls render, but native outer container plus ornate image border plus opaque label backing create nested buttons. Compact controls show particularly poor fit and leaf/label crowding. Full-width buttons look stretched.
- Home hero and course art retain a coherent woodland palette, but course cards and actual match controls do not share the ornate button treatment.
- Illustrated board view is a scenic painting with straight graph edges overlaid; close camera makes background flowers, cottages and rocks disproportionately large. Full map shows entire graph but terrain/path alignment is weak. Do not infer these screenshots prove the V2 atlas was rendered: selected Illustrated mode may use the separate scenic backplate.
- Top HUD has cramped QA label next to Full map, large action tray consumes board space, route sheet and results remain relatively plain. The route choices are readable. Results show extremely short match, but one run is not balance evidence.

## Corrective order
1. Separate visual button surface from native interaction semantics. Remove visible double frames and opaque text backing when art is used; keep readable text with tested contrast, safe label zone and native fallback. Review actual screenshots before accepting. Use compact variant for narrow utility actions; do not stretch ornaments blindly.
2. Inspect camera defaults and Illustrated/Overhead rendering routes; establish wider playable framing without changing graph or hit testing. Test all three courses. Keep scenic artwork for appropriate surfaces; author board-specific art against actual route geometry, with coherent scale and graph-aligned trails. Do not regenerate approved originals indiscriminately.
3. Harmonize HUD, action tray, route choices and results; reduce redundant chrome and reserve board space. Review compact and large-font layouts.
4. Verify screenshot evidence, then pacing across repeated races; only after visual and gameplay gates consider private beta consolidation. Preserve QA and private beta identities separately.

No code fixes or new build claimed by this audit. Existing artwork source archives and verified pipeline must be retained. No release acceptance.
