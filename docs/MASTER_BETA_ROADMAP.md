> **Current repository:** [EdenP-Development/GridMates-Crossroads](https://github.com/EdenP-Development/GridMates-Crossroads). Ownership transfer verified; existing history and QA identity retained. See [migration audit](EDENP_MIGRATION.md). Active branch `integration/beta-1-polish`; QA1707 last verified pre-transfer build. Fresh organization build and private beta signing pending.

# Crossroads master development roadmap — 2026-11-24

Owner endorsed continuing with the proposed staged beta roadmap. Scope below is planning, not locked release dates or authorization to publish. Prefer bundled meaningful milestones with automated QA and physical acceptance evidence over repeated tiny changes.

## Beta 1 — visual and mechanical foundation (current)
- Complete transfer of exact existing 12 environment and 24 UI assets. Source library stays separate from runtime; verify provenance, alpha, state bounds, text contrast and stretch. Do not regenerate approved art merely to work around transport.
- Shared buttons/selection/utility controls and typed icon loader passed QA1706 with native fallbacks; dice decorative icon connected. Next connect other existing map/zoom/settings/navigation controls only where suitable, then reusable panels/frames. Extract remaining source artwork as needed, retain reserve library.
- Actual illustrated build, visual QA, traveler portraits, encounter/victory art. No claim of actual rendering until packaged binaries verified.
- Meaningful authored Classic/Woven course redesign (new immutable board ID), no filler midpoint expansion. Quick/Long pacing review, AI/finish fairness and human playtests. Existing rules default; experiments opt-in.
- Real two-phone trusted Wi-Fi discovery, race, mixed AI/humans, reconnect/rematch; improve error messages. Plaintext LAN is a production security risk.

## Beta 2 — progression and personality
- Local profile, match history, achievements, traveler/cosmetic collections, customization, natural gameplay rewards and optional shareable profile.
- Expand encounter variety, risk/reward choices, replayability. Avoid grind, mandatory daily chores, pay-to-win, accounts and forced cloud.
- Optional music, woodland ambience, die/movement/encounter/victory/button feedback, independent audio preferences. Audio assets require provenance/licensing; no presumed sibling reuse.
- Versioned portable save/export migration coverage for new progression and preferences.

## Beta 3 — complete game experience
- Content expansion, balance and human pacing, accessibility (large text/TalkBack), multiplayer refinement and full game QA. Online matchmaking deferred rather than initial launch dependency.
- Audit device sizes, performance, memory, art consistency, security/privacy and restore/recovery.

## Release candidate
Follow https://github.com/PlayerEden/Eden-Development-Standards and local EDEN_STANDARDS_ADOPTION.md. Verify actual previous distributed APK -> candidate without uninstall, exact package/cert/increasing version/APK hash, save and preferences coverage, owner-approved durable artifact and receipt, private signing backup and independent recovery. QA public test key is not production suitable. Production identity/custody unverified, no replacement key generation, no main merge or publication without owner approval.

## Evidence and next work
QA1706 source head 638f6b65fc26a9b5897548ea04d04fbe3e710b17, built merge 6743a8f1b021bb6b77cb41ac53ec1f8ba596330c; Android run 45467623391 success. Artifact 14957455551 expires Dec 8. APK hash not independently retrieved; archive digest is not APK digest. Prior QA1705 passed shared controls. 36 new artwork binaries remain external; see IDENTITY_10_ART_IMPORT.md. Latest physical acceptance and upgrade unverified. No measured completion percentage.
