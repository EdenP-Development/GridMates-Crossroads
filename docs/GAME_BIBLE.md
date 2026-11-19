# GRIDMATES: CROSSROADS — GAME BIBLE

**Version:** 0.2 (living game and UX bible)  
**Date:** 2026-11-19  
**Status:** Active Android QA prototypes; rules, artwork, HUD and camera behavior still require owner playtest approval.  
**Intended platform:** Android; optional local same-Wi-Fi multiplayer; no mandatory account.  
**Working name:** GridMates: Crossroads (not release-cleared).

## 1. One-sentence pitch

A cozy, competitive board-racing game where one shared starting point leads through a maze of branching roads to one finish, and every crossroads forces players to decide whether to play safe, chase a shortcut, or risk helping a rival win.

**Candidate tagline:** *Same destination. Different journeys.* (proposed, not locked.)

## 2. Player promise

- Start a match easily. Pick a token, play with AI or someone nearby, and race.
- Understand the board, what a choice risks, and why a win/loss happened.
- Feel the drama of a shortcut, a lucky event, or a rival's cheeky sabotage.
- Remember the board and improve, but experience different encounters each game.
- Finish a satisfying match rather than needing to return for timers, purchases, town upgrades, or daily chores.

## 3. GridMates fit

The game should be warm without being childish, modern without being busy, and inviting to solo play or people already in the same room. Traditional tabletop play is the inspiration; good usability and clean native interactions remain non-negotiable. Crossroads belongs next to Sudoku and Word Search because it is **accessible, familiar in spirit, and fun together**, but its in-match UI must **not** copy their app-style pages and buttons. It needs its own immersive board-game world. It is not the appropriate home for a heavy action shooter.

## 4. Player experience

**Beginning:** Home → Solo / Play Nearby → choose playable piece → select match setup → start. One sensible default should bypass unnecessary configuration.

**Middle:** Roll, move, choose a direction whenever you arrive at a junction, occasionally resolve an event and sometimes interact with another player. Stay in control of choices; don't interrupt every space.

**End:** First legitimate arrival at the finish wins (design intent). See placements, short highlights, optional profile/achievement changes, and a clear Play Again action. Results and unlocks are purely Crossroads-local.

### Fun model

- **Anticipation:** dice; seeing an approaching crossroads/event space.
- **Agency:** choose a route and event option with meaningful tradeoffs.
- **Tension:** rivals on alternative routes, a chase, an attack opportunity.
- **Surprise:** controlled random event draw in a known region.
- **Recovery:** setbacks are survivable, not a reason to give up.
- **Mastery:** learn where high-risk paths, event regions and reunions occur.

## 5. Core rules — decided vs to test

### Decided

1. One shared Start and one shared Finish for the course.
2. Multiple visibly connected paths between them; forks/crossroads are the **signature**.
3. The victory condition is to reach the finish first.
4. Events, risk/reward and light sabotage add variety to a match.
5. Handcrafted board layouts, region-based controlled event randomness; neither fully fixed content nor a procedural maze for V1.
6. Target up to four total participants (humans and/or AI), with local Wi-Fi as later verified delivery milestone.

### Prototype questions, intentionally unresolved

- Dice model: one standard die versus two dice with a choice.
- Movement: whether steps unused when reaching a crossroads continue after choice (recommended: yes).
- Whether passing through a rival causes anything versus landing exactly on the same spot.
- Attacks: allowable targets and effects, shields, and limits that prevent griefing.
- Whether all event draws happen at landing or some locations reveal events before the fork.
- How best to support Quick, Classic and Long: course variants on the same theme, modular sections, or rule scaling.

Use [Core Rules Spec](CORE_RULES_SPEC.md) as a testable starting proposal, not approval of these questions.

## 6. Board philosophy

Each board is a **place players learn**. For example, a woodland course might include meadow, forest, and village stretches, each with its own event flavor. Its landmarks and path graph remain stable, including the location of junctions and the finish. Outcomes/events are selected from each area's allowed pool and within repetition and fairness rules. A route should not be strictly superior in every situation.

The same artwork can later support additional hand-authored layouts (short/medium/long or different shapes), with one shared event engine. The initial playable needs only one small board.

## 7. Events and player interaction

Events should be quick to read and short to resolve. Prefer one-line effects, or a decision with two clear options. Keep a small vocabulary: advance, setback, shield, mischief, choice, challenge. Avoid money, inventories, long story-card prose, and frequent UI overlays.

**Tone:** lighthearted competitive mischief — a nudge, detour, challenge or opportunity to hinder. No player elimination, prolonged stuns, or total loss of earned cosmetics. A player behind needs believable chances, but no hidden rubber-banding or opaque AI assistance.

**Transparency:** tell players the *type of risk* before they pick a path. The specific random event need not be known. A crossroads should allow skillful choice based on location and opponents, not be a disguised coin flip.

## 8. Match lengths / optional modes

**Product requirement:** quick, regular and longer matches should eventually be selectable when creating a room or solo session. Current **unverified** targets: Quick ~5–10 min, Classic ~15–20 min, Long ~25–40 min for a representative multiplayer session; re-measure on actual Android devices, with 2/3/4 seats.

**Proposed V1 priority:** one well-balanced Classic Race ruleset; prototype Quick course first for fast iteration. Later, host setup may offer Extra Mischief or Cozy Race modifiers, but these are separate experiments and **not** requirements for the initial game engine.

## 9. Art direction

- Cozy, calm tabletop; recognizable GridMates feel inspired by cream/forest/gold visual direction but not a literal copy of another game's screen.
- Make paths and choices readable before adding decorative art.
- Use source-tracked illustrated image assets for scenery and reusable tabletop elements such as paths and pieces; keep dynamic route eligibility, selection highlights, risk labels, hit areas and dice mechanics programmatic and graph-driven.
- Optional existing animal portraits become circular tabletop tokens; characters have no special stats.
- Different themes/board shapes later with the same core mechanics.
- All source assets need an explicit registry/provenance check before import. Never copy another app's saved unlock progress.

## 10. Technical principle

Keep game rules/data independent of Android UI and LAN transport. An authoritative match state should advance only by validated player actions, with host-controlled dice/event randomness in LAN mode. Make every action replayable for debugging and test board graph integrity separately from graphics. **No runtime implementation is asserted by this bible.**

## 11. Quality bar

A playable prototype must handle a complete AI race, explain every crossroads, avoid getting stuck or softlocking a player, honor event eligibility rules, and produce a correct result. A release candidate later needs real multi-device LAN and lifecycle tests, readable small-screen presentation, and stable local saves/unlocks.

## 12. Deliberate exclusions

Not a Monopoly GO clone, not The Game of Life, and not a full life simulator. No property acquisition, persistent city, combat system, account requirement, server economy, gacha, massive collectible burden, forced notifications, or cross-app progression service. Avoid scope creep until the basic game is proven fun.

## Camera and board legibility (implemented QA experiment, November 16/17, 2026)
To make the fixed map feel like a little traveling world, the prototype now has a 2D camera:
- Follow the moving piece; widen around a crossroads; allow overview.
- Allow direct pinch zoom, drag to pan, ± controls and a deliberate return to Follow, while keeping the fixed turn/action area unchanged.
- Camera behavior is **presentation only** and must never affect the die, event odds, board graph, user choice or result.
- An Android QA1057 implementation exists in Draft PR #12; actual device gesture feel and final zoom defaults are **not yet owner-approved**. Artwork can use this camera foundation later.

## 13. Development rule

**A plain prototype must already be fun.** The original geometry-first playtest informed the later illustrated scene; future assets must serve gameplay and never substitute for it. Every new feature must improve player decisions, social play, usability or replayability enough to justify code, QA, and maintenance cost.

## 14. Locked experience direction — Crossroads is a game world, not a GridMates page (owner direction, November 19, 2026)

The **GridMates brand** may unify home screens, warmth, offline-first access, and visual quality. **Crossroads gameplay is intentionally different from Sudoku and Word Search.** Inside an active match, the illustrated course is the full-screen game environment, not an image card in a vertically scrolling Android screen.

### Active match hierarchy
1. **Game world first.** Show a correctly proportioned square illustrated course filling the viewport. Crop through a zoomable/pannable camera; never stretch the map to the phone's aspect ratio. Keep graph-defined roads, pieces, events and node hit targets aligned to the artwork.
2. **Floating HUD, not a page header.** Small, translucent overlays show whose turn it is, seat/color/shield status, current course, pace, and map/settings actions. Avoid taking up the top of the screen with large cards, taglines, instructional paragraphs or full-width solid bars.
3. **Contextual controls.** Roll, Steady/Push, encounter choice, and target controls appear in a thumb-friendly bottom in-game panel while relevant. Show only the current actionable decision and essential feedback. Extra text (odds, prior actions, explanations) remains discoverable through an Info/Details control, not perpetually stacked below the map.
4. **Interact with the board.** At a crossroads, highlight only routes currently legal in the authoritative game engine; tapping a highlighted route can choose it. An accessible Compare paths sheet must always offer the same legal options and risk information. Ambiguous taps must never silently choose a route.
5. **Let the scene breathe.** On a phone, movement should show enough of the landscape and the active piece to feel like travel. Avoid opaque overlays that obstruct every fork or obscure camera follow; zoom and Full map remain separate ways to reorient.
6. **No loss of clarity.** Preserve readable player numbers, color-independent status, explicit move effects, accessible tap targets, pass-and-play handoff, offline/LAN status and save warnings. Neither animation nor visual style is allowed to alter rules or networking authority.

### Android implementation status and constraints
- Draft **UI-52 and INPUT-53** moved toward a board-first HUD and direct graph-validated route taps.
- Draft **UI-54** takes the game immersive/full-screen with a shared camera-aware square world frame.
- Draft **UI-55** reduces the remaining app-like HUD chrome and adds progressive disclosure for narrative/odds.
- These are **isolated QA experiments**, not release-approved visual specifications. Physical Android checks for camera reachability, touch alignment, immersive bars, large-font settings, small screens, 2–4 players, LAN and install-over saved progress are required before promotion.
- Keep the Home/setup experience approachable. Do not impose mandatory accounts, cloud features, timers or a new monetization system merely to mimic other mobile games.

**Design rejection rule:** If the gameplay screen once again resembles a webpage or a list of forms with a decorative board on top, it does not meet the Crossroads identity direction—even if every image asset looks polished.
