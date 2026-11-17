# Owner QA — QA1040 full four-player human win

**Observed:** November 16, 2026, approximately 8:59–9:00 PM in owner-supplied Android screenshots. Exact installed APK version is not visible directly; UI matches QA1040. Screenshots remain in the conversation, not published to GitHub.

## Directly supported
- Main menu shows **2 wins and 3 completed matches** before a new match starts. Previous results survived at least the observed playtest/update sequence.
- New Woven Trail game shows the player plus AI1/AI2/AI3, and Meadow Crossing's two route-choice buttons at 1 step remaining.
- AI3 receives a **Friendly scarecrow** shield. The shield icon remains visible in its player strip as the game advances.
- Human later has a shield icon too. In the expanded Journey Journal, the human's **Borrowed umbrella** grants protection against a prank; earlier AI3 Friendly scarecrow appears as another entry.
- Human's **Open market road** encounter moves forward 3 spaces. Later result screen says **You reached the finish and won** with Rematch/Back to menu.
- Tokens change places visibly on the fixed board, while event and shield state change. This is meaningful evidence of the full 4-seat game path and encounter mechanics.

## Not yet verified
- Menu count *after* this latest victory: expected **3 wins / 4 completed matches**, but no post-victory menu screenshot provided.
- A shield successfully consuming on a prank attempt, the dice-face animation's perceived quality, or a three-route Old Forest Junction choice on QA1040. Screenshots cannot prove these.
- Measured match duration: screenshots include only approximate clock times, not an isolated start-to-finish timer.
- Real visual motion: position updates are evident but their animation cannot be determined from still frames.

## Next focused implementation
- **Board Motion v0.4 Draft PR #9**: animate numbered pieces along actual recorded route nodes, including retreat effects; keep dice/event probabilities untouched.
- Slightly enlarge tokens and offer a read-only **Inspect map** view for small phone screens.
- Pure Kotlin movement-frame tests and signed Android install-over QA precede a new owner APK.
- Continue evaluating visual balance: token collision at Start/intersections, reachable crossroads choices, and readability with four seats.
