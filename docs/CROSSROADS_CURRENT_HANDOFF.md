# Active checkpoint — AI2-10B executable Gambler policy

AI2-10A both workflows passed (45919107891 rules,45919107983 Android). Added immutable GAMBLER_V1 identifier and strategy preset, dispatch for visible-state travel and encounter decisions; route and targeting reuse R4 chain, classic rules retain legacy behavior through fallback except existing R4 target behavior. Explicitly pinned original five-policy cohorts in mixed/four-seat tournaments, balance diagnostics and both calibration tests. Added 24 mixed Gambler/Scout convergence races, opening Push receipt, midpoint codec/replay, continued exact decisions and rematch tests. CI pending. No Android UI, LAN or default policy change. Exact calibration CSV analysis still outstanding. Next run dedicated Gambler competition and behavior comparisons. No merge/release.

---

# Active checkpoint — AI2-10A Gambler decision foundation

AI2-09C rules 45918709240 passed; Android 45918709219 in progress at inspection. Added pure GamblerPersonalityV1 candidate travel and encounter heuristics and legality/contrast tests. Opening/midgame Push when gap >= -2; preserve near safe finish unless final round; shelter supplies unless substantial lead with defensive need; hazard Gamble when visible upside or close race. These are uncalibrated heuristics, not demonstrated difficulty. No registration, dispatch, serialized ID, preset mapping, RNG, UI or LAN changes yet. Prior tests use enum entries with fixed matrix sizes; pin historical comparison cohorts before adding new executable policy. Exact calibration CSV numerical analysis outstanding. CI pending. Next integrate separately versioned policy and run mixed competition with exact receipts. No merge/release.

---

# Active checkpoint — AI2-09C exact decision receipts

AI2-09B both CI passed (45916504120 rules,45916504122 Android). Added internal ephemeral lastBotDecision receipt captured at nested accepted legal action record; reset on botStep, no serialization, policy reads, RNG changes or additional checkpoint actions. Added 720 exact calibration races across two Woven layouts, five presets, 2–4 seats, 24 seeds, legal action assertions, action count reconciliation and replay receipt/snapshot checks. Prior inferred calibration retained for comparison. Exact CSV uploaded. CI pending. Need inspect actual data before selecting personality or difficulty policies. No UI/LAN/default changes, no merge/release.

---

# Active checkpoint — AI2-09B strategy behavior calibration

AI2-09A both workflows passed (45915821720 rules,45915821724 Android). Added 720 homogeneous diagnostic races across original Woven and convergence V02, five strategy presets, 2/3/4 seats and 24 seeds. Reports route risk, encounter responses, target frequency, action/roll pacing and winner seat; checks completion and deterministic replay. Post-action steady flag provides only confirmed steady lower bound, remaining rolls unclassified (never infer push from die); route retreat and ambiguous encounter observations likewise unclassified. No difficulty strength claims from homogeneous races. CSV uploaded by rules workflow. CI pending. Next measure exact travel decisions with a replay-safe observer if needed, then separately calibrated difficulty policies and fairness. No runtime/default/save/UI/LAN changes, no merge/release.

---

# Active checkpoint — AI2-09A strategy preset foundation

AI2-08G both workflows passed (45894542176 rules,45894542225 Android). Added core-only BotStrategyPreset and BotStrategyLineup resolver mapping five provisional descriptive strategies to existing immutable V1/R1–R4. Tests for human exclusion, invalid size, 24 mixed-policy convergence races and checkpoint/rematch preservation. No new policies, difficulty ranking, UI, LAN, runtime defaults or save schema changes. CI pending. Design rationale docs/AI2_09A_STRATEGY_PRESETS.md. Next calibrate difficulty separately from strategy, inspect fairness and mischief behavior, retain physical QA checkpoint. No merge/release.

---

# Active checkpoint — AI2-08G QA preview acceptance and repository sync

AI2-08F both workflows passed (45893695980 rules,45893696070 Android), QA1900 artifact 15152852006, 20277538 bytes. Added Android JVM regression tests for forced V5 course selection and distinct title, QA registry recovery, classic checkpoint preservation, preview backup roundtrip and unavailable public registry rejecting without erasing checkpoint. Tests awaiting exact-head CI. No runtime changes. Physical review of scenery, paths, target encounters, install-over, resume and LAN remains outstanding. Legacy V1 default, no public release. Next: difficulty/personalities design and starting-position fairness experiment; retain previous checkpoints and draft PR.

---

# Active checkpoint — AI2-08F optional QA convergence preview

AI2-08E both CI passed (45891358945 rules,45891358980 Android). Added debug-only asset loading and explicit offline Woven Balance Preview choice, distinct course label, registered base identity for home and V5 identity for recovery, forced V5 new-match selection for this candidate regardless of ordinary tactical toggles. LAN continues using original classic board. Normalize imported selected course to base ID. V1 bot default retained. Existing original courses untouched. Exact-head CI pending; actual renderer, scenery and physical save/restore require owner review. Home tactical switches do not apply to this preview; explicit subtitle indicates Lab v5. No release or merge.

---

# Active checkpoint — AI2-08E Woven candidate geometry refinement

AI2-08D rules 45891026493 and Android 45891027084 in progress at inspection. Candidate v01 normalized straight L4→F2 length 0.35725, longest edge. Created distinct unregistered v02 prototype design/prototypes/woodland_woven_convergence_v02.json repositioning L2 (.27,.75), L3 (.34,.65), L4 (.32,.51); L4→F2 now 0.13647; longest remaining edge 0.23537; no proper nonadjacent straight-line edge crossings in coordinate inspection. Original and v01 retained. Added geometry/topology and 24 seeded identical snapshot comparison tests. Straight-line geometry not proof of actual curved renderer, scenery clearance or tablet readability. Exact-head CI pending. No shipped boards, Android registration, rules, policies or save changes. Next verify CI and prepare optional QA preview with visual review. No merge/release.

---

# Active checkpoint — AI2-08D Woven candidate frozen for review

AI2-08C both workflows passed (45889974903 rules,45889975088 Android); QA1894 artifact 15150519782. Inspected actual convergence CSV artifact 15151191767: original / +0 / +1 / +2 / +3, 144 games each, F2 decisions 141/344/364/394/392, encounter availability 0/153/181/221/255, targets 0/56/58/93/105, rolls means 8.3889/8.8611/9.7917/10.7222/11.2778, actions means 24.5417/26.2153/27.7639/29.6667/30.0764. Selected zero added spaces as lowest pacing impact (5.63% more rolls, 6.82% more actions). Added separately identified unregistered prototype design/prototypes/woodland_woven_convergence_v01.json redirecting L4 to F2, and exact graph / fingerprint / 144-game replay and target count regression. Geometry coordinates unchanged; visual crossing/readability, owner acceptance, real device and starting seat fairness outstanding. No runtime registration, policy changes, shipped board changes or release. CI pending. Next inspect geometry and prepare opt-in QA preview only after verification.

---

# Active checkpoint — AI2-08C Woven convergence experiment

AI2-08B both CI passed: rules 45690985708, Android 45690985744, QA1892 artifact 15059591276. Actual CSV artifact 15058777173: 144 races per arm; original 141 F2 route decisions, 129 observed medium, zero encounter available/target; +1/+2/+3 all zero F2 decisions, 3596 actions and 1252 rolls per arm vs original 3534/1208. Graph inspection confirms low approach L4→F3 bypasses F2; lengthening R3→F2 makes bypass relatively attractive. This is plausible explanation, not directly measured approach choices. Do not adopt spacing variants. Added test-only convergence candidate redirecting L4→F2 and adding 0–3 plain spaces on both approaches, plus original control (-1), 720 races, completion/replay and guaranteed F2 coverage assertions. Same encounter rules and policies. No registered course or save changes. Geometry changes route incentives and require visual review if adopted. Exact-head CI pending. Next inspect encounter availability and target frequency, pacing and route diversity before deciding. No merge/release.

---

# Active checkpoint — AI2-08B Woven encounter spacing experiment

AI2-08A rules 45690185706 passed, Android 45690185607 in progress at inspection. Actual artifact 15059066050: 960 finish receipts, first finisher wins 858, later winner 102, tied best 18. Woven F2 medium offered 223, encounter available 0, observed medium 199, targets 0. This supports exhausted encounter allowance rather than medium avoidance within observed runs. Added test-only geometry comparison: original plus 1/2/3 plain spaces R3→F2, 3 policies V1/R2/R4, 2/4 seats, 24 seeds, 576 matches. Measure F2 exposure, available encounters, observed medium, targets, actions/rolls and winner; require completion and replay. Spacing may alter route attractiveness; no causal isolation from geometry incentive changes. No registered board, runtime, save, or rule modifications. CI pending. Next compare variants and assess pacing before considering separately versioned course change. Fair finish tie rule alone unlikely to explain seat imbalance; further diagnostics needed. No merge or release.

---

# Active checkpoint — AI2-08A balance diagnostics

AI2-07B both workflows passed; QA1888 available. Four-seat actual results documented in docs/AI2_08A_BALANCE_DIAGNOSTICS.md. Added 960 diagnostic matches with route exposure and fair finish receipt CSVs; no gameplay changes. Exact-head CI pending. Investigate Woven mischief availability and seat bias before any versioned rules changes. Legacy V1 default; no Android/LAN experimental activation. Owner physical review pending. No merge/release.

---

# Active checkpoint — AI2-07B tournament evidence and four-seat validation

Both AI2-07A workflows passed: rules 45683192102; Android 45683191929, QA1886 artifact 15054214634. Inspected actual CSV artifact 15053909983. Added docs/AI2_07B_TOURNAMENT_EVIDENCE.md: V1/R1/R2/R3/R4 wins 163/168/205/212/212 out of 384 appearances each; seat1 574/960; R3 vs R4 48/48. R2/R3 encounter comparison 24 target phases each, all Brookside. Added 960-game four-seat cyclic lineup test with each policy omitted once, 12 seeds, four courses, codec/replay assertions and CSV upload. CI pending, no new runtime or menu changes. Do not select difficulty tiers until larger-room evidence; fairness and mischief scarcity remain open. No release or merge.

---

# Active checkpoint — AI2-07A mixed-policy tournament

AI2-06B rules 45671745077 passed; Android 45671745006 failed Maven HTTP 429 dependency resolution, last verified QA1876. Added optional per-bot-seat policy overrides, copied and validated against human/out-of-range seats; default homogeneous policy remains unchanged, omitted empty overrides preserve legacy JSON. Checkpoint restore/rematch retain overrides. Real pairwise tournament: 960 games, five policies, ten pairs, four V5 courses, 12 seeds, both seat orders. Asserts completion, final replay and rematch lineup; CSV uploaded. No menu/LAN activation or default change. CI pending; no tournament results yet. Historical gate remains mandatory. Encounter comparison artifact 15049112495 retrieved previously but numerical analysis outstanding. No merge/release.

---

# Active checkpoint — AI2-06B deterministic targeting coverage repair

R4 rules 45669778345 and Android 45669778595 failed on AdaptiveBotTargetR4Test line 44 only (124 core tests, 1 failed): natural Woven matrix produced zero TARGET phases; all completion and replay assertions before it passed. Prior AI2-05B rules 45669084844 passed, artifact 15049112495 available, Android cancelled. Replaced unsupported stochastic coverage expectation with diagnostic count and separate guaranteed medium-risk fork / single prank catalogue fixture, exercising legal targets and codec restore at pending TARGET then identical continuation. No production rules or policy change. Exact-head CI pending. Natural mischief scarcity remains a balance issue to investigate, not fixed by fixture. Next inspect encounter CSV and build mixed-policy tournament only after green CI. No release or merge.

---

# Active checkpoint — AI2-06A experimental threat assessment R4

AI2-05B rules 45669084844 and Android 45669084948 in progress at inspection; encounter comparison results not yet available. Added ADAPTIVE_V2_R4, explicit core-only policy. R4 delegates all non-target phases to R3 and ranks legal targets by visible distance, shield, actual traversed-path retreat distance estimate and proximity to finish. Shield stripping valued at provisional 0.75; urgency multiplier 1.5 within six spaces, distance-gap penalty 0.65. No hidden draws or added RNG; no rules changes. Tests cover target ranking, ties, empty set and 64 seeded four-bot Woven V5 races with target-boundary and final replay. CI pending; target coverage not yet established. V1 remains default; no Android/LAN activation. Next inspect encounter comparison and target results, then mixed-policy competition. Owner visual review outstanding. No merge/release.

---

# Active checkpoint — AI2-05B encounter policy comparison

AI2-05A rules 45668310891 passed; Android 45668310797 in progress at inspection. Added homogeneous actual-engine R2/R3 comparison, 576 races / 288 paired configurations over four V5 courses, 2–4 seats, 24 seeds. Counts shelter and hazard responses using lastEventId after encounter action, distinct hazard result IDs per match, target opportunities and shielded legacy targets, action and roll pacing; requires completion and replay. Target counts are not full encounter variety, and distinct hazard IDs per race are not catalogue coverage. Both policies retain legacy targeting. CSV included in AI workflow artifact. No runtime changes, default remains V1. Exact-head CI pending. Inspect results before deciding on targeting policy; mixed-policy competitive testing still outstanding. Owner visual review still incomplete. No merge or release.

---

# Active checkpoint — AI2-05A encounter-aware experimental R3

AI2-04C both CI passed (45592178746 rules,45592178745 Android); QA1876 artifact 15011083088 (20269970 bytes). Actual route CSV artifact 15011602785: 288 completed and replayed per arm, R1/R2 actions 14081/13522, rolls 5825/5471, routes 4565/4477, encounters 3651/3550, targets 40/24. Mean actions 48.89/46.95; mean rolls 20.23/19.00. Homogeneous policy races, not competitive superiority. R2 fewer rolls in 3 of 4 courses; mischief reduction merits review.

Added ADAPTIVE_V2_R3, explicit experimental core only. Retains R2 route and R1 travel; encounter choices use remaining movement, shield, signed gap, Steady protection, visible Push bonus and final-round status. Heuristic not probability-optimal; shield does not block hazard retreat (already-shielded bots may gamble to avoid redundant protection). Added focused tests and 12 seed completed replay checks; exact-head CI pending. V1 default unchanged, R1/R2 untouched, no UI/LAN activation or release. Next compare encounter choices and mischief diversity, mixed-policy competition, then threat assessment. QA1857 physical review outstanding.

---

# Active checkpoint — AI2-04C route policy comparison and portfolio handoff

AI2-04B rules 45591493548 passed; Android 45591493481 running at inspection. Added actual-engine homogeneous R1/R2 comparison: 576 races, 288 paired course/seat/seed configurations, 4 V5 courses, 2–4 seats, 24 seeds. Records pacing, route/encounter/target counts and winner seats; requires finish and replay. All seats share one policy per race; cannot infer relative competitive strength from this test. CSV uploaded with existing AI artifact. No runtime changes; exact-head CI pending.

Current state: legacy V1 remains default; R1/R2 explicit experimental core only. Historical V1 gate passes on prior head; R1/R2 same-version replay tests pass on prior rules head. Travel result source artifact 15009953323: 134 vs 145 wins in 432 per arm, 2150/2918 vs 1980/2922 push rolls. QA1857 owner screenshots incomplete due upload limit; retain visual checkpoint. Next inspect route comparison CSV, design mixed-policy competition or controlled focal proxy, then encounter intelligence. No public release, merge, or LAN activation. Draft PR120 remains development lane.

---

# Active checkpoint — AI2-04B experimental adaptive route R2

AI2-04A rules 45590871078 and Android 45590871122 still running at inspection. Added explicit ADAPTIVE_V2_R2: on Lab routes score remaining distance, advertised risk only if encounter available, Steady shelter momentum, visible finish and next safe fork when encounter spent. Deterministic ID tie-break; R2 consumes no route scoring RNG (different stream from R1 by design). R1 travel reused, all other phases unchanged. V1 and R1 branches untouched; default V1; no Android/LAN selection. Added scoring and 12 seed replay tests. Geometry not guaranteed if encounters intervene; weights provisional, no measured superiority. Exact-head CI pending. Next compare R2 against R1 and retain historical compatibility. QA1857 physical review outstanding.

---

# Active checkpoint — AI2-04A route horizon foundation

AI2-03A rules 45589884978 passed including historical gate and artifact 15009953323; Android 45589884972 in progress at inspection. Inspected actual 864-row CSV: V1 134/432 wins, 2150/2918 push rolls; R1 145/432, 1980/2922; 21 paired gains, 10 paired losses. Course wins V1/R1: Brookside 33/33, Woven 35/38, Cedarwood 36/36, Regional 30/38 (108 per arm each). Controlled travel proxy, not full tournament or statistical proof. Retain R1 frozen, no default activation. Added pure BotRouteLookahead and unit tests for deterministic corridor horizon, finish, next fork advertised risks, exhausted movement, malformed cycles. No RNG, no event forecasting, not yet wired to live decisions. Current geometry-only horizon ignores intervening encounter changes to movement. Exact-head CI pending. Next evaluate route scoring and explicit new policy revision without altering R1 or legacy.

---

# Active checkpoint — AI2-03A mirrored travel comparison

Adaptive V2 R1 rules 45589117704 passed; Android 45589117637 running at inspection. Added AdaptiveTravelComparisonTest: 864 races / 432 paired scenarios, four V5 courses, 2–4 seats, every focal seat, twelve seeds, legacy vs adaptive travel on a synthetic human seat against unchanged V1 bots. Both arms use identical deterministic non-travel choices; this isolates travel heuristics but is not a full mixed-policy tournament. Record wins, pushes, rolls and actions, assert completion and pairing; publish CSV/summary via existing AI artifact. No runtime, policy IDs, RNG or save changes. Exact-head CI pending; no measured superiority claim. Review actual report before modifying policy.

---

# Active checkpoint — AI2-02B experimental adaptive travel R1

AI2-02A both CI passed (45586915751 rules,45586916198 Android). Added explicit ADAPTIVE_V2_R1 policy; only Lab travel roll choices differ, based on pure race assessment, signed gap, protection and final round. Other phases use unchanged legacy implementation. Existing default remains LEGACY_V1; no Android menu/LAN selection or public activation. Experimental policy saved explicitly and rematch retains it; older strict decoders cannot read experimental saves. Tests cover heuristic branches and 12 three-bot V5 Woven races with restored future; historical replay gate still runs. Freeze R1 behavior for replay, future revisions new policy IDs. Thresholds provisional, no strength claim. Exact-head CI pending; no actual device or LAN validation. QA1857 visual review deferred.

---

# Active checkpoint — AI2-02A pure race assessment

AI2-01C rules workflow 45586159958 passed including pinned old-engine fixture generation, current replay comparison and artifact upload; Android 45586159877 running at inspection. Added BotRaceAssessment and tests: visible remaining distance, shield, ordered opponent threat, signed gap, lead/tie/trail, opening/midgame/endgame and final-round awareness. Preliminary thresholds: opening front >=75% remaining, endgame front <= max(6,20% course length) or final round. Pure helper not wired into GameSession; legacy policy only, no V2 selectable, no RNG or save changes. Exact-head CI pending. Next integrate an explicitly versioned experimental policy and exercise historical gate before exposing it. QA1857 visual review still deferred.

---

# Active checkpoint — AI2-01C historical replay gate

AI2-01B rules 45585566634 passed; Android 45585566974 running at inspection. Added HistoricalBotReplayTest and rules workflow step that checks out pinned pre-policy commit 3bf768046e08f0724c4171c91b5a547647436289, runs the same fixture harness against old engine to produce 24 mid/end checkpoint pairs, then verifies current engine restores old midpoint, re-encodes identical legacy JSON and finishes with identical final checkpoint. Upload baseline CSV and historical fixtures. No engine behavior changes. CI pending; historical fixtures not yet generated/verified. No claim of Android install-over or LAN validation. Next enable experimental policy only after gate passes.

---

# Active checkpoint — AI2-01B legacy policy identity

AI2-01 rules 45584918650 passed; Android 45584918793 running at inspection. Added stable serialized BotPolicyVersion.LEGACY_V1, default and only supported policy, explicit session dispatch to unchanged legacyBotStep, checkpoint/restore/rematch propagation. EncodeDefault NEVER preserves existing format-1 legacy JSON shape; omitted field defaults to legacy. Baseline test now exercises codec roundtrip, omitted field and policy identity across 252 races. No V2 behavior or UI enabled; no changes to RNG draws, board fingerprints or save format number. Exact-head CI pending; cross-version historical fixture still needed before enabling a new policy. New policy must explicitly handle older binary rejection and LAN compatibility.

---

# Active checkpoint — AI2-01 baseline instrumentation

Added BotPolicyBaselineTest (252 all-bot games, seven course/rule combinations, 2–4 seats, 12 seeds; finish and checkpoint replay assertions, phase counts CSV) and docs/AI2_01_BASELINE.md. Runtime unchanged. MatchCheckpoint v1 stores BOT commands, so policy/RNG changes may break legacy saves; version policy before changing behavior. CI pending, no simulation results yet. QA1857 visual review deferred due owner upload limit. No release changes.

---

# Active checkpoint — WW2-06 consolidated visual audit / QA1857

Both exact-head WW2-05A workflows passed: rules 45581152204, Android 45581152201; QA1857 artifact 15006113348 (20257423 bytes). Added docs/WW2_06_VISUAL_ARTWORK_AUDIT.md with inventory (17 slots, 10 QA-preview approved, 7 planned), reuse and decode audit, theoretical memory costs, owner test matrix, and explicit limitations. No runtime changes; no new APK needed for this documentation-only checkpoint. Physical review and performance measurements outstanding. Next focus gameplay/AI/balance and multiplayer after review.

---

# Active checkpoint — WW2-05A low-risk draw-path cleanup

WW2-04A exact-head rules 45580068227 and Android 45580068405 both passed; signed QA1855 artifact 15006376951 (20256824 bytes). All prior WW2 additions included. WW2-05A hoists constant groundcover colors and caches waypoint angle lists/unit vectors instead of rebuilding them on draw. Same colors, shapes, geometry and placement, no new art or gameplay changes. This is a small static hot-path cleanup, not measured speedup; no claim of improved FPS or memory use. Existing board scene plans already remembered by board/style; larger raster caching deferred until profiling/physical evidence. Exact-head CI pending. QA1855 is prior verified fallback; physical WW2 review pending. Startup/icon/signing/saves untouched.

---

# Active checkpoint — WW2-04A regional terrain palette

WW2-03A rules 45579266484 passed. Android 45579266581 failed before compilation/tests resolving existing dependencies: Maven Central HTTP 429 Too Many Requests. No dependency changes or test waiver. Regional overhead terrain now has ten overlapping fixed pigment pools (warm village earth, shaded forest, golden meadow), plus region-specific fleck tints; clipped to existing board, beneath graph and sprites. Added composition regression for count, bounds and three tones. Existing course palettes untouched. No new assets, game geometry, signing, startup, icon or saves changes. Exact-head CI and physical review pending; visual colors provisional. Next prioritize consolidated verification before additional changes if repository rate limiting persists.

---

# Active checkpoint — WW2-03A clustered groundcover

WW2-02B rules run 45578950534 failed before tests: Maven Central and Gradle plugin portal HTTP 403 resolving existing Kotlin dependencies. Android 45578950645 running at inspection. No dependency change or test waiver. Groundcover planner now prioritizes up to 60 deterministic companion tufts around existing scenery, then retains a sparser background distribution; original route/landmark/patch clearance checks remain. Added regional cluster and total budget regression. Applies to existing illustrated groundcover planner across courses; companion clusters require occupied landmarks. No new assets, graph, camera, signing or saves changes. Exact-head CI and physical review pending; cluster density provisional.

---

# Active checkpoint — WW2-02B Regional trailhead and destination scenery

WW2-02A rules 45578412836 passed; Android 45578412699 was running at inspection. Added existing atlas fence near Regional START (.035,.67) at (.15,.725), and cottage near FINISH (.88,.27) at (.925,.175). Independent JS check against actual authored JSON confirms both satisfy existing .078 route clearance, .09 separation and sprite bounds; Kotlin regression requires both placements. No new artwork, graph changes, camera changes or extra playable spaces. Only Regional overhead QA view affected; other courses still need landmark composition. Exact-head CI and phone review pending. Startup/icon/signing/saves unchanged.

---

# Active checkpoint — WW2-02A waypoint identity

WW2-01 rules 45577715018 passed; Android 45577715008 running at inspection. Start/Finish sockets now have distinct engraved rim treatments: green trailhead with four leaf-like marks and warm brass destination with six sunburst marks. Existing outer footprint, radius clamp, S/F text and pawn layering retained; no new selectable node, asset or graph change. Added deterministic rim-pattern regression. This is a small first waypoint identity pass, not the promised full environmental trailhead/village landmark; larger scenery requires placement clearance and phone review. Exact-head Android/rules CI pending. Startup, icon, saved data and signing unchanged.

---

# Active checkpoint — WW2-01 Regional scenery composition

QA1844 owner accepted startup; launcher retained. Expanded Regional Journey authored scenery from 9 to 16 candidates: village fence, orchard and bushes, forest fir companions and meadow bush. Existing .078 graph clearance and .09 spacing remain; independent JS check on actual Regional JSON predicts all 16 accepted. Kotlin regression now requires >=15 and unchanged geometry; CI pending. Compact gameplay header Regional Journey retains full accessible title and map name. No changes to gameplay graph, camera, signing, startup or saves. Scenery still provisional; review physical appearance and performance. Next: Start/Finish landmarks and less uniform region composition.

---

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
