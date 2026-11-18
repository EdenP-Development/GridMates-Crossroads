# THEME-42 — Board appearance foundation and Batch B production gate

**2026-11-18 • Draft stacked after GAMEFEEL-41.** This feature is not approval to merge `main`.

## Why

The owner explicitly expected **different board themes** as well as Brookside / Woven / Cedarwood boards. These are two separate design axes:

- **Course** chooses the actual board graph, journey length, turn structure and event data. Existing choices: Brookside Dash (Quick), Woven Trail (Classic), Cedarwood Expedition (Long).
- **Board appearance** chooses visuals only, independently of rules, state, LAN, and save data. Current built-in looks: **Illustrated** (default, uses approved local PNG if present) and **Classic map** (original Compose-drawn scenery).

No season/biome menu, cosmetics currency, unlocks, daily grind, store dependency, or fake promises of installed images. This is a small technical foundation for future art packs. The existing 12-slot `art/v1` registry remains authoritative.

## Actual code changes

- Introduce `CrossroadsBoardLook` enum with safe stored-value fallback. A distinct preference `board_look_v1` is persisted in the existing QA profile file without touching wins, checkpoints or saved games.
- Add one compact **Board appearance** row to Home; its simple dialog offers the two current styles. It does not add another onboarding flow or prevent one-tap play.
- Respect the chosen appearance in both the live board and the **Full map** overview. The procedural fallback is intentionally forced for Classic map even when a course PNG is present.
- Keep all routes, road coordinates, tokens, colors, gameplay markers, gesture detection and accessibility semantics drawn by Android on top, regardless of appearance. Gameplay rules and RNG unchanged.
- Automated tests cover preference round-trips and safe fallback from unrecognized / future serialized values.

## Batch B scope and status

The first illustration batch is committed in ART-PREVIEW-40 (home, Woven, shelter, hazard) and was seen on an actual phone. The two empty 1536×1536 `art/v1` course slots are `BOARD_BROOKSIDE` and `BOARD_CEDARWOOD`.

- **Brookside:** a bright standalone creek-and-blossom square has been generated as an *external review candidate* with a local export and provenance, but is **NOT yet installed into GitHub / Android**. Inspect after actual route overlays.
- **Cedarwood:** strong visual direction (dense forest and expedition camp), but the generated candidates came out as presentation posters. **Reject** them as board PNGs; do not crop a labeled poster or smuggle printed routes into the image.
- Do not set manifest status to approved until each standalone image has been imported with exact dimensions, SHA, origin/license and actual end-to-end Android QA.
- Art presence on one course must never be inferred as art presence on the others. QA screens should still safely use their procedural fallback.

## Phone acceptance

1. Launch: Illustrated should be selected by default for existing profiles; it should continue to show Woven artwork as before.
2. Open **Board appearance**; pick Classic map. Start Woven. Routes and turn controls should be unchanged, with original drawn scenery.
3. Return to Home; pick Illustrated, then start Woven. The PNG backplate should return; the Full map must use the same appearance as the live board.
4. Force stop and relaunch. The selected visual style persists; an interrupted game restores its real course and decisions unchanged.
5. Verify all Quick/Classic/Long in illustrated and classic looks, with Lab v3 ON and OFF. On missing PNGs, Illustrated should use safe procedural fallback.
6. On small Android font scale and large accessibility font, the appearance picker, Start Journey, course selector and scroll area remain reachable.
7. Confirm match results, local record, checkpoint fingerprint, offline play, and host/join protocol are unchanged.

## Future board themes (design target, not shipped)

An additional independent visual pack dimension may later support **Autumn Harvest**, **Winter Hollow**, and **Coastal Adventure**. New season/biome art cannot be represented by merely selecting Brookside or Cedarwood; additional audited artwork variants and a package-aware catalogue will be needed, so players never see a theme option with missing art.

Do not promise all environments for an appearance until files exist and QA approves their visibility and route contrast.
