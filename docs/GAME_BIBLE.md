# GRIDMATES: CROSSROADS — GAME BIBLE

**Version:** 0.1 (pre-production living bible)  
**Date:** 2026-11-16  
**Status:** Product vision grounded in owner conversations; prototype rules are proposals and not yet playtested.  
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

The game should be warm without being childish, modern without being busy, and inviting to solo play or people already in the same room. Traditional tabletop play is the inspiration; good usability and clean native interactions remain non-negotiable. Crossroads belongs next to Sudoku and Word Search because it is **accessible, familiar in spirit, and fun together**. It is not the appropriate home for a heavy action shooter.

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
- Draw paths, tiles, junction highlights, dice and piece markers programmatically wherever practical.
- Optional existing animal portraits become circular tabletop tokens; characters have no special stats.
- Different themes/board shapes later with the same core mechanics.
- All source assets need an explicit registry/provenance check before import. Never copy another app's saved unlock progress.

## 10. Technical principle

Keep game rules/data independent of Android UI and LAN transport. An authoritative match state should advance only by validated player actions, with host-controlled dice/event randomness in LAN mode. Make every action replayable for debugging and test board graph integrity separately from graphics. **No runtime implementation is asserted by this bible.**

## 11. Quality bar

A playable prototype must handle a complete AI race, explain every crossroads, avoid getting stuck or softlocking a player, honor event eligibility rules, and produce a correct result. A release candidate later needs real multi-device LAN and lifecycle tests, readable small-screen presentation, and stable local saves/unlocks.

## 12. Deliberate exclusions

Not a Monopoly GO clone, not The Game of Life, and not a full life simulator. No property acquisition, persistent city, combat system, account requirement, server economy, gacha, massive collectible burden, forced notifications, or cross-app progression service. Avoid scope creep until the basic game is proven fun.

## 13. Development rule

**A plain prototype must already be fun.** Test the basic board with colored circles before commissioning more images. Every new feature must improve player decisions, social play, usability or replayability enough to justify code, QA, and maintenance cost.