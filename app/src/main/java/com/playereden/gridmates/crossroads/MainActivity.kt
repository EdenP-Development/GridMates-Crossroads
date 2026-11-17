package com.playereden.gridmates.crossroads

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.playereden.crossroads.core.*
import kotlinx.coroutines.delay

private val Forest = Color(0xFF48684D)
private val Cream = Color(0xFFF7F0DE)
private val Bark = Color(0xFF80664B)
private val PlayerColors = listOf(Color(0xFFC75C49), Color(0xFF3D729F), Color(0xFF729B60), Color(0xFFB48A36))
private enum class BoardCameraMode { FOLLOW, MANUAL }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
        val board = GameData.board(assets.open("boards/woodland_woven_trail.json").bufferedReader().use { it.readText() })
        val catalogue = GameData.events(assets.open("events/woven_events_v02.json").bufferedReader().use { it.readText() })
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Forest, onPrimary = Color.White, surface = Cream,
                    background = Cream, onSurface = Color(0xFF302E28), secondary = Bark
                )
            ) {
                CrossroadsApp(board, catalogue)
            }
        }
    }
}

@Composable
private fun CrossroadsApp(board: BoardDefinition, catalogue: EventCatalogue) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("crossroads_qa_profile_v1", 0) }
    val recoveryStore = remember(prefs, board, catalogue) {
        MatchRecoveryStore(prefs, board, catalogue)
    }
    // A restarted Activity gets its entire authoritative match from the saved
    // seed + accepted actions; incompatible/corrupt saves never half-restore.
    val initialRecovery = remember(recoveryStore) { recoveryStore.load() }
    var matches by remember { mutableIntStateOf(prefs.getInt("completed", 0)) }
    var wins by remember { mutableIntStateOf(prefs.getInt("wins", 0)) }
    var session by remember { mutableStateOf(initialRecovery.match?.game) }
    var activeMatchId by remember { mutableStateOf(initialRecovery.match?.matchId ?: "") }
    var matchStartedAtEpochMillis by remember {
        mutableLongStateOf(initialRecovery.match?.startedAtEpochMillis ?: 0L)
    }
    var startedFromCheckpoint by remember {
        mutableStateOf(initialRecovery.match != null)
    }
    var recoveryWarning by remember {
        mutableStateOf(
            if (initialRecovery.invalidSavedMatch)
                "The previous journey couldn't be restored. Starting a new one will replace that save."
            else null
        )
    }
    var revision by remember { mutableIntStateOf(0) }
    val game = session
    val state = remember(game, revision) { game?.snapshot() }
    // QA wall-clock timer intentionally continues while away from the app.
    // The start timestamp is persisted, unlike elapsedRealtime().
    var showJournal by remember(game) { mutableStateOf(false) }
    var showMatchOptions by remember(game) { mutableStateOf(false) }
    var showGuide by remember(game) { mutableStateOf(false) }
    var showResults by remember(game) { mutableStateOf(false) }
    var resultsPresented by remember(game) { mutableStateOf(false) }
    var finishedElapsedSeconds by remember(game) { mutableLongStateOf(0L) }
    var confirmExit by remember(game) { mutableStateOf(false) }
    var inspectBoard by remember(game) { mutableStateOf(false) }
    var showBoardGestureHint by remember { mutableStateOf(!prefs.getBoolean("board_gesture_hint_seen_v1", false)) }
    var rollingDie by remember(game) { mutableStateOf(false) }
    var speed by remember {
        mutableStateOf(
            runCatching {
                PresentationSpeed.valueOf(prefs.getString("match_pace_v1", "COZY") ?: "COZY")
            }.getOrDefault(PresentationSpeed.COZY)
        )
    }
    var transitionDelay by remember(game) { mutableLongStateOf(0L) }
    var turnReady by remember(game) { mutableStateOf(true) }
    var cameraTrackIndex by remember(game) { mutableIntStateOf(game?.currentIndex ?: 0) }
    var routeDialogDismissed by remember(game) { mutableStateOf(false) }
    LaunchedEffect(game, state?.revision) { routeDialogDismissed = false }
    fun persistGame(current: GameSession) {
        val saved = runCatching {
            recoveryStore.persist(current, activeMatchId, matchStartedAtEpochMillis)
        }.getOrDefault(false)
        if (!saved) {
            recoveryWarning = "Progress couldn't be saved on this device. Please check storage before leaving."
        } else {
            matches = prefs.getInt("completed", 0)
            wins = prefs.getInt("wins", 0)
            recoveryWarning = null
        }
    }
    fun leaveGame() {
        val cleared = recoveryStore.discard()
        if (!cleared) {
            recoveryWarning = "The saved journey couldn't be cleared; it may reopen on restart."
        }
        session = null
    }
    var rollingFace by remember(game) { mutableIntStateOf(1) }
    val rollingTilt by animateFloatAsState(
        targetValue = if (rollingDie) {
            when (rollingFace % 4) { 0 -> -12f; 1 -> 9f; 2 -> 13f; else -> -8f }
        } else 0f,
        animationSpec = tween(durationMillis = 90),
        label = "dice-wobble"
    )
    var recentReveal by remember(game) { mutableStateOf<EncounterNote?>(null) }
    val latestNote = state?.encounters?.lastOrNull()
    LaunchedEffect(game, latestNote) {
        if (latestNote != null && !(startedFromCheckpoint && revision == 0)) {
            recentReveal = latestNote
            delay(2100)
            recentReveal = null
        }
    }
    LaunchedEffect(game, rollingDie) {
        if (game != null && rollingDie) {
            // Visual dice cycling is presentation only: the rules engine draws the
            // real die exactly once after the animation; no new RNG affects matches.
            // Capture the pace for the full animation, even if the user
            // switches Cozy/Brisk while a roll is in progress.
            val rollSpeed = speed
            val previewFaces = intArrayOf(2, 5, 1, 6, 3, 4)
            repeat(rollSpeed.dieFrames) { step ->
                // These deterministic preview pips are decorative only; the
                // authoritative result is drawn once by GameSession.roll().
                rollingFace = previewFaces[step % previewFaces.size]
                delay(rollSpeed.dieFrameDelayMillis(step))
            }
            if (game.phase == Phase.ROLL && !game.players[game.currentIndex].isBot) {
                val before = game.snapshot()
                game.roll() // Exactly one real engine roll; visual faces use no RNG.
                persistGame(game)
                cameraTrackIndex = PresentationPacing.mover(before, game.snapshot())
                transitionDelay = PresentationPacing.waitForMovement(before, game.snapshot(), speed)
                turnReady = false
                revision++
            }
            rollingDie = false
        }
    }
    val actionScroll = rememberScrollState()
    // The lower control pane changes height between roll, route choice and results.
    // Reset its position on each new action so the title/options don't open half-clipped.
    LaunchedEffect(game, state?.revision) {
        // Also resets when a die roll reaches a second fork in the same turn.
        actionScroll.scrollTo(0)
    }

    LaunchedEffect(game, revision) {
        if (game != null && state != null) {
            // Let the animated piece reach its authoritative destination before
            // continuing the next turn or accepting a new crossroads choice.
            if (transitionDelay > 0L) delay(transitionDelay)
            cameraTrackIndex = state.playerIndex // hand over focus after movement is visible
            turnReady = true
            if (state.phase != Phase.FINISHED && state.players[state.playerIndex].isBot) {
                delay(speed.aiBreathMillis)
                val before = game.snapshot()
                game.botStep()
                persistGame(game)
                cameraTrackIndex = PresentationPacing.mover(before, game.snapshot())
                transitionDelay = PresentationPacing.waitForMovement(before, game.snapshot(), speed)
                turnReady = false
                revision++
            }
        }
    }
    // Completion stats are written once in the *same committed transaction* as
    // the final action by MatchRecoveryStore. Recomposition/recovery cannot
    // double-count a finished match.
    // Freeze elapsed time when final movement has settled: reopening the results
    // must not make the match duration continue growing.
    LaunchedEffect(game, state?.phase, turnReady) {
        if (game != null && state?.phase == Phase.FINISHED && turnReady && !resultsPresented) {
            finishedElapsedSeconds = ((System.currentTimeMillis() -
                matchStartedAtEpochMillis) / 1000L).coerceAtLeast(0L)
            resultsPresented = true
            showResults = true
        }
    }
    fun start(seats: Int) {
        val newGame = GameSession(board, catalogue, seats = seats,
            seed = java.util.Random().nextInt(), humanIndices = setOf(0))
        activeMatchId = java.util.UUID.randomUUID().toString()
        matchStartedAtEpochMillis = System.currentTimeMillis()
        startedFromCheckpoint = false
        persistGame(newGame)
        session = newGame
        revision++
    }
    fun update(block: () -> Unit) {
        val before = game?.snapshot()
        block()
        if (before != null && game != null) {
            persistGame(game)
            cameraTrackIndex = PresentationPacing.mover(before, game.snapshot())
            transitionDelay = PresentationPacing.waitForMovement(before, game.snapshot(), speed)
            turnReady = false
        }
        revision++
    }

    // The OS Back action follows the same protection as Leave Match. Dialogs
    // (including the 3-way junction) own Back while they are visible.
    val threeWayDialogVisible = game != null && state?.phase == Phase.ROUTE &&
        state.playerIndex == 0 && game.availableRoutes().size > 2 && turnReady &&
        !routeDialogDismissed && !inspectBoard && !confirmExit && !showGuide
    BackHandler(
        enabled = game != null && state != null &&
            !threeWayDialogVisible && !showGuide && !showJournal &&
            !showResults && !inspectBoard && !confirmExit && !showMatchOptions
    ) {
        if (state?.phase == Phase.FINISHED) {
            leaveGame()
        } else {
            confirmExit = true
        }
    }

    if (game == null || state == null) {
        CrossroadsHomeScreen(
            wins = wins,
            completed = matches,
            onStart = { seats -> start(seats) }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(Cream)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CROSSROADS · QA", style = MaterialTheme.typography.labelLarge, color = Forest)
                TextButton(onClick = {
                    speed = if (speed == PresentationSpeed.COZY)
                        PresentationSpeed.BRISK else PresentationSpeed.COZY
                    prefs.edit().putString("match_pace_v1", speed.name).apply()
                }) {
                    Text(if (speed == PresentationSpeed.COZY) "Pace: Cozy" else "Pace: Brisk",
                        style = MaterialTheme.typography.labelMedium)
                }
            }
            val displayedSeat = (if (!turnReady) cameraTrackIndex else state.playerIndex)
                .coerceIn(state.players.indices)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.players.forEachIndexed { i, player ->
                    val isFocused = i == displayedSeat && state.phase != Phase.FINISHED
                    val seatStatus = when {
                        state.phase == Phase.FINISHED && state.winnerIndex == i -> "finished first"
                        state.phase == Phase.FINISHED -> "still on the trail"
                        isFocused && !turnReady -> "currently moving"
                        isFocused -> "current player"
                        else -> "waiting"
                    }
                    Surface(
                        modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {
                            contentDescription = "Player ${i + 1}, ${player.name}, $seatStatus" +
                                if (player.shield) ", shielded" else ""
                        },
                        color = if (isFocused)
                            Color(0xFFE6E9D6) else Color(0xFFF0E9D9),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(12.dp).background(PlayerColors[i], androidx.compose.foundation.shape.CircleShape))
                            Spacer(Modifier.width(5.dp))
                            Text(player.name, fontSize = 12.sp, fontWeight =
                                if (isFocused) FontWeight.Bold else FontWeight.Normal)
                            if (player.shield) Text(" 🛡", fontSize = 11.sp)
                        }
                    }
                }
            }
            // Size the board to the actual portrait viewport above the controls.
            // On ordinary screens the parent does not steal one-finger board drags.
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val boardChrome = if (showBoardGestureHint) 152.dp else 84.dp
                val boardSpace = maxHeight - boardChrome
                val fittedBoardHeight = boardSpace.coerceIn(220.dp, 520.dp)
                val needsScroll = boardSpace < 220.dp
                Column(
                    modifier = Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState(), enabled = needsScroll)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Woodland Crossing", fontWeight = FontWeight.Bold, color = Forest,
                        style = MaterialTheme.typography.titleMedium)
                    Box {
                        IconButton(
                            onClick = { showMatchOptions = true },
                            modifier = Modifier.size(48.dp).semantics {
                                contentDescription = "Match options"
                            }
                        ) { Text("⋯", fontSize = 25.sp, color = Forest) }
                        DropdownMenu(
                            expanded = showMatchOptions,
                            onDismissRequest = { showMatchOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("How to play") },
                                onClick = { showMatchOptions = false; showGuide = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Board overview") },
                                onClick = { showMatchOptions = false; inspectBoard = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Journey journal (${state.encounters.size})") },
                                enabled = state.encounters.isNotEmpty(),
                                onClick = { showMatchOptions = false; showJournal = true }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(if (state.phase == Phase.FINISHED) "Main menu"
                                        else "Leave match", color = Bark)
                                },
                                onClick = {
                                    showMatchOptions = false
                                    if (state.phase == Phase.FINISHED) leaveGame()
                                    else confirmExit = true
                                }
                            )
                        }
                    }
                }
                if (showBoardGestureHint) {
                    Surface(
                        color = Color(0xFFE6E9D6),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, top = 5.dp, bottom = 5.dp, end = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Explore the board", fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium, color = Forest)
                                Text("Pinch to zoom · Drag to explore · Double-tap to follow",
                                    style = MaterialTheme.typography.bodySmall, color = Bark)
                            }
                            TextButton(onClick = {
                                showBoardGestureHint = false
                                prefs.edit().putBoolean("board_gesture_hint_seen_v1", true).apply()
                            }) { Text("Got it") }
                        }
                    }
                }
                // The event reveal already appears in the fixed action pane.
                // Keep the complete playable board visible, with history in Match options.
                BoardCanvas(board, state, height = fittedBoardHeight, matchKey = game,
                    stepMillis = speed.stepMillis, focusPlayerIndex = cameraTrackIndex,
                    isTurnTransitioning = !turnReady,
                    startAtCurrent = startedFromCheckpoint)
                }
            }
            Surface(color = Color(0xFFECE3CE), tonalElevation = 2.dp, shadowElevation = 5.dp,
                modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    val round = (state.turn - 1) / state.players.size + 1
                    val currentName = state.players[state.playerIndex].name
                    val trackedName = state.players[
                        cameraTrackIndex.coerceIn(state.players.indices)
                    ].name
                    Text(
                        if (state.phase == Phase.FINISHED && !turnReady) "Approaching the finish…"
                        else if (state.phase == Phase.FINISHED) "Journey complete!"
                        else if (!turnReady && !rollingDie) {
                            if (cameraTrackIndex == 0) "You're on the move…"
                            else "$trackedName is on the move…"
                        }
                        else if (state.playerIndex == 0) "Round $round · Your turn"
                        else "Round $round · $currentName's turn",
                        fontWeight = FontWeight.Bold, color = Forest, fontSize = 16.sp,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                    val initialInstructions = state.turn == 1 &&
                        state.phase == Phase.ROLL && state.die == null &&
                        state.encounters.isEmpty()
                    Text(
                        if (initialInstructions) "Roll the die to begin your journey."
                        else "Latest action · ${state.lastMessage}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (state.phase != Phase.FINISHED) {
                        AnimatedContent(
                            targetState = recentReveal,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "encounter-reveal"
                        ) { note ->
                            if (note != null) {
                                EncounterRibbon(note)
                            }
                        }
                    }
                    // Snapshot is provided by the core module, so cache its nullable
                    // die value locally for Kotlin's safe smart cast.
                    val resolvedDie = state.die
                    if (resolvedDie != null && state.phase != Phase.FINISHED) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DieFace(resolvedDie, modifier = Modifier.size(34.dp))
                            Text(
                                "${state.remainingSteps} ${if (state.remainingSteps == 1) "step" else "steps"} remaining",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (state.phase == Phase.ROUTE && state.playerIndex == 0 &&
                        game.availableRoutes().size > 2) {
                        Text("Three paths ahead · choose in the expanded route panel",
                            style = MaterialTheme.typography.labelSmall, color = Bark)
                    }
                    // Keep the turn title and die visible while long choice lists scroll.
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 230.dp)
                            .verticalScroll(actionScroll),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                    if (state.phase == Phase.FINISHED && !turnReady) {
                        Text("Crossing the finish line…", color = Forest,
                            style = MaterialTheme.typography.bodyMedium)
                    } else if (state.phase == Phase.FINISHED) {
                        val winner = state.winnerIndex?.let { state.players.getOrNull(it)?.name }
                            ?: "A traveler"
                        Text(if (state.winnerIndex == 0) "You reached the finish!"
                            else "$winner reached the finish!",
                            fontWeight = FontWeight.SemiBold, color = Forest)
                        Button(onClick = { showResults = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("View match results")
                        }
                    } else if (state.playerIndex != 0) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Playing automatically…",
                            style = MaterialTheme.typography.bodySmall, color = Bark)
                    } else {
                        when (state.phase) {
                            Phase.ROLL -> {
                                Button(
                                    onClick = { rollingDie = true },
                                    enabled = !rollingDie && turnReady,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        DieFace(if (rollingDie) rollingFace else 1,
                                            modifier = Modifier.size(38.dp).graphicsLayer {
                                                rotationZ = rollingTilt
                                                scaleX = if (rollingDie) 0.96f else 1f
                                                scaleY = if (rollingDie) 1.04f else 1f
                                            },
                                            announceFace = false)
                                        Text(if (rollingDie) "Rolling…" else "Roll the die",
                                            fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Phase.ROUTE -> {
                                val fork = board.forkAt(state.players[0].position)
                                Text(fork?.title ?: "Choose your route", fontWeight = FontWeight.SemiBold, color = Forest)
                                if (fork != null) {
                                    Text("Check each trail’s risk before choosing.",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                                if (game.availableRoutes().size > 2) {
                                    Button(
                                        onClick = { routeDialogDismissed = false },
                                        enabled = turnReady,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Choose a path · 3 routes") }
                                } else {
                                    game.availableRoutes().forEach { route ->
                                        val choice = fork?.choices?.firstOrNull { it.to == route.id }
                                        OutlinedButton(
                                            onClick = { update { game.chooseRoute(route.id) } },
                                            enabled = turnReady,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            RouteChoiceSummary(
                                                label = choice?.label ?: route.id,
                                                description = choice?.description ?: "Follow this trail",
                                                risk = choice?.risk
                                            )
                                        }
                                    }
                                }
                            }
                            Phase.TARGET -> {
                                Text("Mischief! Choose an opponent.", fontWeight = FontWeight.SemiBold, color = Forest)
                                game.availableTargets().forEach { target ->
                                    OutlinedButton(onClick = { update { game.chooseTarget(target) } },
                                        enabled = turnReady,
                                        modifier = Modifier.fillMaxWidth()) {
                                        Text("Target ${state.players[target].name}${if (state.players[target].shield) " · shielded" else ""}")
                                    }
                                }
                            }
                            Phase.FINISHED -> Unit
                        }
                    }
                    }
                }
            }
        }
        // A dedicated scrollable chooser avoids the third option being hidden
        // under the fixed action pane on smaller handsets.
        if (state.phase == Phase.ROUTE && state.playerIndex == 0 &&
            game.availableRoutes().size > 2 && turnReady &&
            !routeDialogDismissed && !inspectBoard && !confirmExit && !showGuide) {
            val junction = board.forkAt(state.players[0].position)
            AlertDialog(
                onDismissRequest = { routeDialogDismissed = true },
                title = { Text(junction?.title ?: "Choose your path", color = Forest) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Three routes available · ${state.remainingSteps} " +
                            "${if (state.remainingSteps == 1) "step" else "steps"} to move",
                            style = MaterialTheme.typography.bodySmall, color = Bark)
                        game.availableRoutes().forEach { route ->
                            val choice = junction?.choices?.firstOrNull { it.to == route.id }
                            OutlinedButton(
                                onClick = { update { game.chooseRoute(route.id) } },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                RouteChoiceSummary(
                                    label = choice?.label ?: route.id,
                                    description = choice?.description ?: "Follow this trail",
                                    risk = choice?.risk
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { routeDialogDismissed = true }) {
                        Text("View board")
                    }
                }
            )
        }
        if (showGuide) {
            CrossroadsHowToPlayDialog(onDismiss = { showGuide = false })
        }
        if (showJournal) {
            AlertDialog(
                onDismissRequest = { showJournal = false },
                title = { Text("Journey journal", color = Forest) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 370.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.encounters.asReversed().forEach { note ->
                            Text("Turn ${note.turn} · ${note.actor}: ${note.title}",
                                fontWeight = FontWeight.SemiBold)
                            Text(note.outcome, style = MaterialTheme.typography.bodySmall, color = Bark)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showJournal = false }) { Text("Back to game") }
                }
            )
        }
        if (inspectBoard) {
            AlertDialog(
                onDismissRequest = { inspectBoard = false },
                title = { Text("Woodland Crossing · Route map", color = Forest) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BoardCanvas(board, state, height = 385.dp, tokenScale = 1.35f,
                            animateTokens = false, matchKey = game)
                        Text("Amber: hazard  •  Green: shelter  •  Purple: mischief",
                            style = MaterialTheme.typography.bodySmall, color = Bark)
                        Text("This map is for inspection; close it to make your next move.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { inspectBoard = false }) { Text("Back to game") }
                }
            )
        }
        if (showResults && state.phase == Phase.FINISHED && turnReady) {
            // Standard AlertDialog gave the small-screen results an oversized
            // lavender shell and pushed the encounter details below the fold.
            // This tabletop-colored dialog keeps actions visible at all times.
            Dialog(
                onDismissRequest = { showResults = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.91f)
                            .heightIn(max = (maxHeight - 24.dp).coerceAtLeast(1.dp)),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = Cream,
                        shadowElevation = 10.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 17.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Journey complete",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold, color = Forest)
                            CrossroadsResultsContent(
                                state = state,
                                elapsedSeconds = finishedElapsedSeconds,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Button(
                                onClick = { start(state.players.size) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Text("Rematch")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showResults = false },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("View board")
                                }
                                TextButton(
                                    onClick = { leaveGame() },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("Main menu")
                                }
                            }
                        }
                    }
                }
            }
        }
        if (confirmExit) {
            AlertDialog(
                onDismissRequest = { confirmExit = false },
                title = { Text("Leave this match?") },
                text = { Text("Leave and discard this journey? Your completed-match record will be kept.") },
                confirmButton = {
                    TextButton(onClick = { leaveGame(); confirmExit = false }) { Text("Leave") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmExit = false }) { Text("Keep playing") }
                }
            )
        }
    }
}

@Composable
private fun BoardCanvas(
    board: BoardDefinition,
    state: Snapshot,
    height: Dp = 315.dp,
    tokenScale: Float = 1f,
    animateTokens: Boolean = true,
    matchKey: Any? = null,
    stepMillis: Int = PresentationSpeed.COZY.stepMillis,
    focusPlayerIndex: Int = state.playerIndex,
    isTurnTransitioning: Boolean = false,
    startAtCurrent: Boolean = false
) {
    // The new GameSession object resets visual positions after Rematch.
    val motion = remember(matchKey, board.id, state.players.size, animateTokens, startAtCurrent) {
        List(state.players.size) { i ->
            val node = board.node(
                if (animateTokens && !startAtCurrent) board.start else state.players[i].position
            )
            Animatable(Offset(node.x, node.y), Offset.VectorConverter)
        }
    }
    val landingGlow = remember(matchKey, board.id, state.players.size, animateTokens) {
        List(state.players.size) { Animatable(0f) }
    }
    val displayed = state.players.mapIndexed { i, player ->
        val path = player.traversed.toList()
        // The animation observes the rendered route history, never changes the rules engine.
        var previous by remember(matchKey, board.id, i, animateTokens, startAtCurrent) {
            mutableStateOf(if (animateTokens && !startAtCurrent) listOf(board.start) else path)
        }
        LaunchedEffect(matchKey, board.id, path, animateTokens) {
            if (!animateTokens) {
                motion[i].snapTo(Offset(board.node(player.position).x, board.node(player.position).y))
            } else {
                val nodes = MoveTrail.steps(previous, path)
                previous = path
                // Use brief segments along the exact route, including backwards pranks.
                for (id in nodes) {
                    val target = board.node(id)
                    motion[i].animateTo(
                        Offset(target.x, target.y),
                        animationSpec = tween(durationMillis = stepMillis)
                    )
                }
                if (nodes.isNotEmpty()) {
                    landingGlow[i].snapTo(1f)
                    landingGlow[i].animateTo(0f, animationSpec = tween(durationMillis = 260))
                }
            }
        }
        motion[i].value
    }
    // Camera is purely visual and match-scoped. Moving it cannot move a piece,
    // change the route graph, or affect the authoritative game snapshot.
    var cameraMode by remember(matchKey, board.id, animateTokens) {
        mutableStateOf(if (animateTokens) BoardCameraMode.FOLLOW else BoardCameraMode.MANUAL)
    }
    var manualZoom by remember(matchKey, board.id, animateTokens) { mutableFloatStateOf(1f) }
    var manualTranslation by remember(matchKey, board.id, animateTokens) {
        mutableStateOf(Offset.Zero)
    }
    var canvasDimensions by remember(matchKey, board.id, animateTokens) {
        mutableStateOf(IntSize.Zero)
    }
    val cameraSubject = focusPlayerIndex.coerceIn(displayed.indices)
    val focusPlayerName = state.players[cameraSubject].name
    val focusRegion = board.node(state.players[cameraSubject].position).region
    val cameraDescription = "Woodland Crossing board. $focusPlayerName in $focusRegion."
    // The snapshot may have advanced to the next player while a piece is still
    // visibly traveling. The highlighted token follows the actual presentation.
    val highlightedSeat = if (isTurnTransitioning && animateTokens)
        cameraSubject else state.playerIndex
    val active = displayed[cameraSubject]
    val cameraTarget = if (state.phase == Phase.ROUTE &&
        focusPlayerIndex == state.playerIndex) {
        val node = board.node(state.players[state.playerIndex].position)
        val fork = CameraGeometry.forkFocus(
            node.x, node.y, node.next.map { board.node(it).x to board.node(it).y }
        )
        Offset(fork.first, fork.second)
    } else active
    val followedFocus by animateOffsetAsState(
        targetValue = cameraTarget,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "follow-board-position"
    )
    val autoZoom by animateFloatAsState(
        targetValue = when (cameraMode) {
            BoardCameraMode.FOLLOW -> if (state.phase == Phase.ROUTE) 1.4f else 1.7f
            BoardCameraMode.MANUAL -> manualZoom
        },
        animationSpec = tween(durationMillis = 330),
        label = "board-camera-zoom"
    )
    val cameraZoom = if (cameraMode == BoardCameraMode.MANUAL) manualZoom else autoZoom
    val viewportWidth = canvasDimensions.width.toFloat()
    val viewportHeight = canvasDimensions.height.toFloat()
    val boardPadding = 18f
    val focusPx = Offset(
        boardPadding + followedFocus.x * (viewportWidth - 2f * boardPadding).coerceAtLeast(0f),
        boardPadding + followedFocus.y * (viewportHeight - 2f * boardPadding).coerceAtLeast(0f)
    )
    val desiredTranslation = when (cameraMode) {
        BoardCameraMode.FOLLOW -> Offset(
            CameraGeometry.centerTranslation(viewportWidth / 2f, focusPx.x, cameraZoom),
            CameraGeometry.centerTranslation(viewportHeight / 2f, focusPx.y, cameraZoom)
        )
        BoardCameraMode.MANUAL -> manualTranslation
    }
    val actualTranslation = Offset(
        CameraGeometry.boundTranslation(desiredTranslation.x, viewportWidth,
            boardPadding, viewportWidth - boardPadding, cameraZoom),
        CameraGeometry.boundTranslation(desiredTranslation.y, viewportHeight,
            boardPadding, viewportHeight - boardPadding, cameraZoom)
    )
    // Keep the pointer handler stable while receiving the newest following pose.
    val liveZoom by rememberUpdatedState(cameraZoom)
    val liveTranslation by rememberUpdatedState(actualTranslation)

    // Touching the focused piece or double-tapping resumes cinematic following.
    val liveViewportSize by rememberUpdatedState(canvasDimensions)
    // Match the pawn's true visual offset at the shared Start; tapping its
    // painted location should still recenter at higher zoom.
    val focusedGroup = displayed.indices.filter { index ->
        (displayed[index] - active).getDistance() < 0.012f
    }
    val focusedRank = focusedGroup.indexOf(cameraSubject)
    val focusedDx = if (focusedGroup.size == 1) 0f
        else (focusedRank - (focusedGroup.size - 1) / 2f) * 18f * tokenScale
    val focusedDy = if (focusedGroup.size == 1) -14f * tokenScale else -17f * tokenScale
    val livePieceLocation by rememberUpdatedState(
        Offset(
            boardPadding + active.x * (viewportWidth - 2f * boardPadding).coerceAtLeast(0f) + focusedDx,
            boardPadding + active.y * (viewportHeight - 2f * boardPadding).coerceAtLeast(0f) + focusedDy
        )
    )
    val density = LocalDensity.current
    val insetPx = with(density) { 8.dp.toPx() }
    val pieceTapRadiusPx = with(density) { 32.dp.toPx() }

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE4D0))) {
        Box(
            modifier = Modifier.fillMaxWidth().height(height).clipToBounds()
                .semantics {
                    contentDescription = "$cameraDescription Pinch to zoom or drag to explore."
                    stateDescription = if (cameraMode == BoardCameraMode.FOLLOW)
                        "Following the action" else "Exploring manually"
                    customActions = listOf(
                        CustomAccessibilityAction("Recenter board") {
                            cameraMode = if (animateTokens) BoardCameraMode.FOLLOW else BoardCameraMode.MANUAL
                            manualZoom = 1f
                            manualTranslation = Offset.Zero
                            true
                        },
                        CustomAccessibilityAction("Show whole board") {
                            cameraMode = BoardCameraMode.MANUAL
                            manualZoom = 1f
                            manualTranslation = Offset.Zero
                            true
                        }
                    )
                }
                .pointerInput(matchKey, animateTokens) {
                    detectTapGestures(
                        onDoubleTap = {
                            cameraMode = if (animateTokens) BoardCameraMode.FOLLOW else BoardCameraMode.MANUAL
                            manualZoom = 1f
                            manualTranslation = Offset.Zero
                        },
                        onTap = { tap ->
                            if (animateTokens) {
                                // Convert the token's canvas coordinates through the visual transform.
                                val width = liveViewportSize.width.toFloat()
                                val height = liveViewportSize.height.toFloat()
                                val renderedPiece = Offset(
                                    insetPx + width / 2f +
                                        (livePieceLocation.x - width / 2f) * liveZoom +
                                        liveTranslation.x,
                                    insetPx + height / 2f +
                                        (livePieceLocation.y - height / 2f) * liveZoom +
                                        liveTranslation.y
                                )
                                if ((tap - renderedPiece).getDistance() <= pieceTapRadiusPx) {
                                    cameraMode = BoardCameraMode.FOLLOW
                                    manualTranslation = Offset.Zero
                                }
                            }
                        }
                    )
                }
                .pointerInput(matchKey, animateTokens) {
                    detectTransformGestures(panZoomLock = true) { centroid, pan, gestureZoom, _ ->
                        val wasManual = cameraMode == BoardCameraMode.MANUAL
                        val fromZoom = if (wasManual) manualZoom else liveZoom
                        val fromTranslation = if (wasManual) manualTranslation else liveTranslation
                        val nextZoom = CameraGeometry.zoom(fromZoom * gestureZoom)
                        val nextTranslation = Offset(
                            CameraGeometry.zoomAt(
                                fromTranslation.x, centroid.x - (size.width - canvasDimensions.width) / 2f,
                                canvasDimensions.width / 2f, fromZoom, nextZoom, pan.x
                            ),
                            CameraGeometry.zoomAt(
                                fromTranslation.y, centroid.y - (size.height - canvasDimensions.height) / 2f,
                                canvasDimensions.height / 2f, fromZoom, nextZoom, pan.y
                            )
                        )
                        manualZoom = nextZoom
                        manualTranslation = Offset(
                            CameraGeometry.boundTranslation(nextTranslation.x, canvasDimensions.width.toFloat(),
                                boardPadding, canvasDimensions.width.toFloat() - boardPadding, nextZoom),
                            CameraGeometry.boundTranslation(nextTranslation.y, canvasDimensions.height.toFloat(),
                                boardPadding, canvasDimensions.height.toFloat() - boardPadding, nextZoom)
                        )
                        cameraMode = BoardCameraMode.MANUAL
                    }
                }
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize().padding(8.dp)
                    .onSizeChanged { canvasDimensions = it }
                    .graphicsLayer {
                        scaleX = cameraZoom
                        scaleY = cameraZoom
                        translationX = actualTranslation.x
                        translationY = actualTranslation.y
                    }
            ) {
            val left = 18f
            val top = 18f
            val drawWidth = size.width - left * 2
            val drawHeight = size.height - top * 2
            fun position(id: String): Offset {
                val node = board.node(id)
                return Offset(left + node.x * drawWidth, top + node.y * drawHeight)
            }
            // Original ART-08 region illustration is always BELOW navigable roads.
            // It scales with the existing gesture-first 2D camera, never the game graph.
            drawWoodlandBoardArt(
                boardId = board.id,
                left = left, top = top, width = drawWidth, height = drawHeight
            )
            // Region names make the fixed course easier to learn between matches.
            drawIntoCanvas { canvas ->
                val labelPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    this.color = android.graphics.Color.rgb(110, 99, 79)
                    textSize = 12f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                listOf("MEADOW", "FOREST", "VILLAGE").forEachIndexed { index, label ->
                    canvas.nativeCanvas.drawText(label, left + (0.2f + index * 0.29f) * drawWidth, top + 8f, labelPaint)
                }
            }
            // Shared board geography never changes with the event seed.
            board.nodes.forEach { node ->
                val from = position(node.id)
                node.next.forEach { dest ->
                    val route = when {
                        board.node(dest).route != "neutral" -> board.node(dest).route
                        node.route != "neutral" -> node.route
                        else -> "neutral"
                    }
                    val laneColor = when (route) {
                        "risky" -> Color(0xFFC29450)
                        "safe" -> Color(0xFF83A181)
                        "mischief" -> Color(0xFFA18BB1)
                        else -> Color(0xFFD4C3A3)
                    }
                    drawLine(laneColor, from, position(dest), strokeWidth = 11f, cap = StrokeCap.Round)
                    drawLine(Color(0xFFFCF8ED), from, position(dest), strokeWidth = 3f, cap = StrokeCap.Round)
                }
            }
            board.nodes.forEach { node ->
                val center = position(node.id)
                val color = when (node.kind) {
                    "fork" -> Color(0xFFD7AB50)
                    "event" -> when (node.eventPool) {
                        "hazard" -> Color(0xFFC28D43)
                        "shelter" -> Color(0xFF709973)
                        "mischief" -> Color(0xFF9B80AB)
                        else -> Color(0xFF8B9F7B)
                    }
                    "start" -> Forest
                    "finish" -> Color(0xFFB47E40)
                    else -> Color(0xFFFDF8E9)
                }
                val radius = if (node.kind == "fork") 12f else if (node.kind == "start" || node.kind == "finish") 13f else 7f
                drawCircle(color, radius, center)
                drawCircle(Color(0xFF9E896B), radius, center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.3f))
                if (node.kind == "fork" || node.kind == "start" || node.kind == "finish") {
                    drawIntoCanvas { canvas ->
                        val label = when (node.kind) { "fork" -> "?"; "start" -> "S"; else -> "F" }
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            this.color = android.graphics.Color.WHITE
                            textSize = 11f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        canvas.nativeCanvas.drawText(label, center.x, center.y + 4f, paint)
                    }
                }
            }
            // Quietly trace each player's actual chosen journey.
            state.players.forEachIndexed { playerIndex, player ->
                val trail = PlayerColors[playerIndex].copy(alpha = 0.50f)
                player.traversed.zipWithNext().forEach { (from, to) ->
                    drawLine(trail, position(from), position(to), strokeWidth = 3f, cap = StrokeCap.Round)
                }
            }
            // Brief, low-contrast landing ripple communicates the arrival.
            state.players.forEachIndexed { i, _ ->
                val strength = landingGlow[i].value
                if (strength > 0.001f) {
                    val at = displayed[i]
                    drawCircle(
                        PlayerColors[i].copy(alpha = strength * 0.35f),
                        radius = 19f + (1f - strength) * 12f,
                        center = Offset(left + at.x * drawWidth, top + at.y * drawHeight),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                    )
                }
            }
            // Highlight the active piece and both available route entrances.
            if (state.phase == Phase.ROUTE && !isTurnTransitioning) {
                val fork = board.node(state.players[state.playerIndex].position)
                fork.next.forEach { candidate ->
                    val ring = when (board.node(candidate).route) {
                        "risky" -> Color(0xFFAE773B)
                        "mischief" -> Color(0xFF8A6D9B)
                        else -> Forest
                    }
                    drawCircle(ring, radius = 14f, center = position(candidate),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                }
            }
            // Use animated on-screen coordinates to spread clustered tokens.
            // Snapshot positions can already be several spaces ahead mid-animation.
            state.players.forEachIndexed { i, _ ->
                val group = displayed.indices.filter { index ->
                    (displayed[index] - displayed[i]).getDistance() < 0.012f
                }
                val rank = group.indexOf(i)
                val dx = if (group.size == 1) 0f
                    else (rank - (group.size - 1) / 2f) * 18f * tokenScale
                val dy = if (group.size == 1) -14f * tokenScale else -17f * tokenScale
                val marker = Offset(
                    left + displayed[i].x * drawWidth + dx,
                    top + displayed[i].y * drawHeight + dy
                )
                val highlighted = i == highlightedSeat
                val tokenRadius = (if (highlighted) 12.5f else 11f) * tokenScale
                drawCrossroadsPiece(
                    center = marker,
                    radius = tokenRadius,
                    seatColor = PlayerColors[i],
                    highlighted = highlighted,
                    traveling = highlighted && isTurnTransitioning
                )
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 12f * tokenScale
                        isAntiAlias = true
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        setShadowLayer(1.6f, 0f, 0.6f,
                            android.graphics.Color.argb(110, 40, 34, 24))
                    }
                    canvas.nativeCanvas.drawText("${i + 1}", marker.x,
                        marker.y + 4f * tokenScale, paint)
                }
            }
            }

        }
    }
}
