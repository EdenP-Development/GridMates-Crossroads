# ART-PREVIEW-40 — Real Batch A illustrated Android QA candidate

**November 18, 2026 · stacked DRAFT on HOME-39, ARTREADY-38 and GAMEPLAY-37.**

This branch packages **four actual PNG images**, NOT placeholders: `home_hero.png`, `board_woven.png`, `event_shelter.png`, and `event_hazard.png`. These images were newly generated in ChatGPT for the Crossroads woodland concept and exported to the exact fixed dimensions required by ARTREADY-38. The other eight asset slots remain planned with procedural fallback. No third-party GridMates collection art was copied.

## Scope and release boundary

- `status: approved` is used for **technical packaging into an isolated QA build** because the current registry requires it for any shipped image. Every activated record also carries `reviewStatus: qa-preview-only-not-owner-approved`. This is **not the owner's final visual approval**, nor authorization to merge/release.
- Rights category `generated`, generation IDs, exact SHA-256 fingerprints and source notes are stored in the manifest. Legal/product review and owner visual acceptance are separate release gates.
- Woven is a scene backdrop only: no playable routes/nodes, text, seat positions, fork markers, or risks baked into the PNG. Actual runtime graph and controls remain on top.
- Four images total 5572480 bytes, under 32 MiB combined / 8 MiB each. The other eight use the original fallback artwork.
- No mechanic, physics, win balance, checkpoint/save format, LAN protocol, statistics, or signing identity changed.

## Acceptance required before promoting art

1. Install the exact signed QA APK over the previous QA build and verify offline save/stat preservation. **Automated build success is not proof of install-over safety.**
2. Initial home screen: panorama visible and no Start/Host/Join obstruction at small Android portrait and large font scale; Start stays available during scroll.
3. Woven 1× / 3×: all route rings, paths and numbers legible over illustrated creek/rocks/village; no fake playable paths in background; camera smooth without jank.
4. Play Lab v3 to both Shelter and Hazard; compare the 62 dp thumbnail recognizability, all native event-choice buttons, accessibility and screen-fit.
5. Run actual Quick, Classic, Long human matches and record wall-clock length, agency, idle time and whether visual flow improves play.
6. Test original saved Quick/Classic/Long, v1/v2 pending states and real two-phone LAN before merge. Nearby LAN continues to require a separate real-device acceptance test.
7. Owner accepts or rejects the art *as a coordinated set*; rejected art should be replaced while retaining the working fallback and exact slot contract.

### Four-pack file integrity

| Image | SHA-256 | Bytes |
| --- | --- | ---: |
| `home_hero.png` | `b338bf65526e9b3a6fccaff571edd0f9e66a5e9f21c20af91f19b8dd1c5b42e8` | 1574408 |
| `board_woven.png` | `2c6675130e80743957d79fd28501c6f5ae245f7a09541395f0da5d8b9ac7973b` | 2064571 |
| `event_shelter.png` | `e41a4e993585524b306892fd4dc76084fc2a0023dcde320b55869efb932d566a` | 964308 |
| `event_hazard.png` | `c345af18a45069e4136e85dc293e7256c5144c441dbf53a5ad7b0eb50c47e770` | 969193 |

**Not for main merge or release without explicit owner approval.**
