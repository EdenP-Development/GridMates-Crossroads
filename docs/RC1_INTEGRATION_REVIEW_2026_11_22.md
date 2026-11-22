# RC1 — Consolidated integration review, not a public release
**Date:** November 22, 2026  
**Status:** DRAFT review candidate; no merged code, production package or public distribution

## Why this exists

Crossroads has accumulated many valid but stacked feature milestones. The aim of **RC1** is to make the *complete active source* inspectable as **one draft pull request against `main`**, without requiring reviewers to mentally merge 70+ independent PRs, accidentally adopting abandoned HUD variants, or presenting an Android QA binary as a store release.

**Player-facing benefit:** a stable, approachable racing game with one reliable Start, one Finish, and honest rules/status labels. **Maintenance benefit:** one coherent integration gate, smaller verified fixes, and clear evidence boundaries.

## Source and lineage audit

- Upstream source: `feature/qa-79-real-apk-upgrade-save-gate` from [draft PR #79](https://github.com/PlayerEden/GridMates-Crossroads/pull/79), on top of SAVE-78 / LAN-77 and the RC0 staging source.
- At the start of this review GitHub compared PR #79 to `main`: **ahead 584 commits, behind 0; 237 changed paths**. This is a broad cumulative diff, not 584 new RC1 changes.
- Active staging lineage proceeds **RC0 #27 → successive features through #79**. Historical PRs #1–26 are already present in RC0's baseline through branch ancestry, even though not directly in the #27–#79 PR stack.
- **Exclude divergent alternative HUD PR #55** (`feature/ui-55-compact-overlay-hud`); current game follows the chosen `feature/ui-55-compact-hud` branch in PR #56 and its descendants.
- An earlier [RC0 review](RC0_CONSOLIDATED_REVIEW.md) still exists as history. RC1 should be evaluated against the latest complete source, not replace RC0's old evidence or pretend independent approval of an experimental rule.
- This PR is intentionally **DRAFT against `main`** for inspectable combined diff and CI. Never merge it, auto-merge it, publish an APK as production or close the historical drafts without the owner's explicit approval.

## Proven source/build baseline — not owner-device proof

| Capability | Evidence | Classification |
| --- | --- | --- |
| Three published-in-QA courses, Solo AI and Pass & Play | GameSession/course tests; QA1613 Android build | JVM + Android CI |
| Original first-to-finish match | Core tests, `GameSession` and normal `CrossroadsExperimentalMode.boardId` | JVM + Android CI |
| Tactical v4 and optional Fair Finish v5 | Lab variants and v4/v5 tests; no automatic promotion | Research-only opt-in |
| LAN private room/rejoin/stale command handling | LAN-77 in-process TCP loopback and Android QA1607 | JVM+Android CI; NOT two phone |
| Local checkpoint exact replay + manual export/import | SAVE-78 core and app tests; 144 seeded full races, 1,798 restores, 5,024 actions | JVM/Android CI |
| Real signed QA APK install-over | QA1607 → QA1613 emulator [run 45031430316](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45031430316), preserved actual one-roll Woven checkpoint and 7/3 stats | **Android 15 emulator tested** |
| Real owner-device recovery and LAN | No matching acceptance receipt | **OPEN / not accepted** |
| Private production key, permission/privacy/name clearance | No production acceptance receipt | **OPEN / not public-release-ready** |

The **last proven signed QA binary is QA1613**, from [Android 45027345072](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45027345072) and [rules 45027345005](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45027345005). RC1's new UI-summary change will require its own head CI; QA1613 does not contain it.

## RC1 integration finding and surgical fix

**Finding:** The Start button stays pinned on short screens while its expandable options collapse. With Tactical + Fair Finish turned on, its caption previously said only **"Tactical rules ON"**, concealing the fact that v5 changes the winner determination to equal-turn final chance. The optional choice is described deeper in settings but invisible next to Start.

**Fix:** The caption now says **"Tactical + Fair Finish ON"** when and only when *both* Tactical and Fair Finish are selected. Tactical-only v4 retains its existing **"Tactical rules ON"** label. With Tactical off—even if an earlier v5 preference remains stored—the normal original-rules caption stays unchanged. This **does not** change the game rules or toggle values. New Android-side JVM unit tests cover 2-player Solo and 4-player Pass & Play, plus the remembered-but-inactive preference case.

**Documentation repairs:** README, START_HERE, CURRENT_STATUS, APK_UPDATE_POLICY and QA-79 acceptance evidence now direct readers to QA1613 and passed emulator install-over, while explicitly labeling older reports as historical. No long-form historical project receipts were erased.

## Integrated correctness and release gates

| Gate | Expected | RC1 disposition |
| --- | --- | --- |
| Complete source history is present on one review branch | Direct PR diff to `main`, all active lineage, alternate #55 excluded | **SOURCE AUDITED** |
| New summary doesn't misstate original, v4 or v5 | App-side JVM tests at RC1 SHA | **PENDING RC1 CI** |
| Existing deterministic rules and save/restore replay | `:core:test` + design fixtures | **PENDING RC1 CI** |
| Android app/compose tests, lint, QA signature/build | Existing Android QA workflow | **PENDING RC1 CI** |
| Previous verified installer and checkpoint safety | Immutable QA1607→QA1613 signed emulator evidence | **PASSED on previous builds; no RC1 install-over claim** |
| Long-duration match feel and accessibility on actual phone | Human play + small screens/TalkBack | **OPEN** |
| Same-Wi-Fi two-phone join/rejoin/full result/rematch | Real trusted home network test | **OPEN** |
| Release security and distribution | New private signer, privacy/disclosure, LAN security decision, store/IP/name checks | **OPEN** |
| Final merge or public release | Explicit owner approval | **NOT AUTHORIZED** |

## Review order and deliberate limits

1. Check the draft `main`-targeted RC1 diff and confirm that the alternate HUD and any unrelated sibling app code are absent.
2. Run existing **design, rules, Android QA** workflows at the exact RC1 head. Investigate failures before claiming candidate quality. Keep the QA signer public-test-only, and `main` unchanged.
3. Verify new pinned summary and overall board legibility on owner phone at normal/large text; decide whether the optional Fair Finish research mode is genuinely fun enough to ship later.
4. On two physical phones, run host/join to the finish, guest drops/rejoin, wrong code, bot seat and rematch. The protocol currently uses **unencrypted TCP**, so restrict to a trusted household Wi-Fi for prototype testing. Decide on secure transport or removing Nearby for public distribution; do not claim a six-digit code encrypts packets.
5. Retest the **RC1-specific** Android install-over against QA1613 only after a signed RC1 QA APK exists and source checks pass. The older QA1607→QA1613 result establishes a precedent, not all future updates.
6. Obtain production-private signing and independently review data disclosures, privacy policy, accessibility, trademarks, app support, and real device performance. Owner decides what belongs in the first public game.

**No new economies, accounts, currency, forced quests or speculative gameplay modes.** A stable simple race is the goal.
