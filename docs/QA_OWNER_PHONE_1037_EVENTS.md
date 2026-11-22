# Owner QA 1037 — real-device event and mischief follow-up
**Observed:** November 16, 2026, around 8:44–8:45 PM (device screenshot timestamps); six owner-supplied screenshots in ChatGPT. These are not uploaded to the public GitHub repository.

## Visible evidence
- The phone shows **1 win • 2 completed matches**, confirming expected stats after the previous four-player AI victory (owner subsequently opened a new match).
- Four seats entered the Woven Trail game. Player, AI1/2/3 markers diverged on different route branches.
- Human turn reads **Your turn**, with normal singular/plural menu text and a visible **Meadow Crossing** route choice panel.
- AI3 encountered **Tangled briars** and was told to move back 2 spaces (amber setback presentation).
- AI1 then encountered **Woodland prank**, whose result says **AI 1 pranked You: back 2!** (purple Mischief presentation). This is source-supported proof that opponent-targeting effects are visible in real play; it is not an instrumented board-position assertion.
- The lower action panel and AI turn status remained available. No complete new match is pictured in this set.

## Experience feedback and next design gates
- Event cards are differentiated, but player impact still happens instantly and the log is often physically below the map. On-screen **temporary encounter reveal** in the pinned action area would make AI and player encounters harder to miss.
- Die rolls lack anticipation; add short visual-only face cycling before the real core-engine roll, without changing RNG or probabilities.
- A three-way forest junction needs clear internal list scrolling and a consistent **Leave match** control outside the choice list.
- Players remain tiny; cautiously emphasize the active token without changing board topology or obscuring forks.
- The match length remains unmeasured; short screenshot intervals are not sufficient to set Quick/Classic/Long targets.

## Implementation candidate
This feedback informs a minimal presentation-only change: brief 360ms die spin, ~2-second nonblocking encounter banner, visible Leave match control and slight active-token emphasis. Preserve single RNG core result, original board/rules, public QA-only signing package, and saved win/completion keys.

## Verify
- [ ] Kotlin/JS test suite, Android compilation and lint.
- [ ] Actual previous-QA→new-QA APK install-over preserving stats.
- [ ] Owner physical QA confirms noticeable but not intrusive animation, no duplicate actions, and accessible three-option route list.
