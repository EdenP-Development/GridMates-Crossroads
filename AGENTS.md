# GridMates: Crossroads — contributor/agent guardrails
1. First inspect live GitHub branch, [docs/CURRENT_STATUS.md](docs/CURRENT_STATUS.md), [docs/GAME_BIBLE.md](docs/GAME_BIBLE.md) and [docs/DECISION_LOG.md](docs/DECISION_LOG.md). Historical chat claims and generated concepts are not executable build evidence.
2. Keep Crossroads simple, cozy and competitive. One shared Start and Finish; consequential forks; handcrafted recognizable topology plus controlled regional event randomness.
3. Prioritize functioning match/AI and reproducible Android QA before cosmetic artwork. Shared original GridMates characters are **source art only**; unlocks/saves remain independent.
4. Android package/signing rules: compare **actual APK** signer, package, code, checksum and saved state; do not rotate keys or reset user data casually. Public QA-only key is *never* production signing. See [APK policy](docs/APK_UPDATE_POLICY.md).
5. One consolidated test workflow and one Android QA APK workflow; don't create one job/workflow per feature. No need for many checkpoint branches; preserve milestones with refs/receipts.
6. Record what is SOURCE-ONLY vs JVM-TESTED vs EMULATOR-TESTED vs OWNER-PHONE-TESTED vs LAN-TESTED. Never invent an APK download.
7. Work in branch/PR; do not merge, release, alter sibling repos, import masses of art or publish publicly without owner direction.

8. **GridMates identity:** [Classic Games Charter](docs/GRIDMATES_CLASSIC_GAMES_CHARTER.md) — modern versions of accessible classic newspaper/puzzle/tabletop games. Simple for players **and** low-maintenance for this small AI-assisted development team. One satisfying core loop, offline first, avoid RPG/economy/progression complexity unless explicitly approved.
9. Prefer tangible, small functionality such as fully tested Pass & Play over a premature generic networking framework. Same-Wi-Fi multiplayer stays a separate real-device milestone; never call single-device Pass & Play "LAN".
10. Scope creep requires a clear one-sentence player benefit and its test/maintenance cost. Games may be considered finished after polished core play; do not add daily quests, currency, energy, battle passes or forced accounts by default.
