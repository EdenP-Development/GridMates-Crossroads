# BETA-ITCH-01 — First itch.io beta release gate

Owner direction Nov 27: prioritize Beta 1 on itch.io over further speculative AI development. No publication or merge authorization inferred. AI QA feature expansion frozen; retain existing default Classic behavior.

## Verified current candidate
Head fa97c9fe6b6cbe51046ec5dc0866752e069f0534; rules 46098835258 (#476) and Android 46098835214 (#1031) passed, including build/unit/lint, unsigned public variant and QA signing verification. Prior #1030 cancelled, superseded by successful #1031; AI2-17D compilation repair verified. Actual PR build checkout named 5d5d903117f3daf6c74e819861fd74d6953978ad. [QA2031 ZIP](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/46098835214/artifacts/15244109906), 20322166 bytes, expires Dec 12 UTC, ZIP SHA256 71f1216f991c3de335267f439749f444b1e2a26c3d785d4c3cd73fbbc98b75e7. QA signer is public test-only; never upload QA APK as public beta. Archive not independently downloaded. Physical acceptance P01–P12 and A01–A08 still open.

## Recommended minimum public scope (owner approval required)
Three authored courses, Classic Solo vs AI, 2–4 human Pass & Play, local resume/records and manual backup, coherent woodland presentation, accessible guide and rematch. Keep experimental rules, AI personalities/difficulty, starting-order controls and prototype art QA-only. Recommend excluding Nearby from first public beta until real two-device verification and secure transport decision; must actually gate entry points, guide, permissions and manifest, not just hide marketing text. Public release variant currently needs source audit for those surfaces. No extra boards, currency, accounts, achievements or tutorial campaign required.

## Signing and integration blocker
Separate feature/edenp-beta-signing branch already has private signer and verified signed build 2002 per docs/CROSSROADS_BETA_SIGNING.md (run 45488059431). Package com.playereden.gridmates.crossroads, pinned certificate e13d5b336c01a7ca4f29e83072ac304adc3a4233cf484b43268a88aec3f28914. Owner custody/independent backup and physical install remain unverified. That older source is NOT current QA2031. Current active branch lacks signing workflow/doc. Reconcile reviewed signing branch with current source on dedicated trusted staging, preserve key/cert, ensure versionCode strictly increases within public channel. Never sign untrusted PR code; no key generation, rotation or secret disclosure. QA and public package differ; test explicit portable transfer and public-to-public upgrade. Inspect ancestry before any integration.

## Release sequence
1. Audit public surface: rules/experimental toggles, Nearby exclusion vs explicit secure approval, network permissions, art/defaults, app identity. Implement narrowly scoped gates/tests once disposition agreed.
2. Physical QA2031: existing compatible QA install-over, backup/restore, complete Quick/Classic/Long Solo and Pass & Play, actual pacing, map/camera, events, results/rematch, large fonts/TalkBack, device performance. Fix only blockers.
3. Integrate trusted private signing path; confirm independent owner backup custody and build exact reviewed candidate, verify APK hash/cert/package/version. Test clean public install and public update with saved match, plus QA transfer where compatible.
4. Public privacy/data disclosure and permissions audit, asset provenance/name review; prepare actual screenshots, itch description, Android requirements (minSdk 26), installation instructions, limitations, feedback channel. No price commitment; recommend free initial beta for feedback.
5. Owner approval of final candidate and scope, archive immutable APK/receipt, then explicit publication decision. Google Play later. No release now.

Do not conflate earlier RC1/Beta integration records with current candidate. Keep draft PR #120 and main unchanged. Next BETA-ITCH-02 public surface audit and signing lineage reconciliation plan.
