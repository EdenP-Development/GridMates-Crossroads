# GridMates: Crossroads — current status: Board Motion v0.4 / QA1044

**Verified:** November 16, 2026 local (Nov 17 UTC in GitHub workflows).

## Active source
- [`feature/board-motion-v0.4`](https://github.com/PlayerEden/GridMates-Crossroads/tree/feature/board-motion-v0.4), [Draft PR #9](https://github.com/PlayerEden/GridMates-Crossroads/pull/9), stacked on [Turn Flow PR #8](https://github.com/PlayerEden/GridMates-Crossroads/pull/8), Woven Trail PR #7 and Foundation PR #1. **No merge to main** and no public production release.
- 38-node handcrafted Woven Trail with 5 crossroads and one Start/Finish; one human + 1–3 AI. Solo game wins/losses, seeded regional events and shields/Mischief; same QA app identity and stable public **test-only** signer.

## Owner physical QA through QA1040
- Earlier screenshots confirmed a 3-seat human win and a 4-seat AI victory.
- Latest screenshots show **2 wins / 3 completed matches** in menu, then a new 4-seat race in which AI3 and human both gain shields and human reaches the finish first. The screenshots do **not** show the menu counter after that latest win (it should become 3 wins / 4 completed matches).
- Latest owner QA notes: [Full QA1040 match](QA_OWNER_PHONE_1040_FULL_WIN.md). Screenshots are retained in chat, not published in GitHub.
- The new QA1040 dice/encounter UI is visible on the phone, but still screenshots cannot establish animation smoothness, exact match duration or shield consumption on an actual prank.

## QA1044 — implemented
- Numbered player tokens visually move node by node along their actual traversed trail; retreat effects retrace the preceding nodes. Animation is local Compose presentation and has **no influence** over dice/results/events.
- Slightly increased token size and numeric labels; active ring follows the moving position.
- **Inspect map** button opens a larger static, read-only board view and closes back to the match. Animations reset with the GameSession to prevent stale token positions after Rematch.
- [Board design-data QA PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44007530563), [Kotlin/JVM movement tests PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44007580050).
- [Signed APK QA1044 build PASS](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44007530364), [artifact download](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44007530364/artifacts/14295396752); SHA-256 `cf521f23096051eb32cc378c2b5cf1c5d3224d5e7acee31ef54a076a9d18f0a6`, versionCode 1044, source `271eada0aacad22562c7b4a394f6c7263e4ea39a`.
- [**Real Android 15 emulator QA1040→1044 upgrade PASS**](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44008186807): separately built APKs checked by SHA, seeded profile completed=7/wins=3 preserved byte-for-byte, updated app opened and interacted with a board. [Emulator screenshots](https://github.com/PlayerEden/GridMates-Crossroads/actions/runs/44008186807/artifacts/14294789619).
- [Full signed-artifact receipt](QA_RECEIPT_1044_2026_11_16.md).

## Unverified and next
1. **QA1044 owner physical phone test**: ensure piece animation follows routes, setbacks travel backward rather than teleport, numbered token clusters remain legible, Inspect map fits and dismisses, and no animation blocks control input.
2. Confirm **3 wins / 4 matches** after the photographed human victory in QA1040, if no additional matches intervened; check it stays saved after install.
3. Verify old forest 3-way options, leave-confirmation, 2/3/4-seat results, and large-font accessibility.
4. Measure actual match durations and route balance; later add hand-selected woodland artwork and cohesive GridMates tokens while keeping app practical.
5. LAN multiplayer, active match save/resume and other themed boards remain unimplemented.

**Do not merge** PR #9 before the owner has reviewed the visual movement, and do not use the public QA signer for a store/production APK.
