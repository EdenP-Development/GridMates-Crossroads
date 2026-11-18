# VISUAL-30 — Illustrated Brookside and readable map overview

**Draft, November 17, 2026.** Based on owner's real Android QA1247 screenshots, stacked on VISUAL-29, not merged into main.

## Evidence from actual device

- New home arrangement puts **Start journey above the decorative hero** on the owner's device.
- The game now identifies **Brookside Dash** consistently in play, map inspection and results.
- Four numbered pawns at Start are separated better and existing stats continued from **5/7 to 6/8**, consistent with another completed Solo win. This is visual evidence, not a full backup-migration test.
- A four-racer Quick match finished in **34 seconds** and four rounds. Quick is deliberately shorter, but human pacing/Classic comparisons still need more samples.
- The live board is much easier to follow, though the overview dialog used a tall 385dp map area; Brookside scenery remained broad translucent ovals, with few landmarks.

## Source changes

1. **Brookside bespoke decorative illustration:** original conifers, meadow flower/grass motifs, three village cottages, creek pebbles, water ripples and a subdued footbridge. Fixed coordinates; all drawn beneath the actual course paths, tokens and event spaces.
2. **Responsive overview:** The map inspection dialog now uses width-based height (0.94 × available width, clamped 245–328dp) rather than a fixed portrait 385dp viewport. Course name and "Route overview" are separate readable title lines.
3. **Start/Finish visibility:** A forest-green starting emblem and warm-gold finishing ring read at a glance in the fit-to-map view while keeping the original numbered player tokens and node text.
4. **No gameplay/schema modifications:** Board JSON, lane types, route IDs, dice, AI decisions, matchmaking, LAN protocol, saved checkpoint format, QA package ID, signing and backup files all remain unchanged.

## Automated acceptance

- [ ] Android compilation, unit tests, lint, release-variant identity check, signed QA APK.
- [ ] Previous presentation regression tests and unchanged core gameplay suite remain valid.
- [ ] Diff confirms **no changes to data/boards/**, encounter definition files or game rules.

## Owner one-phone acceptance

1. Install signed QA over QA1247, preserving local wins/matches.
2. Start Quick. Compare the first board view to QA1247: more personality, but primary roads and fork rings should remain the clearest marks.
3. Move through both crossroads and inspect hazards/shelters. New scenery must not visually obscure risks or make a decorative bridge look like a legal path.
4. Open Full map. Verify a wider, more proportionate overview, shorter title block, and legible legend on normal and large-font settings.
5. Test Start with four pieces, Finish marker at end, and results. No new clipping, token overlap, misleading route or lost progress.
6. Confirm Classic still retains its own established scenery, the new Start/Finish rings, and the same match flow.
7. Nothing in these screenshots verifies real two-phone LAN or production signing. Those gates remain open.

## Boundaries

This is a targeted visual enhancement, not a claim that all game art is store-final. Any more substantial visual redesign should follow screenshot comparison and existing game design references. No implicit permission to merge to main or distribute the app publicly.
