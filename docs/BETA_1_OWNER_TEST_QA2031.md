# Beta 1 owner test — QA2031

Owner requested a testable build and screenshot feedback checkpoint before further release development. Freeze runtime changes until feedback or a reproducible blocker; documentation-only work may continue. No public release approval.

## Exact candidate
- Source head fa97c9fe6b6cbe51046ec5dc0866752e069f0534, PR #120 synthetic merge checkout 5d5d903117f3daf6c74e819861fd74d6953978ad.
- Rules run 46098835258 and Android run 46098835214 passed. Direct Android job 140836452959 logs confirm package com.playereden.gridmates.crossroads.qa, versionCode 2031, versionName 0.1.0-qa, signer certificate SHA256 75cc3c896247d71fbd51f7a3ed41f90c8218a8a5a70df8c0f6b494b5cc23d419.
- [Download QA2031](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/46098835214/artifacts/15244109906), ZIP 20322166 bytes, expires Dec 12 2026 UTC, ZIP SHA256 71f1216f991c3de335267f439749f444b1e2a26c3d785d4c3cd73fbbc98b75e7. APK GridMates-Crossroads-QA-2031.apk; independently check included APK receipt/hash if needed. Archive not locally inspected.
- Newer ddcdf80 is documentation-only, rules #477 passed, Android #1032 in progress at inspection; do not chase incremental APK numbers. QA2031 is the pinned owner test target.
- QA signer is intentionally public test-only. Never publish QA2031 as itch beta. Private signing branch has older build 2002; reconcile after public surface review, no new key.

## First test: focus on actual player experience
Export private backup before installing over older compatible QA; never uninstall/clear data to bypass mismatch. If already on newer build do not downgrade. Record phone model, Android version, build and existing completed/wins if available. All gates NOT RUN until evidence arrives.
| Screenshot | Action | What to inspect |
|---|---|---|
| S01 | Home on first open | Branding, setup, start button, clutter |
| S02 | Course picker / each course | Art, clear length choices |
| S03 | Start normal Solo Quick, opening board | Board scale, player pieces, action tray |
| S04 | First crossroads choice | Clear routes, risk wording, tap targets |
| S05 | Shelter/hazard/mischief if encountered | Encounter card, consequence feedback |
| S06 | Full map, return to board | Readability, camera/zoom, controls |
| S07 | Finished race and rematch | Result clarity, desire to play again |
| S08 | Classic or Long opening and later board | Course identity and readability |
| S09 | Pass & Play setup and second human turn | Turn ownership, local handoff |
| S10 | How to Play / match options | Readability and discoverability |
| S11 | Close/reopen mid-match | Exact active race and records preserved |
| S12 | Optional QA More options, mixed AI and Fair Finish result | Style descriptions, numbered names, starter/rotation, no clipping |

Screenshots in batches are welcome; do not require every screenshot to give feedback. Note roughly how long a race took, anything confusing, boring, too cramped or visually inconsistent; note whether would voluntarily replay. Do not force rare encounters. Record actual observed results and preserve original screenshots; no mockups as evidence. Optional advanced tests and full P01–P12/A01–A08 remain separately tracked; this first round is not full public acceptance. Two-phone LAN remains unaccepted and plaintext; test only trusted private Wi-Fi if desired, not necessary for initial screenshot batch.

## Recovery instructions
Read this file, CROSSROADS_CURRENT_HANDOFF.md, BETA_1_ITCH_RELEASE_GATE.md, AI2_16F_ANDROID_ACCEPTANCE.md and AI2_17E_AI_ACCEPTANCE.md. Active branch feature/identity-17-art-import, draft PR #120, main unchanged. After owner feedback triage release blockers vs later improvements; public surface audit and signing integration still open. No publication, merge, secret changes or new gameplay in this checkpoint.
