# Active checkpoint — IDENTITY-23B (2026-11-24)

Supersedes historical artwork-transfer status below. Active stacked draft [PR120](https://github.com/EdenP-Development/GridMates-Crossroads/pull/120), branch feature/identity-17-art-import, base feature/identity-16-panels. Latest functional commit 0de491e3315ec0c9832c6c840be86e8971a27ff5. Both original ZIPs committed under design/art/imports; original hashes verified in [QA1712 run](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45519675722), [artifact](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45519675722/artifacts/14978134266). V2 atlas checksum also verified inside signed QA APK. Shared procedure recorded in [Eden artwork pipeline](https://github.com/EdenP-Development/Eden-Development-Standards/blob/main/ARTWORK_PIPELINE.md).

Subsequent commits promote 16 UI assets at build time, preload button states, stabilize icon bounds, add provisional three-slice horizontal scaling, native label contrast and tests, and shared panel framing across home/results/LAN lobby. These newer changes are not yet confirmed by completed CI or physical screenshot review. QA1731 was in progress at checkpoint before LAN panel commit. Check newest PR run; avoid repeated code changes while consolidated CI runs. Next: verify exact packaged assets, inspect actual screenshots for narrow/wide buttons and native label backing, then settings/panels and launcher icon as justified. No new generated panel artwork yet.

QA1712 uses .qa package and public QA-only signer; NOT a private beta upgrade. Owner's installed private beta 2002 uses separate package and signer (see historical private beta checkpoint); retain its exact identity and increasing version code. Do not replace or publish it until trusted signing branch and install-over gates pass. Main untouched. No new gameplay or save-format changes.

---

# Current visual checkpoint — QA1709

Board navigation and shared panel PRs 118–119 passed Android QA; six commits fast-forwarded to integration/beta-1-polish, zero divergence. QA1709 [run](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45494047473), [artifact](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45494047473/artifacts/14967623369). Original launcher icon retained per owner. Private beta 2002 remains owner playtest baseline.

Exact artwork archives recovered and SHA256 verified in local session: environment ZIP c14294767e47a1489baa4f4415bd18232310ed1c9295a9fbdcc13d7112e3c1cb; UI ZIP 80e8030c5520dde93eeb4a45eb56137b407a57a2c83d65fc0a944df0ff437085. Local container cannot resolve GitHub and GitHub connector only accepts literal binary base64, not file path. Transfer not yet completed. Use trusted checkout or owner upload of exact archives on dedicated art branch, then existing importers and runtime promotion; do not commit signing credentials. Artwork not yet rendered in APK. No main merge or public release.

---

# Active checkpoint — private beta transition (2026-11-24)

Owner reports successful installation/import of private beta candidate 2002 (0.1.0), package com.playereden.gridmates.crossroads, signer e13d5b336c01a7ca4f29e83072ac304adc3a4233cf484b43268a88aec3f28914. [Verified signed candidate](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45488059431/artifacts/14964897190); source 86120c37c1f175349b4ead4529e944e111b4347e. Owner report is physical import evidence, not a byte-level comparison or subsequent install-over test. Private beta is now preferred owner playtest channel; preserve exact package/signer and increasing version codes. QA remains independent for engineering. No public release or deletion of old installation inferred. Private backup custody unverified. [Signing PR117](https://github.com/EdenP-Development/GridMates-Crossroads/pull/117) remains separate; consolidate only verified changes and retain trusted signing branch restrictions. Next: board navigation, panels and exact artwork transfer, then course depth. Main untouched.

> **Shared standards also transferred:** https://github.com/EdenP-Development/Eden-Development-Standards (original repository ID verified). Use canonical organization URLs. Registry updated; fresh QA and private beta signing pending.

> **Current repository:** [EdenP-Development/GridMates-Crossroads](https://github.com/EdenP-Development/GridMates-Crossroads). Ownership transfer verified; existing history and QA identity retained. See [migration audit](EDENP_MIGRATION.md). Active branch `integration/beta-1-polish`; QA1707 last verified pre-transfer build. Fresh organization build and private beta signing pending.

> **Current checkpoint: QA1707; [EdenP migration inventory](EDENP_MIGRATION.md).** PR116 verified and consolidated. Await owner transfer; no key or package change. Prior sections historical. See master roadmap for continuation.

# Current checkpoint — QA1706 (2026-11-24)

Active branch integration/beta-1-polish now includes verified IDENTITY-13 icon implementation (head 638f6b65fc26a9b5897548ea04d04fbe3e710b17). [Android QA passed](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45467623391), [QA1706 ZIP](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45467623391/artifacts/14957455551), expires Dec 8. Existing QA package/cert and versionName verified in logs. Exact APK SHA not independently retrieved; not owner accepted or install-over verified.

Read [master beta roadmap](MASTER_BETA_ROADMAP.md) for the agreed planning direction, [shared standards adoption](EDEN_STANDARDS_ADOPTION.md) and central registry before distribution. 36 prepared images remain external; no actual new artwork rendering claim. Next bundled milestone: navigation and panels, exact artwork import, illustrated device review. Keep native fallback; do not change rules/signers/saves or publish. Main untouched. Older checkpoint sections below are historical.

---

# Active checkpoint — 2026-11-24 (supersedes earlier status below)

Active branch: integration/beta-1-polish. Consolidated feature/identity-10-import-ui through b69f0d169e83c7cfb6d430d92236b17531f13f35, 12 commits, zero divergence. Latest verified Android build QA1700, [run 45460844972](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45460844972), [artifact](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45460844972/artifacts/14952348643), [receipt](receipts/QA1700.json). Source head 22123bfba6e2b7c7cda11b31a30f9d4af1fac4a3; built merge 05d4fece4976758634af508e45cb994032191786. Latest standards revision Android run 45463437412 was in progress at checkpoint; recheck live CI. No owner acceptance or latest install-over claim.

Read [Eden standards adoption](EDEN_STANDARDS_ADOPTION.md) and [central registry](https://github.com/EdenP-Development/Eden-Development-Standards/blob/main/APP_REGISTRY.json) before distribution. Public QA signer remains unchanged, production signer unconfigured, private backup custody unverified. Preserve all existing identities and saves; do not generate replacement signing keys. No main merge or public release.

Artwork: 12 environment and 24 UI candidates remain external; import instructions in [IDENTITY-10](IDENTITY_10_ART_IMPORT.md), exact archive hashes there. Do not claim binaries committed. Optional primary art component and state unit test passed QA1700, but actual illustrated runtime rendering not tested. Native fallback retained. Next: improve shared secondary/utility controls and state loading, then integrate actual reviewed artwork with stretch/contrast/device QA. Artwork transfer remains priority, no needless new concept sheets. Gameplay roadmap: authored Classic board depth, progression/collections, physical testing. Use the central release checklist, exact APK receipt and actual prior-APK install-over before accepted distribution.

---

> **Current standards (November 24, 2026):** [Eden Development Standards](https://github.com/EdenP-Development/Eden-Development-Standards) adopted. Read [Crossroads signing, recovery and release audit](EDEN_STANDARDS_ADOPTION.md) first for QA1700 identity and pending custody/upgrade gates. Existing packages/signers unchanged; no production signing or public release authorized. Historical status below is retained.

# Crossroads current handoff — November 24, 2026

**Active development branch:** integration/beta-1-polish. Verified source 83a50e7544accbf04b2de00d1e823047e6610bad; QA1698 built merge d42777eea909240aba1abd79d81572929c838f7f, [Android QA passed](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45454544489), [signed APK ZIP](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45454544489/artifacts/14949148782) (expires December 8, 2026). Fast-forwarded nine commits from 4d0305b72adbea3df245206a1fad7d8d57c896b2, zero divergence. Main and original integration/beta-1 unchanged. Draft PRs 110–112 preserve history.

## Read first
- [Beta integration](BETA_1_INTEGRATION.md): verified gameplay and visual polish, release gates.
- [Original art bible](ARTREADY_38_ASSET_BIBLE_AND_IMPORT_QA.md): canonical woodland style.
- [Identity art library](IDENTITY_01_ART_LIBRARY.md) and [UI library](IDENTITY_07_UI_LIBRARY.md): current artwork contracts.

## Exact artwork state
- Environment V2: 12 generated and normalized 256×256 sprites packed into 1024×1024 RGBA atlas, four reserved cells empty. External ZIP crossroads_environment_v2.zip, atlas environment_atlas_v2.png. Reported SHA256 779c42c21558a24ef9eb56f341d465ddeae68061389149bfe17fa00fe53525ab; 910844 bytes. Contract and generation IDs in design/art/environment_v2_contract.json and environment_v2_provenance.json. Owner liked all three visual batches; actual in-game appearance unverified.
- UI Core V1: 24 externally extracted PNGs (12 button states across primary/secondary/wood; 12 icons). External ZIP crossroads_ui_core_v1.zip with provenance/individual hashes; contract design/art/ui_core_v1_contract.json. Owner liked concept sheets. Verify actual state alignment and stretch before using; panel and other control sheets still reserve source, not cleaned assets.
- The actual new PNG/ZIP binaries are NOT in GitHub. A chat download or filename is not a permanent repository source. Preserve the ZIPs externally and supply them to the next Work/Codex session if necessary. Do not claim transfer or invent replacement hashes. The repository has metadata/contracts, not the new art bytes.
- Loader prefers optional valid-dimension V2, falls back to original V1. Manifest V2 slot is planned. QA1697 predecessor failed one stale 16-slot assertion; repaired 17-slot check passed QA1698. V2 actual decode and render not tested. Storybook remains default, environment atlas appears in optional Overhead QA.
- To import in a checkout of this branch with the actual atlas file: python3 tools/import_environment_v2.py /path/to/environment_atlas_v2.png ; then python3 tools/check_crossroads_art_pack.py ; commit exact PNG and manifest, build signed QA, inspect actual phone. Do not merge main. Use PNG directly rather than assume ZIP layout. Check hash before import.

## Next actions in order
1. Resolve binary transfer and verify V2 on actual Android; retain V1 fallback. Use the prepared archive, do not regenerate.
2. Import UI Core candidates into source-library area (not runtime pack), verify provenance, alpha, state geometry, stretch and text safe area. Package only approved used assets. Implement a small shared UI component set with native labels and accessibility; retain fallback. Extract panels and frames as needed; reserve extra artwork for later. User explicitly prefers ample asset library but uncluttered screens.
3. Expanded Woven prototype stays unshipped. 1080 baseline plus 480 comparison races passed; two-seat original Woven median rounds 5→9 with doubled distance, forks 8→8. Need authored new layout and meaningful choices, not filler spaces.
4. Later progression, achievements, traveler collections; no unnecessary currencies/accounts. 
5. Physical install-over, large text/TalkBack, actual race pacing, two-phone trusted Wi-Fi full race/rejoin/rematch still outstanding. TCP plaintext; public signing/privacy/security and explicit owner approval remain release gates.

No public release. Do not treat generated posters or conceptual UI previews as actual app screenshots. Never assert a binary or test result that was not verified.
