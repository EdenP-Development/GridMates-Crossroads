# Decision log
## APPROVED DIRECTION — GridMates classic simplicity (November 17, 2026)
- Owner wants GridMates to resemble approachable newspaper, puzzle-book and familiar board games (Sudoku, Word Search, Minesweeper, easy tabletop), **not** an advanced board-game platform.
- Equal priority: **easy for our tiny development team to build, test, finish and maintain**. One good gameplay loop and reusable visuals/QA are more valuable than complicated accounts, economies and feature trees.
- Default to offline-first, short setup and self-contained apps. Optional local togetherness should be as low-friction as possible. No mandatory backend/account.
- **Pass & Play** for 2–4 humans on a single device is an approved-practical development direction, complementary to the longer-term same-Wi-Fi plan. It is not actual LAN implementation.
- More boards, characters, optional sound and lightweight achievements remain *opportunities*, not obligations. Extra unlock trees, currency and daily chore systems are not approved by this direction.
- See [Classic Games Charter](GRIDMATES_CLASSIC_GAMES_CHARTER.md) and [lean roadmap](ROADMAP.md).

**Status markers:** APPROVED = explicitly agreed in brainstorming. PROPOSED = work hypothesis. OPEN = playtest pending. Do not silently promote ideas to approvals.

## APPROVED — November 16, 2026
- The game belongs to the GridMates family; original approachable board game, not a persistent town, property/economy simulator, or Sector Rebound-style action game.
- Preferred *working* title: **GridMates: Crossroads** (not trademark/store cleared).
- Cozy but competitive, accessible and family-friendly, satisfying short and longer matches.
- **One shared Start, one shared Finish.** A maze-like network offers multiple routes; players choose at crossroads. Choices should be consequential enough to win or lose matches.
- **Race to finish** is the core win condition, not collect-three-stamps or cumulative score.
- Board paths and recognizable theme-area geography are handcrafted/fixed; content varies through controlled random events selected within area-specific eligibility pools. No fully random maze in V1.
- Life-inspired decisions and surprise events; light player sabotage/mischief, not an elimination or combat system.
- Solo versus AI and **up to four total players** over same Wi-Fi are intended capabilities, not presently implemented.
- Multiple match lengths and selectable optional rule variations are product goals; focus on one solid foundational mode first.
- Existing approved original GridMates character art may reappear as simple board tokens. **Each app keeps independent unlock conditions, achievements, saves and inventory.** No cross-game sync.
- Board themes/collections and alternate shapes are expansion opportunities; reduce asset workload with reusable components.

## OPEN / needs prototype
- One die versus choose-one-of-two; exact roll and overshoot finish behavior.
- Path continuation within a roll, minimum branch decision UI, node occupancy and pass-through.
- Mischief eligibility, shields, immunity duration, limits preventing targeting loops.
- Event weights/cooldown rules, reveal timing, and any rerolls.
- Whether Quick/Classic/Long require distinct handcrafted courses or scaled course segments.
- Which landscape, first board colors, and specific asset IDs.
- Android stack decision after branch/current code audit; local reconnect/host-loss policy.
- App title/logo/trademark clearance, monetization, release plan.

## PROPOSED initial prototypes
- One six-sided die; select path at forks and spend remaining roll along the chosen route.
- Exact landing at Finish unnecessary; crossing it wins.
- Short 2-space mischief setback and one-use shield; no chained attack events.
- Plain drawable paths and colored tokens before decorative art.

## APPROVED DIRECTION / CAM-06 — November 16, 2026
- User requested mobile-board-game-style **zooming and board camera exploration** and explicitly asked to continue building it. 2D dynamic camera is in scope while keeping core race rules unchanged.
- Implemented QA candidate: follow a moving piece, wider crossroads framing, Overview, pinch zoom, pan and recenter. Zoom defaults and gesture response remain **OPEN for phone playtest**, not locked production decisions.
- [Draft camera PR #12](https://github.com/PlayerEden/GridMates-Crossroads/pull/12) and [QA handoff](CAM_06_QA_HANDOFF.md).

## APPROVED DIRECTION / CAM-06B — November 16–17, 2026
- **No permanent in-match camera buttons**: owner explicitly prefers gesture-first map manipulation to the previous visible `− / + / Overview / Follow` strip, similar to a natural mobile board-game interaction.
- Use pinch-to-zoom, drag-to-pan, and intentional double-tap/tap-focused-piece recentering; preserve automatic following unless manually exploring. Keep an unobtrusive first-match hint rather than a standing camera toolbar.
- Research comparable mobile board-game user-facing UI before refining broader layouts. See [mobile board-game UI study](RESEARCH_MOBILE_BOARD_GAME_UI_2026_11.md). The Game of Life 2 is a stronger structural reference than a rolling reward-economy app; do not copy third-party IP or overbuild a metagame.
- CAM-06B gesture-first code has been committed on camera Draft PR #12. **QA confirmation, tap/gesture response, accessibility and physical phone acceptance remain OPEN.** No game logic change, production release or main merge.
