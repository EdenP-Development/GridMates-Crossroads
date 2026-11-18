# VISUAL-29 — Unified Crossroads player experience

**Source milestone (November 17, 2026).** Stacked on BALANCE-28. Reference: owner's 13 on-phone Android screenshots showing home setup, nearby host/wait/join, in-race camera/menus, route overview and finished results.

## Problems actually visible in owner screenshots

1. At the top of Home, the full decorative map and long introductory copy appear before play setup; the user must scroll before finding **Start journey**.
2. In-race camera regularly crops away almost all surrounding route context. The previous automatic zoom was **1.7×**, or **1.4×** at a fork.
3. Four player tokens cluster around Start in one partly overlapping row; distinct individuals are hard to distinguish.
4. On-screen title and results still say **Woodland Crossing** even when the new game is **Brookside Dash** or **Woven Trail**.
5. LAN menus and selected chips show a material/light default different from the green-and-parchment language of the home screen; the join flow has a large plain empty region without a helpful explanation.
6. The full map is hidden in Match Options; players might not discover it until reading the instruction guide.

## Implemented (presentation only)

- **Home:** Compact setup first; the decorative woodland hero moves **after** Start journey, Host/Join and secondary actions; art height is 104 dp, compared with 175 dp previously. Reduced spacing and vertical padding. Existing Solo/Pass & Play/participant/course selections preserved.
- **Material palette:** Consistent forest/cream/earth primary, secondary, surface and outline colors so menus and default components do not fall back to lavender/white. Dropdown uses explicit cream paper.
- **Course identity:** New shared presentation names, **Brookside Dash · Quick** and **Woven Trail · Classic**, used on in-game heading, accessible board label, full-map dialog and results subtitle. No saved board IDs or asset names changed.
- **Camera:** Start and Finish use 1.0× overview; route choices use 1.15×; other follow movement uses 1.28× instead of 1.7×. Manual pinch/pan and double-tap follow remain, as do screen boundaries. A visible **Full map** button sits beside Match Options.
- **Tokens:** Gold-rimmed raised numbered pawns, with two-row shared-space arrangement (instead of a long overlap row). Render and hit-testing use the **same** offsets. Numeric labels and established seat colors remain for colorblind identification.
- **Nearby:** Parchment background, forest-selected chips, informative empty-room message, truthful wording for 2-person versus AI-filled rooms, copyable manual host address. No connection protocol or discovery code was touched.
- **Tests:** Course title mapping, first/route/finish camera levels, separation and repeatability for 1–4 same-space pawns.

## Deliberately not changed

Dice probabilities, route graph, AI, first-player advantage (BALANCE-28 remains a design-review issue), Board/Event fingerprints, matching and LAN wire protocol, offline save data schema, checkpoint repair, backup file format, account/network needs, release identity, signing, stats or existing result calculations.

## Automated acceptance

- [ ] Android APK compile + tests + lint, release-surface identity preflight and signed QA artifact pass on exact source commit.
- [ ] JVM rules/design tests and BALANCE-28 simulations remain passing; gameplay metrics should be unchanged.
- [ ] No automatic changes to completed-count/win-stat or current saved race on install-over.
- [ ] No production signing, store release, or merge into main.

## Owner device acceptance (one phone suffices, LAN two-device later)

1. Install the latest signed QA APK **over** the existing QA app without deleting its local statistics or saved match.
2. On Home, verify **Start journey** is visible at normal font without scrolling on your device; at larger font, setup remains scrollable and buttons don't overlap.
3. Start each course; check Woven Trail and Brookside Dash are correctly identified in the live title, full-map dialog and results.
4. Start a four-racer race. The four numbered pieces at shared Start should be distinct, not layered on top of each other.
5. Move around a fork. Confirm automatic follow shows useful nearby decisions and no unexpected blank regions; test tap-to-follow, double-tap and pinch/pan.
6. Open **Full map**, return to gameplay, inspect Match Options. Confirm cream menu instead of lavender and no new obstruction.
7. Enter Host setup, select 3 racers, create room; verify green selected chip, readable waiting card and **Copy host address** actually copies the exact address/port.
8. Enter Join; verify helpful nearby-empty state and manual fallback remain. Do not label LAN connected without two phones.
9. Finish a race and inspect result title, winner, event text and stable elapsed time.
10. Continue checking QA backup exports/imports and saved statistics on a physical phone; CI alone cannot prove system document-picker operation.

**Decision checkpoint:** Keep/adjust automatic zoom and menu layout based on the actual Android viewport screenshots, not mockups. Do not make global gameplay alterations as part of this visual milestone.
