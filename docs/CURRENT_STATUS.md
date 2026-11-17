# Current Crossroads Status — Phone Polish v0.3.1 (QA 1037)

**Last verified:** November 16, 2026 (America/Kentucky/Louisville); GitHub CI completed November 17 UTC.

## Repository state
- Active: [`feature/turn-flow-v0.3`](https://github.com/PlayerEden/GridMates-Crossroads/tree/feature/turn-flow-v0.3), [Draft PR #8](https://github.com/PlayerEden/GridMates-Crossroads/pull/8), stacked on Woven Trail Draft PR #7 and Foundation Draft PR #1. No merge to `main` or public beta release.
- Stable gameplay prototype: one human against 1–3 local AI on a fixed 38-node Woven Trail board, five crossroads, regional hazard/shelter/mischief event pools, dice, winners, local win/completed statistics, bounded event history and a scrollable map with pinned action controls.

## Real owner-phone evidence
- Earlier screenshot set confirmed one **three-seat human victory**.
- Latest owner screenshot set confirms the main menu reading **1 win and 1 completed match**, consistent with the previous victory surviving the user's install-over. It also shows a **four-seat AI3 victory**, visible route choices, ongoing AI movement, recent events and an expanded journey journal.
- The screenshots revealed the literal copy defects “You's turn”, “1 wins”, “1 completed matches” and “1 steps remaining”. They also revealed that the action panel could open with its heading partly scrolled away.
- Full details: [Owner four-player QA](QA_OWNER_PHONE_FOUR_PLAYER_TURN_FLOW.md). Owner screenshots are not published in this public repository.
- The four-seat game appeared to span roughly 8:27 to 8:29 PM in screenshot timestamps. This is **not** reliable timed measurement; formal Quick/Classic/Long pacing remains untested.

## QA 1037 implemented and independently verified
- User-turn heading reads “Your turn”; win/match and steps/encounter text pluralization correct.
- Match action heading, dice summary and multi-path hint stay visible above the separately scrollable lower choice list.
- Action list scroll resets after every game revision, including different intersections within the same turn.
- Latest event cards subtly distinguish benefit, setback and mischief by background color.
- [Design data QA PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44003804575); [Kotlin/JVM QA PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44003810169).
- [**Signed QA 1037 build PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44003804869), [APK artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44003804869/artifacts/14292919347). APK source SHA `47e158e1e8f29435a2d44815754a6360d84a15fa`, SHA-256 `1dd8e270cbc14823f91432d3635e4f87529f19b7c6b7f377be4ce9b5db9bc0c9`.
- [**1026→1037 real Android 15 emulator install-over PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44004288480): both real SHA-pinned APKs; seeded profile completed=7 and wins=3 preserved byte-for-byte; updated app launched and interacted with a crossroads. [Emulator screenshots](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44004288480/artifacts/14293642513) confirm “Round 1 · Your turn”, “1 step remaining”, and visible path choices.
- Independent QA package `com.playereden.gridmates.crossroads.qa`, same **public test-only QA key** as prior builds; not suitable for production signing.

## Still open
1. **Owner phone review of QA 1037** on top of 1026; test three-way Old Forest Junction, action panel small-screen scrolling, dismissible Leave dialog, and complete a full match.
2. After the four-player AI loss, menu should show the previous 1 win **and 2 completed matches** if the record update happened once; check and then restart app. The last screenshots ended at the loss screen, so this outcome wasn't verified.
3. Token/event marker legibility, accessibility and board zoom/inspection; full game-balance measurements and actual match-length modes.
4. More satisfying dice/space movement and event reveals, optional audio, approved GridMates animal art, active-match saving, LAN and multiplayer modes; no automatic expansion of scope without owner review.

**No further code changes are required merely to document this receipt.** The owner should test the exact signed QA1037 artifact from the link above. Do not mistake later documentation-only CI builds for accepted updated APKs.
