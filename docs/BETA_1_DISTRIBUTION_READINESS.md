# Beta 1 distribution readiness

## Verified baseline
Runtime 7e3cddc387c0d8156df32409aaea6321dfbf4660: rules #516 run 46115680081 and Android #1097 run 46115680050 passed. Release candidate branch release/beta-1-candidate. Existing QA2072 remains owner test evidence, not public release. No publication.

## Player instructions
Android 8.0 (API 26) or newer; actual device compatibility and performance require testing. Download the privately signed public APK from the official itch page when published, extract if supplied as ZIP, allow installation from the chosen trusted source if Android requests it, then install. Do not disable Play Protect or bypass security warnings; contact publisher if unexpected. Verify checksum against release receipt where practical. Disable install-from-source permission afterward if desired. Future updates: use same package and signer with higher versionCode; export a backup first and install over existing public app. Do not uninstall, clear storage, or downgrade. QA package is separate and will not automatically transfer progress. Manual transfer of compatible original-rules backup only; experimental races rejected. Import replaces existing progress on confirmation. Keep original backup until verified.

## Privacy and scope
Draft PRIVACY_POLICY.md is specific to offline public edition. Confirm publisher identity, actual contact, effective date and durable public URL before publication; no claim URL is hosted. Verify final signed APK permissions, dependencies, data handling, first launch offline, no Nearby or experimental setup entry points, actual backup/restore. App-specific dynamic receiver permission is not a network permission. Android cloud backup disabled by manifest; device migration behavior may differ. No analytics/ad/account dependency identified in inspected Gradle configuration; full final package audit remains required. Storefront processing is separate. No age-rating or child-directed compliance certification inferred.

## Store assets and release checklist (open)
- Real signed-candidate screenshots: home, standard gameplay, full map, results. No mockups presented as screenshots.
- Cover/launcher artwork, source provenance and redistribution review; final owner visual acceptance.
- Itch page copy: offline Solo vs AI and 2–4 Pass & Play, Quick/Classic/Long, local saves, beta limitations, Android requirements, install/update/backup instructions.
- Confirm pricing (free beta recommended), feedback contact, publisher identity, policy URL and target audience.
- Review signing workflow trust boundary, dispatch path, key custody; build exact reviewed source; verify certificate, version, APK checksum and archive.
- Clean public install and public-to-public update with compatible saved race; QA transfer, invalid transfer, normal race completion and rematch, accessibility/large fonts, performance, map/camera.
- Archive approved APK and receipt durably; explicit owner approval before publish. No public QA signer distribution.

This document is a readiness plan, not a storefront publication or legal review.
