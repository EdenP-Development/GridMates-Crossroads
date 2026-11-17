# Crossroads — Lean Classic Game Roadmap

## Immediate implementation priority — LAN-22 (Nov 17, 2026)

Owner redirected development from repeated fine tuning to **missing core features**. The first LAN milestone implements two actual Android devices: Host Nearby, Join Nearby (NSD with manual IP fallback), one host-authoritative 2-human Woodland match, shared legal turns/routes/pranks, host rematch, and in-memory seat rejoin. Source and loopback tests are a test candidate; real two-device gameplay must be verified before declaring LAN ready. [LAN-22 two-phone QA](LAN_22_TWO_PHONE_VERTICAL_SLICE_QA.md).

Next after this test: prove first complete two-phone match and fix blocking connectivity/turn bugs; then support 3–4 remote seats, robust resume, and broader content/boards. Avoid another string of cosmetic micro-milestones until major planned functions work. Do not assume unverified LAN room discovery or use public networks for the unencrypted QA protocol.

**November 17, 2026.** Player and developer simplicity are now first-class requirements. See [GridMates Classic Games Charter](GRIDMATES_CLASSIC_GAMES_CHARTER.md).

## Actual current foundation

- **Verified automated QA baseline:** signed [QA1121 Android run](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44163403210) and deterministic save-replay tests passed. Real owner-phone force-stop recovery and install-over are not yet proven.
- **Implemented:** one Woven Trail woodland board, 2–4 player seats (one solo human plus AI), single die, crossroads, encounters, mischievous pranks/shields, camera gestures, results, home, How to Play and local match checkpoint.
- **In this candidate:** optional **2–4 player Pass & Play on one phone** (all seats human). Source + tests must receive exact new Android CI receipt before calling it installable.
- **Not implemented:** real same-Wi-Fi host/join, reconnect, full remote multiplayer, additional boards/match lengths, production app store release.

## Small, verifiable stages

| Priority | Stage | What we actually deliver | Gate |
| --- | --- | --- | --- |
| 1 | R2 finish solo and saved games | Current board/AI, complete matches, exact-save recovery on phone | QA install-over; force-stop/reopen mid-fork; statistics once; accessibility |
| 2 | CLASSIC-17 local Pass & Play | Solo vs AI **or** 2–4 humans passing one phone, same unchanged rules | 2/3/4 human full-game tests, Android build, real device handoff and rematch |
| 3 | R3 optional Nearby Play | Two phones on one Wi-Fi: Host, Join, play, reconnect | Host-authoritative rules and *real two-phone* match; no server account |
| 4 | R4 presentation polish | Approved simple animal token art, gentle audio and legible game UI | Performance, art provenance, readability, phone screenshots |
| 5 | R5 replayability content | Only meaningful new events or one more authored course/length | Measured 2–4 seat pace, route fairness and willingness to replay |
| 6 | R6 release | Production signing, store/IP audit, accessibility, testing | Owner approval before merge/release |

## What we explicitly do NOT build by default

- Premium currency, XP grinding, battle passes, daily quests, loot boxes, paid advantages, battle royale modes, player economy, property management, server-required solo play, forced notifications, or complicated account/friend systems.
- Three layers of profile/collection menus just to start a match.
- Generic networking frameworks before a *simple working Host/Join* actually needs them.
- Endless new features when one excellent, finished classic game is enough.

## Constraints

- No date promises or tiny APKs for every text change.
- A rules or UI change must pass existing automated workflows **and** be verified on an actual device before being labeled phone-accepted.
- Source stays on **draft PRs** until owner authorizes merge to main.
- Sibling Sudoku and Word Search repos remain independent. Share approved art/design principles deliberately, not their entitlement ledgers or local saves.
- Past unapproved rules hypotheses in the Game Bible/Open Questions stay open; this roadmap does not silently lock them.
