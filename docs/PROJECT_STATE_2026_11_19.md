# Crossroads — Current game state / design review (2026-11-19)

This is the first **current** summary after the long Nov 16–19 prototype stack. It supersedes older top-of-file status claims for planning purposes, but does not erase their historical QA receipts.

## One-sentence state
**Playable, illustrated, Android QA prototype — not a polished released game.** The Android build verifies that the source compiles and many rules are exercised; it does not prove responsive on-phone game feel, installed-version data preservation, real two-phone networking, accessibility or public-release readiness.

## The stable direction
A **cozy, competitive, local-first digital tabletop racer** with one shared Start/Finish, fixed handcrafted woodland maps, consequential route choices and occasional encounters. Unlike GridMates Sudoku / Word Search's page-and-puzzle menus, the active match should feel like an *immersive world with a small in-game HUD*. No accounts, timers, virtual currencies, property game or compulsory server. Original illustrations should remain untouched by rule changes.

## Implemented in the feature/draft stack
- 2–4 seats, Solo AI, Pass & Play, original/experimental decision rules, route choices, events/shields, first-to-finish results.
- Three illustrated courses: Brookside Quick, Woven Trail Classic, Cedarwood Long.
- Host/Join same-Wi-Fi **prototype**, manual IP fallback and host-authoritative rules; real two-handset and security acceptance remain pending.
- Local save/resume/replay and an optional manual progress transfer; QA install-over and public-app migration are distinct tests.
- Illustrated backplates, tiled trail atlas, animal player pieces, event/junction artwork, direct route taps, immersive camera and whole-map inspector.
- Signed QA APK, unit/rules tests, lint, validation checks, release-surface preflight and art hashes.

## Phone-verified findings, not conjecture
- Owner supplied real-phone QA 1479 screenshots on November 19: larger animal tokens, road widths and closer player-follow camera are a substantial readability improvement.
- The *same screenshots* still show webpage/form qualities: tall parchment text action dock, clipped Compare paths action, huge cream/colored route strips and hard rectangular dimming, conventional full-map/results dialogs, header labels cut off.
- Match completion and local record visuals work (a prior owner screenshot showed 7 Solo wins / 9 finished), but this doesn't validate all save/release migration cases.
- Real human play has been short in some prior screenshots (~4 rounds / 45 seconds). The Game Bible's longer desired match durations are not achieved by simply slowing the animations. Measure actual runs before any balance change.

## Latest integration chain (all DRAFT, unmerged)
- #54 immersive board world; #56 compact HUD; #57 follow-camera reticle
- #58 closer camera, playable element scale, stronger branch focus — Android QA build **1479 green**, owner screenshots supplied
- #59 small animated hops and landing ripples — Android QA run **44632302763 succeeded** (source still needs device review)
- #60 complete turn presentation — compact pinned controls, in-world die anticipation, organic painted route cues. **In development; must pass final exact-head CI and owner phone acceptance**.
- **52 open draft PRs** seen at this review; most form an unmerged stacked chain of incremental changes. PR #55 is a parallel compact-HUD experiment, not part of the active #56→#60 chain. Avoid merging them blindly.

## Current assessment
| Area | Readiness | Remaining gate |
| --- | --- | --- |
| Rules/core board | Strong prototype | Human fairness, meaningful choices and actual match duration |
| Solo Android | Playable | Repeated realistic sessions, camera and UI feel |
| Pass & Play | Implemented | 2–4-human device acceptance |
| Same-Wi-Fi LAN | Prototype | Two physical phones, rematch and reconnect, security policy |
| Saves/QA stats | Implemented | Force-stop, install-over and manual export/import |
| Illustrated art | Strong original foundation | Device legibility/contrast, art owner approval |
| Game presentation | Major active work | Pinned action choices; roll, encounter, route, results choreography |
| Accessibility | Partially supported | Large fonts, screen readers, reduced motion, 48dp controls |
| Release | **Not ready** | Reviewed privacy/IP, private signing, explicit consolidation and owner signoff |

## Next decisions in order
1. Complete EXPERIENCE-60 and play through **one full, compelling Woven Trail turn** on a real device. The principal action should never be clipped. Avoid further decorative assets until the core turn feels coherent.
2. Based on those screenshots, revise main menu/setup and full-map/results presentation into game-native flows; maintain clear readable accessible alternatives.
3. Prove saved-match/install-over and two-phone LAN behavior; measure human session duration and AI pacing. Fix game/rules only after evidence.
4. Make a **single reviewable integration branch** with release receipts and QA history; keep `main` unchanged unless explicitly authorized. Do not auto-merge 52 PRs.
5. Consider publishing only after QA, security/privacy/store-signing and explicit owner release approval.

See [Game Bible](GAME_BIBLE.md), [Experience Bible](GAMEFEEL_33_EXPERIENCE_BIBLE_AND_ROADMAP.md), [Classic Games Charter](GRIDMATES_CLASSIC_GAMES_CHARTER.md), [Milestone 60 device acceptance](EXPERIENCE_60_COMPLETE_TURN_QA.md), [RC0 review](RC0_CONSOLIDATED_REVIEW.md) and the exact GitHub Actions run for each commit.

**Do not conflate “CI green,” “physically tested on a phone,” “merged to main,” and “ready to ship.”**
