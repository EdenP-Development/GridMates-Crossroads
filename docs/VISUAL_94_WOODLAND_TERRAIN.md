# VISUAL-94 — Continuous woodland terrain

Replaces three flat overlapping backdrop ovals and 54 dots in the opt-in Woven overhead scene with an irregular meadow/forest treatment, continuous muted regional color, six soft shade/daylight pools, and 240 fixed paper/vegetation micro-marks. All are clipped to the board and painted underneath the original creek, bridges, scenery, routes, nodes and pieces. No changes to Storybook, atlas bytes, cameras, saved games or game rules.

## Evidence motivating this work
VISUAL-92 run 45315961076 captured six emulator screenshots (artifact 14884454361). APK signatures/atlas checks and install-over checkpoint and appearance preservation reached their assertions successfully. Whole-map procedural and atlas images differed, but the opening live images were byte-identical. The job failed its final difference assertion; this is not a full QA pass. The harness was revised separately on #92 to report opening sameness rather than hide it. The fix does not substitute for visual review. The opening camera needs meaningful terrain even where no ornament appears.

## Acceptance
Check exact candidate Android compilation, unit tests, lint, and signed package. Compare actual normal zoom and Full Map in meadow, forest and village against QA1678. Assess readability, terrain richness, frame performance, and whether contrast remains subordinate to graph and pawns. The texture count and distribution tests do not prove artistic quality or frame rate. No main merge. Physical-phone install-over, real LAN and art approval remain open.
