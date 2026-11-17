# GridMates sibling-repository audit: organization and Android APK updates
**Audited:** 2026-11-16 (US Central) · **Status:** source/documentation audit, not an Android build or change to sibling repositories.

## Scope and evidence
- [GridMates: Sudoku](https://github.com/PlayerEden/SudokuTogether), default `main` at `c526e02cb372aada6a54326ccc4a0f1fddc303e5`; also inspected `development` at `3bcdd4dd15185cf5837fc5c0c957db9cd0571a2d`.
- [GridMates: Word Search](https://github.com/PlayerEden/GridMates-Word-Search), `main` at `9043cf4c942da598b5841cc39c361c99abd9ef26`; active cumulative QA branch `qa/ws32-my-puzzles-emulator-2026-11-16` at `183b8845b247a0ca40541f06e517999961600bc9`, [PR #143](https://github.com/PlayerEden/GridMates-Word-Search/pull/143). The `feature/ws32-illustrated-my-puzzles-2026-11-16` branch sits under [PR #142](https://github.com/PlayerEden/GridMates-Word-Search/pull/142).
- Crossroads: [foundation draft PR #1](https://github.com/PlayerEden/GridMates-Crossroads/pull/1), documentation/design only.

**Important:** Word Search's `main` is presently a largely documentation/design skeleton; use a specifically verified feature/QA branch when assessing its Android source, not `main` alone. Sudoku's v1 Beta 1 source was consolidated into `main` on 2026-11-16. Check the *live* repository before citing these as current again.

## Comparative findings

| Concern | Sudoku | Word Search | Crossroads decision |
| --- | --- | --- | --- |
| Canonical source | Integrated Android app on `main`; earlier feature branches preserved | Extensive cumulative stacked draft PRs/branches with most active Android code off `main` | Promote known-good first Android app to `main` early; short-lived PRs |
| Documentation | `CONTINUE_HERE.md`, release documents, delivery plan, milestone QA | `AGENTS.md`, `START_HERE.md`, dated development journal, milestone QA | Compact `START_HERE`, Game Bible, decision log, APK policy, one current status |
| App stack | Native Kotlin/Compose; independent Kotlin/JVM `core` | Native Kotlin/Compose; `core` module | Pure game rules + Compose app; do not copy Sudoku game-specific protocol |
| APK identity | `com.playereden.sudokutogether` production, `.qa` QA | `com.playereden.gridmates.wordsearch` production, `.qa` QA | Unique Crossroads package, independent `.qa` |
| QA signing | Original private key restored from GitHub secret; fail closed if unavailable | Public, intentionally test-only QA key committed to repository | Prefer private durable QA key, GitHub Actions secret + offline backup; production separate |
| Version code | Manual QA workflow reserves `1000 + GITHUB_RUN_NUMBER` to avoid reusing build numbers | Gradle property `qaVersionCode` or branch default (v94 at inspected QA HEAD) | One authoritative monotonic QA allocator, no per-feature independent counters |
| APK integrity | `apksigner` + `aapt` + checksum + JSON receipt | Signer check, `aapt` package check and hash; project-specific asset verification | One required package/signer/version/checksum preflight |
| Update testing | Separate real old→new APK emulator upgrade evidence, plus private save checks | WS05J built v20→21 with same stable QA key, ran `adb install -r`, verified private saves | Compare **actual last accepted artifact** against new candidate, not only two same-source builds |
| Build automation | Small group of manual build/QA workflows and separate artifact-cleanup workflow | Extensive per-milestone workflows (40+ on inspected branch), large art/build pipeline | Two or three consolidated workflows, not one per feature |
| Artifact distribution | Selected manual QA workflow retains candidate 5 days on `main`; production beta via itch.io | Current general Android QA workflow retains ZIP 30 days | CI artifacts temporary; selected owner-tested builds may get approved GitHub pre-release APK |
| Asset reuse | Original portraits / profile icons | SHA-pinned checkout/import of 12 approved Sudoku woodland portraits | Adopt manifest, source SHA, verified derivation, only after rules are fun |
| Rollback | Identity-checked QA baseline + latest verified artifacts | Saved checkpoint branches and receipt docs | Keep known-good APK + source SHA + tested save migration baseline, not dozens of checkpoint branches |

## Particularly useful implementation patterns

### 1. Stable install-over identity
Sudoku [`app/build.gradle.kts`](https://github.com/PlayerEden/SudokuTogether/blob/main/app/build.gradle.kts) and [`qa-debug.yml`](https://github.com/PlayerEden/SudokuTogether/blob/main/.github/workflows/qa-debug.yml) intentionally separate the QA package from production, require the prior QA signer on distributable builds, and reject missing signing material instead of silently generating a different certificate.

The accepted QA identity is recorded in [`distribution/qa-update-baseline.json`](https://github.com/PlayerEden/SudokuTogether/blob/main/distribution/qa-update-baseline.json), and [`scripts/apk_update_preflight.py`](https://github.com/PlayerEden/SudokuTogether/blob/main/scripts/apk_update_preflight.py) verifies package, certificate, version, minSdk and file digest before producing a receipt. Its receipt explicitly states that install-over and save retention are **not** proven by these metadata checks.

Word Search's [stable QA update report](https://github.com/PlayerEden/GridMates-Word-Search/blob/183b8845b247a0ca40541f06e517999961600bc9/docs/WS05J_STABLE_QA_UPDATES_2026-11-07.md) records the earlier debug-signing mismatch and one-time backup/uninstall/restore migration. **Crossroads should avoid this painful initial key migration** by setting signer/QA ID before first shared APK.

### 2. Real saved-data update rehearsal
Word Search's [WS05J workflow](https://github.com/PlayerEden/GridMates-Word-Search/blob/183b8845b247a0ca40541f06e517999961600bc9/.github/workflows/ws05j-qa-upgrade.yml) installs a seeded baseline, updates it in-place with a higher-version APK and verifies private data. Sudoku's [Beta 1 release report](https://github.com/PlayerEden/SudokuTogether/blob/main/docs/GRIDMATES_V1_BETA_RELEASE.md) documents an old-artifact-to-new-artifact update and explicitly lists its acceptance gaps.

Crossroads should eventually run **last accepted QA APK → candidate APK** tests, with saved profile, character ownership/equips and active match fixture. Also test process death and app restart. Do **not** declare a physical-device pass based on emulator runs.

### 3. Independent asset source/provenance
Word Search's [`ws01-android-qa.yml`](https://github.com/PlayerEden/GridMates-Word-Search/blob/183b8845b247a0ca40541f06e517999961600bc9/.github/workflows/ws01-android-qa.yml) checks out 12 approved Sudoku Woodland portraits at a **specific immutable source commit** and verifies their import. This is excellent provenance discipline, though Crossroads should eventually copy the small approved token assets into its own versioned manifest/build with hash checks rather than requiring a giant external source pipeline every build.

### 4. Documentation and handoff
Sudoku's [`CONTINUE_HERE.md`](https://github.com/PlayerEden/SudokuTogether/blob/main/CONTINUE_HERE.md) is a clear single entry point. Word Search's [`AGENTS.md`](https://github.com/PlayerEden/GridMates-Word-Search/blob/183b8845b247a0ca40541f06e517999961600bc9/AGENTS.md) insists on date/source/build/evidence/fallback tracking. Borrow both in a **short** handoff, with one current status and concise per-milestone release receipts.

## Problems to avoid

1. **Huge stacked PR chains:** Word Search has many open, interdependent feature PRs and numerous historical branches. The cost is uncertainty over which branch is authoritative and whether old evidence applies to new code. Use normal merge-to-`main` after acceptance; tag milestones, don't spawn a checkpoint branch for every change.
2. **One Android workflow for every feature:** Word Search's branch contains more than forty milestone-specific workflows, and its general QA workflow rebuilds a large history of art assets. Crossroads should have `test.yml` + `android-qa.yml` + optional `release.yml`, with module-specific tests—not dozens of separate workflows.
3. **Stale 'latest' READMEs:** Word Search's README still starts with November 14/v75 material while current feature work advances further. Keep an always-updated 10-line `CURRENT_STATUS.md` or README top section; archive old milestone notes elsewhere.
4. **Workflow artifacts as permanent share links:** GitHub Actions artifacts expire (5 days for Sudoku's current QA flow, 30 days for the Word Search branch's general Android QA). GitHub requires signed-in read access to download artifacts, even from public repositories. See [GitHub docs](https://docs.github.com/en/actions/how-tos/manage-workflow-runs/download-workflow-artifacts). Consider selected pre-release APK assets only after explicit owner approval; keep test-only and public/production signing distinct.
5. **A public QA keystore as release key:** Word Search intentionally commits a *test-only* key. No actual production signing secret belongs in public source. Use separate private signing credentials for Crossroads production and make the QA chain fail closed.
6. **Treating green CI as owner install success:** Both repositories carefully distinguish source/unit/emulator checks from real handset, multi-device and visual approval. Preserve that distinction.

## Concrete Crossroads repository recommendation

```text
.github/workflows/
    tests.yml                    # code/board validation on PR
    android-qa.yml               # manual owner-testable signed APK
    publish-qa.yml               # later, owner-approved pinned prerelease only
app/                              # Compose Android app
core/                             # Android-free deterministic rules + bot AI
assets/
    token-manifest.json          # source hashes / provenance
data/
    boards/
    events/
distribution/
    qa-update-policy.json        # package/certificate/min-sdk/accepted baseline
docs/
    START_HERE.md
    GAME_BIBLE.md
    DECISION_LOG.md
    CURRENT_STATUS.md
    APK_UPDATE_POLICY.md
    RESEARCH_*.md
    QA_AND_PLAYTEST.md
scripts/
    apk_preflight.py
    verify_install_over.sh
```

**Branch policy:** `main` = last integrated stable source; `feature/*` or `fix/*` = short-lived work; `qa/*` only for temporary test harness; `v0.x` tags as stable source checkpoints. Main and checkpoint tags are source references, not proof an APK was tested.

**Release policy:** do not create a public GitHub Release or production package automatically. Preserve exact last accepted owner APK and SHA; only make an explicitly approved tested QA candidate easy to download.

## Proposed APK updating design for Crossroads
See [APK Update Policy](APK_UPDATE_POLICY.md). The aim is to install a new Crossroads QA APK **on top of the last one**, preserving all local records and cosmetics, with a reliable fallback and no signing surprises.

## External official references
- [Android: how app updates work](https://developer.android.com/google/play/app-updates): package and certificate identity, valid rotation, same-or-higher version code for platform acceptance; use strictly increasing codes in our own CI.
- [Android: version your app](https://developer.android.com/studio/publish/versioning): use increasing version codes and distinct Play release numbers.
- [Android: app signing](https://developer.android.com/studio/publish/app-signing): key custody and eventual Play signing.
- [GitHub: downloading Actions artifacts](https://docs.github.com/en/actions/how-tos/manage-workflow-runs/download-workflow-artifacts): signed-in read access and expiry.

**Audit limitation:** Read repository files/branch metadata, but did not independently run their existing Android workflows, open previously produced APK binaries, or perform physical installations. No sibling source was modified.
