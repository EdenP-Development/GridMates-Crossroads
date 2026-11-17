# Decision log
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
