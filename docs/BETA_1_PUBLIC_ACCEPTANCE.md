# Beta 1 public candidate — physical acceptance and screenshot plan

Prepared November 28, 2026. All device tests below are NOT RUN on the actual privately signed Beta 1 candidate. Do not substitute QA or disposable-signer rehearsal results. Frozen release source f99409374beb652f3661e872813c8bd392e57427 passed rehearsal #7 and Android #1101; actual signing pending.

## Before testing
Record APK source commit, versionCode, versionName, package, signer and APK SHA256 from verified receipt; device model, Android version, orientation and font/display scale. Expected public package com.playereden.gridmates.crossroads, private certificate e13d5b336c01a7ca4f29e83072ac304adc3a4233cf484b43268a88aec3f28914. Do not install a rehearsal APK or publish QA. Export and retain existing progress before any installation or import. Never uninstall to resolve an update conflict. Do not disable Play Protect.

## Owner quick pass (one sitting, report PASS/FAIL/NOT RUN)
| ID | Test | Expected |
| --- | --- | --- |
| P01 | Install actual public APK and open with airplane mode enabled | Correct name/icon; no account, network or Nearby prompt; no crash |
| P02 | Inspect home and select Quick, Classic, Long | Only standard courses and original rules; no experimental controls |
| P03 | Start standard Solo race; roll, move, select a fork | Responsive input, active pawn visible, valid choices, no obstructed controls |
| P04 | Pan/recenter, open full map during route choice, return | Board fit and readable markers; pending choice preserved |
| P05 | Play events, inspect journal and continue | Clear event outcome; no soft lock, understandable history |
| P06 | Background and reopen during active race; force-stop only after normal save | Same saved race and record; no duplicate completion |
| P07 | Complete one Quick Solo race; review results, rematch | Correct winner, record increments once; rematch works |
| P08 | Start 2-player Pass & Play and hand off turns; finish a race | Correct active player, no unintended AI seat or blocked turn |
| P09 | Export backup to chosen private location, validate import on disposable test data | Confirmation; compatible save and stats restored; cancel leaves state unchanged |
| P10 | Large font/display scale, portrait/landscape where supported | Essential controls legible and usable; no blocking clip |
| P11 | Play longer Classic/Long race | No unacceptable lag, memory crash, camera jitter or stuck AI |
| P12 | Test 3–4 player Pass & Play and additional Solo seats | Turn order, outcomes and rematch coherent |

## Separate update gates
U01: clean public install; save active original race and nonzero stats. U02: install a later privately signed public APK with greater versionCode and same package/cert over it; verify active race, stats, settings and continue. Requires two real signed public builds; not satisfied by rehearsal or QA upgrade. U03: export compatible original-rules QA backup; import into public app only after exporting public progress; verify save/stats. U04: reject incompatible experimental or malformed backup without changing existing progress. Preserve originals. No downgrade or uninstall. Keep existing QA app separate.

## Screenshots (capture during P02/P03/P04/P07; no extra race needed)
S01 home with standard course selector; S02 ordinary race with illustrated pieces and visible roll/route controls; S03 full map of a standard course; S04 completed race/results; optional S05 Pass & Play turn. Native screenshots, no overlays, no QA label, no mockups, no personal notifications or names. Keep originals; do not crop away relevant controls or falsely present promotional art as gameplay. Record version and test ID. Upload to private owner storage and link in marketing PR; no public upload before acceptance. If screenshot upload is unavailable, written feedback is enough for triage, not evidence of screenshot completion.

## Triage
Blocker: launch failure, corrupt/lost progress, wrong signer, unauthorized network permission, gameplay soft lock, incorrect winner/record, or unusable critical control. Stop release, preserve evidence and backup. Major: repeatable performance/legibility/route issue; review before release. Minor: polish issues that do not prevent play; document for later. Report test ID, build, device, expected vs actual, reproduction steps, frequency, screenshot if available. Avoid publishing private backups.

## Release decision
Required: verified real signed receipt, physical acceptance of core flow and update gate, privacy URL/contact, approved assets and screenshots, explicit publication approval. Untested rows remain NOT RUN. Existing QA2072/QA2099 evidence is supplementary only. No runtime change or release approval inferred.
