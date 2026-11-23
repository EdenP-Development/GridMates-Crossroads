# VISUAL-85 — Owner QA1672 phone readability review

**November 23, 2026 · draft presentation refinement only · source:** seven owner-supplied QA1672 phone screenshots (`8098.png`–`8104.png`). These are real-phone visual references, not a completed end-to-end performance, accessibility, LAN, or upgrade test.

## What the screenshots demonstrate

The physical tabletop design is working: the dark walnut board rim, brass-highlighted player-seat plates, numbered fox/owl travelers, cream action tray, and revised woodland green/cream switches belong to one design language. The actual Storybook Woven Trail art is still attractive at a closer view. Active route selection, first roll, and complete course overview are visible.

But **board artwork loses priority in the live view**:

1. The new carved trail's broad cream deck appears as a very bright, thick beam. In closeup it dominates an otherwise intricate painted meadow. Wood joints look like frequent rail divisions; risk colors are hard to see along the narrow edges. The live paths should read as substantial *game pieces* but not consume the scenery.
2. At an active two-way fork the stationary action tray repeats both prior-move narration and a large section header. The player already sees `4 steps to move`, the title `Meadow Crossing`, and `COMPARE OR CHOOSE PATHS`. The repeated sentence occupies illustrated-board area without improving the current action.
3. The Full Map is legible as a course overview but intentionally shows fine details small. This is a different use case from the live close-up and remains unchanged for now; it should eventually support an inspection zoom without ever sending real route actions.
4. The **QA1672 screenshots use the Storybook landscape**, not the Woven-only Overhead QA toggle. Therefore they validate UI-81/82/83 visibility but do **not** approve or reject the overhead VISUAL-84 art direction.

## Specific source changes

- **Carved wood width 0.79× previous** (a ~21% reduction in the ink footprint) while preserving the full-size scenic backdrop, camera preset, node medallions, traveling animal pawns and all route touch bounds.
- **Warm natural-wood center colors** replace the very bright cream stripe; the colored green/amber/purple inset grows proportionally relative to the pale center, and the soft ground shadow is reduced.
- **Fewer plank joints** on long roads (spacing from approximately `65 × visibilityScale` to `95 × visibilityScale`, maximum decorative divisions five rather than seven). Never generate interactive stepping spaces.
- **Compact collapsed fork tray:** when the user has a genuine ROUTE choice, hide the redundant automatic last-action sentence while retaining `steps to move`, fork title, route count and primary action button. The same original action narration remains available by expanding **Rules & last action**. Encounter choice descriptions and other phases are unchanged.
- Slightly less vertical tray padding/gap while maintaining the original max-height guard and full tappable action surfaces.

## Explicitly preserved

Core game graph, junction topology, choose/compare callbacks, action logs, saves, rule variants, results, LAN behavior, app ID/signing, camera magnification, manual zoom, enlarged scenic world, token scale, overhead artwork toggle, and the entire read-only full map. `main` untouched.

## QA gates

- JUnit: carved road width still legible but no longer oversized, join spacing sparse, risk colors distinct, no fake nodes; collapsed route summary hidden but present in expandable notes; encounter/roll statuses unchanged.
- Run exact-head Android APK build, app unit tests, lint and design-data validation before offering a new version.
- Owner phone: compare **same Woven Trail original Storybook view** against QA1672 at the same fork and default zoom. Is more meadow visible without paths becoming too subtle? Is the route CTA reachable on small screen and large font scale? Do thin color borders still distinguish safe/hazard/mischief?
- Also compare optional overhead art after Android gate; that style remains an **unapproved QA experiment** and was not switched on in the supplied images.

**No claims of physical acceptance for this next revision until the owner tests it.** Do not merge, auto-release, or update production signing.
