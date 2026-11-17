# Crossroads — comparative mobile board-game UI study
**Date:** November 16–17, 2026
**Status:** RESEARCHED design guidance and CAM-06B implementation candidate; no owner phone acceptance yet.
**Scope:** User-facing match UI, board navigation, crossroads, pacing, first-use affordances, accessibility and results. Not a request to copy other games' art, rules, monetization or branding.

## Closest reference games and evidence

| Game | Why it matters | What we can adapt | What we should not infer or copy |
| --- | --- | --- | --- |
| **The Game of Life 2** (Marmalade) | Closest structural comparison: travel through a themed winding board and make decisions at forks. Official store listing explicitly describes spinning, arriving at spaces, and choosing a path at crossroads. Screenshots show illustrated junction decisions and card-style choices. | Themed journey; choice surfaces that appear at the relevant moment; clearly contrasted options; scenery landmarks that orient the route. | Its life simulation, vehicles, jobs, economy, character/asset IP, or assumptions about its exact pinch/tap implementation. |
| **Board Kings** | Published gameplay images show a miniature town and visually dominant dice/board activity. | The board should look like a *place*, not a schematic. Give each region a recognizable silhouette and environmental landmarks. | Coin/energy economies, events grid, persistent metagame and excessive status indicators. |
| **MONOPOLY GO!** | Official listing and screenshots emphasize one primary action (roll) and visible token/dice spectacle. | Concentrate attention on the next meaningful input; let animated board events briefly take over the focal area. | Endless rolls, timed events, currency clutter, aggressive reward overlays. |
| **Ludo King** | Official listing supports a classic dice-and-token game with simple rule language, local and offline play. Gameplay images use strong player colors and a stable recognizable board. | Color-coded seats, uncluttered immediate turn state, readable board and easy player identification. | Four-quadrant board structure, exact visual branding or game-specific social features. |
| **Ticket to Ride (digital)** | Published board screenshots show complex interlinked routes where players can compare an entire map with a current decision. | High-level strategic overview must remain legible; active branch and destination emphasis must be stronger at close zoom. | Train/card inventory layout, territory labels or route mechanics. |

### Primary/public references
- Marmalade official The Game of Life 2: https://www.marmaladegamestudio.com/games/the-game-of-life-2
- The Game of Life 2 Google Play: https://play.google.com/store/apps/details?id=com.marmalade.gol2
- MONOPOLY GO! Google Play: https://play.google.com/store/apps/details?id=com.scopely.monopolygo
- Ludo King Google Play: https://play.google.com/store/apps/details?id=com.ludo.king
- Board Kings published gameplay images (third-party reference): https://www.wwgdb.com/games/board_kings
- Ticket to Ride mobile store listing: https://apps.apple.com/us/app/ticket-to-ride/id6463616555
- Android Compose gesture guidance: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures
- Android accessibility custom actions: https://developer.android.com/develop/ui/compose/accessibility/semantics
- Android accessible touch targets: https://developer.android.com/codelabs/jetpack-compose-accessibility

**Method/limitations:** This is a documented screenshot/store-page/official-guidance review, not instrumented user testing of other games. In particular, do not claim competitor apps use a specific camera gesture unless independently tested. Crossroads UI choices below are original design synthesis.

## Key design principles for Crossroads

1. **The board is the hero.** More visible area for paths, pieces and future woodland landmarks; fewer overlay bars and controls. Do not allow transient controls to obscure a fork or finish.
2. **One primary action at a time.** The visible roll button is warranted; permanent camera mode, +, − and Follow buttons are not. At junctions, a clear route choice should temporarily be the primary action.
3. **Direct-manipulation camera.** Pinch to zoom continuously 1×–3×, drag within map viewport, double-tap or tap the followed token to resume automatic follow. If the player pans, manual control persists across AI turns until intentional recentering. Inspect map can remain during transition while we establish discoverability.
4. **Automatic framing should be calm.** Keep the actively traveling piece on camera through bot actions and setbacks; widen enough to show every immediate fork exit. Never bounce to the next player before the previous movement has ended.
5. **Teach once, don't advertise forever.** Show a small dismissible helper the first time a user enters a match: "Pinch to zoom · Drag to explore · Double-tap to follow." Persist the dismissed flag only; do not modify stats or save schema.
6. **Support non-gesture access.** Expose semantic accessibility actions for recenter / whole-board in Compose. A player should not be blocked because they cannot pinch or double-tap.
7. **Always show the stakes.** Current player, active phase, last event and outstanding roll steps should be understandable without opening a log or guessing whether the status line describes a previous player's action.
8. **Motion should serve comprehension.** Die lands before piece travels; piece arrives before effects; bot turn handoff doesn't steal camera; Cozy/Brisk affect presentation only.
9. **Keep route choices readable.** Cliff Track, Mill Lane and Cedar Bridge require complete names, risk description and generous tap targets even with increased Android text size.
10. **No visual clutter creep.** Do not introduce coins, timers, cooldowns, event tickers or persistent chat just because successful mobile games have them.

## Screenshot-specific audit of owner QA1057 (Nov 16, 2026)

- Zoomed and overview views render, but previous floating "−  +  Follow/Overview" control occludes lower right routes. **CAM-06B removes the persistent bar.**
- At 1× the full course is visible but tokens, event markers and region labels are exceptionally small. **Future:** strengthen symbol hierarchy and look at alternate view defaults before any art overhaul.
- Latest Encounter card can appear partly clipped at the bottom of the scrollable board section when the fixed action sheet takes space. **Next UI pass:** ensure the latest outcome appears briefly near the turn action, with expandable history elsewhere; no critical information exclusively beneath the sheet.
- Turn header "AI 2's turn" can coexist with a previous "AI 1 rolled 3" message. **Next UI pass:** disambiguate "Now playing" versus "Previous action".
- The three colored seats are identifiable; moving forward, use avatars and clear focus treatment without requiring reading very small token numbers.

## Proposed screen composition (not implemented in CAM-06B)

**In match:**
- Compact title/pace or settings; small player turn strip.
- Board viewport occupying the central majority of portrait height, without floating camera controls.
- Subtle first-match gesture hint, then disappears permanently after acknowledgement.
- Contextual mini event message, if necessary; journal accessible when wanted.
- One fixed action region: roll, route choice, effect resolution or results. Avoid stacked repeated descriptions.
- Optional "Inspect map" while physical-phone tests establish whether gesture overview is discoverable.

**At a fork:**
- Frame all available path entrances and mark their risk colors.
- Brief "Choose a path" decision sheet showing complete path names and descriptions, 48dp+ target sizes.
- No dice roll until the route is selected. Avoid bottom-sheet clipping on short Android screens.

**At match end:**
- Winner and finishing order, round count, wall-clock match time and one memorable highlight.
- Rematch and back-to-menu actions.
- Do not invent or extrapolate Classic/Long durations; use real finished-match samples by seat count and pace.

**Home and setup (future):**
- "Play" primary, 2/3/4 participants as simple setup; secondary customization and rules discoverable, not mandatory.
- Cozy nostalgic paper/forest/gold style should support readability, not overwhelm the board.

## CAM-06B implementation boundary

- Reuse existing 2D Kotlin/Jetpack Compose camera math and 1×–3× clamp. Remove visible zoom/mode controls; add double-tap and focused-piece tap recentering.
- Keep manual pan mode persistent and support Inspect Map at 1× initially.
- Include persisted one-time gesture instruction and accessible semantic actions.
- No new currency, reward system, online mode, artwork, rules, RNG, gameplay data, or profile resets.
- Preserve stacked draft PR #12. Do not merge or release before acceptance.

## Required QA before acceptance

- Build/lint/JVM test source; verify exact signed APK hash/version on GitHub.
- Real Android portrait: start 2–4 seat games, pinch at corners and midboard, pan both axes, test 1×–3× clamping and double-tap recenter.
- Tap actual piece after manual navigation; camera must not snap back during manual map inspection.
- AI/bot movement through 3-seat game; check camera ownership handoff across animated turns and prank targets.
- Inspect Map: starts whole-board and double-tap returns whole-board; fixed controls never scale or absorb board gestures.
- Screen reader: board actions are exposed; test with TalkBack and larger system fonts.
- Check 3-way fork chooser and action panel clipping, no double dice, exact match record preservation and install-over data safety.

**Milestone ordering:** CAM-06B QA → user-facing match UI cleanup → original woodland visual kit → real match-length measurements → Classic/Long topology choices.
