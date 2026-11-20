# BRAND-63 — Approved fox-at-the-fork Android launcher icon

**Status (November 19, 2026): exact approved concept #3 artwork integrated as a genuine binary Android resource on stacked draft PR #63.** Android QA and owner-phone launcher acceptance are required before this is release-ready.

## Owner-approved visual identity
The third generated concept was chosen: smiling orange/cream fox as a wooden board-game piece at a Y-shaped golden woodland trail with a two-direction signpost, richly illustrated green forest, no text. This uses the same owner-approved source art, not a hand-drawn vector substitute.

## Resource integration
- Android launcher manifest remains `@mipmap/ic_launcher` for both normal and round icons.
- Android adaptive icon `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` references `@drawable/ic_crossroads_fox_foreground` against existing `crossroads_icon_background`.
- **Actual binary image**: `app/src/main/res/drawable-nodpi/ic_crossroads_fox_foreground.webp` (432×432 transparent canvas, approved original illustration scaled to a 272×272 centered tile). Exact Git blob SHA: `cba155af230e2f0af7a61331cff754abc0e24756`.
- Main app minSdk 26 supports adaptive icons. No other `ic_launcher` variants were altered; old fallback vector is intentionally kept as unused backup. This is a QA optimization for installing the real artwork; store listing master and optional multi-density fallback artwork are in the separate source package but not needed to test this launcher.
- Compressed original-style WebP source, not an approximation. Smaller artwork in a transparent canvas is intentional to keep the fox and fork inside common adaptive masks.

## Verification checklist
1. Exact-head Android compile, unit tests, lint, resource merge, signing and artifact inspection.
2. Check actual app launcher on Android with round and non-round masks; confirm fox ears, wooden piece and Y-fork visible, and no dark bounding-square edge.
3. Install new QA over the existing `com.playereden.gridmates.crossroads.qa` app without uninstalling; ensure stored match/stat data remains.
4. Compare icon against prior simple crossroads icon at actual 48dp launcher size; the woodland fox should remain recognizable.
5. Confirm game screens, art, tokens, gameplay, saves and LAN behavior unchanged. No experimental icons exported for production by this QA.
6. Do not merge PR stack or publish to the Play Store without explicit user approval and release gates.

## Dependencies and safety
Stacks on draft PR #62: GAMEFEEL-62 signed QA1513 already passed. Only launcher resources and this documentation changed in BRAND-63; rules, saved schema, networking, signing identity, version/config and woodland backplates untouched. `main` remains unchanged.
