# LAN-77 — Nearby room admission, reconnect and rematch regression

**Status:** stacked draft after M76 / signed QA1603. All testing below is **JVM loopback socket testing**, not two-phone Wi-Fi acceptance. No change to game rules, animation, board files, host/guest seat count, saved matches, Android permissions, LAN protocol version, wire format or app signing. `main` stays untouched.

## Why

The source already supports host-authoritative local TCP races, NSD room discovery with manual IPv4 fallback, optional six-digit room codes, guest seat reservation via in-memory rejoin tokens, guest revision/MatchId guards, and a first host/guest TCP test. The existing socket test validates one admitted guest roll; it does **not** cover the normal lifecycle where somebody types a bad room code, a second device attempts to join an occupied room, the original guest disconnects/rejoins, or the host starts a new race while old packets are still in transit.

The owner is not available for phone testing right now. These deterministic tests address transport behavior that can be verified without hardware, while keeping physical Android NSD, router multicast, device sleep and two-handset convergence **explicitly open**.

## Implemented

### JVM socket lifecycle tests (`LanConnectionLifecycleTest`)

1. **Private room:** a bad code is refused with a specific error; a correct code admits one guest and returns a non-empty, high-entropy rejoin token plus verified initial checkpoint.
2. **Occupied seat:** while the first guest is present, another socket is refused with `Room is full`. Closing the authorized socket results in host disconnect and preserves the reservation; a different token cannot steal the seat.
3. **Same guest rejoin:** presenting the exact reserved token on a new socket regains the same room and receives the current authoritative snapshot. A later host-published roll arrives and restores identically from the checkpoint.
4. **Rematch/late packets:** within an admitted connection, a valid guest roll advances the game once; replaying the same action cannot roll twice. The host switches to a new MatchId and seeded game; a stale old-MatchId command is rejected without mutating it, while a fresh new-MatchId command works.
5. **Wrong-room packet:** after admission, a socket action bearing a different room ID is rejected and the connection closed without ever executing an action.
6. **Specific error preservation:** a real `CrossroadsLanGuest` receiving `Incorrect room code` reports that message exactly once, rather than immediately overwriting it with a generic disconnect notice.

Tests use local sockets, bounded reads and synchronization/latches, and close their sockets and executors. They don't mock packet serialization.

### One targeted production fix

`CrossroadsLanGuest.connect` previously called `onConnection(false, specific_error)` in its catch block and then unconditionally called `onConnection(false, "Disconnected. Rejoin the host to continue.")` in finally. On Android, the second callback overwrote the useful code/room rejection in the lobby. `failureReported` now suppresses that redundant generic callback while preserving the ordinary one when no specific failure was already emitted.

This is UI-feedback correctness; the authorization, reserved seat, game engine, encryption status, matchmaking scope and network protocol are unchanged.

## Exact-head acceptance

- [ ] New four JVM tests pass along with `LanMultiplayerTest`, all game modes, save/replay/transfer, tactical Lab v1–v5, existing Android tests and unit/lint.
- [ ] One successful **exact-head** rules workflow and Android QA workflow; verified signed QA archive and source-data checks.
- [ ] No changed production files outside `core/src/main/kotlin/.../LanTransport.kt`; no new Android permissions or network backend.
- [ ] Repeated socket tests do not hang or leave live sockets/threads. A one-off green run is not a physical LAN test.

## Remaining real two-phone acceptance (unchanged)

- Both phones on the same Wi-Fi, host advertisement and NSD discovery; manual IPv4/port fallback.
- Incorrect code should remain visible, but correct code should join; an already-full room must stay inaccessible.
- Host and guest both view identical dice, branch choices, turns and results; duplicate guest taps cannot issue two accepted rolls.
- Guest disconnect, same-process token rejoin, room rematch and guest seat reservation. No persistence across host process death or across guest app restart is claimed.
- Router restrictions, VPNs, access-point isolation and Android Wi-Fi socket binding need actual hardware verification.
- **Security warning:** cleartext QA TCP transmits seed/action history. No production or public/internet multiplayer claims, account system or untrusted-network recommendation.

## Next work

Only after exact-head CI and two-phone tests should gameplay transport be considered accepted. If the owner still has no test window, the next offline task can be a deeper integration check of guest life-cycle callbacks, state ordering and progress-export/install-over consistency, not a claim that same-Wi-Fi play is finished.
