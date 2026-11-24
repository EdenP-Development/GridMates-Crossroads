# Current checkpoint — QA1706 (2026-11-24)

Active branch integration/beta-1-polish now includes verified IDENTITY-13 icon implementation (head 638f6b65fc26a9b5897548ea04d04fbe3e710b17). [Android QA passed](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45467623391), [QA1706 ZIP](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45467623391/artifacts/14957455551), expires Dec 8. Existing QA package/cert and versionName verified in logs. Exact APK SHA not independently retrieved; not owner accepted or install-over verified.

Read [master beta roadmap](MASTER_BETA_ROADMAP.md) for the agreed planning direction, [shared standards adoption](EDEN_STANDARDS_ADOPTION.md) and central registry before distribution. 36 prepared images remain external; no actual new artwork rendering claim. Next bundled milestone: navigation and panels, exact artwork import, illustrated device review. Keep native fallback; do not change rules/signers/saves or publish. Main untouched. Older checkpoint sections below are historical.

---

# Active checkpoint — 2026-11-24 (supersedes earlier status below)

Active branch: integration/beta-1-polish. Consolidated feature/identity-10-import-ui through b69f0d169e83c7cfb6d430d92236b17531f13f35, 12 commits, zero divergence. Latest verified Android build QA1700, [run 45460844972](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45460844972), [artifact](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45460844972/artifacts/14952348643), [receipt](receipts/QA1700.json). Source head 22123bfba6e2b7c7cda11b31a30f9d4af1fac4a3; built merge 05d4fece4976758634af508e45cb994032191786. Latest standards revision Android run 45463437412 was in progress at checkpoint; recheck live CI. No owner acceptance or latest install-over claim.

Read [Eden standards adoption](EDEN_STANDARDS_ADOPTION.md) and [central registry](https://github.com/PlayerEden/Eden-Development-Standards/blob/main/APP_REGISTRY.json) before distribution. Public QA signer remains unchanged, production signer unconfigured, private backup custody unverified. Preserve all existing identities and saves; do not generate replacement signing keys. No main merge or public release.

Artwork: 12 environment and 24 UI candidates remain external; import instructions in [IDENTITY-10](IDENTITY_10_ART_IMPORT.md), exact archive hashes there. Do not claim binaries committed. Optional primary art component and state unit test passed QA1700, but actual illustrated runtime rendering not tested. Native fallback retained. Next: improve shared secondary/utility controls and state loading, then integrate actual reviewed artwork with stretch/contrast/device QA. Artwork transfer remains priority, no needless new concept sheets. Gameplay roadmap: authored Classic board depth, progression/collections, physical testing. Use the central release checklist, exact APK receipt and actual prior-APK install-over before accepted distribution.

---

> **Current standards (November 24, 2026):** [Eden Development Standards](https://github.com/PlayerEden/Eden-Development-Standards) adopted. Read [Crossroads signing, recovery and release audit](EDEN_STANDARDS_ADOPTION.md) first for QA1700 identity and pending custody/upgrade gates. Existing packages/signers unchanged; no production signing or public release authorized. Historical status below is retained.

# Crossroads current handoff — November 24, 2026

**Active development branch:** integration/beta-1-polish. Verified source 83a50e7544accbf04b2de00d1e823047e6610bad; QA1698 built merge d42777eea909240aba1abd79d81572929c838f7f, [Android QA passed](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45454544489), [signed APK ZIP](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/45454544489/artifacts/14949148782) (expires December 8, 2026). Fast-forwarded nine commits from 4d0305b72adbea3df245206a1fad7d8d57c896b2, zero divergence. Main and original integration/beta-1 unchanged. Draft PRs 110–112 preserve history.

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
