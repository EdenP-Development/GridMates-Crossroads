# CLASSIC-17 — Simple Pass & Play, no LAN required

**Date:** 2026-11-17. **Status:** source and tests committed, exact Android CI / owner-phone acceptance pending.
**Base:** SAVE-16 signed QA1121, Draft PR #20. **Branch:** `feature/classic-pass-play-v1.5`.
**Product direction:** [GridMates Classic Games Charter](GRIDMATES_CLASSIC_GAMES_CHARTER.md).

## The feature
- Home offers only **Solo vs AI** (existing default) and **Pass & Play** (new); one Start journey button. Choose **2–4 total players** in either mode.
- Solo = one human ("You") and 1–3 bots, unchanged dice/paths/balance. Pass & Play = all human seats labeled **Player 1…Player 4**. Pass the same phone to the next person when their turn is shown.
- Any human seat can roll, choose a 2-way or 3-way trail, target an opponent for mischief and finish. The action panel no longer mistakes Player 2+ for an automatic AI turn.
- Results show **Player N** as winner in Pass & Play, and keep "You" for solo.
- Rematch keeps the seat count and play style. Auto-save and action replay preserve every human participant and decision; updates do not convert them into bots.
- Completed matches include both modes, but the **Solo wins** statistic only counts the original single-human Solo mode; Pass & Play does not inflate it.
- The optional How to Play guide accurately names both offline modes. Same-Wi-Fi play is a **separate later milestone**.

## Not included
No sockets, Wi-Fi discovery, online identities, per-person profiles, hidden dice tricks, leaderboards, XP, currency, inventory, backend, added art or new rule complexity.

## Required automated tests
- [ ] Existing 100-seed all-bot and previous save/replay regressions pass.
- [ ] New 2–4 human **full races** finish; every seat gets human turns, all human names unique and no bots.
- [ ] New checkpoints restore all-human configuration, correct current seat, and identical future dice/events.
- [ ] Solo naming and bot status remain unchanged.
- [ ] Exact-head Android build/unit/lint and signed QA artifact succeed; design validation also passes.

## Android owner phone acceptance
- [ ] Install over QA1121 without uninstalling; existing local Solo wins, completed matches and gesture-hint dismissal remain.
- [ ] Solo (default) starts one human and 1–3 AI depending on selected seats; tap to roll, AI plays automatically.
- [ ] Pass & Play 2 participants: Player 1 rolls; Player 2 gets a clearly labeled manual roll instead of the `Playing automatically` spinner; repeat throughout game.
- [ ] Pass & Play 3/4: reach 2-way and Old Forest 3-way choices with **Player 2+**, verify they can select routes; prank targets selectable.
- [ ] Before game finish, force-stop/reopen; the correct human seat, turn and decision restore, with no unexpected AI action.
- [ ] On finish, winner label is Player N; no incorrect "You won" for Player 1. Rematch starts all human with same seat count.
- [ ] Completed match increments once, Solo wins do not increase from Pass & Play results.
- [ ] Large fonts, small Android portrait, TalkBack and Back confirmation remain usable.

## Release boundary
Compilation and JVM success do not prove phone ergonomics or install-over. Keep DRAFT; no merge to `main`, no public release, no claim of working LAN.
