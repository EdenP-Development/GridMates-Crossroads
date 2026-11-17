# GUIDE-14 — Playable rules reference / first-use clarity (November 17, 2026)

**Status:** source implemented, latest Android CI and owner-phone acceptance pending.
**Scope:** one optional, original Compose "How to Play" dialog reused on the home screen and in Match Options. Implemented within Draft PR #18's existing branch to avoid a needless new checkpoint branch. No main merge or public release.

## User problem
QA1082 owner screenshots show an inviting home and clear route choices, but there was no actual in-app guide explaining why paths/risk spaces matter or what to do at a fork. The only first-use help explained pinch/drag/double-tap camera gestures.

## Behavior
- Home screen: secondary **How to play** text button directly beneath the primary **Start journey** action. Default two-person start stays one tap; no mandatory onboarding.
- In match: **How to play** in `⋯` Match Options, next to Board Overview and Journey Journal, so users can review rules mid-game.
- Reused cream/woodland dialog with a dismiss button that remains in view, and scrollable explanations sized to short Android screens.
- Steps are grounded in `GameSession.kt`: shared Start/Finish and first arrival wins; one die roll per human turn, automatic traversal; required human choice at a fork; remaining roll continues on chosen path; landing encounters; shields block pranks; simple low/medium/high route-risk hints not promised odds.
- Board navigation: pinch zoom, drag, double-tap follow, plus menu links to overview and journal. Cozy/Brisk change only visual timing. Offline AI play only; **no claim of working LAN**.
- The dialog is optional and never appears by itself. At a three-way fork, the automatic route dialog does not attempt to render on top of the guide.
- No game data, dice weights, bot steps, events, user stats, SharedPreferences keys, Android signer, package, or permissions were changed.

## Acceptance checks
- [ ] Exact head Android Gradle build, JVM tests, lint and signed artifact upload all PASS.
- [ ] Home "How to play" opens and closes; player count remains unchanged; Start Journey is not obstructed on small phones.
- [ ] Match options "How to play" is usable in an actual 2–4 seat game, with no two dialogs fighting for focus.
- [ ] Large font: scrolling exposes the final text and Got it button without cutting off either.
- [ ] Back gesture dismisses guide and returns to original screen; no forced re-onboarding each match.
- [ ] Information matches current game: route risks are informative, not guaranteed results; map gestures operate when guide is closed.
- [ ] Guided match remains functional after closing: human roll, 2-way and 3-way fork choice, bot turns, encounter results, result actions.
- [ ] QA1093 install-over retains previous wins, completed games and camera gesture-hint dismissal on real device.

**Verification levels:** source committed; CI needs exact run confirmation; no GUIDE-14 emulator or owner phone acceptance yet. QA1093 and earlier owner screenshots are previous version evidence only.
