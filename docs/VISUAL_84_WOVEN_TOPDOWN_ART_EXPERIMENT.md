# VISUAL-84 — Woven Trail: overhead board-art comparison

**Date:** November 22, 2026  
**Status:** opt-in Android QA preview, stacked after VISUAL-83, not final approved artwork

## Important source-image finding

I directly inspected **the actual 1536×1536 RGB PNGs packaged in the signed QA1614 APK** and verified their SHA-256 against the GitHub art manifest:

- Woven Trail `board_woven.png` — `2c6675130e80743957d79fd28501c6f5ae245f7a09541395f0da5d8b9ac7973b`
- Brookside `board_brookside.png` — `036f8dacbd7c03999f0467205df31cdec34707fb88c9b676f1024a4f53d0b4c7`
- Cedarwood `board_cedarwood.png` — `36528260c2a330282da93e35dcc0395d9a1a0046e5627c`

**All three have a forward-looking landscape perspective:** visible horizon/sky, distant hills, upright houses, trees and scenic water. The authorized game graph is an **overhead square** of normalized x/y path segments. Dropping top-down playable roads and pieces onto a perspective landscape is a structural reason the result can look like game UI painted over scenery, not a handcrafted board game.

This finding is from source art, **not** a claim that an image is blurry, unlicensed or aesthetically bad. The art itself is attractive. The mismatch is its viewing perspective and its relationship to the gameplay topology.

## The actual first implementation

The code contains a QA-only alternative backdrop called **Woven board art: Overhead** in the in-match `⋯` menu. It is a composition trial, **not** an approved PNG replacement.

- An original vector-drawn overhead printed environment: earth/paper, meadow/forest/village clearings, **enlarged layered tree canopies, detailed rooftops, rocks and flower clusters** and a blue/teal stream that cannot be mistaken for the brown wooden roads. Two distinct village rooftops now identify the destination region; all ornaments remain graph-cleared.
- Static features are calculated **once per board** from a fixed lattice and removed when they would collide with any real node or directed graph segment. Decorative scenery cannot create a fake route, obscure a junction or become a tap target.
- The existing high-resolution carved gameplay roads from VISUAL-83, action/state symbols, numbered animal traveler tokens, 1.2× live art frame, camera, pan and hit testing all work over the new composition without changing graph coordinates.
- Unlike the existing landscape, this new art has **no horizon or upright building elevation**. It is drawn from above, matching the paths and pieces.
- The Full Map uses the same preview art when enabled, so the actual board and inspection map agree.

**Crucial constraint:** This preview is active **only for Woven Trail**, only in a DEBUG/QA build, only when the **Illustrated** board look is selected and the owner explicitly enables the switch. Brookside and Cedarwood keep their distinct original illustrations; otherwise the experiment would falsely make three handcrafted courses look like one geography. The existing Woven landscape remains the default, and can be instantly restored from the same menu. No new art binary or asset-provenance status is changed.

## Acceptance and next artistic phase

1. Compare Woven in default landscape versus overhead QA mode at identical player/round/zoom settings; examine Start, first fork, a safe/amber/purple route, a shelter event, Finish, and Full Map. Save/recovery must be unaffected.
2. Decide whether overhead composition **actually solves the relationship between art and navigation**. This code-drawn preview deliberately prioritizes perspective and readability over the final polish of a bespoke illustrated PNG.
3. If accepted, produce and review a **new 1536×1536 overhead illustrated Woven backplate** in the existing flat-cut-paper Woodland palette, using the actual course graph as a planning overlay but with **no paths or symbols baked into the finished image**. Test the replacement against an exact-head QA APK and record provenance/hashes before marking it approved.
4. Then design distinct overhead boards for Brookside (brighter creek/willows) and Cedarwood (dense forest/expedition camp), preserving both their own event/region identity and their own verified route geometry. Do not simply recolor or reuse the same composition.

## CI/runtime budget correction

The previous Android runs at `42894ff` ended prematurely in the build step without a usable APK (cancelled job/failure-status run; no compiler error log was retrievable). They were **not passes**. The branch now avoids redundant Android APK jobs on both `push` and `pull_request` for each feature-PR commit: the Android workflow keeps the PR job as its exact-head QA gate, `workflow_dispatch` for manual tests, and standalone design-branch push testing. Design-data QA remains independent. Do not infer an Android defect or success solely from those aborted CI runs.

The newest art changes enlarge the actual decorative features themselves, rather than only camera zoom. The fixed authored Woven graph now yields **20** graph-cleared decorative features (**10 trees, four rocks, four flower clusters and two village rooftops**), with tests enforcing this manuscript count and the no-road-overlap constraint. This is a visual comparison prototype, not a final commissioned backplate or game asset license approval.

## Automated quality gates

New app/JVM tests assert deterministic, bounded feature placement; exclusion from node/graph edge space, stable per-graph content, segment endpoint distance and invalid coordinate handling. Another test ensures the preview never leaks to Classic Map, non-QA public variants, off preference, Brookside or Cedarwood.

Existing live art atlas/roads, rules, game seeds, action replay checkpoints, save migrations, LAN behavior, board data, version codes, signing keys and production packages are untouched. No public release or merge is authorized. Green CI **does not** equal physical phone art approval; the owner must see it and decide whether we should commission/generate properly illustrated overhead PNG assets.
