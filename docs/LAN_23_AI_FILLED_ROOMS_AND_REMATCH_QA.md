# LAN-23 — Nearby AI-filled rooms and safe rematches

**November 17, 2026 — Draft follow-up to LAN-22. Source and automated tests are not a substitute for a real two-phone session.**

- Branch: `feature/nearby-ai-fill-v0.2`, based on `feature/nearby-lan-v0.1`.
- Scope: one Android host phone (human Player 1), one guest phone (human Player 2), with **zero, one or two AI opponents** (2–4 total seats).
- LAN protocol version: **2**. Both phones need the same new build. QA1187 (v1 protocol) and this build are **not** cross-compatible.
- This is **not** 3–4 human phones yet, and **not** cloud multiplayer.

## Why this matters

The initial LAN-22 implementation hard-coded exactly two human seats and two total participants. LAN-23 adds a substantial playable setup: the two friends can play a 3- or 4-racer Woodland Crossing race against AI while remaining on separate devices, with no new rules engine or backend.

During review, the LAN-22 host's guest-command callback was found to retain the *original* GameSession object. After the host started a rematch, guest actions could no longer be applied because the active session was a new object. LAN-23 changes this to apply validated guest actions to the **current** host session.

## Code and behavior

1. Host Nearby > Create nearby room offers **2/3/4 racers**. Player 1 and Player 2 are humans on separate phones. Remaining participants are normal deterministic GameSession bots.
2. The host alone executes bot actions and publishes checkpoints. The guest renders only verified checkpoints and never independently runs a bot, draws dice or draws events.
3. The host stops advancing AI when the guest disconnects, and resumes ordinary pacing when that guest rejoins. Offline Solo and Pass & Play AI progression is unchanged.
4. The host's guest-command callback follows the live GameSession across rematches and presence callbacks follow the active server, rather than the original match ID.
5. Every remote ROLL/ROUTE/TARGET command includes the match ID and expected rules revision. The host rejects stale, duplicate, or previous-match commands *before* they alter the engine. Rejected commands cause a fresh authoritative checkpoint to be sent to the guest, clearing its pending action.
6. Rematch preserves 2/3/4 total racers and the exact two-human configuration.
7. LAN matches still do not overwrite local Solo/Pass & Play checkpoint or local win records.

## Automated gates

- [ ] LAN unit tests: reject old-match and duplicate-revision requests without changing rules.
- [ ] LAN unit tests: complete deterministic 3/4-player races with exactly two human seats and verified replay after each bot/human action.
- [ ] Existing two-peer socket test updated to the versioned, revision-checked remote action protocol.
- [ ] Full core rules tests and design-data checks PASS on exact branch SHA.
- [ ] Android compile, app unit tests, lint and signed QA APK PASS on the same SHA.
- [ ] APK version/signature compatible with install-over from the previously tested QA app.
- [ ] Confirm no release/main merge; owner installs and verifies on actual phones.

## Owner two-phone acceptance checklist

Use **the same new LAN-23 QA APK on both phones**, on the same non-isolated home Wi-Fi. Update existing QA installs rather than uninstalling. Check saved local statistics first.

1. Host selects **2 racers**; join on the second device; complete a normal two-human race and rematch.
2. Host selects **3 racers** (2 humans + 1 AI). See 3 players on **both** phones. Human Player 2 can roll and choose a fork; AI Player 3 advances only once; both devices agree on location, die, encounters and active seat.
3. Host selects **4 racers** (2 humans + 2 AI). Follow at least two complete turn cycles, including an AI ROUTE/TARGET if reached. Both phones must show the same state.
4. In a 3- or 4-racer match, let AI act, then background the host. The current AI turn must not silently progress. Return to host and verify it resumes without duplicate dice.
5. With host connected, rapidly tap guest action twice. Only one matching-revision command may affect rules state; the guest must recover to a usable UI on rejection.
6. Finish any 3/4-racer match, press **Rematch on host**, then **have Player 2 act in the rematch**. This specifically exercises the fixed stale-session callback.
7. During a rematch, drop and rejoin guest Wi-Fi while its app stays alive; confirm player identity, seat state, bot turns and host connection status recover.
8. Leave Nearby, start Solo and Pass & Play, and confirm old saved records and offline behavior remain intact.

## Known limitations / explicit exclusions

- Household Wi-Fi, plaintext TCP; **not suitable for untrusted networks**. Room code is not cryptographic access control.
- An untrusted guest can see full checkpoint seed/history; no ranked/fairness protections.
- Only **two human phones** per room. Additional AI opponents are not additional peer devices.
- No process-death room recovery, persistent guest rejoin token, host migration or production-grade network resilience.
- Real Android NSD discovery, Wi-Fi routing, reconnect/rejoin, touch UX and actual 2-phone match acceptance are still pending physical confirmation.
- No changes to board graph, encounters, PRNG probabilities, save format, achievements, signing key, other GridMates apps or `main`.
