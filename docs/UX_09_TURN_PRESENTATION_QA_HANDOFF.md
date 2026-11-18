# UX-09 — Dice, crossroads and encounter presentation
**Status:** source candidate on `feature/turn-presentation-v0.9`; exact Android CI, phone UI and install-over acceptance pending until verified.
**Base:** [ART-08 Draft PR #14](https://github.com/PlayerEden/GridMates-Crossroads/pull/14), signed Android QA1070 automated pass.
**Change class:** presentation only. No dice distribution, gameplay state, route graph, target rules, AI, event draw, local preferences, network or signing modifications.

## Why
In phone screenshots and peer mobile board game research, the active decision should be obvious while the board remains visible. The previous bottom panel displayed a plain dice emoji and numeric roll, and the two-route action panel did not give the same understandable risk summary as the three-route dialog. These are polish gaps, not requests for new rules.

## Implemented source changes
- `TurnPresentation.kt` adds **original pip-die illustration** for faces 1–6 with a readable forest-color pip scheme and outline.
- The visible die on **Roll the die** cycles only the existing **presentation** frames. It does not call any new RNG, change the delay counts or call the game engine more than once.
- Once an actual game-engine die value is present, the bottom sheet renders a matching pip die and the accurate *remaining steps*. It never substitutes a preview animation face for the authoritative roll.
- Two-way and three-way crossroads now use **one visual card component**: complete authored route name, authored description, and clear `High risk` / `Medium risk` / `Low risk` where the fixture specifies those categories.
- Route risk wording is a display label only, not a probability estimate or a promise of a particular event. No crossroad options change or get filtered.
- Existing encounter reveal is now a compact, single tinted card with the actual actor, event title and outcome. The full real game journal remains in Match options.
- Primary button, board camera, action-panel separation, AI turn handoff, and Cozy/Brisk pacing are preserved.

## Evidence & playtest checklist
- [ ] Exact-source GitHub Android CI compilation, unit tests, lint and signed QA artifact pass.
- [ ] Install the new QA over QA1070; check package/signer/versionCode and saved wins, completed games and onboarding-hint preference. Do not uninstall.
- [ ] At the start of a match, tap Roll. Observe a visibly cycling pip die and **one** authoritative dice result; never double-roll or allow a second click while rolling.
- [ ] When a roll stops at a fork, the die and remaining steps reflect engine state; no disagreement between pips and the game event message.
- [ ] Old Forest's **three choices** (Cliff Track, Mill Lane, Cedar Bridge) are completely readable and selectable at normal and larger system font scale.
- [ ] Two-option forks use identical risk/card hierarchy and do not clip under the fixed bottom panel.
- [ ] Encounter reveal remains visible during the result and leaves the correct log entry in the Journey journal.
- [ ] Cozy and Brisk affect animation timing only; bot path choices, pranks, shields and results still behave.
- [ ] Overview/Follow gesture navigation and ART-08 scenery do not regress; no permanent camera bar.
- [ ] 2-, 3-, and 4-seat runs: short portrait viewport, low/mid zoom, dice action and expanded route choices never overlap.

## Boundaries
The visual die is flat 2D Canvas art, not a physical rolling simulation. This is a deliberately small UX layer; custom dice skins, audio/haptics, scenic art iterations, and board-length expansion are deferred. Android CI success alone is not physical phone gesture or subjective quality acceptance. Keep the PR draft and do not merge/release without owner direction.
