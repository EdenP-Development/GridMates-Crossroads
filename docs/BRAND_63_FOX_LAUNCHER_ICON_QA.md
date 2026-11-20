# BRAND-63 — Approved fox-at-the-fork launcher identity

Status: artwork selected and Android resource pack prepared off-repository, NOT yet applied to app or QA APK.

## User decision
Owner chose generated concept #3: orange-and-cream smiling fox carved wooden game piece on a wooden stand, at a Y-shaped warm golden forest board path, rustic fork signpost, rich green forest, flowers, no text. Owner approved direction ('3 is sick'). Do not substitute a rough redraw.

## Prepared asset pack (off-repository, actual approved raster)
- Concept_3_Master.png: full 1254 x 1254 approved original.
- Play_Store_512.png: 512 x 512 artwork.
- app/src/main/res/drawable-nodpi/ic_crossroads_fox_foreground.png: 432 x 432.
- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml: maps launcher foreground to new drawable.
- standard-density fallback mipmap PNGs (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi).
- Icon_Mask_Preview.png and 48dp_Small_Icon_4x_Preview.png for review.
Unmodified source SHA-256: f0f9831220953ee8c237ac9cc844038024816b85994fbd2354cf024e4b5da34f.

## Integration and acceptance
1. Import the files from the prepared ZIP at their app/src/main/res paths (must be binary, not text). Keep the existing crossroads_icon_background color.
2. AndroidManifest is already set to @mipmap/ic_launcher and app minSdk 26.
3. Run the exact-head signed Android QA build with compilation, unit tests, lint and public variant checks.
4. Check round/squircle crop, fox face and ears, visible Y path and readability at ~48 dp.
5. Verify install-over previous QA and all ordinary menus. No rules, saved data, engine, network, app ID or production signing changes.
6. Do not merge to main or distribute publicly before acceptance.

## Explicit status boundary
This branch presently holds documentation only. The approved bitmap has NOT been uploaded to GitHub because the connected repository write interface accepts UTF-8 text or base64 blob strings, not local binary paths. A verified APK with the new icon must follow a genuine binary import. Do not assert a new branded APK exists.

Depends on stacked PR #62, which already passed Android QA1513.