# GAMEPLAY-37 — Interactive encounters (Lab v3)
**November 18, 2026 · Draft offline playtest; not a default-rules change**

## Why

The owner demonstrated that Crossroads can be won through random taps in under a minute. AGENCY-34 measured random tapping and route dominance. GAMEPLAY-35 added Steady/Push and guaranteed branch events; GAMEPLAY-36 improved shelter travel and showed remaining problems, especially on Long. The missing dimension is **bounded counterplay**: choosing what to do *after* committing to a trail rather than accepting an automatic bonus/setback.

## New Lab v3 loop

On the home screen, switch **Gameplay 2.0 · Playtest v3** ON, then select an offline Quick, Classic or Long match. The default OFF continues to use the untouched original rules. LAN still explicitly uses the original Classic engine, not v3.

At each human turn, choose **Steady (4, blocks a setback)** or **Push (1–2 stumble for 1; 3–6 moves 5–8)**. At a fork, the familiar risk colors and shortest remaining graph distance remain visible. An eligible route commitment triggers at most one event/encounter per turn. Steady + Shelter still grants a visible +1 step.

Under **v3 only**, an encountered **Shelter** offers a small but genuinely contextual decision:
- **Take supplies:** advance 2 additional steps right away, no new shield.
- **Take cover:** gain a shield against the next prank and advance 1 additional step.

A **Hazard** offers:
- **Take the gamble:** draw the usual, weighted regional hazard encounter (advance or retreat); Steady still blocks one setback.
- **Brace:** end the current move on the entered route, with no hazard setback. Good when protecting a lead; costly if you have unspent movement.

**Mischief** continues using its existing opponent-targeting action. No currency, deckbuilder, mandatory tutorial, inventory, automatic catch-up rigging, new event JSON, new animations or timed popup was introduced. Every decision is a deliberate button in the fixed game controls.

A large die can cross two junctions, but **only the first encounter per turn triggers**. Later route previews must say the event was already used; no second benefit is implied.

## Exact technical safety

- New course identity `<existing board ID>__decision_lab_v3`; `DecisionLab.baseId` recognizes original, v1, v2 and v3. Original board/event source JSON stays unchanged.
- Append `Phase.ENCOUNTER` and replay action `MatchActionKind.ENCOUNTER`. The existing serialized `MatchAction.routeId` stores the chosen response string. No saved schema fields or content fingerprints for previous variants change.
- Both pending encounter state and chosen result are reconstructed from seed + actions. Checkpoint restore rejects replay under another Lab version. Invalid or duplicate actions must be rejected before moving anything.
- Lab v1 and Lab v2 continue using their original auto-resolved encounter rules; no silent changes to their saved checkpoints or RNG consumption.
- Human responses are offline-only; no new LAN packet types, no game-model authority delegated to the UI.
- All v3 results remain excluded from established Solo wins/completed-match counters. Manual JSON backup and import recognize v3, while older APK versions cannot read active v3 saves.

## Automated acceptance

- The existing QA1331 rules and Android suite must remain green.
- V3 synthetic fork proves both shelter choices and two hazard responses, with exact deterministic checkpoint replay during the pending decision and afterward.
- Invalid choice rejects without a free move or loss of state.
- V1/v2/original checkpoints encode/replay identically and cannot be reinterpreted as v3.
- 3 courses × 2–4 seats × 28 seeded games finish without softlocks and early checkpoints restore, including bot encounters.
- 2,304 synthetic races / 1,152 paired v2/v3 scenarios over **every 2- and 4-player human seat** compare random, fast, cautious and adaptive policies. Measure completed races, actual encounter decisions per person and wins; do **not** assert statistical significance or human enjoyment.
- Android unit tests cover v3 pending-encounter persistence, import/export, stats integrity, title/result identity, lint, compile and signed QA package. Signed QA artifacts are only recommended after exact-head pass.

## One-phone playtest / acceptance questions

1. On Quick Lab v3, try both Steady and Push. Select Shelter, choose supplies, and observe the added movement **once**.
2. Repeat and choose cover; confirm a shield is gained and remains until an opponent uses a prank, and that you see fewer immediate spaces than with supplies.
3. Choose Hazard. Test Brace (no retreat, movement ends) versus Gamble (weighted risk, visible response). Distinguish the hazard outcome from the route name.
4. Force-stop while `Choose your response` is active, reopen and ensure the same options and current turn return; repeat after an option is selected.
5. Confirm existing Lab v1/v2 saves and original Quick/Classic/Long race records are preserved, and experimental wins don't increment official stats.
6. Record **real** Quick/Classic/Long session time and whether the extra decisions create interest or just extra tapping.
7. Observe AI turn pacing: bot encounters should be understandable without stalling the game or hiding the board.
8. Test large text, small screen, dialog accessibility and match results. Real two-phone LAN remains a separate gate.

**Do not promote as balanced merely because tests pass.** If all players choose supplies/gamble every time, or if Long feels tedious, it still needs redesign. Keep this draft unmerged and unpublished until owner approval.
