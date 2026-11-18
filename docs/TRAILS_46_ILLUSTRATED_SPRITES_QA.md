# TRAILS-46 — Illustrated path and junction assets (Android QA only)

**November 18, 2026. Stacked draft on TURNFIX-44A (#45), TRAILS-44 (#44), BOARDS-43 (#43), THEME-42 (#42). Do not merge into main or call release-approved.**

## Why
Real-phone screenshots of Woven Trail show the first printed path style remains too uniform and digital over the generated scenic background. This experiment installs **real shared transparent artwork**, not baked-in routes per board.

## Assets
- `app/src/main/assets/art/v1/trail_atlas_v1.png` — 128×128 original RGBA atlas, upper sprite [0,0,128,32] is a hand-painted-looking woodland trail; [32,42,64,64] is an illustrated junction frame.
- Source: original ChatGPT image generation ID `b21a09d7-9c4d-46ec-a027-e006fcf790b4` on November 18, cropped, edited into a compact six-color alpha atlas. This is a low-resolution *QA experiment*, not a locked final material.
- License category `generated`, hash `ae5e4e8e5724b49f55f2892f96f5dca92b4b31b7bc8c84a821aef07fe2a11293` pinned in the existing 13-slot art manifest, with explicit owner-approval hold.
- **No new route images or gameplay content**; the same atlas is reused across every route and node.

## Runtime
- Board graph produces both endpoints. Renderer draws an edge-aligned, rotated atlas section; original position, movement, hit targets, tokens, event colors, result logic, RNG, save state, and LAN protocol are unchanged.
- Colored edge rim underneath the illustrated road distinguishes safe (green), risky (amber), and mischief (purple), but does not change route eligibility or rules.
- Illustrated junction frame is drawn *beneath* the authoritative node circle and labels.
- A small DEBUG-only QA menu cycles **Trail style: Artwork → Printed → Simple**, saved in visual-only `qa_trail_style_v2`. Full map and live board share the choice. Classic map intentionally uses Simple lines. Missing or corrupt alpha atlas falls back to Printed.
- The existing `qa_printed_trails_v1` preference is not touched. Release does not enable the experimental artwork mode.

## Validation / gate
- Require Android build, unit tests, lint, APK signature and art manifest checksum validation.
- Do not infer image is approved by successful CI; test owner-device readability at original fit and max zoom and check all three boards.
- Confirm drawn roads do not show stretched rocks / seams or obscure forks. A reused straight strip may still look artificial: reject it rather than prematurely committing to it. If necessary make 2–3 organic alpha variants and maintain route color markers.
- Verify **safe / risky / mischief** remain distinguishable, especially Cedarwood's dense fork network.
- Inspect zoom, panning, Full map, AI turns, comeback, re-entry after pause, duplicate lines at junctions, and saved-game continuity (install *over* old QA; do not uninstall).
- Compare original Printed and Simple in the `⋯` menu on the same artwork, without changing course.
- The UI copy fix in PR #45 should prevent a bot-owned encounter from displaying “YOUR CHOICE”.
- No modifications to seasons or base game rules. Autumn/Winter/Coastal are later art sets.

**Owner acceptance pending. Do not merge this stacked PR into main.**
