# GridMates — Classic Games, Practical Development

**Owner direction:** November 17, 2026. This is the standard for GridMates: Crossroads and a *proposal to apply consistently* to the sibling Sudoku and Word Search games. This document does not change other repositories, release approvals or their existing design decisions.

## Identity

**Familiar games, thoughtfully made.** The aspiration is a comfortable, modern version of games people already understand from newspapers, puzzle books, a kitchen table, or an old home computer. This is about being welcoming and replayable, **not** making an advanced strategy board-game franchise.

A GridMates game should satisfy **two simplicity tests at the same time**:

1. **Easy for players:** One obvious primary action, legible text and controls, quick learning, low-stress pace, optional local togetherness, no mandatory account or persistent meta-game.
2. **Easy for us to develop and maintain:** A single strong gameplay loop, small self-contained code and tests, reused approved art/design tokens, modest game-specific storage, offline by default, simple release engineering, no mandatory hosted services.

## Non-negotiable product filter

- Add something only if it improves core gameplay, replayability through *content*, accessibility, local togetherness, or reliability.
- Prefer another well-balanced puzzle / board / layout / difficulty to another menu, currency, progression tree, daily obligation, or live event.
- No paid power, energy meter, gacha, property/economy simulator, mandatory login, battle-pass chores, social feed or backend-dependent solo mode.
- Skip any feature that takes longer to maintain than the enjoyment it adds unless the owner explicitly approves the tradeoff.
- Avoid forced tutorials. Offer simple, optional How to Play.
- Each app is independent: no cross-app profile sync, cosmetics entitlements, database or unlock logic without a specific later decision.
- A playable, polished small game is a valid **finished** game. Feature count is not a success metric.

## Crossroads — small V1 promise

**Roll → travel → choose at a fork → see occasional encounter → first to Finish wins.**
One handcrafted Woodland Crossing course is enough for V1. No RPG layer or giant inventory.

- Default: offline one person versus 1–3 fair AI players.
- **Pass & Play**: offline 2–4 people sharing one phone. All turns are human; no internet, accounts or room codes.
- Later *optional*: nearby phones on the same Wi-Fi with one authoritative host and simple Host / Join. Networking must be built once reliably and reused as a design pattern, not copied without adapting game rules.
- One saved local match; clear rematch/menu and accurate stats. No complex profile required to play.
- Pleasant illustrations, readable risks, subtle dice feedback, camera gestures; visual polish should not hide the board.
- Long/Quick course variants, more encounters and cosmetics are content *after* core modes work on phones, not prerequisites.

## Development ordering, not a promise to deliver everything at once

1. **Prove reliability on actual devices:** update-over install, 2–4 seat completed matches, force-stop/recovery, Back behavior, small/large font and controls.
2. **Pass & Play**: minimum useful togetherness with no networking.
3. **One minimal LAN flow**: build and physically test Host/Join, one room, reconnect, fair host-owned rules; no friends/account service.
4. **Polish gameplay presentation:** animal/token assets *only* when approved and provenance-tracked; audio/haptic optional, default unobtrusive.
5. **Expand content sparingly:** one additional handcrafted course or meaningful event set, measured by actual match duration and enjoyment.
6. **Finish/release audit:** production signing, accessibility, store/IP checks, regression and owner signoff.

## Definition of done for each change

- A clear player-facing benefit in one sentence.
- Automated unit/board/Android CI results tied to exact source SHA; signed QA APK for installable milestones.
- A device test plan, with unsupported claims labeled pending (especially LAN and install-over).
- Source stays in a draft PR, **no merge to main without owner direction**.
- No unnecessary dependency, cloud service, account flow, schema migration, custom economy, or duplicated subsystem.
- Be willing to remove an unnecessary feature instead of polishing it forever.

## Open decisions

LAN implementation and multi-phone acceptance; optional rule variants and course lengths; audio defaults; exact character art imports; monetization and store listing. None are silently approved by this charter.
