# LAN-22 — Real two-phone Host / Join vertical slice

**November 17, 2026. Draft source milestone, not a LAN acceptance claim.**

Source branch: `feature/nearby-lan-v0.1` stacked on TURN-21 QA1167 source. Do not merge to `main` without owner approval. Historical screenshots show offline Solo/Pass & Play; no screenshot yet shows two phones completing a Crossroads LAN game.

## Why we shifted
The owner explicitly requested a return to **major planned features** rather than repeated minor polish. The highest unimplemented essential feature is **same-Wi-Fi play**, as already prototyped in sibling GridMates Sudoku. That app's implementation uses NSD/mDNS to list games, a host-owned TCP rules engine, room codes and reconnect tokens. This milestone adapts the *pattern*, not a blind copy of Sudoku's puzzle operations.

## LAN-22 player feature, initial test scope
- Two Android phones on the **same Wi-Fi**. The host taps **Host nearby** and starts one Woodland Crossing race with two human players. The second phone taps **Join nearby**, discovers the host, or joins with `IPv4:port` as fallback.
- A six-digit room code is optional; no mandatory account, public matchmaking or internet backend. Nearby listing stays advertised during the active room.
- Host is **Player 1**, guest is **Player 2**. Their existing board/dice/route UI remains familiar.
- **Host-authoritative rules**: the host creates one seeded `GameSession` and checks each remote ROLL / ROUTE / TARGET command against seat ownership and the legal phase/available paths. The guest never rolls independently and never mutates the host's rules engine.
- Authenticated admitted guest gets versioned, length-bounded TCP checkpoint snapshots, rebuilt with the existing `MatchCheckpoint` replay/verification code. Only one admitted guest at a time; an admitted guest gets an in-memory rejoin token and reuses that reserved seat after a drop.
- Both players see turns and results. The host starts a rematch for both phones. Host/Join networking and guest rendering do **not** count toward the existing offline Solo wins and completed-match records.
- Host shows Wi-Fi IPv4 and TCP port, which guests can enter if Android NSD fails or Wi-Fi multicast is filtered.
- On link loss, local inputs pause and a reconnect/wait message appears. Host room continues advertising for the returning guest.

## Architecture and source
- Pure core: `LanProtocol.kt`, `LanTransport.kt` with versioned `LanPacket`/framed JSON, CRC-independent exact snapshot replay and seat validation.
- Android: `CrossroadsNearby.kt` (Android NSD/mDNS and Wi-Fi socket binding), `CrossroadsLanLobby.kt` (minimal Host/Join/room-code/manual fallback), and integration in `MainActivity.kt`.
- Uses the already approved Woodland board and `GameSession`; no gameplay rule, board graph, odds, event catalogue, art, package identity or signing change.
- Permissions only: INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE and CHANGE_WIFI_MULTICAST_STATE; no new servers/account permissions.
- Existing single-device Solo and Pass & Play keep their local recovery and records; the **LAN room is a live session**. **LAN active-game recovery across host process death is NOT yet implemented.** Close/reopen creates a new room; do not imply persistent hosted multiplayer. Guest rejoin token is in memory and is lost after guest process termination.

## Security and deliberate limitations
- Room codes are optional. This is a QA household-LAN prototype, **not an encrypted/zero-trust internet service**: TCP is not TLS, and the full checkpoint contains the game seed/action history. Participants could theoretically inspect future randomness. Do not use on untrusted public networks or add competitive leaderboards based on this protocol.
- Only two phones / two human seats in this initial vertical slice. Support for 3–4 separate phones, bot-fill in a LAN room, persistent rejoin, protocol secrecy and host-loss migration requires further implementation.
- Android room discovery and Wi-Fi routing can vary by access point, VPN, wireless guest isolation or manufacturer restrictions; manual IPv4 is a fallback, not proof that all LANs support peer-to-peer packets.
- There is no public server, account, cloud sync, multiplayer chat or monetization attached.
- A guest mirror is rebuilt from a verified checkpoint after host updates; it may show simpler/jumpier visual transitions than the host during this first functional integration.
- Large rooms/historical action replay are not optimized. Keep the initial race short and prove correctness before performance tuning.

## Automated acceptance gates
- [ ] Pure JVM `LanMultiplayerTest`: legal/illegal seat commands, bounded wire data, exact 2-peer TCP transfer of host checkpoint and guest ROLL.
- [ ] Existing 2–4-seat Solo/Pass & Play/saved-session core tests remain green.
- [ ] Android compilation, app unit tests, lint and *exact signed QA APK* pass.
- [ ] Design-data QA passes at exact head.
- [ ] Installer retains existing QA app ID, test signature and saved local Solo/Pass & Play stats. No `main` merge.

## Required owner **two-phone** acceptance
1. Install **same LAN QA APK version on both phones**, updating each Crossroads QA installation in place; connect both to the same Wi-Fi (avoid isolated guest Wi-Fi). Check old Solo statistics on each phone.
2. On Phone A: **Host nearby → Create nearby room** with code OFF. It should show a local Wi-Fi IPv4/port and “Waiting for Player 2”.
3. On Phone B: **Join nearby**. The advertised Crossroads room should appear; tap it. If no room appears, use the address displayed by Phone A. Both should open the same Woodland board.
4. Phone A (Player 1): roll. Phone B should see the **same** number, route and piece movement state. Phone B must not be able to roll on A's turn.
5. Phone B (Player 2): roll, choose the two-way Meadow fork and three-way Old Forest fork when reached; both devices must agree on remaining steps, branch, position, shields, event notes and next turn.
6. Attempt two rapid presses/taps on the guest and make sure only one legal authoritative action is accepted. Avoid duplicate dice/turns.
7. Finish a race, inspect both result screens, then have host press Rematch; both start a new game with fresh rolls, not old stale states. Guest cannot independently start rematch.
8. While guest is present, switch Wi-Fi off or background/close its network connection, then reconnect from the same still-running guest app using **Rejoin previous room**. Host must not accept unrelated new guests in a reserved seat.
9. Host background/foreground and various portrait sizes; check button availability, room status and readable join screen. When host actually **force-stops**, hosted room ends; this is not a cross-process resume feature.
10. Leave LAN game and start offline Solo/Pass & Play again. Both offline modes must still work, and local Solo wins must not be altered by LAN test matches.

## Evidence boundaries
Pure JVM socket tests prove the simulated host/guest protocol over loopback, *not* Android NSD discovery, actual Wi-Fi routing or two physical devices. APK CI verifies compilation/signing and an installable candidate, not successful real LAN gameplay. Do not claim real two-phone support as accepted until an owner phone pair completes the above.
