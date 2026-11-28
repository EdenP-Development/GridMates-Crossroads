# Beta 1 consolidated owner test — QA2072

Runtime frozen for owner feedback. All five visual improvement areas implemented; no new gameplay features.
- Branch feature/identity-17-art-import, draft PR #120.
- Exact source 0192abcef6ef9c0da36fb0e1fa3c7ae206907a0e; PR merge checkout 5d854c0926d842f9970611aa9cbd3f3eacfb4e89.
- Rules #501 run 46106126591 passed; Android #1072 run 46106126584 job 140859166608 passed.
- [Download QA2072](https://github.com/EdenP-Development/GridMates-Crossroads/actions/runs/46106126584/artifacts/15247627332), 20336600-byte ZIP, expires Dec 12 2026 UTC, SHA256 c9edafe6c692f11884ee30afb11b461114727c255c60e1e529c8ed242ff83209.
- Direct CI logs confirm com.playereden.gridmates.crossroads.qa, versionCode 2072, versionName 0.1.0-qa, signer SHA256 75cc3c896247d71fbd51f7a3ed41f90c8218a8a5a70df8c0f6b494b5cc23d419 (same as QA2031). Archive not independently downloaded/inspected. Publicly known QA key: never distribute as public beta.
- Back up progress before installing over older compatible QA. Never uninstall/clear data or downgrade. Record phone and font scale if available.

## Focused retest (all NOT RUN on QA2072)
1. Home initial view, normal course selection, collapsed/expanded QA course picker, selected test course warning, options.
2. Woven overhead presentation: compare bridge width, actual bank coverage and direction, scenery cohesion; note if missing bridges.
3. Start untouched race: active pawn visible, shared-start edge, movement and camera transitions; manually pan, then recenter; note jitter or empty board edges.
4. Steady/Push action panel collapsed/expanded; check detail header and short/large-font clipping; original-rules roll as well.
5. Regional Journey overhead scenery; confirm region identity and contrast.
6. Full map initial fit, legend, pinch/drag, close/back, return to pending route choice; short screen/landscape if available.
7. Complete normal Solo race, results/rematch, pacing and enjoyment; Pass & Play if possible.
8. Close/reopen mid-race and compare state/stats; test backup/import only with safe copy. No screenshot implies full acceptance.
Send screenshots in batches. Preserve observations in repository docs (not raw uploads without need). See QA2031 audit, P01–P12 and A01–A08 for full physical gate scope. Next triage actual feedback; public Storybook vs experimental overhead scope, Nearby security, private signing branch integration, privacy and itch packaging still open. No merge/publication or signing changes.
