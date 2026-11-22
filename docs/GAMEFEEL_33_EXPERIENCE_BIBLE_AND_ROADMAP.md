# CROSSROADS — EXPERIENCE BIBLE & GAME-FEEL ROADMAP
**GAMEFEEL-33 · November 17, 2026 · draft design direction, not rule-change approval**

## The product we are actually making

**A cozy competitive *digital tabletop* race.** One shared Start, one Finish, three intentional race lengths, up to four players (Solo + fair bots / Pass & Play; optional same-Wi-Fi Host/Join after two-phone acceptance). Players learn the woodland, weigh risky shortcuts versus safe detours, remember lucky moments, laugh about mischief, then want a rematch. No accounts, currencies, energy, gacha or required server.

The game already has a working rules engine, seeded AI tests, three handcrafted boards, pause/resume, results and a two-phone LAN *prototype*. It is not missing a giant feature tree. It needs **a repeatable emotional loop**: *anticipation → readable action → decision → consequence → reaction → next stake*.

**Current evidence, not invented readiness:**
- Owner screenshots: Start Journey, board, Woven and Cedarwood maps, three-route Cedar Creek Junction on Android. Quick four-racer phone sample finished in 34 seconds. There is **no completed Long-owner-phone result yet**.
- CI: 2,160 deterministic AI races. Four-seat **median total dice rolls** Quick 11 / Classic 16 / Long 58. This is NOT wall-clock playtime or human enjoyment.
- Two-seat first-player win fraction across a fixed seed sample: Quick 64.6%, Classic 60.4%, Long 54.6%. No proof of human fairness or cause.
- The three-way **Inspect full map** correction exists in draft JUNCTION-32; no permission to merge to main and no accepted two-phone Wi-Fi match.
- Existing research in `RESEARCH_MOBILE_BOARD_GAME_PACING_AND_FEEL_2026_11.md` predicted the need for dice / travel / event / fork beats. This plan makes those a cohesive presentation system rather than stacking disconnected effects.

## Research synthesis (design heuristics, not guaranteed sales results)

| Framework / source | Meaning | Crossroads design test |
| --- | --- | --- |
| [MDA — Hunicke, LeBlanc, Zubek (2004)](https://aaai.org/papers/ws04-04-001-mda-a-formal-approach-to-game-design-and-game-research/) | Mechanics create dynamics that generate player experiences | Make *choice/rivalry/hope* apparent, not just route data |
| [Self-determination motivation](https://journals.sagepub.com/doi/pdf/10.1037/a0019440) | Autonomy, competence and relatedness help explain game engagement | Player can understand routes, get better, and share an amusing moment with another person |
| [Board-game usability heuristics — NN/g](https://www.nngroup.com/articles/usability-heuristics-board-games/) | Legible state, manageable mental work and meaningful agency aid use | No hidden third choice, no mystery symbol, clear active seat and causal feedback |
| [Game-feel survey](https://doi.org/10.1109/TG.2021.3072241) | Responsive amplification clarifies the impact of actions | Dice anticipates briefly, pawn lands visibly, shield actually communicates blocking |
| [Board Game Arena UX guidelines](https://en.doc.boardgamearena.com/BGA_Studio_Guidelines) | Put the current action at visual center; secondary material below/aside on mobile | The action panel and readable board win over decorative wallpaper |
| [Apple developer game onboarding](https://developer.apple.com/app-store/onboarding-for-games/) | Introduce complexity when needed; get the user playing | One-tap Solo default, contextual instruction at first fork, no forced multistep tutorial |
| [Flow theory in games review](https://pmc.ncbi.nlm.nih.gov/articles/PMC7751419/) | Clear goals, immediate feedback and reasonable challenge support flow | Player always knows what to do and WHY a route choice matters; not just more dice rolls |

These studies/frameworks are perspectives, **not formulas for commercial success**. Do not infer that adding animations alone increases retention or that any specific time target is universally optimal.

## What premium board-game presentation means here

**Wingspan:** draws players into a single atmosphere with illustration, transitions, sound and consistent paper-card identity — rather than a static grid with menus. References: [Monster Couch](https://www.monstercouch.com/merging-worlds-creating-wingspan-digital/), [TouchArcade review](https://toucharcade.com/2021/08/16/wingspan-review-iphone-ipad-ios-board-game-adaptation-monster-couch/).

**Ticket to Ride digital:** makes routes, player ownership, state and choices readable at multiple device sizes, including color-accessible alternatives. [Designer case study](https://www.carolinechenriksen.com/ticket-to-ride).

**Root Digital:** uses illustrated pieces, board and action cards as parts of one world instead of generic Android form controls. We are not copying their images, names, characters or gameplay.

The target is **intentional, professional and coherent**, not literally AAA production budget, high asset count or hyperactive visual effects.

## Canonical Crossroads design pillars

1. **Tangible tabletop.** Cream paper, warm wood, forest-green felt, engraved dice, miniature numbered pieces, readable cards, a restrained warm-gold accent. Paths remain the clearest elements and risk colors never change meaning.
2. **Legible rivalry.** Know whose turn it is, where everyone is, who is approaching Finish, and what meaningful encounter just happened. Do not calculate or imply rankings from inaccurate physical x-position on a winding board.
3. **Agency before spectacle.** At a crossroads, show High/Medium/Low risk and the implied tradeoff; allow full-map inspection and return. Don't turn any choice into automatic video interruption or a fake pick where one road always dominates.
4. **A recognizable emotional rhythm.** Roll (brief anticipation) → travel (visible pawn movement) → land (tiny feedback) → crossroads/event (consequence card) → rival response → continue. Memorable moments get a little extra emphasis, routine AI turns don't.
5. **Calm, not sluggish.** A short satisfying ritual on actions; no long mandatory animation, especially when the same event happens 15 times in a Long race. Cozy/Brisk remain real, with sound/touch/reduced-motion controls.
6. **Useful from the first tap.** One-tap Solo default, accessible text and colors, no forced tutorials, options only when needed. Optional Pass & Play and nearby co-play do not make offline solo depend on Wi-Fi.

## Roadmap in the order that solves the actual problem

| Priority | Milestone | Meaningful deliverable | Acceptance / guardrail |
| --- | --- | --- | --- |
| P0 | **GAMEFEEL-33: Signature turn** | A visually distinct compact turn plaque for roll/travel/route/mischief/finish; light opt-out haptic on deliberate human input. No new rules | Android builds, labels tested, no AI-triggered vibrations, phone view remains readable, pause/recovery unchanged |
| P0 | **GAMEFEEL-34: Events & consequences** | One language for landing pulses, shield blocks, setback/prank impacts, event card appearance, route-selection response and results celebration. Use an unobtrusive cue hierarchy | Show cause→effect reliably; no duplicate event, input locking, forced long holds or hidden opponent move; honor Cozy/Brisk |
| P0 | **VISUAL-35: Art direction master pass** | One reusable tabletop component library (board frame, route chips, tokens, dice, event card, modal, title plaque, victory), coherent wood/paper palette, illustrated landmarks not fake routes | Approved source/provenance for assets, real 360–430dp phone and large font; route contrast/touch remain excellent |
| P1 | **GAMEPLAY-36: Agency and balance experiments** | Measure *choice value* at every fork and first-seat advantage; simulate risk-reward, explore optional experimental rules in a separate draft | No gameplay fingerprint or save-schema change without a migration plan; no unverified claims of human fairness |
| P1 | **PACE-37: Actual playtesting** | Manual timed 2/3/4-seat Quick, Classic, Long sessions, decision count, downtime, event attention and postgame reaction; optional local-only debug trace | Decide match targets from player experience; never artificially pad Quick or Long to hit clock minutes |
| P1 | **HOME-38: Game-first navigation** | Play/Continue first, curated course tiles with illustrated preview, optional modes beneath, sensible defaults, a real game home rather than a setup form | Start accessible without scrolling on reference phone, readable at large fonts, no extra pages just to begin |
| P1 | **SOCIAL-39: Nearby/Pass & Play** | Actual 2-phone Host/Join finish/rematch/reconnect, private trusted-Wi-Fi threat review; better seat presence and handoff | Physically tested on two phones; no account, no public-network security claim |
| P2 | **RELEASE-40: Store-quality pass** | Production signing, accessibility, consent/privacy text, art/name rights, installation/data migration, stable offline release pipeline and actual release QA | Explicit owner approval, no inadvertent `main` merge or publication |

## One whole match as a dramatic arc

- **Opening (first roll):** sense of place, clear start, players gathered, learn risks without a long tutorial.
- **Exploration (first forks):** 1–2 real decisions, friendly surprises, rivals visibly on different roads.
- **Middle (consequences):** uncertain outcomes, occasional smart recovery and cheeky pranks, not endless interruption by an identical notification.
- **Closing (finish approaches):** show genuine proximity, no dice rigging or secret comeback boosts; a memorable crossing and a concise result celebration.
- **After:** match recap that tells *why* the race was interesting; rematch is obvious. No demand to claim daily rewards or join a persistent grind.

## Proposed pacing experiment (NOT locked timings)

A normal phone turn should immediately acknowledge tapping **Roll**. Try ~250–450ms decorative die anticipation, ~150–220ms per visibly traveled space, and a brief arrival cue. An important shield or prank may have 0.5–1.2s of extra clarity; routine repeat events should be considerably briefer. Do not make the *core action* wait if visuals can be skipped. These are prototype ranges from existing local research, not values verified by an external study. A player must always retain authoritative legal choices and a functional Brisk option.

The current Game Bible aspirational lengths (Quick 5–10m / Classic 15–20m / Long 25–40m) are **not validated** and the owner's 34-second Quick game strongly cautions against treating them as actual product facts. Recalibrate with human test evidence rather than simply adding nodes or compulsory animation.

## What to measure at the next 12 human play sessions

Capture locally or manually, without accounts/cloud analytics: course, participants, real duration, turns, decisions, event types, approximate waiting/downtime, misread risk icons, fork hesitation, moments that made players smile, observed impatience and whether they voluntarily choose Rematch. Ask three questions immediately: **What choice mattered? What felt slow or confusing? Would you play another match right now?** Record observations rather than generic 5-star reactions.

Avoid optimizing for one vanity metric. A 15-minute Long session that is boring is worse than a 7-minute Long session that players eagerly repeat. A quick race shouldn't be a simulation where tapping roll is the only consequential human contribution.

## Quality gate for visual/moment work

- No change in `core/`, board JSON, event odds, save fingerprint, LAN protocol or deterministic outcomes for presentation-only milestones.
- All source changes stay on draft branches until owner approves main merge.
- Exact-head Android compile/tests/lint/signing, existing core/rules CI and 1-phone screenshots; 2-phone LAN is a separate gate.
- Three-choice junction map inspection returns to the same pending action. Background/resume cannot consume a dice roll, move or event.
- Accessibility: legible body text at Android font scaling, touch targets, reduce-motion support or accessible alternatives, correct semantics, colors not the only status indicator.
- If feedback is animated, the **game model never waits for decoration to decide legality or determine odds**.
- Do not add new monetization, currencies, levels, extensive profiles, achievements or cosmetic collection requirements to solve a presentation problem.

**Design review rule:** For every new effect or asset ask: *Does it help players feel anticipation, agency, consequence, togetherness, or clarity?* If not, don't ship it.
