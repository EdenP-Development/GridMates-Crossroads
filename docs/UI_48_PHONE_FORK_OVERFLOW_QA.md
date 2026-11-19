# UI-48 — Phone QA: three-way card overflow and turn-transition controls

**November 18, 2026.** QA-only fix stacked on TRAILS-47; keep draft, do not merge into `main`.

## Evidence
Owner's 674 × 1536 real-device screenshots of Woven Trail, GamePlay 2.0/Lab v3:

1. The `Old Forest Junction` three-path chooser lists Cliff Track, Mill Lane, and Cedar Bridge. The Cedar Bridge preview wraps to a third line, but that line visually intersects the rounded bottom boundary of its selectable card.
2. A previous-turn animation displays `ON THE TRAIL · AI 1 is on the move…` while the next player's Steady and Push controls are disabled underneath, giving a premature/mixed action prompt.
3. The newly illustrated woodland roads appear substantially more integrated into the backplate at ordinary zoom. The oversized circular medallions and path repetition at zoom remain *art QA*, not part of this functional repair.

## Patch
- Replace **only the three-choice dialog's** internal `OutlinedButton` layout with an accessible, clickable Material3 `Surface`. It wraps the existing `RouteChoiceSummary` in content-first 14dp horizontal / 15dp vertical padding so a third or fourth description line expands the card without crossing its border. Preserve its outlined rounded look, touch semantics, unchanged actions and full-width hit area.
- Maintain the option-list scroll. Display a `Scroll for more route details ↓` hint only if the scroll state has additional vertical content. Keep `Inspect full map` accessible below the choices.
- During a move animation (`!turnReady && !rollingDie`), show `Finishing the move…` rather than the next player's disabled Steady/Push/route controls. This is presentation-only and does not alter engine state or turn timing.
- No changes to graph nodes, fork data, route descriptions, AI decision-making, saved match codec, RNG, LAN, signed QA identity, or art bytes.

## Android acceptance
- Compile, unit tests, lint, APK signature, provenance checks.
- On a short portrait phone, open Woven Trail > Old Forest Junction > choose a path. Verify the complete Cedar Bridge text remains **inside** its border. Test system larger-font mode, long localized text where possible, and the bottom of the scrollable card list.
- Confirm all three routes remain individually tappable and trigger exactly their corresponding existing engine actions, including after Inspect full map/back and reopening.
- Compare while AI travels: status plaque can say `AI 1 is on the move…`, but disabled human Steady/Push controls should not be shown until the move ends. Check fast and cozy paces, recovery/resume, and LAN if available.
- Install over previous QA, never uninstall to update. Verify stored wins and saved in-progress match.

**Unapproved visual and physical QA until owner reviews.** `main` remains untouched.
