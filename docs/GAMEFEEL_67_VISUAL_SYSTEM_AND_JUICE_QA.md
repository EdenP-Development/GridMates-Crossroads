# GAMEFEEL-67 — Visual-system and game-juice audit

**Draft QA experiment · Nov 19–20, 2026 · stacked on passed GAMEFEEL-66.** Source of truth: [Game Bible](GAME_BIBLE.md), [Experience Bible](GAMEFEEL_33_EXPERIENCE_BIBLE_AND_ROADMAP.md), real owner phone screenshots, and the checked references below. No `main` merge/public release.

## Owner's newest direction
Real phone screenshots of QA1518 and later woodland course views show:
- Actual painted road lanes, numbered animal tokens and fork markers have become significantly more readable. **Retain their substance**, don't return to tiny map pins.
- Large route intersections can still look like overlapping woody tubes; distinguish a junction as a decision space. Earlier M65 partly reduced width/zoom; phone comparison needed.
- The 44dp encounter thumbnail, vanilla Material setup controls, and static parchment buttons still look like an ordinary Android app sitting *on top of* an illustrated game.
- The home cover art, fox launcher icon, layered wooden roads and green/cream/gold identity are worth keeping. Make all screens feel designed by the same game.
- Screenshots show layout only: **they do not establish animation timing, haptic quality, frame rate, sound quality, or whether a match is fun.** Observe those in real sessions.

## Research synthesis, with caveats
1. [Designing Game Feel: A Survey](https://doi.org/10.1109/TG.2021.3072241): readable amplification communicates importance and makes interaction feel intentional. **Apply to Crossroads**: a pressed control should react immediately; rolls, choice confirmation, each true landing and win should each have a distinctive but restrained beat.
2. [Good Game Feel: An Empirically Grounded Framework for Juicy Design](https://dl.digra.org/index.php/dl/article/view/936/): juice is not identical to fun and benefit depends on the feedback purpose. **Apply**: prioritize recognizable cause and effect over extra visual noise.
3. [Game Developer — Principles of Virtual Sensation](https://www.gamedeveloper.com/design/principles-of-virtual-sensation): anticipation, predictable results, follow-through and good feedback help establish the player/action connection. **Apply**: a brief roll cue, visible per-node movement, decisive fork selection, gentle arrival.
4. [Unity — Video Game UI Basics](https://unity.com/blog/video-game-ui-ux-basics): changing/critical info stays visible, secondary info belongs in menus, and phone controls need comfortable targets. **Apply**: active player, remaining steps, immediate choice stay visible; explanation and backup tools can be secondary.
5. [Apple — Designing for Games](https://developer.apple.com/design/human-interface-guidelines/designing-for-games/): maintain readable touch-first controls and avoid controls obscuring the game world. Use as cross-platform heuristics, not Android platform policy.

These are **design heuristics**—not proof of playtime improvement, commercial success, specific timing rules, or device acceptance.

## A coherent hierarchy
- **Board**: dominant stage. Road space, miniature token, rivals, branching option. Never cover an essential route under fixed UI.
- **Actions**: exactly one short panel or decision surface; clear available action first; avoid a long explanation until requested.
- **Feedback**: rare important event (win/impact) > choice > ordinary landing > idle. No camera shake or particle storms by default.
- **Atmosphere**: already-approved woodland illustration and warm printed art. Never use decoration to imply a false route or space.
- **Visual control family**: selected forest-green button, thin warm-gold edge, cream labels, muted wood/paper unselected state, same character at home and in-match.

## M67 implementation (this draft)
- Replaces generic Material FilterChip selections for Solo/Pass&Play, 2/3/4 players, and Quick/Classic/Long with one reusable `TabletopChoicePill`.
- Selected chips have unmistakable filled pine coloring and warm edging; unselected choices use subdued parchment. Preserve existing callback/rules.
- Reuses the same press-responsive primary action for Start Race and Compare/Choose Paths. A small original 4-pip die mark is used at the home Start Race button only.
- Animated press scale 1.0 -> 0.975 over ~95ms; immediate onClick, no blocking/hold or game-state mutation. Touch target minimum 48dp and text labels visible.
- No changes to background art, game engine, events, board geometry, dice odds, networking, saving, release signing or application ID.

## Next ranked improvements (after owner phone acceptance)
- **P0 — readability and gameplay feedback:** visually distinct path-choice confirmation and a junction hub that doesn't look like timber intersecting in midair; preserve hit targets and graph.
- **P0 — card-like encounters:** replace thumbnail-plus-plain-label with a coherent small illustrated event card; never hide the actual two choices.
- **P1 — responsive home:** curated course preview/selection without hiding Start Race, overfilling a small display, or adding a setup maze.
- **P1 — judicious sound:** very short original roll/wooden tap/branch selection/finish cues with one volume/mute option and respect for device settings. **No audio implemented in M67.**
- **P1 — finish moment:** make arrival and results a coherent celebration with a short, optional/skippable ritual; don't inflate real match time.
- **P2 — visual audit:** side-by-side screenshot checks on small portrait, wide/landscape and large text; verify color contrast and dynamic system text.

## QA gate
1. Exact-head Android compile, unit tests, lint, resource check, APK signing/artifact check and source data CI.
2. On a phone, compare screenshot (same device) of M66 vs M67 home: no selection overflow at 2/3/4 players, large fonts, portrait/landscape; the Start Race button remains visible even when scrolling.
3. Tap all settings; selected state, summary line and actual initiated mode must match. No gameplay change.
4. Tap Begin Race and Compare/Choose Paths: press reaction is obvious but calm, no double action, no route ambiguity, no timing delay. Screen reader describes selection and clickable controls.
5. Observe real four-space Steady move, encounter choice, AI turn, two-way fork and finish. No result or save regression, no stutter.
6. Test physical install-over preserving saved statistics; no `main` merge until owner approval and separate two-phone LAN/release gates.
