# GAMEFEEL-41 — Real-phone decision flow and route clarity

**November 18, 2026 · Draft, stacked on ART-PREVIEW-40.** No merge to `main` or game-rule changes without owner approval.

## Why this slice exists

Owner completed a real Android **Woven Trail, Gameplay 2.0 Lab v3, two-player solo-vs-AI** race. Screenshots show **AI 1 won in 5 rounds and 1m 32s**. The owner reports that decisions now *start to feel good*, but the experience still seems "a little structured". Treat that as valuable subjective playtest feedback, **not proof** of the cause.

The screenshots reveal two concrete presentation issues:
- The *Meadow Crossing* two-route chooser wraps long risk and preview copy inside narrow labels; the lower route is offscreen with no visible scroll indication.
- Several different shelters/hazards share the repetitive "A woodland encounter — choose your response" framing.
- The illustrated Woven background reads as one setting, but the active route can visually compete with creek/fence detail.

## Changes to Android UI only

1. **More readable route cards** — place route name and written risk in the same header, with a full-width concise stakes preview underneath. For Lab v3, prefer the core-authoritative `DecisionLab.routePreview` over redundant static flavor text. Original rules keep their route descriptions.
2. **Two-route choice panel** — raise its content cap from 230dp to 280dp; retain vertical scrolling and add an explicit *scroll down* cue only when content extends beyond the visible panel. The existing three-route dialog stays intact.
3. **Fewer form-like repeated prompts** — say "A shelter along the trail" or "Trouble on the trail"; replace generic lengthy option text with compact labels whose consequences are still explicit. Steady/Push controls receive neutral equal styling instead of preselecting Steady visually. No automatic choice, timers, or change to probabilities.
4. **Readable painted paths** — add a soft forest keyline under authoritative route lines. Keep risk colors, actual node coordinates, and camera gestures unchanged.

## Gameplay boundaries

- **No** changes to `core/`, `data/boards`, `data/events`, win logic, seeded results, checkpoint fingerprints, game save format, replay, offline/LAN actions, or signing.
- **No** new currency, collection, daily mechanic, account, artificial delays or forced extra event cards.
- All changes apply to normal and experimental UI where appropriate; **Lab v3 alone retains** Steady/Push and on-entry shelter/hazard decisions.
- Old/sibling artworks aren't copied. Four QA preview images remain in the same asset registry.

## Exact-head automated QA required

1. Artwork manifest integrity; 4 approved-for-QA packaged PNGs and eight planned fallbacks.
2. Android `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug`, debug APK, and release-variant compile and QA signing fingerprint.
3. Design data/release preflight must remain green.
4. No release claim based on green CI alone; screenshots cannot verify clickability, touch targets, save restore, or two-phone behavior.

## Owner real-phone acceptance checklist

- **Two-way crossroads:** on a small handset, both High Ridge and Creekside Walk must be selectable, with an unmistakable scroll cue if the second option lies below the fold; scroll, choose, and inspect the full map without losing the pending turn.
- **Three-way crossroads:** all three paths remain reachable in their dedicated dialog, and returning from full-map inspection retains the original pending choices.
- **Legibility:** compare board at fit, 1x and 3x with the four-asset Woven backdrop; routes and colored tokens should be obvious despite scenery.
- **Encounter clarity:** Shelter Supplies/Cover and Hazard Gamble/Brace must still communicate exactly the real outcomes; after each choice, the game advances only once.
- **Pacing:** time another Classic Lab v3 two-player match (then Long if practical). Note which decisions were meaningful and which felt like repeating the same choice.
- **Accessibility:** large font, portrait/landscape if supported, TalkBack if available; controls, scroll and focus must not become trapped.
- **Regression:** mid-encounter force-stop/reopen, rematch, existing win totals, standard rules OFF, and trusted two-phone LAN remain separate acceptance gates.

## Next research question — not yet a rule change

"A little structured" might mean **every turn feels scripted**, **known choices repeat**, **the AI doesn't visibly react**, or **the shared board/race never changes**. Don't add more button prompts as a knee-jerk fix. Observe a few complete human sessions and identify the source before prototyping optional Lab v4 variants. The proper target is *anticipation → action → consequence → rival reaction*, not longer mandatory animations.
