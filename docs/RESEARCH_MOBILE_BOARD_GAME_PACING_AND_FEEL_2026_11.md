# GridMates: Crossroads — mobile board-game pacing and game-feel research
**Research date:** November 16, 2026 (US Eastern) • **Status:** investigative recommendations, NOT locked game rules.
**Audience:** game-design / Android implementation / owner QA. Public product descriptions are evidence for their stated features; animation timings and design transfers below are **our hypotheses**, not claims of measured mechanics in those commercial games.

## The problem observed on the owner phone
- QA1044 screenshots: menu **3 wins / 4 completed matches** before another game; three-player play opens two Meadow paths and a three-way Old Forest Junction; later AI1 wins; a subsequent menu shows **3 wins / 5 completed matches**. The counters remain coherent.
- The three-way Old Forest Junction option **Cedar Bridge is partially clipped** by the bottom action area in the 9:18 PM screenshot. Some of its description is hidden; the separate bottom Leave control is visible. A text instruction to scroll does not fully solve discoverability/visual priority.
- Inspect map works in the supplied screenshot and displays the complete path, but is essentially a larger static diagram/modal, **not** real pan/zoom. Piece numbers are still tiny during normal gameplay and grouped tokens remain difficult to see.
- The previous 1044 animations cannot be judged quantitatively from still screenshots. The owner explicitly reports that a match **feels too fast**.
- There is useful proof of risk, shields, and attack events; however most effects are immediately reflected as text and a changed token position. A little more anticipation/impact and map atmosphere would help.
- **Measured graph audit of `data/boards/woodland_woven_trail.json`:** 38 unique nodes; 5 forks; 14 event nodes; 24 valid unique Start-to-Finish branch paths; path lengths **16–22 space moves**. With one normal six-sided die and occasional movement events, this is structurally a short race. This fact does not establish a precise match duration or frequency of events in practice.

## Comparator research — transferable design ideas

| Game / source | Grounded public mechanic | Lesson for Crossroads | Do NOT copy |
|---|---|---|---|
| [The Game of Life 2](https://www.marmaladegamestudio.com/games/the-game-of-life-2) and [Google Play](https://play.google.com/store/apps/details?id=com.marmalade.gol2) | Spinner, distinct life-event cards, branching decisions and themed worlds; solo/AI, local and online play | Make **crossroads and events** presentation-first moments, and let fixed boards gain flavor through themed collections | Entire life/wealth/career economy, heavy 3D assets, lengthy reading |
| [MONOPOLY GO! (Google Play)](https://play.google.com/store/apps/details?id=com.scopely.monopolygo) | Roll/landmark loop, cards, shields, and memorable shutdown/heist interruptions; short mobile interactions | Give dice, landing and a successful prank a recognisable **visual/audio payoff** | Energy/dice scarcity, forced social economy, timers, sticker monetization loops |
| [Board Kings (Google Play)](https://play.google.com/store/apps/details?id=com.jellybtn.boardkings) | Interactive 3D boards, special spaces, buildings, opponent visits, power cards and new themed boards | Make areas on the path visually legible and introduce board-specific encounter flavor | Persistent destruction/building costs and heavy 3D production |
| [Dice Dreams official help](https://superplay.helpshift.com/hc/en/5-dice-dreams/faq/53-how-do-i-play-the-game/) | Symbol dice generate shield, attacks, raids, fights and extra-roll events | Different **result classes** feel dramatically different; especially foreground shield-block and target selection | Replacing movement die with slots, currencies, infinite reward chaining |
| [Ludo King (Google Play)](https://play.google.com/store/apps/details?id=com.ludo.king) | Classic local/offline vs AI, pass-and-play, themed boards and Quick mode; broad player-count support | Keep a recognizable board-game ruleset and later offer clearly labeled **Quick / Classic** lengths and offline play | Endless settings, confusing menus or rapid forced monetization |
| [Super Mario Party Jamboree (Nintendo)](https://www.nintendo.com/us/store/products/super-mario-party-jamboree-120369/) | Discrete turn phases, consequential special spaces, between-turn minigames, rule/customization variants | Create **distinct beats** between dice, move, landing, crossroads, interaction, result | Large minigame suite, long forced turn animations or complex item economy |

### Game-feel guidance
- [Designing Game Feel: A Survey](https://doi.org/10.1109/TG.2021.3072241): game feel combines **physicality/tuning**, **feedback/amplification** and **support/streamlining**. Good juice communicates what happened and helps users act.
- [Squeezing more juice out of your game design](https://www.gamedeveloper.com/design/squeezing-more-juice-out-of-your-game-design-): game juice should **reinforce core mechanics**. Crossroads needs cozy, readable tabletop motion, not shooter-style screen shake.
- [Android Compose animation guide](https://developer.android.com/develop/ui/compose/animation/quick-guide): prefer draw/graphics-layer animation rather than expensive relayout when feasible; spring/tween/keyframes can express movement/feedback.
- [Android haptic feedback guide](https://developer.android.com/develop/ui/views/haptics/haptic-feedback): gentle, purposeful, system-respecting haptics are possible without heavy custom vibration sequences.
- Qualitative community caution: [Monopoly GO requests for skippable animations](https://www.reddit.com/r/Monopoly_GO/comments/1tbfzc8/speed_up_skip_animations_please/) and [Mario Party pacing complaints](https://www.reddit.com/r/MARIOPARTY/comments/1g8y4re) show that repetitive, unskippable effects can **make games feel slow in the wrong way**. These are discussion examples, not representative surveys.

## Key finding: two different pacing problems
**Perceived speed**: roll resolves, token travels, landing has no meaningful pause or sound, event text replaces the prior text, AI acts again. It is too instantaneous to observe stakes.
**Structural speed**: current routes are only 16–22 spaces Start-to-Finish. Because of the 1–6 die and movement bonuses, even perfect animation will not turn this into a long strategy session. **Do not confuse longer animations with a longer game.**

## Proposed short-term prototype — keep core engine fixed (candidate UX experiment)
1. **Dice moment**: Roll remains a single input and a single authoritative random outcome. The die gets 350–500ms total anticipation, optional small tap/haptic and clear final face. No outcome rigging or paid reroll.
2. **Travel moment**: Instead of current approximate **115ms/node** on QA1044, trial a comfortable 180–240ms/node, subtle hop/ease, very quiet 'tap' on key board spaces. Avoid sound every step in long sessions. Camera/view must keep current player legible, and UI must remain responsive.
3. **Arrival moment**: 150–300ms subtle glow on destination; event tile slightly rises/brightens. Mark where you landed and why the event happened.
4. **Event moment**: show a small thematic encounter card with title/illustrative icon, clear effect and actor/target, 0.8–1.5 seconds (duration is a **proposed test range**). Tap to dismiss/continue. Important shield/prank/big shortcut can linger slightly longer than routine +1.
5. **Crossroads moment**: freeze on the junction, highlight available routes on the board, present *all* choices in a full-height dedicated choice sheet or responsive stacked panel. Include risk + distance/shortcut label. A choice is a decision, not a partly hidden list. Never auto-pick for the player. The three-choice forest crossing is the first acceptance case.
6. **Mischief / shield**: visually link attacker to target, show target moving backwards or shield catching the prank. Include feedback for *both* sides. Do not hide game state while animating.
7. **AI turns**: show meaningful rolls/movement/events with a quicker default than the human; summarize routine AI turns. Keep an optional **Speed: Relaxed / Normal / Brisk** or simple skip button. Never block player choice with a forced lengthy clip.
8. **Final stretch**: simple 'Final Bend!' cue when a real player reaches the finish region, with visible opponents' positions. Do not secretly rubber-band dice or change odds.

**Example turn, proposed not measured:** Tap Roll → brief die anticipation → 4 visible hops → arrive at Meadow Crossing → highlight two roads → choose Creekside Walk → remaining hops → optional 'Friendly scarecrow' event card → shield icon pops onto player. All mechanics already exist; mainly change presentation and ordering.

## Medium-term mechanics — only after gameplay feel is good
- **One consequential optional event choice** (e.g., “Take the shortcut and risk a setback, or take shelter and keep a shield”) drawn from bounded region-specific pools. Limit interactions so turns do not become reading tests.
- **Telegraphed short vs safe paths**: same static road geography, event pools random within region/risk. Long safe route must have meaningful advantages; short risky route should not be mechanically dominant. Test route win rates before tuning.
- **One-time travel aid** slot as a future candidate (shelter token / reroute token), separate from account currencies, and not pay-to-win.
- **Personalities for the event deck** using simple localized phrases, a tiny icon, and consistent color coding; keep their effects compact and easy for older players to read.
- **Multiple match lengths** built from intentional board sections/course variants; existing Woven Trail can be Quick. Classic and Long require more *decisions and turns*, not only a speed multiplier or artificially longer animations. Preserve **first to FINISH** as the main win condition.

## First QA experiments to run
**A — Familiar fast:** existing QA1044 presentation (115ms/space) and no extra event pause.
**B — Cozy standard:** 210ms/space, ~400ms die, landing pulse and ~1s event card, AI animations shortened.
**C — Cozy plus:** same as B + route preview, distinct shield/prank treatment, small sound and optional haptics.

For each variant, log locally or note manually: total match time; rounds; human decisions; encounter frequency; first encounter time; AI waiting time; percentage of events user can identify; whether third forest choice is discoverable; player verdict on too fast / right / sluggish; animation skips. Record **2-, 3-, and 4-seat** matches. No cloud analytics/accounts needed.

### Acceptance rules
- First three-way forest option chooser presents **Cliff Track / Mill Lane / Cedar Bridge** with all text and actionable targets accessible on the owner's phone. Hidden third option is a **blocker**.
- The real final state is decided only by Kotlin rules, not interpolated token location. No duplicate dice, encounters or winners from animations/cancelled transitions.
- Any animation can be skipped or brisked after repeat viewing; user can turn off sound/haptics and use reduced motion. No artificial blocking of player input or timers.
- Stats update once per finished match, retain prior record on APK install-over, and state actions are preserved on app background/resume as far as the current design supports (active-match persistence is still a separate feature).
- Longer session modes should be measured against user preference, not presented as timed promises before playtests.

## Explicitly out of scope in this research milestone
New animations not implemented by this document. No world art generation, online multiplayer, sound pack, resource economy, purchase timers, daily rewards, ads, or new APK. The game rules are unchanged; treat suggestions as experiments requiring review.
