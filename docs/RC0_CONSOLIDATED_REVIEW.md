# Crossroads RC0 — consolidated source review, NOT a public release

This staging branch was created from the complete `feature/progress-transfer-v0.1` draft stack, including the prior gameplay/UI/QA, local save recovery, Pass & Play, LAN prototype, Quick/Classic boards, public-facing cleanup and local backup-transfer work.

**Branch:** `release/crossroads-rc0-review`
**Source baseline:** `92f3c162125fb0d97a1d4e69e697ad28af34a920`
**Scope:** A single reviewable GitHub PR targeting `main` — it must remain **DRAFT**, unmerged and unpublished until explicit owner authorization.

## What a reviewer should inspect

| Area | Implemented in source | Evidence boundary |
| --- | --- | --- |
| Local game | 2–4 racers, AI, forks, events, Solo and Pass & Play | Kotlin/JVM seeded tests, Android unit and build QA; phone acceptance remains separate |
| Game variety | Brookside Quick and Woven Trail Classic | Graph/event tests and deterministic save replay; gameplay pacing/legibility still needs owner feedback |
| Offline durability | Single-slot local checkpoint, finished receipt and win record | Automated recovery tests; clearing/uninstalling app data still loses data without export |
| Nearby LAN | Two human Android phones on same Wi-Fi, optional host-owned AI | Automated socket/state tests; **real two-phone Wi-Fi test not yet accepted** |
| Progress transfer | User-initiated JSON backup + confirmation-based replace/import | Automated validation and separate preference-store test; real QA-to-public import untested |
| Public surface | Proper build label/icon, QA-only debug text, About & data | Automated unsigned public-preview checks; production signing/privacy policy not done |
| Code governance | Full current app available in one staging review PR | No code has been merged into `main` by making this branch/PR |

## Hard blockers before release

- [ ] Real two-phone Android Host/Join gameplay from lobby through finish, rematch and reconnect, with both devices using the same APK.
- [ ] Private production signing key and secure release channel; **never** use the publicly known QA test certificate to distribute the public app.
- [ ] Publication-ready privacy policy, actual data/permission disclosures and careful treatment of current *unencrypted* direct LAN TCP; use only trusted home Wi-Fi while this prototype is tested.
- [ ] QA install-over test on owner device and verified manual export/import into a separately signed production package. Different Android IDs are distinct apps; progress cannot automatically follow.
- [ ] Android physical UI/accessibility, large-font, gesture, process-death and install/update acceptance; accurate app icon and version identity.
- [ ] IP/name clearance, actual store/release presentation, supported contact/documentation, and decision on `Long` course versus focusing on two quality courses.
- [ ] Explicit owner approval for final branch consolidation/merge and for publication.

## Review sequence

1. Verify `feature/progress-transfer-v0.1` exact-head rules/Android automated QA.
2. Review this staging PR against `main`; check it contains the **entire** game rather than relying on the earlier stacked PRs remaining open.
3. Fix findings *on a reviewed feature/draft branch*, then deliberately update the RC branch; do not silently merge into `main`.
4. Treat any signed debug APK as **QA only**; the unsigned public preview produced by CI is not installable for distribution.
5. Owner physical QA and publication decision follow separately. Do not claim a public release merely because CI is green.
