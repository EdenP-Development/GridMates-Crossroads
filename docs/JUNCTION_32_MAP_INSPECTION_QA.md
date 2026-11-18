# JUNCTION-32 — Three-path map inspection acceptance

**Android owner screenshots (November 17, 2026):** QA1270 on Cedarwood Expedition shows the three-option **Cedar Creek Junction** modal and the separate Cedarwood and Woven Trail route-overview dialogs.

## Observed and verified behavior

- The High / Low / Medium risk cards, text, and current remaining-steps count fit the owner's phone without being cut off.
- The previous modal button said **View board**, but the code only toggled `routeDialogDismissed = true`. It did **not** open the board overview. Players needed to find another control to inspect the route before deciding.
- While a route-choice dialog is active, a player can deliberately dismiss it by Android Back or tapping outside, leaving the pending move to the fixed action controls. This behavior is retained.

## JUNCTION-32 correction

- Rename to **Inspect full map** and directly open the *existing* route overview dialog by setting `inspectBoard = true`.
- The original route modal condition includes `!inspectBoard`, so it is not displayed over the map.
- The original route-choice dismissal state remains false; after closing the overview, the **same** three-route chooser reopens automatically.
- The map's confirmation button says **Back to route choices** when returning to an active three-way choice. Otherwise it still says **Back to game**.
- The map remains inspection-only; no route is selected, no dice are rolled, and remaining steps/revision/saved-game schema/probabilities/LAN messages are changed.

## One-phone acceptance

1. Launch Cedarwood Solo, get to Cedar Creek Junction (or Deepwood Crossing); open the 3-route picker.
2. Note each risk label and remaining steps. Tap **Inspect full map**: the actual Cedarwood route overview should open immediately.
3. Tap **Back to route choices** or Android Back: all three choices return with the **same** remaining steps. Nothing moves automatically.
4. Repeat twice, then choose a valid route. The move should happen only on the explicit risk-card tap, exactly once.
5. Dismiss the picker without choosing by using Android Back or tapping outside. The fixed **Choose a path · 3 routes** control should let you reopen it.
6. Compare with Classic 2-choice junctions and the Woven Trail full-map dialog: those paths should behave as before.
7. Force-stop while a human Long fork is pending; reopen and confirm same pending decision. Confirm offline wins/completed counts remain unchanged until a legitimate finish.
8. Separate two-phone LAN testing remains pending. This change does not touch network protocol, host authority, saves, or board data.

## Release gate

The draft must pass the exact-head Android QA workflow and rules/design tests before an APK is described as installable. No merge to `main` or public release without owner approval.
