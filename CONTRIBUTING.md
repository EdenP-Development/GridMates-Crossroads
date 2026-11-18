# Working agreement for GridMates: Crossroads

- Branch for implementation changes and create a PR. Keep `main` as the documentation-approved stable baseline.
- The project owner must approve publishing, release APK/AAB, mass asset imports, or merging unfinished feature work.
- Do not alter Sudoku or Word Search source repositories to support Crossroads without separate explicit authorization.
- Every meaningful gameplay change includes updated rules or a decision-log entry, tests, and notes about player-facing consequences.
- Proposed rule values are not confirmed owner decisions. Preserve the difference in docs.
- Tests must be reported honestly; no emulator-only LAN claims, no made-up APK/CI results.
- Prefer reusable code-drawn geometry and limited verified assets over unbounded art generation.
- No external secret, keystore, API key, or personal data in source control.
- No automatic cross-app saves or unlocks. Crossroads uses its own persistent IDs and achievement logic.
- Do not add accounts, analytics SDKs, remote matchmaking, currency economies or daily engagement obligations absent a separate decision.