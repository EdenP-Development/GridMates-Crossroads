# Current signing checkpoint

Organization secrets and pinned private-key preflight passed [run 45486186420](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45486186420). First actual APK build [run 45486977715](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/45486977715) failed before signing: sdkmanager not on PATH (exit 127). Corrected SDK discovery using existing QA workflow procedure; fresh build pending. No credential changes, no verified beta APK yet. Existing QA remains unchanged. Owner backup custody unverified.

# First private itch.io beta identity — 2026-11-24

Owner confirmed no prior public Crossroads distribution. Existing internal QA identity unchanged. Reserved beta package com.playereden.gridmates.crossroads. First private beta PKCS12 key generated locally, RSA 3072, SHA256withRSA, 10958-day validity. Local signing probe passed and base64 round trip verified. Public certificate SHA256: e13d5b336c01a7ca4f29e83072ac304adc3a4233cf484b43268a88aec3f28914. Alias crossroadsBeta (non-secret).

Organization secret names GRIDMATES_CROSSROADS_BETA_KEYSTORE_BASE64 and GRIDMATES_CROSSROADS_BETA_KEYSTORE_PASSWORD; owner restricts access to this repository only. Values supplied privately as downloadable files, never repository content. Owner must save encrypted keystore and password separately in owner-controlled vault plus independent encrypted offline backup and verify recovery. Do not rely on chat file retention. Owner custody and GitHub secret availability not yet verified.

Preflight .github/workflows/crossroads-beta-signing-preflight.yml uses pinned certificate and signs/verifies a temporary JAR on the specific trusted staging branch; no APK built or distributed, no key artifact. Initial push may fail until owner sets secrets; trigger a fresh trusted branch workflow revision after setup. Production APK signing implementation, exact APK receipt, beta update tests and owner release approval remain outstanding. Preserve current QA package/cert; do not rotate first beta key after establishment. Google Play deferred. Follow central standards https://github.com/EdenP-Development/Eden-Development-Standards.

Active development remains integration/beta-1-polish; signing staging is separate. QA1707 pre-transfer passed, post-transfer full QA pending. Artwork transfer remains outstanding. No main merge.
