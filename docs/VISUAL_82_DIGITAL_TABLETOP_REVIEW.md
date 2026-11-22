# VISUAL-82 — Coherent digital tabletop pass
**Date:** November 22, 2026  
**Status:** Source candidate on draft branch; Android owner visual acceptance remains open

## Problem the phone screenshots exposed

The supplied QA1614 screenshots show beautiful original painted woodland scenery, routes and illustrated player tokens, but the game UI reads as a **fullscreen woodland map with Android interface panels floating over it**. It lacks one physical tabletop identity. The user also explicitly reported that both the scenery and interactive visuals were too small to enjoy, even with normal Follow framing: this is a **rendered visual-scale problem**, not just camera motion.

The [Game Feel Experience Bible](GAMEFEEL_33_EXPERIENCE_BIBLE_AND_ROADMAP.md) already calls for a *cozy competitive digital tabletop* and a reusable presentation library of wood, parchment, felt, engraved dice, numbered pawns, event cards and route chips. VISUAL-82 implements that direction incrementally, without changing the rules, app architecture, game outcomes, or permission model.

## Visual hierarchy: intended player experience

1. **Board is an object, not wallpaper.** A narrow stationary walnut edge with a subtle brass inlay and corner rivets surrounds the live playfield. It is **not** attached to the camera; route gestures and animal movement continue beneath the frame. The edge is 8dp to preserve playable space.
2. **Players are pieces, not status chips.** The top HUD seats become numbered enamel pawns on small raised walnut/forest plates. Player name remains visible; current seat gets a gold-highlighted frame. Shield is indicated by an additional glyph, not color alone. Screen readers retain explicit seat/name/status/shield information.
3. **Actions live in a tabletop tray.** The existing turn and choice content is now displayed inside a raised paper tray with a walnut lip and gold edge. No new pages or scrolling setup are added.
4. **Movement choices share one material system.** Roll uses the existing raised die-stamped primary action rather than a generic Material button; experimental Steady/Push and targeting use reusable dual-line tactile tiles. Path-choice and results actions use the same paper/wood/brass hierarchy.
5. **Encounter cards and route consequences stay contextual.** Existing illustration and risk colors are preserved. The same legal actions and callbacks are used. No decorative animation locks an action or changes randomness.
6. **Actual artwork size is a separate prerequisite.** The previous UI-81 owner-led pass enlarged the painted live world, wooden roads, pieces and markers together while leaving the Full Map overview intact. VISUAL-82 does not silently undo or further enlarge that scale.

## Visual system (one vocabulary)

| Material | Usage | Design restraint |
| --- | --- | --- |
| Walnut | Board edge, HUD seat base, command-tray lip | Narrow dark outlines instead of a massive simulated desk |
| Brass / warm gold | Raised edges, active seat, button/card borders | Never replace the hazard/low-risk/mischief semantics |
| Cream parchment | Primary game controls, decision/encounter card | Maintain body text contrast and readable 2-line instructions |
| Forest-green felt/enamel | Current seat, primary roll/action controls | Inactive seats remain distinguishable with numbers and text |
| Illustrated wood roads | Actual navigable graph trails | Legal routes, click targets and camera remain mathematically aligned |
| Woodland scenery | Original course-specific painted square | Artwork must be legible at normal zoom; never obscure legal forks |

## Tests and safety boundaries

- Unit test for all four seats' numbered accessible labels, including current/moving and shield states; restrict board rail to a small mobile-safe thickness.
- Existing `CrossroadsPresentationTest`, `BoardWorldViewportTest`, `BoardRouteHitTestTest`, LAN and save/recovery suites remain unchanged in intent.
- Exact-head design QA, Android build + unit/lint, and rules tests need passing receipts before a new QA APK is called verified.
- **No source changes** to core engine, event probabilities, turn order, match fingerprints, stored board graph, route identifiers, progress and LAN protocol; this source change only introduces drawing/composable controls. The game still uses authoritative callbacks.
- **No new art packages imported** and no production key or public release authorization. Historical draft PR #80 remains the consolidated `main` review; VISUAL-82 is stacked on #81.

## Owner-phone visual acceptance checklist

- [ ] Default startup: the large illustration is visible without pinch, with dark walnut board edges subtly present but not covering roads.
- [ ] Two- and four-seat top bars: numbered pawns, actual player names and active-seat highlight legible and not overlapping when large system text is enabled.
- [ ] First roll: clear physical-looking **ROLL THE DIE** action and die; no extra roll or action dispatch.
- [ ] Tactical choice: **STEADY** and **PUSH** are equally tappable; large text does not hide their consequences.
- [ ] Two- and three-path junctions: painted branches are selectable; route options appear as paper decision cards with readable risk.
- [ ] Shelter and hazard: existing illustrated encounter cards match the command tray's style and remain fully readable.
- [ ] Finish: results action is clear and rematch/state restoration are not affected.
- [ ] Camera: actual artwork and tokens are big enough; zoom/pan and Full Map still work, frame edge does not capture gestures.
- [ ] TalkBack and font scale (1.0/1.3/1.5): numbered seat/status descriptions and 48dp action targets remain usable.
- [ ] Portrait and landscape Android: no inaccessible commands or visual clipping from the fixed action dock.

**Acceptance rule:** Green CI proves source consistency, not that it *feels* like a board game. Real phone screenshots of the same moments at default zoom decide whether the wood/paper/board hierarchy is right before any more embellishment.
