# Active checkpoint — QA1844 physical review / Woodland World 2.0 planning

Owner supplied QA1844 physical screenshots and explicitly confirmed startup is good. Startup presentation accepted; no claim of separate skip/reduced-motion/lifecycle verification. Launcher screenshot shows close visual family alignment with Sudoku; retain current master and 58dp recipe, no separate exact gold-frame pixel acceptance claimed. Regional Journey full map, initial play HUD, manual exploration views and home settings visible. Host/Join labels fit; camera board coverage improved in supplied views. Regional Journey still has sparse scenery, large flat terrain areas, clipped long course name in live header and a heavily regular loop layout; no gameplay or finish/multiplayer acceptance inferred. Next Woodland World 2.0: prioritize course-specific environmental clusters and compact Regional Journey header, preserve graph and touch geometry; evaluate Start/Finish landmarks and performance. QA1844 CI both passed (45574853504,45574853404). No runtime changes in this documentation checkpoint.

---

# Active checkpoint — GridMates launcher standard v1.0 adoption

Owner designated illustrated Sudoku Beta 3 icon as canonical for green surround and margin. Shared standard committed at EdenP-Development/Eden-Development-Standards@9821e84330057c7744ff0f84ae3e85d380445c57, branding/gridmates/app-icons/v1.0/README.md. Verified actual Sudoku build recipe, not legacy vector: 58dp/232px foreground centered at 100,100 on 432px adaptive canvas, runtime #214F3F. Crossroads already uses same green; changed 56dp/224px to exact nominal 58dp/232px footprint. Original fox master, edge-connected corner cleanup, IDs, signing, saves and startup timing retained. Actual gold-frame pixel extents and mask appearance still require side-by-side physical review; no final visual acceptance claimed. Exact-head CI pending. Shared standard contains source hashes from Sudoku recipe; original bytes not independently reconstructed in this audit. Other GridMates titles need independent adoption.

---

# Active checkpoint — VISUAL-120F3

VISUAL-120F2 rules 45571927615 passed; Android 45571927630 running at inspection. Nearby setup reuses shared primary action controls for Create room and Join by address; room-code input is placed before discovered rooms so protected-room buttons no longer require scrolling to manual address controls. Same code state, parser, validation, callbacks and transport unchanged. Home tactical and fair-finish settings use shared tabletop paper panels. Exact-head CI and two-phone physical review pending; no protocol, rules, saved data or signing changes. Consolidate QA next.

---

# Active checkpoint — VISUAL-120F2

VISUAL-120F1 rules 45571106183 passed; Android 45571106233 was running at inspection. Results: shared engraved TabletopPanel for winner and last encounter, rounded optional victory art. Journal: each existing encounter is a separate parchment entry with turn/actor, title and outcome; newest first unchanged. No new victory asset, no fabricated placements, scoring/timing/receipt logic unchanged. No signing, save or gameplay changes. CI pending for this head; physical review of short screens/large fonts needed. Settings and multiplayer remain open; consolidate QA after this checkpoint rather than repeatedly superseding builds.

---

# Active checkpoint — VISUAL-120F1

VISUAL-120E1 Android 45570109811 and rules 45570109817 passed. Encounter cards now reuse TabletopActionTile rather than duplicating colors, feedback and sizing; two-line consequence copy matches tactical actions, legal choices and callbacks unchanged. Route comparison sheet adopts shared parchment/brass tokens and shorter Board button with accessible Back to board description; route comparison and map inspection preserved. No gameplay, signing or data changes. New exact-head CI and physical review pending, particularly four-option encounter fit and large fonts. Results, journal and settings remain candidates for later cohesion. No final visual approval claimed.

---

# Active checkpoint — VISUAL-120E1

VISUAL-120C1 Android 45568422712 and rules 45568422627 passed. Compact HUD pass: fixed-width Map utility with full accessible description, omit redundant opening narration for offline tactical roll (actions already explain choice), Push tile displays actual outcomes 1 or 5–8 spaces, turn strip tighter and flatter, action tiles use warm brass and reduced elevation/padding with two-line consequence text. Existing dock safety bounds and 48dp+ interactive targets retained, no camera or rules changes. Provisional design; exact-head CI and phone review needed, including four seats, Regional Journey, large fonts and turn details. No signing or saved-data changes.

---

# Active checkpoint — VISUAL-120C1

VISUAL-120D1 Android 45567414743 and rules 45567414689 passed. Refined existing earthen trail and junction rendering: soft layered shoulders, reduced dark outline, warmer earth deck, deterministic broken verge pigment and matched junction palette. Centerlines, marker coordinates, route colors, width of playable deck, hit testing, motion, saves and signing unchanged. No curved geometry introduced; visual-only first pass. Simple/Printed and atlas overview remain intact. New CI and phone review pending; verify risk-band legibility and dense regional routes. Next review HUD proportions and environmental density in consolidated build.

---

# Active checkpoint — VISUAL-120D1

QA1825 screenshot shows board bottom ending above action tray, exposing an unwanted green strip. Existing follow clamp allowed world bottom to reach 68% viewport in roll/route, 55% encounter, 57% target. Move bounds to 80%, 65%, 63% respectively as provisional visual correction; preserves existing focus target, top clamp, manual pan/zoom, Full Map and exact shared rendering/hit geometry. Updated edge clamp tests to assert each phase. Real-device checks required at start/finish, forks and tall encounter/target panels; active pawn may shift down near edge, check visibility before acceptance. VISUAL-120A rules 45566999949 passed; Android 45567000206 in progress at inspection. No gameplay or signing changes. Exact-head CI pending. Organic trails and wider scene composition remain open.

---

# Active checkpoint — VISUAL-120A/B

QA1825 Android 45564597458 and rules 45564597444 passed. Owner supplied phone screenshots: green launcher and native icon shown without previous black surround; home cover rounded; map header uncluttered; Nearby labels still crowd ornament; Woven overhead terrain still flat and river bleeds into outer mat. No explicit owner final art acceptance. Audited runtime catalog and original import guide: three backplates, home hero, event art, trail/token/marker/environment atlases; V2 environment preferred if valid, 24 UI source candidates remain separate until promoted. First terrain pass uses fixed soft pigment washes and subtle shared atlas contact shadows; clip Woven river to printed board rectangle; shorten Nearby actions to Host/Join under existing same-Wi-Fi heading. No graph, hit testing, camera, save or signing changes. Visual styling provisional; exact-head CI and phone review pending. Next inspect camera edge behavior and terrain composition before changing geometry.

---

# Active checkpoint — VISUAL-119A

VISUAL-118A rules 45564231581 passed; Android 45564231618 was still running when checked. Small HUD cleanup: avoid displaying the same narration twice when turn details are expanded; shorten the disclosure to Turn details / Hide details, covered by a unit test; allow turn title to wrap to two lines rather than truncate long names. Existing action dock minimum heights and camera geometry deliberately retained until physical review. No rules, signing or data changes. Exact-head CI pending. Next consolidated APK review should cover launcher masks, home controls, Storybook groundcover, map exit, and HUD with normal/large fonts. Further board scenery and camera improvements remain open.

---

# Active checkpoint — VISUAL-118A

VISUAL-117C rules 45563639767 passed; Android 45563639649 failed because utility button safeInset was not declared in its scope. Fixed with BoxWithConstraints and local inset. First board pass: reuse deterministic, graph-clearance-filtered groundcover on Storybook illustrated view as well as Overhead (no duplicate drawing); retain all route geometry, camera and input transforms. Woven procedural fallback now resolves tactical variants through baseId, matching other courses. No new artwork or rule changes. Groundcover over existing backplates is provisional and needs device review for density and readability. Exact-head CI pending. Next examine actual camera edge framing and HUD rather than changing geometry blindly.

---

# Active checkpoint — VISUAL-117C

VISUAL-117B Android 45562649186 and rules 45562649121 passed. Shared tabletop controls now reserve larger horizontal text insets on wide faces, use quiet native frames for aspect ratios below 3:1, retain selected borders and add pressed-state shading in native fallback, and constrain utility labels to two centered lines. Added pure geometry policy regression test. These thresholds are provisional pending screenshots; no gameplay, saves, signing or artwork master changes. Exact-head CI pending. Next inspect compact and wide controls on phone; then HUD and board presentation.

---

# Active checkpoint — VISUAL-117B

VISUAL-117A Android 45561783809 and rules 45561783745 passed. Home cleanup: clip cover overlay to rounded cover, clip scrolling content to its allocated viewport, increase scroll-end breathing room, use explicit Players heading with compact 2/3/4 choices and accessible labels, separate QA course disclosure from Regional Journey button, allow board appearance label to wrap in weighted column. These are provisional visual adjustments, not physical approval. Pinned Start Race, rule summary, game rules, saves and signing unchanged. Exact-head CI pending. Shared control art insets and board presentation remain open.

---

# Active checkpoint — VISUAL-117A

BRANDING-116 Android run 45561130523 and rules run 45561130482 failed at Gradle script compilation: java.util reference shadowed by Gradle java extension. Import ArrayDeque directly; retain all pixel processing logic. First visual cleanup: remove redundant cramped Close map header button, retain pinned Back to the trail/Return to route choices and dialog dismissal. No rules, signing or data changes. Exact new CI and device review pending. Further shared button insets, home footer spacing, board/HUD and terrain work remain open.

---

# Active checkpoint — BRANDING-116 / owner QA1813 visual review

Owner phone screenshots confirm QA1813 installed and the new fox icon appears, but black surround/corners are rejected. Sudoku Beta 3 feat/beta3-foundation uses icon_background #214F3F (actual colors.xml and adaptive XML inspected). Preserve approved master and SHA unchanged. Runtime derivative removes only edge-connected near-black pixels (threshold 48, four-connected flood fill, removal sanity gate); use same family green adaptive background and enlarge artwork from 52 to 56 dp provisionally. No claim of visual acceptance; inspect launcher and native startup on next phone build. Prior QA1813 Android run 45557527365 and rules 45557527355 passed. New exact-head CI pending.

Owner also requests game-wide visual cohesion. Screenshot audit: mixed illustrated/native buttons, cramped Close map label overlapping right ornament, large action panel and repeated Rules & last action, blank board edge near start, sparse terrain and inconsistent header/panel styling; home course metadata can be obscured by fixed start footer. Woven Trail was shown, not Regional Journey, and shared splash screens were not captured. Next visual pass: unify control variants and safe text insets, fix map close header layout, verify home footer/scroll spacing, refine board camera bounds and HUD hierarchy, improve terrain composition without hiding routes. Preserve rules, saves, signing and approved art. No main merge/public release.

---

# Active checkpoint — BRANDING-115 (2026-11-24 local)

Owner-approved master recovered from private Drive and verified locally: PNG RGB 1254×1254, 2257645 bytes, SHA-256 711cde86e7d7da605206b3c531ee80ba0d768860ab5f528ec836388555208d4b, Git blob a1beaa04a9f143a1c685ed176fedcd6828f1b893. Exact original is committed at design/art/imports/Crossroads-approved-icon-v1.png; no regeneration. Gradle generates a centered 52dp artwork on a 108dp adaptive layer (432px raster), black background matches original corners. Conservative initial sizing is provisional pending actual launcher mask review. Existing manifest, app IDs, signing, saves, startup and rules unchanged. Existing fox asset retained as historical source.

Prior BRANDING-114 rules run 45554247752 and Android QA run 45554247775 both passed. New exact-head CI, APK resource verification and physical launcher/Regional Journey/startup review pending. Engineering QA only; private beta 2002 remains a separate signing channel. No public distribution or main merge.

---

# Active checkpoint — BRANDING-114 / GAMEPLAY-113 (2026-11-24)

Owner authorized Spudacus → GridMates → existing Crossroads root per shared startup v1.0 after physically accepting Sudoku Beta 3 Visual Review 4. Read central BRAND_IDENTITY.md, branding/game-startup/v1.0/README.md, reference/StartupBranding.kt and both asset guides. Newer shared standard supersedes older pilot-only language.

Pinned runtime PNGs: Spudacus source 015645bffb612e0f4311ec5a2d29e9c8bad91289 SHA256 74773dbda822161f73a639f959e8bce5c6e0b333a94ebeb8ee27f620d4995d25; GridMates source 9affedc94ca5a6b896d36148f46f14a4e8218c3a SHA256 eef6d631cac0b9523f7e0dbebac229f9a2458d36fe2e73e111d251704d9ff292. GitHub connector could not decode binary PNGs. Central repository public; Gradle prepareStartupBranding fetches pinned bytes at build time, verifies checksum and caches generated assets; CI verifies both exact PNGs inside signed QA and unsigned public APK. Installed runtime reads local assets only. New clean build requires network to central source. No regeneration.

StartupBranding overlay adapted to Compose root, cream #FAE7CD, 250/1000/250ms per logo, nominal 3 seconds plus decode, tap/Back skips remainder, reduced motion bypass, ON_STOP dismissal, savedInstanceState gate, local decode failure skips, underlying semantics cleared during overlay. Unit tests cover stage order, nominal duration and initial gate. No actual device startup timing, skip, focus, lifecycle, orientation, offline, native splash or performance verification yet. Exact new CI pending. No package/signing/launcher changes, no public release.

Previous consolidated regional development verified QA1799 workflow 45553389006 and rules 45553388398. Regional V5 selectable in debug only, dedicated terrain and labels, dense marker spacing, expanded recovery test 192 matches, regional original rules excluded from official stats. Next verify branding exact-head CI, then owner internal QA of both startup and Regional Journey, save recovery, gameplay pacing, scenery and readability. Main remains untouched, draft PR120. Keep private beta 2002 separate from public-test-only QA signer.

---

# Active checkpoint — GAMEPLAY-112B (2026-11-24)

Active branch feature/identity-17-art-import, stacked draft PR120; main untouched. Owner QA1759 review: docs/QA1759_OWNER_PHONE_VISUAL_AUDIT.md. Gameplay-first plan: docs/GAMEPLAY_103_COURSE_REDEVELOPMENT.md. Regional V5 selected provisionally: 113 nodes, 49–62 edges, 8 forks, 3 three-way forks, 23 event nodes. Verified QA1776 run 45547159727; 2160 exposure races and 2160 strategy races; V5 tactical median rounds 10 across 2/3/4 seats. V5 fixed policy wins (2/3/4 seats): Steady shortest 60/45/28.8%, Steady safest 55/38.3/25%, Push shortest 45/18.3/10%, Push safest 47.5/45/30%. Aggressive multiplayer weakness remains open; synthetic, not human timing or proven balance.

GAMEPLAY-112A/B commits a17b0724acc7c629c045c2c6ac9df7dd68766204, 4bf9a026c15abed9741b0407dce10a9d60bc0bc1, 719337eb265150ebb7c342a066d6a7217ed1547d, 5c508885412ad4fe21f6fe74d8b4ac45e53d7701: debug assets include design/prototypes; MainActivity loads regional V5 only in DEBUG, registers board and variants, home adds separate QA pill, title/subtitle mapped. LAN host still uses original Classic. No existing board ID or rule change. Exact latest build unverified. Next inspect regional renderer fallback, density, camera, stats and recovery behavior; add tests and verify signed QA build before phone review. No bespoke regional art yet. Keep private beta 2002 and QA signing identities separate.

---

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
