# Development Roadmap
Milestone-based, not date-promised. No frequent tiny APK drops in place of useful progress.

| Stage | Goal | Deliverable | Proof before advancing |
|---|---|---|---|
| R0 Foundation | Resolve initial design baseline | Bible, research, sample data and validator | Documents consistent; fixture QA passes |
| R1 Gameplay experiment | Prove choices are fun | Pure rules simulator, dice + fork + event + 2–4 bots | Complete race simulations; fairness/route metrics |
| R2 Android solo | Play game on an actual phone | Native board, turn UI, AI, results, independent saves | Device playtest and lifecycle regression |
| R3 Same-Wi-Fi | Nearby 2–4 seats | Host/join, authoritative state, reconnect design | Physical two-to-four-phone tests |
| R4 Visual polish | Look/feel like GridMates | Clean theme, shared animal token art, profile unlocks | Small screen/accessibility + asset provenance check |
| R5 Content/modes | Vary match duration and mischief | Quick/Classic/Long, additional handcrafted routes/areas | Real match lengths and balance evaluation |
| R6 Release | Public V1 readiness | Signed builds, IP and policy review, published release notes | **Owner approval** |

## Suggested backlog
1. ENGINE-01: deterministic rules state and invalid-action rejection.
2. BOARD-01: rendered small fixed test course; legible branch selection.
3. AI-01: simple, fair heuristic rival; no dice advantage.
4. EVENTS-01: area-specific event pools and event-chain prevention.
5. TEST-01: automated 1000-match simulation; no unreachable finish.
6. ANDROID-01: choose stable Android/Compose patterns, unique app ID and storage.
7. ANDROID-02: solo APK with one full match and results.
8. LAN-01: host-authoritative 2–4 player session and real devices.
9. ASSETS-01: inventory and review approved source art; flat token variants.
10. UX-01: portrait/landscape accessibility and match length defaults.
11. CONTENT-01: tune three length modes; only after core game is fun.
12. RELEASE-01: release audit and owner approval.
13. ANDROID-APK-01: stable QA signing, monotonic APK codes and install-over-save tests **before** first shared owner APK ([issue #5](https://github.com/PlayerEden/GridMates-Crossroads/issues/5)).

**No Android implementation, release build or LAN check is claimed here.**
