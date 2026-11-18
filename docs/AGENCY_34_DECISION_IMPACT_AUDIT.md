# AGENCY-34 — Stop rewarding button mashing

**November 17, 2026 · Core gameplay diagnosis + automated baseline, draft.**

## Owner's observed issue

> "I can basically just keep pressing randomly. Game can end in under a minute."

This is *not* primarily an art/UI problem or an animation-duration bug. The current game engine implements:
- `ROLL`: single six-sided die, immediately computes all legal forward movement until the next junction or landing; no choice on most turns.
- `ROUTE`: select one of 2–3 branches (known risk label, but no projected distance, opponent race context, or resources to trade).
- `TARGET`: when a rare prank event is rolled, pick an opponent. Most event draws apply a random gain/retreat/shield with no player decision.
- `FINISHED`: first player reaches Finish. This can happen early on Brookside (13–15 edge paths; user's four-racer quick playtest completed in 34 seconds).

**The problem is a weak decision-to-action ratio.** The player spends more attention pressing Roll and waiting than making informed, consequential choices. A longer board would mainly multiply the routine input, not fix it.

## Measurement before mechanics redesign

`AgencyAuditTest` uses *unchanged* rules and boards to measure **1200 baseline synthetic matches** over Quick, Classic and Long × 2/4 players × 200 fixed seeds.

- One simulated human always rolls immediately and chooses routes uniformly randomly (first available legal prank target). Other players use the existing bot policy.
- Records human roll actions, route choices and prank choices, win rate, and whether an AI can finish before the human reaches even their first fork.
- At the first human fork, checkpoint/restore all alternatives and continue each branch under a fixed synthetic post-fork choice policy. Report how often the human's winner status or overall winner changes depending on the initial route.
- A decision that sometimes changes the winner is not automatically *engaging*. A random outcome change alone does not show skill expression.
- This report **does not measure wall-clock seconds or human strategy**. Do not claim either from these counts.

The results will appear under CI artifact `Crossroads-AGENCY-34-<SHA>` containing summary and per-seed CSV. Existing deterministic balance measurements and signed Android QA remain unchanged.

## Candidate mechanics for a dedicated separate experiment — NOT implementation approval

The next gameplay prototype must address both **how often the player chooses** and **whether that choice can be right for a reason**. Prototype only one or two systems at a time:

**A. Make existing crossroads strategic (first, lowest-risk step).**
- Explicitly show **route length**, potential event exposure, and short vs shelter/mischief tradeoff. Don't pretend a risk color alone is a tactical decision.
- Show opponent proximity and the likely finish lane clearly, without revealing future random draws or silently rewriting odds.
- Test whether a novice can explain *why* they chose a road after playing.

**B. One genuine travel choice before each roll (proposed, not locked).**
- A pair of simple stances with an **observable, quantitative tradeoff** (e.g. steady progress / risky burst), not two differently colored "roll" buttons. Make the decision context change with lead, hazards, available protection and upcoming crossroads.
- Keep a single clear default for relaxed players; never require lengthy planning to play.
- Test multiple designs via separate deterministic rule/AI experiments before selecting one. If "always fastest" dominates, reject it. Avoid stacked currencies, long inventories and paid boosts.
- Rule/schema changes must introduce a safely versioned mode so current saved matches, deterministic replays and 2-phone LAN protocol do not silently break.

**C. Limited agency within encounters and rivalry.**
- Make occasional, **important** event moments offer bounded decisions (keep a protective shield vs chase a shortcut; spend a one-time aid to avoid a setback). Most encounters should stay fast and auto-resolve, otherwise Long becomes exhausting.
- Give human players counterplay against mischievous rivals; no unavoidable multi-turn punishment and no magical comeback dice.
- Align event feedback to cause/effect rather than random text.

## Whole-race structure

An enjoyable match should have identifiable **opening / middle / finish** stakes:
- Opening: learn route and establish a plan; first meaningful choice arrives quickly.
- Middle: leaders make different choices from players behind; opponent decisions and risk have visible consequences.
- End: comeback possibility from transparent systems, clear visible Finish tension, immediate meaningful results.

## Success / failure criteria for the next gameplay prototype

- At least one clearly understandable **nontrivial choice** near the start, and interesting decisions during a meaningful fraction of turns. Do not add repetitive mandatory prompts merely to increase the count.
- Random tapping should be a viable *casual* way to play, but an informed player should find explainable ways to improve outcomes, with statistical evidence across seats/boards and a human playtest.
- A shorter race may still be fun and fast; reject both sub-minute autopilot and artificially padded 20-minute boredom.
- Measure **median physical phone minutes**, active human decisions and time waiting for AI, first decision delay, strategic win lift (smart vs random, measured with seat symmetry), perceived fairness and desire to rematch.
- Keep Quick easy to start and suitable for older/nontechnical players; advanced strategy is a light layer, not a complex strategy sim.
- The Android build must survive force-stop/save restore; LAN host authority must validate any new action types and messages before multiplayer receives the rule.
- No `main` merge, changing existing board/event content fingerprints, or public signing without owner approval.

## Work order

1. Run this **agency measurement** on exact current rules.
2. Design and test a **minimal rules-mode prototype** that yields actual tradeoffs at roll/fork/encounter. Compare against random-policy baselines using mirrored starting seats.
3. Present tactical information legibly through the game UI—not a wall of statistics.
4. Test Quick/Classic/Long with 2/4 seats on a phone. Only then increase content, animation complexity or cosmetics.

**Design warning:** Better art and longer session length are not substitutes for consequential action.
