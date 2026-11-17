# Technical architecture direction — not implemented
## Keep it simple
Proposed native Android Kotlin/Jetpack Compose shell, with a pure game-rules module independently testable on desktop JVM. Avoid prematurely extracting a giant universal GridMates framework.

### Core model
- `BoardDefinition`: version, nodes, directed edges, drawable coordinates, choice labels, region IDs and entry/finish.
- `EventDefinition`: ID, eligible regions, trigger and deterministic effect, limits, weight.
- `MatchConfig`: course ID, match seed, seats/bots, optional rule variation, version.
- `GameState`: board ID, seat positions, current seat, turn number, shields, resolved action IDs, random state, winner and event history.
- `Action`: Roll, ChooseRoute, ChooseEventOption, ChooseTarget, Continue/EndTurn. Validate legal actions in a single reducer.

### Rules
`reduce(state, action, rng) -> state + eventLog`. Only deterministic, validated actions advance state. No Android, rendering or sockets in game rules. Prevent duplicate event resolution; prevent event-induced movement from endlessly triggering events. Tests should replay an entire match from seed plus actions.

### Renderer
Compose/Canvas or other native 2D route display. Background and paths are separate layers. Touch a highlighted outgoing route at a junction; no tiny arrow targets. Tokens are simple 2D art. Theme swaps do not change board graph data.

### Offline and AI
Solo supports 1 human with 1–3 bots; pass-and-play is a later option. Bot must choose legal routes based on path length, known region risk and rival distance. AI difficulty affects choices only, not dice.

### LAN
One Android host owns canonical state and PRNG; up to three joining human devices fill four total seats, with any remaining seats bots. Versioned messages send validated intent, ordered snapshots and revisions. Reject duplicated and out-of-turn requests. Define reconnect, loss of host, timeout and background policy before claiming supported multiplayer.
Android 17 local network access rules: https://developer.android.com/privacy-and-security/local-network-permission

### Persistence
Independent app ID and keys; versioned atomic saves. Preserve local achievements/unlocks across updates; no cross-app writes or shared account. Debug seeds and event logs can be exported only with user action.

### Source reference
Sudoku's architecture guidance is at https://github.com/PlayerEden/SudokuTogether/blob/main/docs/REUSABLE_ANDROID_PUZZLE_FOUNDATION.md — verify stable branch and don't assume modules are already generalized.

### Camera implementation checkpoint (CAM-06 QA1057)
The current Android prototype includes pure 2D camera math in `core/CameraGeometry.kt` (zoom, bounds, pinch anchor and fork focus) plus UI-only Compose gesture and `graphicsLayer` transforms in `MainActivity.kt`. Follow mode tracks **visually animating** pieces, using snapshot movement to avoid jumping ahead at turn changes. The Canvas is clipped to the game board, and action buttons remain outside the transform. Visual-only camera state resets per GameSession; it is not serialized into rules or multiplayer messages. [Camera PR #12](https://github.com/PlayerEden/GridMates-Crossroads/pull/12). Physical device gesture testing remains open.
