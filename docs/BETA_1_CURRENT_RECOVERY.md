# Latest checkpoint — BETA-ITCH-08C (2026-11-28)

Owner confirmed reuse of previously established organization signing secrets, no new credentials or environment setup requested. Historical signed build 2002 verified successful again. Separate one-time execution branch release/beta-1-signing-execution, commit 936ab202e74a792549148c1b27f94a3e7aeaee40, attempts signing exact frozen source f99409374beb652f3661e872813c8bd392e57427 with existing secrets and isolated signing job. Push-triggered result not returned by connector; public endpoint retrieval and browser connection unsuccessful. No actual success/failure inferred. Do not retrigger blindly. Registration PR #122 remains draft and not required for this attempted path. Independent backup and current secret scope remain unverified. Owner reports Codex review allowance unavailable; exact notice not inspected, no causal link to Actions established. Continue ordinary development and testing without Codex reviews. Next retrieve actual signing run ID and receipt when accessible; meanwhile use secret-free QA for supplementary acceptance evidence only, not public signed-build acceptance. No public release.

---

# Crossroads current recovery — BETA-ITCH-08A (2026-11-28)

START HERE when resuming Crossroads. Release source frozen at f99409374beb652f3661e872813c8bd392e57427 (release/beta-1-candidate, draft PR #121). Rehearsal #7 run 46124332268 and Android #1101 run 46124332236 passed. Marketing branch prior head 29f75401313e2efd25aa1d72d6623c6a6b77a26c passed media workflow #4 run 46126947355. Main remains 884170dd64d9ff21645dd662665eb3fb136c6fba. Development PR #120 remains separate. No main merge or public release.

## Product scope
Public Beta 1: standard Quick/Brookside, Classic/Woven, Long/Cedarwood; original rules, Solo AI, same-device 2–4 Pass & Play, map/journal, local progress/manual backup. Nearby and experimental rules/courses remain QA only. Release source-set Nearby stub fixes release lint; debug original preserved; reconcile before future development integration. Public package com.playereden.gridmates.crossroads, Android API26+, versionName 0.1.0. QA package separate; do not publish QA.

## Signing (actual blocker)
Historical signed build 2002 source 86120c37c1f175349b4ead4529e944e111b4347e run 45488059431 is not current. Existing permanent signer SHA256 e13d5b336c01a7ca4f29e83072ac304adc3a4233cf484b43268a88aec3f28914 must be preserved. No current private-signed candidate. Registration-only draft PR #122, one fail-closed manual workflow file, not merged. Sign job references crossroads-beta-signing; environment protections, secret scope, trusted action review and independent key custody unverified. Connector cannot configure environment or dispatch. See release branch docs/BETA_1_SIGNING_EXECUTION.md. Do not add automatic secret-bearing triggers, expose or rotate key, or merge runtime merely to dispatch. Obtain explicit owner approval and verify protections before signing.

## Marketing and website
Owner selected V2 woodland pieces cover; V1 retained. Eight PNGs plus receipt archived in owner Google Drive; metadata size 4832027 verified, remote byte hash pending. Recovery location in docs/BETA_1_MEDIA_KIT.md; no private archive or credentials in central registry. Original V2 run 46126155806 artifact 15255796880 expires Dec 12; SHA256 4b9fc5130a95d5affce5d1c77a10b813c90fb5ef0ece53c993412d4a6d19ff54. Renderer and CI preserved on marketing branch PR #123. Storefront theme and artwork selection approved direction, not in-game acceptance. See docs/BETA_1_PUBLIC_ACCEPTANCE.md and BETA_1_PLAYER_HELP.md. Website privacy draft staged in Eden-Development-Standards/website/drafts/crossroads-beta1-privacy.md, proposed path /privacy/crossroads/, not hosted. Central PORTFOLIO_REGISTRY.md and APP_REGISTRY.json synchronized; no live storefront edits.

## Next actual actions
1. Owner confirms independent signing key/password recovery without sending secrets. Verify actual environment/credential access restrictions, reviewed source and action trust. Approve registration-only PR separately; manual dispatch authorized exact release SHA.
2. Verify real signed candidate receipt, package, certificate, version, APK hash and offline permissions; archive privately.
3. Physical public tests P01–P12, screenshot S01–S05, update gates U01–U04 (two signed versions needed). All actual public physical tests NOT RUN. Do not infer from QA or disposable rehearsal.
4. Confirm publisher, contact, effective date, privacy host/URL and final storefront copy/price. Explicit launch approval required.
Do not rebuild or modify frozen release simply to advance milestone numbering. Parallel marketing docs do not alter release source. Prior release handoff is historical; this file and PR #121 supersede its current-status section.
