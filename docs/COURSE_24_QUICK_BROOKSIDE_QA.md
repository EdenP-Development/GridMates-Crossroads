# COURSE-24 — Brookside Dash, a real Quick Race

**Draft milestone, November 17, 2026.** Stacked on LAN-23. No merge to `main`; real-device playability not yet accepted.

## Player feature

- Solo vs AI and local Pass & Play offer **Quick · Brookside** or **Classic · Woven Trail**, followed by the existing 2/3/4 participant choice.
- **Quick** is a separate, original **24-node, two-fork** handcrafted board with six distinct routes of **13–15 moves** to finish (before event effects). It includes a three-way woodland fork, readable high/medium/low risk labels, curated meadow/forest/village events, and quieter themed paper/creek scenery.
- **Classic** is the unchanged, original **38-node, five-fork** Woven Trail; its rules, event pools and asset art were not modified.
- Both courses use the same dice, movement, encounter engine, solo AI and local pass-and-play rules. Quick is meant to be shorter in **route steps**. Exact minute targets and balance are **not** promised until observed on a real phone.
- Nearby Host/Join still uses **Classic only**. No new multiplayer protocol or net compatibility changes in this milestone.

## Save compatibility — do not regress

- The existing single-slot recovery key and format remain unchanged.
- Every checkpoint already contained a `boardId`; the store now looks that ID up in the two bundled definitions when restoring rather than assuming Woven Trail.
- An existing Woven Trail save is loaded using the same untouched Woven Trail board/event fingerprint and resumes as Classic.
- A new Brookside save resumes using Brookside's own fingerprint, seed and action log.
- A checkpoint referencing an unbundled board is rejected with a warning, not silently rewritten. Explicitly starting a new race still replaces the single active-match slot, as before.
- Local match counts/wins remain unchanged from the established rules. Install-over must keep the old QA package ID and signer.

## Automated acceptance

- [ ] Verify Quick's 24 unique positioned nodes, directed paths, accessible finish, 2 forks, and correct risk-to-branch information.
- [ ] Validate there are at least two eligible positive-weight event definitions for each event space.
- [ ] Run 2/3/4-seat bot-only races across a seeded sample; verify termination and checkpoint replay against the exact Quick board.
- [ ] Reject Quick checkpoint restoration against Classic board.
- [ ] Test old-style Classic checkpoint saved with single-board store and loaded with new multi-board store. Confirm match ID, snapshot, stats.
- [ ] Save/reopen Quick race after a committed roll; do not create fake wins/completed counts.
- [ ] Full rules and design-data checks, Android tests/lint, signed artifact, signer and package identity on exact branch PR SHA.

## Owner physical-device acceptance (one phone is enough)

1. Install the new QA APK **over** the previous QA1194 or current matching QA application; do not uninstall or clear data. Verify previous Solo stats and any in-progress Classic game remain intact.
2. From the main menu select **Quick · Brookside**, two players Solo; start and inspect the 24-node board with the new brook/forest artwork. Ensure it is comfortably readable on your handset.
3. Roll and choose both two-way **Meadow Fork** and three-way **Brookside Junction** during ordinary matches. Routes and danger descriptions should make sense.
4. Finish Quick with 2, 3 and 4 participants across Solo/Pass & Play; check winner/results and that it *actually feels shorter* than Classic.
5. Start Quick, stop/reopen the app at a fork, and confirm same positions, decision, RNG continuation, and statistics. Try a Quick rematch and verify it stays Quick.
6. Start Classic; verify existing Woven Trail layout/art, five forks, camera movement, help/results, no changed event outcomes for fixed replay and saved progress.
7. Ensure **Host Nearby / Join Nearby** continues to offer the prior Classic course and does not claim Quick LAN support.

## Boundaries / follow-ups

- This is a **Quick playtest candidate**, not approved balance. If its pace is too short or a path dominates, refine on evidence without sacrificing save compatibility.
- The original Woven Trail board and event catalogue are **not changed**; no breaking fingerprint migration for existing saves.
- Later: real phone time comparison, opt-in Long course, richer approved token art, 3–4 human devices, host recovery and proper network/security hardening. None is implied as implemented here.
- No server, account, purchases, new economy, new saved-data schema, different app ID or production signing.
