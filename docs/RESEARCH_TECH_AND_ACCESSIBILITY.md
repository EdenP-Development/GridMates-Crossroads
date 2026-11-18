# Android / LAN / accessibility research (2026-11-16)
## Android and local networking
- Android has Network Service Discovery (NSD) APIs for discovering services on the same network. Official overview: https://developer.android.com/develop/connectivity/wifi ; reference: https://developer.android.com/reference/android/net/nsd/NsdManager
- **Android 17 (API 37) apps targeting API 37+** have enforced local network protections. Review privacy-preserving service selection/pickers before requesting broad `ACCESS_LOCAL_NETWORK`. https://developer.android.com/privacy-and-security/local-network-permission
- Google Play introduced a 2026 Local Network Permission policy with core-functionality limits and a 2027 declaration timeline. Reconfirm when releasing. https://support.google.com/googleplay/android-developer/answer/16935362
- Same-Wi-Fi four-player turns are feasible; test host disconnect, join failures, permission denial/revocation, protocol versions and actual 2–4 devices. Don't claim LAN tested based on an emulator.

## Proposed platform
Native Android Kotlin + Jetpack Compose is a candidate aligned with GridMates: Sudoku's existing stack; confirm stable branch and quality before reusing any modules. Keep a platform-independent rules engine and immutable match actions.
- Avoid silently reusing package IDs, save keys, signing information or network protocol from Sudoku.
- Host-authoritative dice/events + validated actions + versioned snapshots; don't trust client rolls.
- Local data must survive backgrounding, interrupted writes, upgrades, process death; use schema versions and atomic persistence.

## Accessibility / UX QA
- Large touch targets at intersections, zoom/pan without losing orientation, readable risk labels on small screens, distinguish players without relying on color alone.
- Reduce-motion option; text equivalent for icons; scalable typography; visible current turn; respectful nonviolent language.
- Older / less technically confident players should understand host/join without technical setup, but pass-and-play can provide a simpler offline social mode.

## Evidence standards
Plans are not implemented features. Gate every LAN claim on physical-device test evidence; game balance on measured match runs; SDK/Play policy claims on updated primary docs.
