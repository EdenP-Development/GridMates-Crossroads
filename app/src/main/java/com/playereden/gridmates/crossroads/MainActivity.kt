package com.playereden.gridmates.crossroads

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
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
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.playereden.crossroads.core.*
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Forest = Color(0xFF48684D)
private val Cream = Color(0xFFF7F0DE)
private val Bark = Color(0xFF80664B)
private val PlayerColors = listOf(Color(0xFFC75C49), Color(0xFF3D729F), Color(0xFF729B60), Color(0xFFB48A36))
private enum class BoardCameraMode { FOLLOW, MANUAL }
private enum class LanRole { OFFLINE, HOST, GUEST }
private enum class LabRoll { STEADY, PUSH }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
        val classicBoard = GameData.board(assets.open("boards/woodland_woven_trail.json").bufferedReader().use { it.readText() })
        val quickBoard = GameData.board(assets.open("boards/woodland_brookside_quick.json").bufferedReader().use { it.readText() })
        val longBoard = GameData.board(assets.open("boards/woodland_cedarwood_long.json").bufferedReader().use { it.readText() })
        val catalogue = GameData.events(assets.open("events/woven_events_v02.json").bufferedReader().use { it.readText() })
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Forest, onPrimary = Color.White,
                    primaryContainer = Color(0xFFE2EAD9), onPrimaryContainer = Forest,
                    secondary = Bark, onSecondary = Color.White,
                    secondaryContainer = Color(0xFFECE3CE), onSecondaryContainer = Bark,
                    tertiary = Color(0xFFAD8946), onTertiary = Color.White,
                    background = Cream, onBackground = Color(0xFF302E28),
                    surface = Cream, onSurface = Color(0xFF302E28),
                    surfaceVariant = Color(0xFFECE3CE), onSurfaceVariant = Bark,
                    outline = Color(0xFF978B76), outlineVariant = Color(0xFFD8CCB3)
                )
            ) {
                CrossroadsApp(classicBoard, quickBoard, longBoard, catalogue)
            }
        }
    }
}

@Composable
private fun CrossroadsApp(
    classicBoard: BoardDefinition,
    quickBoard: BoardDefinition,
    longBoard: BoardDefinition,
    catalogue: EventCatalogue
) {
    val knownBoards = remember(classicBoard, quickBoard, longBoard) {
        val original = listOf(classicBoard, quickBoard, longBoard)
        (original + original.map(DecisionLab::variant) +
            original.map(DecisionLab::variantV2) +
            original.map(DecisionLab::variantV3) +
            original.map(DecisionLab::variantV4) +
            original.map(DecisionLab::variantV5)).associateBy { it.id }
    }
    val context = LocalContext.current
    // Crossroads runs inside its own ComponentActivity. Explicitly observe the
    // Activity lifecycle so no AI move happens while the app is paused.
    val lifecycle = (context as ComponentActivity).lifecycle
    var appInForeground by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> appInForeground = true
                Lifecycle.Event.ON_PAUSE -> appInForeground = false
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        appInForeground = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        onDispose { lifecycle.removeObserver(observer) }
    }
    // Preserve this long-standing preference filename for QA install-over updates.
    // Changing it would strand existing local wins and match checkpoints.
    val prefs = remember { context.getSharedPreferences("crossroads_qa_profile_v1", 0) }
    // Stored separately from checkpoint state. Changing a board's appearance
    // must never modify its route graph, seed, replay or old save compatibility.
    var boardLook by remember {
        mutableStateOf(
            CrossroadsBoardLook.fromStored(prefs.getString("board_look_v1", null))
        )
    }
    // VISUAL-84: QA-only A/B art perspective. An overhead board should be
    // judged next to the existing storybook landscapes before any replacement.
    // This is a local appearance preference, never a save or LAN rule.
    var topDownBoardPreview by remember {
        mutableStateOf(BuildConfig.DEBUG && prefs.getBoolean("qa_overhead_board_v1", false))
    }
    val recoveryStore = remember(prefs, classicBoard, quickBoard, longBoard, catalogue) {
        MatchRecoveryStore(prefs, classicBoard, catalogue, knownBoards)
    }
    // A restarted Activity gets its entire authoritative match from the saved
    // seed + accepted actions; incompatible/corrupt saves never half-restore.
    val initialRecovery = remember(recoveryStore) { recoveryStore.load() }
    var matches by remember { mutableIntStateOf(prefs.getInt("completed", 0)) }
    var wins by remember { mutableIntStateOf(prefs.getInt("wins", 0)) }
    var session by remember { mutableStateOf(initialRecovery.match?.game) }
    // The course is player-selected for a new offline race, but an existing
    // checkpoint always determines its own original board.
    var selectedCourseId by remember {
        mutableStateOf(
            DecisionLab.baseId(initialRecovery.match?.game?.board?.id ?: classicBoard.id)
        )
    }
    var playtestRules by remember {
        mutableStateOf(initialRecovery.match?.game?.playtestRules ?: false)
    }
    // Only users who explicitly choose Fair Finish start Lab v5.
    // Existing saved Lab v4/v5 games always restore their own version.
    var fairFinishExperiment by remember {
        mutableStateOf(
            initialRecovery.match?.game?.board?.id?.endsWith(DecisionLab.V5_SUFFIX) == true ||
                prefs.getBoolean("lab_v5_equal_round_opt_in_v1", false)
        )
    }
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
    var transferMessage by remember { mutableStateOf<String?>(null) }
    var importPending by remember { mutableStateOf<VerifiedCrossroadsProgress?>(null) }
    val transferScope = rememberCoroutineScope()
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            transferScope.launch {
                val outcome = withContext(Dispatchers.IO) {
                    runCatching {
                        val payload = recoveryStore.exportProgress()
                        context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use {
                            it.write(payload)
                        } ?: error("Could not write to the chosen document")
                    }
                }
                transferMessage = if (outcome.isSuccess) {
                    "Backup exported. Keep the JSON file somewhere private until you restore it."
                } else {
                    "Backup could not be exported: ${outcome.exceptionOrNull()?.message ?: "Storage error"}"
                }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            transferScope.launch {
                val outcome = withContext(Dispatchers.IO) {
                    runCatching {
                        // Never let a hostile or accidental huge file exhaust app memory.
                        val bytes = context.contentResolver.openInputStream(uri)?.use { source ->
                            val output = ByteArrayOutputStream()
                            val chunk = ByteArray(8192)
                            while (true) {
                                val read = source.read(chunk)
                                if (read < 0) break
                                if (read == 0) continue
                                require(output.size() + read <= CrossroadsProgressCodec.MAX_DOCUMENT_BYTES) {
                                    "This backup file is too large"
                                }
                                output.write(chunk, 0, read)
                            }
                            output.toByteArray()
                        } ?: error("Could not read the chosen document")
                        recoveryStore.previewImport(String(bytes, Charsets.UTF_8))
                    }
                }
                outcome.onSuccess { importPending = it }
                    .onFailure {
                        transferMessage = "No data imported: ${it.message ?: "Invalid or incompatible backup"}"
                    }
            }
        }
    }
    var revision by remember { mutableIntStateOf(0) }
    // Nearby is independent from offline local saves. Only the host changes
    // the LAN GameSession; guests reconstruct the host's verified checkpoint.
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var lanRole by remember { mutableStateOf(LanRole.OFFLINE) }
    var lanScreen by remember { mutableStateOf(LanScreen.NONE) }
    var lanHost by remember { mutableStateOf<CrossroadsLanHost?>(null) }
    var lanGuest by remember { mutableStateOf<CrossroadsLanGuest?>(null) }
    var lanConnected by remember { mutableStateOf(false) }
    var lanSeats by remember { mutableIntStateOf(2) }
    var lanBusy by remember { mutableStateOf(false) }
    var lanMessage by remember { mutableStateOf<String?>(null) }
    var lanRooms by remember { mutableStateOf<List<NearbyCrossroads>>(emptyList()) }
    var lanPendingAction by remember { mutableStateOf(false) }
    var lanLastHost by remember { mutableStateOf("") }
    var lanLastPort by remember { mutableIntStateOf(0) }
    var lanLastCode by remember { mutableStateOf("") }
    val nearby = remember(context) {
        CrossroadsNearby(context) { error ->
            mainHandler.post { lanMessage = error }
        }
    }
    LaunchedEffect(lanScreen, lanHost, lanRole) {
        when {
            lanRole == LanRole.HOST && lanHost != null -> lanHost?.let {
                // Keep the room advertised while the game is active so the
                // same guest can discover it again after a Wi-Fi interruption.
                nearby.advertise(it.port, it.code.isNotEmpty())
            }
            lanScreen == LanScreen.JOIN -> nearby.discover { lanRooms = it }
            else -> nearby.stop()
        }
    }
    DisposableEffect(nearby) {
        onDispose {
            lanHost?.close()
            lanGuest?.close()
            nearby.close()
        }
    }
    val game = session
    val board = game?.board ?: knownBoards.getValue(selectedCourseId)
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
    var queuedLabRoll by remember(game) { mutableStateOf<LabRoll?>(null) }
    var speed by remember {
        mutableStateOf(
            runCatching {
                PresentationSpeed.valueOf(prefs.getString("match_pace_v1", "COZY") ?: "COZY")
            }.getOrDefault(PresentationSpeed.COZY)
        )
    }
    val haptics = LocalHapticFeedback.current
    var touchFeedback by remember {
        mutableStateOf(prefs.getBoolean("feel_touch_feedback_v1", true))
    }
    // QA-only presentation switch, independent from match state and saves.
    // Allows a same-board/same-art comparison of illustrated route treatments.
    var trailStyle by remember {
        mutableStateOf(
            runCatching {
                CrossroadsTrailStyle.valueOf(
                    prefs.getString("qa_trail_style_v2", "ARTWORK") ?: "ARTWORK"
                )
            }.getOrDefault(CrossroadsTrailStyle.ARTWORK)
        )
    }
    // QA-only art preference, never included in checkpoint or LAN protocol.
    var woodlandPieces by remember {
        mutableStateOf(prefs.getBoolean("qa_woodland_pieces_v1", true))
    }
    // Cosmetic QA-only comparison; never included in authoritative match saves.
    var paintedMarkers by remember {
        mutableStateOf(prefs.getBoolean("qa_painted_markers_v1", true))
    }
    var transitionDelay by remember(game) { mutableLongStateOf(0L) }
    var turnReady by remember(game) { mutableStateOf(true) }
    var cameraTrackIndex by remember(game) { mutableIntStateOf(game?.currentIndex ?: 0) }
    var routeDialogDismissed by remember(game) { mutableStateOf(true) }
    // Let players first choose directly on the board; the detail sheet is optional.
    LaunchedEffect(game, state?.revision) { routeDialogDismissed = true }
    fun persistGame(current: GameSession) {
        // Nearby QA does not convert a remote match into an offline
        // pass-the-phone recovery save or change the user's solo record.
        if (lanRole != LanRole.OFFLINE) return
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
        lanHost?.close()
        lanGuest?.close()
        nearby.stop()
        lanHost = null
        lanGuest = null
        lanScreen = LanScreen.NONE
        lanConnected = false
        lanBusy = false
        lanPendingAction = false
        val previousRole = lanRole
        lanRole = LanRole.OFFLINE
        val cleared = if (previousRole == LanRole.OFFLINE)
            recoveryStore.discard() else true
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
    LaunchedEffect(game, rollingDie, appInForeground) {
        if (game != null && rollingDie && appInForeground) {
            // Visual dice cycling is presentation only: the rules engine draws the
            // real die exactly once after the animation; no new RNG affects matches.
            // Capture the pace for the full animation, even if the user
            // switches Cozy/Brisk while a roll is in progress.
            val rollSpeed = speed
            val previewFaces = intArrayOf(2, 5, 1, 6, 3, 4)
            repeat(rollSpeed.dieFrames) { step ->
                // These deterministic preview pips are decorative only; the
                // authoritative result is drawn once by GameSession.roll().
                rollingFace = if (game.playtestRules &&
                    queuedLabRoll == LabRoll.STEADY) 4
                    else previewFaces[step % previewFaces.size]
                delay(rollSpeed.dieFrameDelayMillis(step))
            }
            // If Android paused us just as the last decorative face finished,
            // keep rollingDie pending rather than silently losing the tap.
            // The foreground-keyed effect will replay the visual sequence on resume.
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                return@LaunchedEffect
            }
            if (game.phase == Phase.ROLL && !game.players[game.currentIndex].isBot) {
                val localSeat = if (lanRole == LanRole.GUEST) 1 else 0
                if (lanRole == LanRole.OFFLINE || (
                    lanConnected && game.currentIndex == localSeat && !lanPendingAction
                )) {
                    if (lanRole == LanRole.GUEST) {
                        runCatching {
                            lanGuest?.submit(LanAction(
                                LanActionType.ROLL, expectedMatchId = activeMatchId,
                                expectedRevision = game.revision
                            ))
                        }.onSuccess { lanPendingAction = true }
                            .onFailure { lanMessage = "Reconnect before rolling." }
                    } else {
                        val before = game.snapshot()
                        if (lanRole == LanRole.HOST) {
                            LanTurnAuthority.apply(game, 0, LanAction(LanActionType.ROLL))
                            lanHost?.publish(game.checkpoint(activeMatchId, matchStartedAtEpochMillis))
                        } else {
                            if (game.playtestRules) {
                                when (queuedLabRoll) {
                                    LabRoll.STEADY -> game.rollSteady()
                                    LabRoll.PUSH -> game.rollBold()
                                    null -> error("No tactical travel choice was selected")
                                }
                            } else game.roll() // One authoritative action after preview.
                            persistGame(game)
                        }
                        cameraTrackIndex = PresentationPacing.mover(before, game.snapshot())
                        transitionDelay = PresentationPacing.waitForMovement(before, game.snapshot(), speed)
                        turnReady = false
                        revision++
                    }
                }
            }
            rollingDie = false
            queuedLabRoll = null
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
            // Finish the visual movement even if the player opens a menu.
            // Turn readiness is independent of the optional reading surfaces.
            if (transitionDelay > 0L) delay(transitionDelay)
            cameraTrackIndex = state.playerIndex
            transitionDelay = 0L
            turnReady = true
        }
    }
    // Pausing for instructions, board overview, the journal or match options
    // should also pause the AI. Splitting this from the movement effect prevents
    // a dismissed menu from replaying the last movement delay or skipping a turn.
    val readingOrMenuOpen = showGuide || showJournal || inspectBoard ||
        confirmExit || showMatchOptions || showResults ||
        importPending != null || transferMessage != null
    LaunchedEffect(game, revision, turnReady, readingOrMenuOpen, appInForeground, lanRole, lanConnected) {
        // In nearby play only the host advances AI. Guests replay host checkpoints
        // and must never independently consume their own dice/event randomness.
        // A disconnected host also pauses bots so the returning guest misses nothing.
        if (game != null && lanRole != LanRole.GUEST &&
            (lanRole != LanRole.HOST || lanConnected) &&
            TurnFlowGate.mayAdvanceAi(state, turnReady, readingOrMenuOpen, appInForeground)
        ) {
            delay(speed.aiBreathMillis)
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) ||
                lanRole == LanRole.GUEST || (lanRole == LanRole.HOST && !lanConnected)
            ) {
                return@LaunchedEffect
            }
            val before = game.snapshot()
            game.botStep()
            if (lanRole == LanRole.HOST) {
                lanHost?.publish(game.checkpoint(activeMatchId, matchStartedAtEpochMillis))
            } else {
                persistGame(game)
            }
            cameraTrackIndex = PresentationPacing.mover(before, game.snapshot())
            transitionDelay = PresentationPacing.waitForMovement(before, game.snapshot(), speed)
            turnReady = false
            revision++
        }
    }
    // Completion stats are written once in the *same committed transaction* as
    // the final action by MatchRecoveryStore. Recomposition/recovery cannot
    // double-count a finished match.
    // Freeze elapsed time when final movement has settled: reopening the results
    // must not make the match duration continue growing.
    LaunchedEffect(game, state?.phase, turnReady) {
        if (game != null && state?.phase == Phase.FINISHED && turnReady && !resultsPresented) {
            finishedElapsedSeconds = if (lanRole == LanRole.OFFLINE) {
                recoveryStore.completedElapsedSeconds(activeMatchId, matchStartedAtEpochMillis)
            } else {
                ((System.currentTimeMillis() - matchStartedAtEpochMillis).coerceAtLeast(0L) / 1000L)
            }
            resultsPresented = true
            showResults = true
        }
    }
    fun beginOfflineRace(newGame: GameSession) {
        activeMatchId = java.util.UUID.randomUUID().toString()
        matchStartedAtEpochMillis = System.currentTimeMillis()
        startedFromCheckpoint = false
        persistGame(newGame)
        session = newGame
        revision++
    }

    fun start(seats: Int, passAndPlay: Boolean) {
        if (lanRole != LanRole.OFFLINE) leaveGame()
        val humans = if (passAndPlay) (0 until seats).toSet() else setOf(0)
        val boardId = CrossroadsExperimentalMode.boardId(
            selectedCourseId, playtestRules, fairFinishExperiment
        )
        val newGame = GameSession(knownBoards.getValue(boardId), catalogue, seats = seats,
            seed = java.util.Random().nextInt(), humanIndices = humans)
        beginOfflineRace(newGame)
    }

    fun startLanHost(requireCode: Boolean, seats: Int) {
        if (seats !in 2..4) {
            lanMessage = "Nearby games support two to four participants."
            return
        }
        if (!nearby.hasWifi()) {
            lanMessage = "Connect both phones to the same Wi-Fi before hosting."
            return
        }
        lanBusy = true
        lanMessage = null
        val roomGame = GameSession(
            classicBoard, catalogue, seats = seats, seed = java.util.Random().nextInt(),
            humanIndices = setOf(0, 1)
        )
        val roomId = java.util.UUID.randomUUID().toString()
        val started = System.currentTimeMillis()
        lanRole = LanRole.HOST
        lanSeats = seats
        activeMatchId = roomId
        matchStartedAtEpochMillis = started
        startedFromCheckpoint = false
        // Bind a socket off the UI thread; the host never edits state from a
        // socket worker. Every guest command is forwarded to Android's main queue.
        Thread({
            try {
                lateinit var server: CrossroadsLanHost
                server = CrossroadsLanHost(
                    initial = roomGame.checkpoint(roomId, started),
                    requiresCode = requireCode,
                    onAction = { action ->
                        mainHandler.post {
                            val currentGame = session
                            if (lanRole == LanRole.HOST && lanHost === server &&
                                lanConnected && currentGame != null
                            ) {
                                if (!appInForeground) {
                                    server.message("Host is away. Try again when they return.")
                                } else {
                                    val before = currentGame.snapshot()
                                    runCatching {
                                        LanTurnAuthority.applyRemote(
                                            currentGame, activeMatchId, 1, action
                                        )
                                    }.onSuccess {
                                        server.publish(currentGame.checkpoint(
                                            activeMatchId, matchStartedAtEpochMillis
                                        ))
                                        cameraTrackIndex = PresentationPacing.mover(
                                            before, currentGame.snapshot()
                                        )
                                        transitionDelay = PresentationPacing.waitForMovement(
                                            before, currentGame.snapshot(), speed
                                        )
                                        turnReady = false
                                        revision++
                                    }.onFailure { error ->
                                        server.message(error.message ?: "Invalid turn")
                                        // A rejected duplicate/old command must not strand the
                                        // guest's pending-action UI. Re-send authoritative state.
                                        server.publish(currentGame.checkpoint(
                                            activeMatchId, matchStartedAtEpochMillis
                                        ))
                                    }
                                }
                            }
                        }
                    },
                    onPresence = { joined, detail ->
                        mainHandler.post {
                            if (lanRole == LanRole.HOST && lanHost === server) {
                                lanConnected = joined
                                lanBusy = false
                                if (joined) {
                                    lanMessage = null
                                    lanScreen = LanScreen.NONE
                                } else {
                                    lanMessage = detail ?: "Player 2 disconnected. Rejoin to continue."
                                }
                            }
                        }
                    }
                )
                mainHandler.post {
                    if (lanRole == LanRole.HOST && activeMatchId == roomId) {
                        lanHost = server
                        session = roomGame
                        revision++
                        lanBusy = false
                        lanScreen = LanScreen.HOST_WAIT
                    } else {
                        server.close()
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    if (lanRole == LanRole.HOST && activeMatchId == roomId) {
                        lanBusy = false
                        lanRole = LanRole.OFFLINE
                        lanMessage = e.message ?: "Couldn't open a local room."
                    }
                }
            }
        }, "crossroads-start-lan").apply { isDaemon = true }.start()
    }

    fun joinLan(host: String, port: Int, code: String, resume: Boolean = false) {
        if (!nearby.hasWifi()) {
            lanMessage = "Connect both phones to the same Wi-Fi before joining."
            return
        }
        lanLastHost = host
        lanLastPort = port
        lanLastCode = code
        lanRole = LanRole.GUEST
        lanScreen = LanScreen.JOIN
        lanBusy = true
        lanMessage = null
        if (!resume || lanGuest == null) {
            lanGuest?.close()
            lanGuest = CrossroadsLanGuest(
                socketFactory = { nearby.wifiSocket() },
                onState = { checkpoint ->
                    mainHandler.post {
                        if (lanRole == LanRole.GUEST) {
                            runCatching {
                                // LAN-23 remains Classic-only until actual two-phone QA.
                                GameSession.restore(classicBoard, catalogue, checkpoint)
                            }.onSuccess { restored ->
                                if (restored.players.size !in 2..4 ||
                                    restored.players.take(2).any { it.isBot } ||
                                    restored.players.drop(2).any { !it.isBot }
                                ) {
                                    lanMessage = "The host has an incompatible match."
                                    lanBusy = false
                                } else {
                                    activeMatchId = checkpoint.matchId
                                    matchStartedAtEpochMillis = checkpoint.startedAtEpochMillis
                                    session = restored
                                    startedFromCheckpoint = true
                                    lanPendingAction = false
                                    lanBusy = false
                                    lanScreen = LanScreen.NONE
                                    revision++
                                }
                            }.onFailure {
                                lanMessage = "Couldn't verify this match. Both devices need the same app version."
                                lanBusy = false
                            }
                        }
                    }
                },
                onConnection = { connected, note ->
                    mainHandler.post {
                        if (lanRole == LanRole.GUEST) {
                            lanConnected = connected
                            lanBusy = false
                            if (connected && !note.isNullOrBlank()) {
                                lanPendingAction = false
                            }
                            if (!connected) {
                                lanScreen = LanScreen.JOIN
                                lanPendingAction = false
                                lanMessage = note ?: "Disconnected. Rejoin the room."
                            } else if (!note.isNullOrBlank()) {
                                lanMessage = note
                            }
                        }
                    }
                }
            )
        }
        val token = if (resume) lanGuest?.rejoinToken.orEmpty() else ""
        runCatching { lanGuest?.connect(host, port, code, token) }.onFailure {
            lanBusy = false
            lanMessage = it.message ?: "Unable to connect."
        }
    }

    fun localAction(action: LanAction) {
        val current = session ?: return
        if (lanRole == LanRole.GUEST) {
            if (!lanConnected || lanPendingAction || current.currentIndex != 1) return
            runCatching {
                lanGuest?.submit(action.copy(
                    expectedMatchId = activeMatchId,
                    expectedRevision = current.revision
                ))
            }.onSuccess { lanPendingAction = true }
                .onFailure { lanMessage = it.message ?: "Reconnect to play." }
            return
        }
        if (lanRole == LanRole.HOST &&
            (!lanConnected || current.currentIndex != 0)
        ) return
        val before = current.snapshot()
        val result = runCatching {
            if (lanRole == LanRole.HOST) {
                LanTurnAuthority.apply(current, 0, action)
            } else {
                when (action.type) {
                    LanActionType.ROLL -> current.roll()
                    LanActionType.ROUTE -> current.chooseRoute(requireNotNull(action.route))
                    LanActionType.TARGET -> current.chooseTarget(requireNotNull(action.target))
                }
            }
        }
        if (result.isFailure) {
            lanMessage = result.exceptionOrNull()?.message ?: "Unavailable move"
            return
        }
        // Only a legal human action gets tactile confirmation. The UI never
        // vibrates for AI turns or changes the result of the die.
        if (touchFeedback && lanRole == LanRole.OFFLINE &&
            action.type != LanActionType.ROLL) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        if (lanRole == LanRole.HOST) {
            lanHost?.publish(current.checkpoint(activeMatchId, matchStartedAtEpochMillis))
        } else {
            persistGame(current)
        }
        cameraTrackIndex = PresentationPacing.mover(before, current.snapshot())
        transitionDelay = PresentationPacing.waitForMovement(before, current.snapshot(), speed)
        turnReady = false
        revision++
    }

    fun localEncounterChoice(choice: String) {
        val current = session ?: return
        if (lanRole != LanRole.OFFLINE ||
            current.players[current.currentIndex].isBot || !turnReady) return
        val before = current.snapshot()
        val outcome = runCatching { current.chooseEncounter(choice) }
        if (outcome.isFailure) {
            lanMessage = outcome.exceptionOrNull()?.message ?: "Unavailable encounter choice"
            return
        }
        if (touchFeedback) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        persistGame(current)
        cameraTrackIndex = PresentationPacing.mover(before, current.snapshot())
        transitionDelay = PresentationPacing.waitForMovement(before, current.snapshot(), speed)
        turnReady = false
        revision++
    }

    fun rematch() {
        val current = session ?: return
        if (current.phase != Phase.FINISHED) return
        if (lanRole == LanRole.HOST) {
            if (!lanConnected) return
            val next = current.freshRematch(java.util.Random().nextInt())
            activeMatchId = java.util.UUID.randomUUID().toString()
            matchStartedAtEpochMillis = System.currentTimeMillis()
            startedFromCheckpoint = false
            session = next
            lanHost?.publish(next.checkpoint(activeMatchId, matchStartedAtEpochMillis))
            revision++
        } else if (lanRole == LanRole.OFFLINE) {
            beginOfflineRace(current.freshRematch(java.util.Random().nextInt()))
        }
    }

    // The OS Back action follows the same protection as Leave Match. Dialogs
    // (including the 3-way junction) own Back while they are visible.
    val localIsCurrentSeat = state != null && (
        lanRole == LanRole.OFFLINE ||
            (lanRole == LanRole.HOST && state.playerIndex == 0 && lanConnected) ||
            (lanRole == LanRole.GUEST && state.playerIndex == 1 && lanConnected)
    )
    val routeChoiceSheetVisible = game != null && state?.phase == Phase.ROUTE &&
        localIsCurrentSeat && !state.players[state.playerIndex].isBot &&
        game.availableRoutes().size >= 2 && turnReady &&
        !routeDialogDismissed && !inspectBoard && !confirmExit && !showGuide
    // A real game viewport hides Android system chrome during an active match.
    // The home / lobby / dialogs retain their normal app presentation.
    val matchIsImmersive = game != null && lanScreen == LanScreen.NONE
    DisposableEffect(matchIsImmersive, context) {
        val activity = context as ComponentActivity
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        if (matchIsImmersive) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    BackHandler(
        enabled = lanScreen == LanScreen.NONE && game != null && state != null &&
            !routeChoiceSheetVisible && !showGuide && !showJournal &&
            !showResults && !inspectBoard && !confirmExit && !showMatchOptions
    ) {
        if (state?.phase == Phase.FINISHED) {
            leaveGame()
        } else {
            confirmExit = true
        }
    }

    BackHandler(enabled = lanScreen != LanScreen.NONE) { leaveGame() }
    if (lanScreen != LanScreen.NONE) {
        CrossroadsLanLobby(
            screen = lanScreen,
            rooms = lanRooms,
            hostAddress = nearby.address(),
            hostPort = lanHost?.port ?: 0,
            hostSeats = lanSeats,
            inviteCode = lanHost?.code.orEmpty(),
            note = lanMessage,
            busy = lanBusy,
            onHost = { requireCode, seats -> startLanHost(requireCode, seats) },
            onJoin = { host, port, code -> joinLan(host, port, code) },
            onRefresh = { nearby.discover { lanRooms = it } },
            onExit = { leaveGame() },
            onRejoin = {
                if (lanLastHost.isNotBlank() && lanLastPort in 1..65535) {
                    joinLan(lanLastHost, lanLastPort, lanLastCode, resume = true)
                }
            },
            hasRejoin = lanGuest?.rejoinToken?.isNotBlank() == true &&
                lanLastHost.isNotBlank()
        )
    } else if (game == null || state == null) {
        CrossroadsHomeScreen(
            wins = wins,
            completed = matches,
            onStart = { seats, localPlayers -> start(seats, localPlayers) },
            selectedCourseId = selectedCourseId,
            onCourseChange = { courseId ->
                if (courseId in knownBoards) selectedCourseId = courseId
            },
            boardLook = boardLook,
            onBoardLookChange = { look ->
                boardLook = look
                prefs.edit().putString("board_look_v1", look.name).apply()
            },
            playtestRules = playtestRules,
            onPlaytestRulesChange = { playtestRules = it },
            fairFinishExperiment = fairFinishExperiment,
            onFairFinishExperimentChange = {
                fairFinishExperiment = it
                prefs.edit().putBoolean("lab_v5_equal_round_opt_in_v1", it).apply()
            },
            onNearbyHost = {
                lanMessage = null
                lanRole = LanRole.HOST
                lanScreen = LanScreen.HOST_SETUP
            },
            onNearbyJoin = {
                lanMessage = null
                lanRole = LanRole.GUEST
                lanScreen = LanScreen.JOIN
            },
            onExportProgress = { exportLauncher.launch("GridMates-Crossroads-backup.json") },
            onImportProgress = {
                importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
            },
            recoveryWarning = recoveryWarning
        )
    } else {
        // UI-52: game-first layout, not a scrolling app page. The real board
        // occupies the playfield; status and contextual controls float above it.
        // All actions still invoke the exact existing match/nearby callbacks.
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
                .background(Color(0xFF304433))
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Remember the BoxWithConstraints scope's dimensions outside
            // descendant Box lambdas, which have their own receiver.
            val viewportHeight = maxHeight
            // The world extends behind both HUD layers. It is a cropped
            // square, never a stretched portrait background or board card.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BoardCanvas(board, state, height = viewportHeight,
                    immersiveWorld = true,
                    matchKey = game,
                    boardLook = boardLook, trailStyle = trailStyle,
                    woodlandPieces = woodlandPieces,
                    paintedMarkers = paintedMarkers,
                    topDownPreview = topDownBoardPreview,
                    routeChoiceIds = if (state.phase == Phase.ROUTE &&
                        localIsCurrentSeat && turnReady &&
                        !state.players[state.playerIndex].isBot)
                        game.availableRoutes().map { it.id } else emptyList(),
                    onRouteChosen = { destination ->
                        // Validate against CURRENT engine state before submitting,
                        // even if a touch started before a turn transition.
                        if (turnReady && localIsCurrentSeat &&
                            state.phase == Phase.ROUTE &&
                            !state.players[state.playerIndex].isBot &&
                            game.availableRoutes().any { it.id == destination }) {
                            localAction(LanAction(LanActionType.ROUTE, route = destination))
                        }
                    },
                    stepMillis = speed.stepMillis, focusPlayerIndex = cameraTrackIndex,
                    isTurnTransitioning = !turnReady,
                    startAtCurrent = startedFromCheckpoint)
            }
            // Die anticipation takes place over the world, not in a dialog.
            // The engine still rolls once after this decorative preview.
            AnimatedVisibility(
                visible = rollingDie && state.phase == Phase.ROLL && localIsCurrentSeat,
                modifier = Modifier.align(Alignment.Center).offset(y = (-82).dp),
                enter = fadeIn(tween(durationMillis = 95)),
                exit = fadeOut(tween(durationMillis = 120))
            ) {
                Surface(
                    color = Color(0xF5F5E6CA),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFD9B776)),
                    shadowElevation = 14.dp,
                    modifier = Modifier.semantics {
                        contentDescription = "Rolling travel die"
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        DieFace(rollingFace,
                            modifier = Modifier.size(66.dp).graphicsLayer {
                                rotationZ = rollingTilt
                            },
                            announceFace = false)
                        Text(
                            when (queuedLabRoll) {
                                LabRoll.STEADY -> "STEADY"
                                LabRoll.PUSH -> "PUSH"
                                else -> "ROLLING…"
                            },
                            color = Forest, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            // Compact on-board scoreboard and controls, without a page header.
            Column(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
                    .background(Color(0xED352C20))
                    .padding(top = 6.dp, bottom = 5.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
            val displayedSeat = (if (!turnReady) cameraTrackIndex else state.playerIndex)
                .coerceIn(state.players.indices)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 1.dp),
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
                    TabletopSeatPlate(
                        seatIndex = i,
                        name = player.name,
                        seatColor = PlayerColors[i],
                        active = isFocused,
                        shielded = player.shield,
                        status = seatStatus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val matchBrand = if (BuildConfig.DEBUG) "CROSSROADS · QA" else "CROSSROADS"
                    Text(
                        CrossroadsCourse.title(board.id),
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = "$matchBrand · ${CrossroadsCourse.title(board.id)}"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8EED7),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (BuildConfig.DEBUG) {
                        Text("QA", color = Color(0xFFF0D7A5),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TabletopUtilityButton(
                            text = "Full map", onClick = { inspectBoard = true }, dark = true
                        )
                    Box {
                        IconButton(
                            onClick = { showMatchOptions = true },
                            modifier = Modifier.size(48.dp).semantics {
                                contentDescription = "Match options"
                            }
                        ) { Text("⋯", fontSize = 25.sp, color = Color(0xFFF8EED7)) }
                        DropdownMenu(
                            expanded = showMatchOptions,
                            onDismissRequest = { showMatchOptions = false },
                            containerColor = Cream
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(if (speed == PresentationSpeed.COZY)
                                        "Movement speed: Cozy" else "Movement speed: Brisk")
                                },
                                onClick = {
                                    speed = if (speed == PresentationSpeed.COZY)
                                        PresentationSpeed.BRISK else PresentationSpeed.COZY
                                    prefs.edit().putString("match_pace_v1", speed.name).apply()
                                    showMatchOptions = false
                                }
                            )
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
                            if (lanRole == LanRole.OFFLINE) {
                                DropdownMenuItem(
                                    text = { Text("Export progress") },
                                    onClick = {
                                        showMatchOptions = false
                                        exportLauncher.launch("GridMates-Crossroads-backup.json")
                                    }
                                )
                            }
                            HorizontalDivider()
                            if (BuildConfig.DEBUG) {
                                if (CourseOverheadStyle.forBoard(board.id) != null &&
                                    boardLook == CrossroadsBoardLook.ILLUSTRATED) {
                                DropdownMenuItem(
                                    text = {
                                        Text(if (topDownBoardPreview)
                                            "${if (CourseOverheadStyle.forBoard(board.id) == CourseOverheadStyle.WOVEN) "Woven" else "Course"} board art: Overhead (QA)"
                                        else "${if (CourseOverheadStyle.forBoard(board.id) == CourseOverheadStyle.WOVEN) "Woven" else "Course"} board art: Storybook (QA)")
                                    },
                                    onClick = {
                                        topDownBoardPreview = !topDownBoardPreview
                                        prefs.edit().putBoolean(
                                            "qa_overhead_board_v1", topDownBoardPreview
                                        ).apply()
                                        showMatchOptions = false
                                    }
                                )
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text("Trail style: ${trailStyle.label}")
                                    },
                                    onClick = {
                                        trailStyle = trailStyle.next()
                                        prefs.edit().putString(
                                            "qa_trail_style_v2", trailStyle.name
                                        ).apply()
                                        showMatchOptions = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(if (woodlandPieces) "Pieces: Woodland (zoom detail)"
                                            else "Pieces: Standard")
                                    },
                                    onClick = {
                                        woodlandPieces = !woodlandPieces
                                        prefs.edit().putBoolean(
                                            "qa_woodland_pieces_v1", woodlandPieces
                                        ).apply()
                                        showMatchOptions = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(if (paintedMarkers) "Markers: Illustrated"
                                            else "Markers: Standard")
                                    },
                                    onClick = {
                                        paintedMarkers = !paintedMarkers
                                        prefs.edit().putBoolean(
                                            "qa_painted_markers_v1", paintedMarkers
                                        ).apply()
                                        showMatchOptions = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(if (touchFeedback) "Touch feedback: On"
                                        else "Touch feedback: Off")
                                },
                                onClick = {
                                    touchFeedback = !touchFeedback
                                    prefs.edit().putBoolean(
                                        "feel_touch_feedback_v1", touchFeedback
                                    ).apply()
                                    showMatchOptions = false
                                }
                            )
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

            }
            // EXPERIENCE-60: controls come BEFORE lengthy explanatory text.
            // A short, stable action island is more useful than a scrollable form.
            // Details never push the primary action offscreen.
            var showTurnDetails by remember(game, state.turn, state.phase) {
                mutableStateOf(false)
            }
            val dockMaxFraction = gameActionDockMaxFraction(state.phase)
            TabletopActionTray(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(horizontal = 10.dp, vertical = 9.dp)
                    .fillMaxWidth()
                    .heightIn(
                        max = maxOf(
                            maxHeight * dockMaxFraction,
                            gameActionDockMinimumHeight(state.phase)
                        ).coerceAtMost((maxHeight - 20.dp).coerceAtLeast(1.dp))
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val round = (state.turn - 1) / state.players.size + 1
                    val currentName = state.players[state.playerIndex].name
                    val trackedName = state.players[
                        cameraTrackIndex.coerceIn(state.players.indices)
                    ].name
                    GameHudTurnStrip(
                        title = if (state.phase == Phase.FINISHED && !turnReady)
                            "Approaching the finish…"
                        else if (state.phase == Phase.FINISHED) "Journey complete!"
                        else if (game.finalRoundPending) "FINAL CHANCE · $currentName"
                        else if (!turnReady && !rollingDie) {
                            if (trackedName == "You") "You're on the move…"
                            else "$trackedName is on the move…"
                        } else if (currentName == "You") "Round $round · Your turn"
                        else "Round $round · $currentName's turn",
                        phase = state.phase,
                        moving = !turnReady && !rollingDie,
                        seatColor = PlayerColors[
                            (if (!turnReady) cameraTrackIndex else state.playerIndex)
                                .coerceIn(state.players.indices)
                        ],
                        botTurn = state.players[state.playerIndex].isBot,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (recoveryWarning != null) {
                        Text(recoveryWarning!!, color = Color(0xFF955B33),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    if (lanRole == LanRole.OFFLINE &&
                        state.players.all { !it.isBot } && state.turn > 1 &&
                        state.phase == Phase.ROLL && turnReady && !rollingDie
                    ) {
                        Text("Pass the phone to $currentName",
                            color = Bark, style = MaterialTheme.typography.labelMedium)
                    }
                    if (lanRole != LanRole.OFFLINE && !lanMessage.isNullOrBlank()) {
                        Text(lanMessage!!, color = Color(0xFF955B33),
                            style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                    val initialInstructions = state.turn == 1 &&
                        state.phase == Phase.ROLL && state.die == null &&
                        state.encounters.isEmpty()
                    val actionNote = if (game.finalRoundPending)
                            "Final round · ${game.leadingFinisherName} is at Finish. Remaining racers get one last turn."
                        else if (initialInstructions) "Ready for your first move"
                        else turnActionSummary(
                            phase = state.phase,
                            botTurn = state.players[state.playerIndex].isBot,
                            actorName = currentName,
                            latestAction = state.lastMessage
                        )
                    // Long encounter explanations should not be clipped while
                    // an otherwise empty action dock occupies the bottom.
                    // The encounter card itself explains the human's next action.
                    // Avoid a redundant two-line explanation above it, which
                    // pushed event choices off shorter portrait screens.
                    if (showCollapsedActionNote(
                            phase = state.phase,
                            humanEncounterChoiceReady =
                                state.phase == Phase.ENCOUNTER &&
                                localIsCurrentSeat &&
                                !state.players[state.playerIndex].isBot &&
                                turnReady
                        )
                    ) {
                        Text(actionNote, color = Bark,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    val die = state.die
                    if (die != null && state.phase != Phase.FINISHED) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            DieFace(die, modifier = Modifier.size(25.dp))
                            Text(
                                "${state.remainingSteps} ${if (state.remainingSteps == 1) "step" else "steps"} to move",
                                color = Forest,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    // Expandable information takes only spare height. Primary
                    // action controls below stay at the bottom and always remain
                    // reachable, including at larger Android font scales.
                    if (showTurnDetails) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(actionNote, style = MaterialTheme.typography.bodySmall,
                                color = Bark)
                            when (state.phase) {
                                Phase.ROLL -> {
                                    Text(
                                        if (game.playtestRules && lanRole == LanRole.OFFLINE)
                                            "Steady moves 4 and blocks one setback. Push: 1–2 stumbles 1 space; 3–6 moves 5–8."
                                        else "A fair six-sided die decides how many spaces you travel.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Bark
                                    )
                                }
                                Phase.ROUTE -> Text(
                                    "Tap a highlighted trail, or compare the risk and remaining distance of each legal path.",
                                    style = MaterialTheme.typography.bodySmall, color = Bark
                                )
                                Phase.ENCOUNTER -> Text(
                                    if (game.pendingPushHazardBonus > 0)
                                        "Supplies: +2. Cover: +1 and shield. Gamble: hazard; successful advance gets +2 Push bonus. Brace: stop and shield."
                                    else "Supplies: advance 2. Cover: advance 1 and gain a shield. Gamble: face the hazard. Brace: stop and gain a shield.",
                                    style = MaterialTheme.typography.bodySmall, color = Bark
                                )
                                else -> Unit
                            }
                            if (state.phase != Phase.FINISHED && recentReveal != null) {
                                EncounterRibbon(recentReveal!!)
                            }
                        }
                    }
                    val myAction = localIsCurrentSeat && !state.players[state.playerIndex].isBot
                    if (state.phase == Phase.FINISHED && !turnReady) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else if (state.phase == Phase.FINISHED) {
                        TabletopPrimaryActionButton(
                            text = "SEE RACE RESULTS",
                            onClick = { showResults = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (lanRole != LanRole.OFFLINE &&
                        (!lanConnected || lanPendingAction ||
                            state.playerIndex != (if (lanRole == LanRole.HOST) 0 else 1))
                    ) {
                        Text(
                            if (!lanConnected) "Waiting for your partner to reconnect…"
                            else if (lanPendingAction) "Waiting for the host to confirm…"
                            else "$currentName is taking a turn…",
                            color = Bark, style = MaterialTheme.typography.bodySmall
                        )
                    } else if (!turnReady && !rollingDie) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Following the traveler…",
                            color = Bark, style = MaterialTheme.typography.labelMedium)
                    } else if (!myAction) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Watching $currentName play…",
                            color = Bark, style = MaterialTheme.typography.labelMedium)
                    } else {
                        when (state.phase) {
                            Phase.ROLL -> {
                                if (game.playtestRules && lanRole == LanRole.OFFLINE) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TabletopActionTile(
                                            title = "STEADY",
                                            detail = "4 spaces",
                                            onClick = {
                                                queuedLabRoll = LabRoll.STEADY
                                                rollingDie = true
                                            },
                                            enabled = turnReady && !rollingDie,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TabletopActionTile(
                                            title = "PUSH",
                                            detail = "Risk for speed",
                                            warm = true,
                                            onClick = {
                                                queuedLabRoll = LabRoll.PUSH
                                                rollingDie = true
                                            },
                                            enabled = turnReady && !rollingDie,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                } else {
                                    TabletopPrimaryActionButton(
                                        text = if (rollingDie) "ROLLING…" else "ROLL THE DIE",
                                        onClick = {
                                            if (touchFeedback) {
                                                haptics.performHapticFeedback(
                                                    HapticFeedbackType.TextHandleMove
                                                )
                                            }
                                            rollingDie = true
                                        },
                                        enabled = !rollingDie && turnReady &&
                                            (lanRole == LanRole.OFFLINE ||
                                                (lanConnected && !lanPendingAction)),
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                                        withDie = true
                                    )
                                }
                            }
                            Phase.ROUTE -> {
                                val fork = board.forkAt(state.players[state.playerIndex].position)
                                val routeCount = game.availableRoutes().size
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(fork?.title ?: "Choose a path",
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Bold, color = Forest,
                                        maxLines = 1)
                                    Text("$routeCount paths", color = Bark,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                                TabletopPrimaryActionButton(
                                    text = "COMPARE OR CHOOSE PATHS",
                                    onClick = { routeDialogDismissed = false },
                                    enabled = turnReady && routeCount >= 2,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Phase.ENCOUNTER -> {
                                val choices = game.availableEncounterChoices()
                                val art = rememberOptionalCrossroadsArt(
                                    if (TabletopEncounterPresentation.isShelter(choices))
                                        CrossroadsArtSlot.EVENT_SHELTER
                                    else CrossroadsArtSlot.EVENT_HAZARD
                                )
                                // One compact illustrated tabletop card holds
                                // the actual legal actions. No extra choice
                                // screen, copied outcome, or decorative delay.
                                TabletopEncounterCard(
                                    choices = choices,
                                    artwork = art,
                                    enabled = turnReady,
                                    pushHazardSuccessBonus = game.pendingPushHazardBonus,
                                    onChoose = { option -> localEncounterChoice(option) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Phase.TARGET -> {
                                Text("Choose a rival", color = Forest, fontWeight = FontWeight.Bold)
                                game.availableTargets().chunked(2).forEach { pair ->
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                        pair.forEach { target ->
                                            TabletopActionTile(
                                                title = state.players[target].name,
                                                detail = if (state.players[target].shield)
                                                    "Shielded" else "Choose rival",
                                                onClick = {
                                                    localAction(LanAction(
                                                        LanActionType.TARGET, target = target
                                                    ))
                                                },
                                                enabled = turnReady,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                            Phase.FINISHED -> Unit
                        }
                    }
                    if (state.phase != Phase.FINISHED && (myAction || showTurnDetails)) {
                        TextButton(
                            onClick = { showTurnDetails = !showTurnDetails },
                            modifier = Modifier.align(Alignment.End).heightIn(min = 36.dp)
                        ) {
                            Text(if (showTurnDetails) "Less detail" else "Rules & last action",
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
        // One optional, scrollable route-comparison sheet for two or three exits.
        // The primary action remains touching a highlighted trail on the board.
        if (state.phase == Phase.ROUTE && localIsCurrentSeat &&
            !state.players[state.playerIndex].isBot &&
            game.availableRoutes().size >= 2 && turnReady &&
            !routeDialogDismissed && !inspectBoard && !confirmExit && !showGuide) {
            val junction = board.forkAt(state.players[state.playerIndex].position)
            val routeChoiceScroll = rememberScrollState()
            // Keep all legal route details readable, without a platform dialog.
            // Only the choice list scrolls on short phones;
            // the title and Inspect full map action stay accessible.
            Dialog(
                onDismissRequest = { routeDialogDismissed = true },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(max = (maxHeight * 0.76f).coerceAtLeast(200.dp)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = 24.dp, topEnd = 24.dp
                        ),
                        color = Color(0xFFF1E4C8),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp, Color(0xFF9D865E)
                        ),
                        shadowElevation = 12.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 17.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                junction?.title ?: "Choose your path",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )
                            Text(
                                "${game.availableRoutes().size} paths · ${state.remainingSteps} " +
                                    "${if (state.remainingSteps == 1) "step" else "steps"} left",
                                style = MaterialTheme.typography.bodySmall,
                                color = Bark
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .verticalScroll(routeChoiceScroll),
                                verticalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                game.availableRoutes().forEach { route ->
                                    val choice = junction?.choices?.firstOrNull { it.to == route.id }
                                    // A clickable Surface sizes to ALL text lines.
                                    // OutlinedButton's internal layout allowed the final
                                    // line of Cedar Bridge to paint over its lower border.
                                    Surface(
                                        onClick = {
                                            localAction(LanAction(LanActionType.ROUTE, route = route.id))
                                        },
                                        enabled = turnReady,
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
                                        color = TabletopMaterials.paper,
                                        contentColor = Forest,
                                        shadowElevation = 3.dp,
                                        border = androidx.compose.foundation.BorderStroke(
                                            2.dp, TabletopMaterials.brass
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(
                                                horizontal = 14.dp, vertical = 15.dp
                                            )
                                        ) {
                                            RouteChoiceSummary(
                                                label = choice?.label ?: route.id,
                                                description = if (game.playtestRules)
                                                        DecisionLab.routePreview(
                                                            board, route.id, choice?.risk,
                                                            game.steadyTravelChosen,
                                                            game.routeEncounterAvailable
                                                        )
                                                    else (choice?.description ?: "Follow this trail"),
                                                risk = choice?.risk
                                            )
                                        }
                                    }
                                }
                            }
                            if (routeChoiceScroll.maxValue > routeChoiceScroll.value) {
                                Text(
                                    "Scroll for more route details ↓",
                                    color = Forest,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TabletopUtilityButton(
                                    text = "Back to board",
                                    onClick = { routeDialogDismissed = true },
                                    modifier = Modifier.weight(1f)
                                )
                                TabletopUtilityButton(
                                    // Keep the legal route decision intact while inspecting.
                                    text = "Full map", onClick = { inspectBoard = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showGuide) {
            CrossroadsHowToPlayDialog(onDismiss = { showGuide = false })
        }
        if (showJournal) {
            AlertDialog(
                onDismissRequest = { showJournal = false },
                containerColor = Cream,
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
            // Large, readable read-only game map; this surface intentionally
            // cannot send a ROUTE action or modify a saved position.
            Dialog(
                onDismissRequest = { inspectBoard = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                        .background(Color(0xDB142419))
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .heightIn(max = (maxHeight - 12.dp).coerceAtLeast(220.dp)),
                        color = Cream,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp, Color(0xFFD0B98D)
                        ),
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("THE COURSE", color = Bark,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall)
                                    Text(CrossroadsCourse.title(board.id), color = Forest,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge)
                                }
                                TabletopUtilityButton(
                                    text = "Close map", onClick = { inspectBoard = false }
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF263D2C),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        2.dp, Color(0xFFC5A773)
                                    )
                                ) {
                                    BoxWithConstraints(
                                        modifier = Modifier.fillMaxWidth().padding(3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Fill the available inner width with a square board. The
                                        // scroll container still supports short/landscape screens.
                                        val mapHeight = maxWidth
                                        BoardCanvas(
                                            board, state,
                                            height = mapHeight, tokenScale = 1.35f,
                                            animateTokens = false, matchKey = game,
                                            boardLook = boardLook, trailStyle = trailStyle,
                                            woodlandPieces = woodlandPieces,
                                            paintedMarkers = paintedMarkers,
                                            topDownPreview = topDownBoardPreview
                                        )
                                    }
                                }
                                Text("Amber: hazard  •  Green: shelter  •  Purple: mischief",
                                    style = MaterialTheme.typography.bodySmall, color = Bark)
                                Text("Pinch to inspect · drag to explore",
                                    style = MaterialTheme.typography.labelSmall, color = Bark)
                            }
                            TabletopPrimaryActionButton(
                                text = if (state.phase == Phase.ROUTE && localIsCurrentSeat &&
                                    game.availableRoutes().size >= 2 && !routeDialogDismissed)
                                    "RETURN TO ROUTE CHOICES" else "BACK TO THE TRAIL",
                                onClick = { inspectBoard = false },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        if (showResults && state.phase == Phase.FINISHED && turnReady) {
            // The race outcome is a game moment, not a generic settings popup.
            // The summary scrolls; rematch and return actions stay visible.
            Dialog(
                onDismissRequest = { showResults = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                        .background(Color(0xD914251A))
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .heightIn(max = (maxHeight - 24.dp).coerceAtLeast(260.dp)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
                        color = Cream,
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp, Color(0xFFC9AE78)
                        ),
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                color = Color(0xFF284C38),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp, Color(0xFFD9C08C)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text("JOURNEY COMPLETE", color = Color(0xFFFFF0CD),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium)
                                    Text(CrossroadsCourse.title(board.id),
                                        color = Color(0xFFE6D5AA),
                                        style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            CrossroadsResultsContent(
                                state = state,
                                courseId = board.id,
                                elapsedSeconds = finishedElapsedSeconds,
                                finishReceipt = game.fairFinishReceipt,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            TabletopPrimaryActionButton(
                                text = if (lanRole == LanRole.GUEST)
                                    "HOST STARTS NEXT RACE" else "RACE AGAIN",
                                onClick = { rematch() },
                                enabled = lanRole != LanRole.GUEST &&
                                    (lanRole != LanRole.HOST || lanConnected),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TabletopUtilityButton(
                                    text = "View course",
                                    onClick = {
                                        showResults = false
                                        inspectBoard = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                TabletopUtilityButton(
                                    text = "Main menu",
                                    onClick = { leaveGame() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
        if (confirmExit) {
            AlertDialog(
                onDismissRequest = { confirmExit = false },
                containerColor = Cream,
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
    if (importPending != null) {
        val candidate = requireNotNull(importPending)
        AlertDialog(
            onDismissRequest = { importPending = null },
            containerColor = Cream,
            title = { Text("Replace local progress?") },
            text = {
                val explanation = if (candidate.restoredMatch != null) {
                    " and a saved offline race. Importing replaces this device's current records and saved race."
                } else {
                    ". Importing replaces this device's current records and removes any saved race."
                }
                Text("This backup contains ${candidate.bundle.completed} finished matches, " +
                    "${candidate.bundle.wins} solo wins" + explanation)
            },
            confirmButton = {
                TextButton(onClick = {
                    if (lanRole != LanRole.OFFLINE || session != null) {
                        transferMessage = "Finish or leave the current game before importing."
                    } else {
                        val imported = runCatching { recoveryStore.importProgress(candidate) }
                            .getOrDefault(false)
                        if (imported) {
                            matches = prefs.getInt("completed", 0)
                            wins = prefs.getInt("wins", 0)
                            val restored = recoveryStore.load()
                            session = restored.match?.game
                            activeMatchId = restored.match?.matchId.orEmpty()
                            matchStartedAtEpochMillis = restored.match?.startedAtEpochMillis ?: 0L
                            startedFromCheckpoint = restored.match != null
                            if (restored.match != null) {
                                selectedCourseId = restored.match.game.board.id
                            }
                            recoveryWarning = null
                            revision++
                            transferMessage = "Local progress imported. Keep your backup file until you have checked your records."
                        } else {
                            transferMessage = "Import failed. Your existing local progress was not replaced."
                        }
                    }
                    importPending = null
                }) { Text("Replace & import") }
            },
            dismissButton = {
                TextButton(onClick = { importPending = null }) { Text("Keep my data") }
            }
        )
    }
    if (transferMessage != null) {
        AlertDialog(
            onDismissRequest = { transferMessage = null },
            containerColor = Cream,
            title = { Text("Local backup") },
            text = { Text(transferMessage.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { transferMessage = null }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun BoardCanvas(
    board: BoardDefinition,
    state: Snapshot,
    height: Dp = 315.dp,
    immersiveWorld: Boolean = false,
    boardLook: CrossroadsBoardLook = CrossroadsBoardLook.ILLUSTRATED,
    trailStyle: CrossroadsTrailStyle = CrossroadsTrailStyle.SIMPLE,
    woodlandPieces: Boolean = false,
    paintedMarkers: Boolean = false,
    topDownPreview: Boolean = false,
    routeChoiceIds: List<String> = emptyList(),
    onRouteChosen: (String) -> Unit = {},
    tokenScale: Float = 1f,
    animateTokens: Boolean = true,
    matchKey: Any? = null,
    stepMillis: Int = PresentationSpeed.COZY.stepMillis,
    focusPlayerIndex: Int = state.playerIndex,
    isTurnTransitioning: Boolean = false,
    startAtCurrent: Boolean = false
) {
    // The scene is data-independent: scenery and optional illustrated pawns
    // are drawn beneath the authoritative routes and numeric seat labels.
    val useOverheadArtwork = useOverheadBoardPreview(
        boardId = board.id, debug = BuildConfig.DEBUG,
        requested = topDownPreview,
        illustrated = boardLook == CrossroadsBoardLook.ILLUSTRATED
    )
    val boardBackdrop = rememberOptionalCrossroadsArt(
        if (boardLook == CrossroadsBoardLook.ILLUSTRATED &&
            (!useOverheadArtwork || CourseOverheadStyle.forBoard(board.id) != CourseOverheadStyle.WOVEN))
            CrossroadsArtCatalog.backplate(board.id) else null
    )
    val environmentAtlas = rememberOptionalCrossroadsArt(
        if (useOverheadArtwork) CrossroadsArtSlot.ENVIRONMENT_ATLAS else null
    )
    val trailAtlas = rememberOptionalCrossroadsArt(
        if (BuildConfig.DEBUG && boardLook == CrossroadsBoardLook.ILLUSTRATED)
            CrossroadsArtSlot.TRAIL_ATLAS else null
    )
    val effectiveTrails = effectiveTrailStyle(
        wanted = if (BuildConfig.DEBUG) trailStyle else CrossroadsTrailStyle.SIMPLE,
        illustratedBoard = boardLook == CrossroadsBoardLook.ILLUSTRATED,
        atlasPresent = trailAtlas != null
    )
    val woodlandTokenAtlas = rememberOptionalCrossroadsArt(
        if (BuildConfig.DEBUG && woodlandPieces) CrossroadsArtSlot.TOKEN_ATLAS else null
    )
    val showWoodlandTokens = shouldShowWoodlandToken(
        isQa = BuildConfig.DEBUG,
        requested = woodlandPieces,
        atlasLoaded = woodlandTokenAtlas != null
    )
    val markerAtlas = rememberOptionalCrossroadsArt(
        if (BuildConfig.DEBUG && paintedMarkers &&
            boardLook == CrossroadsBoardLook.ILLUSTRATED)
            CrossroadsArtSlot.MARKER_ATLAS else null
    )
    val showPaintedMarkers = woodlandMarkerEnabled(
        isDebug = BuildConfig.DEBUG,
        requested = paintedMarkers,
        atlasLoaded = markerAtlas != null,
        illustratedBoard = boardLook == CrossroadsBoardLook.ILLUSTRATED
    )
    // Pure, fixed overhead scenery positions are remembered per authored
    // graph; no per-frame random generation or fake game path data.
    val courseOverheadStyle = CourseOverheadStyle.forBoard(board.id)
    val wovenOverhead = useOverheadArtwork && courseOverheadStyle == CourseOverheadStyle.WOVEN
    val siblingAccents = remember(board, courseOverheadStyle, useOverheadArtwork) {
        if (useOverheadArtwork && courseOverheadStyle != null)
            CourseOverheadScenery.plan(courseOverheadStyle, board.nodes) else emptyList()
    }
    val overheadDecor = remember(board, wovenOverhead) {
        if (wovenOverhead) OverheadScenery.plan(board.nodes) else emptyList()
    }
    val environmentAccents = remember(board, overheadDecor, wovenOverhead) {
        if (wovenOverhead) WovenEnvironmentAccents.plan(board.nodes, overheadDecor)
        else emptyList()
    }
    val overheadBridges = remember(board, wovenOverhead) {
        if (wovenOverhead) OverheadScenery.bridges(board.nodes) else emptyList()
    }
    // The live world magnifies interactive art; the full-map inspector keeps
    // its previous compact geometry for legibility and path overview.
    val gamePieceScale = tokenScale * CrossroadsCameraPreset.tokenScale(immersiveWorld)
    val roadVisibilityScale = CrossroadsCameraPreset.trailScale(immersiveWorld)
    val nodeVisibilityScale = CrossroadsCameraPreset.markerScale(immersiveWorld)
    // Cached nearest-node spacing keeps wooden landing discs apart on dense courses.
    // The exact graph and node coordinates remain authoritative.
    val nearestSpaceSpacing = remember(board) {
        board.nodes.associate { node ->
            node.id to CrossroadsBoardSpaceGeometry.nearestSpacing(node, board.nodes)
        }
    }
    val seatImages = listOf(
        rememberOptionalCrossroadsArt(CrossroadsArtSlot.TOKEN_SEAT_1),
        rememberOptionalCrossroadsArt(CrossroadsArtSlot.TOKEN_SEAT_2),
        rememberOptionalCrossroadsArt(CrossroadsArtSlot.TOKEN_SEAT_3),
        rememberOptionalCrossroadsArt(CrossroadsArtSlot.TOKEN_SEAT_4)
    )
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
    // A separate, purely visual hop runs alongside each existing graph step.
    // It never modifies the authoritative position or movement timing.
    val hopProgress = remember(matchKey, board.id, state.players.size, animateTokens) {
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
                hopProgress[i].snapTo(0f)
                motion[i].snapTo(Offset(board.node(player.position).x, board.node(player.position).y))
            } else {
                val nodes = MoveTrail.steps(previous, path)
                previous = path
                // Use brief segments along the exact route, including backwards pranks.
                for (id in nodes) {
                    val target = board.node(id)
                    hopProgress[i].snapTo(0f)
                    kotlinx.coroutines.coroutineScope {
                        launch {
                            hopProgress[i].animateTo(
                                1f, animationSpec = tween(durationMillis = stepMillis)
                            )
                        }
                        motion[i].animateTo(
                            Offset(target.x, target.y),
                            animationSpec = tween(durationMillis = stepMillis)
                        )
                    }
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
    var lastCenteredTurn by remember(matchKey, board.id, animateTokens) {
        mutableIntStateOf(-1)
    }
    // Only re-enter follow mode once a NEW roll is ready. Never interrupt an
    // ongoing step animation or override a player's pan during that turn.
    LaunchedEffect(matchKey, board.id, state.turn, state.phase,
        isTurnTransitioning, immersiveWorld, animateTokens) {
        if (BoardFollowTurnPolicy.shouldRecenter(
                phase = state.phase,
                turn = state.turn,
                lastCenteredTurn = lastCenteredTurn,
                immersiveWorld = immersiveWorld,
                animateTokens = animateTokens,
                isTurnTransitioning = isTurnTransitioning
            )
        ) {
            cameraMode = BoardCameraMode.FOLLOW
            manualZoom = 1f
            manualTranslation = Offset.Zero
            lastCenteredTurn = state.turn
        }
    }
    val cameraSubject = focusPlayerIndex.coerceIn(displayed.indices)
    val focusPlayerName = state.players[cameraSubject].name
    val focusRegion = board.node(state.players[cameraSubject].position).region
    val cameraDescription = "${CrossroadsCourse.title(board.id)} board. $focusPlayerName in $focusRegion."
    // The snapshot may have advanced to the next player while a piece is still
    // visibly traveling. The highlighted token follows the actual presentation.
    val highlightedSeat = if (isTurnTransitioning && animateTokens)
        cameraSubject else state.playerIndex
    val active = displayed[cameraSubject]
    val viewportWidth = canvasDimensions.width.toFloat()
    val viewportHeight = canvasDimensions.height.toFloat()
    val worldFrame = BoardWorldViewport.frame(canvasDimensions, immersiveWorld)
    val atFork = state.phase == Phase.ROUTE && focusPlayerIndex == state.playerIndex
    val forkNode = if (atFork) board.node(state.players[state.playerIndex].position) else null
    val forkExits = forkNode?.next?.map { dest ->
        board.node(dest).let { Offset(it.x, it.y) }
    }.orEmpty()
    val forkPreview = if (immersiveWorld && animateTokens && forkNode != null &&
        forkExits.size >= 2 && viewportWidth > 0f && viewportHeight > 0f) {
        CrossroadsCameraPreset.forkPreview(
            Offset(forkNode.x, forkNode.y), forkExits, worldFrame,
            viewportWidth, viewportHeight
        )
    } else null
    val cameraTarget = forkPreview?.focus ?: if (forkNode != null) {
        CrossroadsCameraPreset.forkAnchor(
            source = Offset(forkNode.x, forkNode.y),
            exits = forkExits,
            immersive = immersiveWorld
        )
    } else active
    val followedFocus by animateOffsetAsState(
        targetValue = cameraTarget,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "follow-board-position"
    )
    val autoZoom by animateFloatAsState(
        targetValue = when (cameraMode) {
            BoardCameraMode.FOLLOW -> forkPreview?.zoom ?: CrossroadsCameraPreset.followZoom(
                state.phase, state.turn, animateTokens
            )
            BoardCameraMode.MANUAL -> manualZoom
        },
        animationSpec = tween(durationMillis = 330),
        label = "board-camera-zoom"
    )
    val cameraZoom = if (cameraMode == BoardCameraMode.MANUAL) manualZoom else autoZoom
    val drawWoodlandPortraits = shouldDrawWoodlandPortrait(
        artAvailable = showWoodlandTokens,
        cameraZoom = cameraZoom
    )
    val focusPx = worldFrame.position(followedFocus.x, followedFocus.y)
    val desiredTranslation = when (cameraMode) {
        BoardCameraMode.FOLLOW -> Offset(
            CameraGeometry.centerTranslation(viewportWidth / 2f, focusPx.x, cameraZoom),
            CameraGeometry.centerTranslation(viewportHeight / 2f, focusPx.y, cameraZoom) +
                viewportHeight * ((if (forkPreview != null)
                    CrossroadsCameraPreset.FORK_PREVIEW_HEIGHT
                else CrossroadsCameraPreset.focusHeightFraction(
                    state.phase, immersiveWorld
                )) - 0.5f)
        )
        BoardCameraMode.MANUAL -> manualTranslation
    }
    val actualTranslation = Offset(
        CameraGeometry.boundTranslation(desiredTranslation.x, viewportWidth,
            worldFrame.left, worldFrame.right, cameraZoom),
        if (cameraMode == BoardCameraMode.FOLLOW)
            CrossroadsCameraPreset.boundFollowVertical(
                desiredTranslation.y, viewportHeight, worldFrame.top,
                worldFrame.bottom, cameraZoom, state.phase, immersiveWorld
            )
        else CameraGeometry.boundTranslation(desiredTranslation.y, viewportHeight,
            worldFrame.top, worldFrame.bottom, cameraZoom)
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
    val focusedOffset = CrossroadsPiecePlacement.offset(
        focusedRank.coerceAtLeast(0), focusedGroup.size.coerceAtLeast(1), gamePieceScale
    )
    val livePieceLocation by rememberUpdatedState(
        worldFrame.position(active.x, active.y) + focusedOffset
    )
    // Pointer handlers persist across Compose recompositions; keep the latest
    // legal destinations and action callback bound to the latest phase.
    val liveRouteDestinations by rememberUpdatedState(
        routeChoiceIds.mapNotNull { board.index[it] }
    )
    val liveRouteOrigin by rememberUpdatedState(
        board.index[state.players[state.playerIndex].position]
    )
    val liveRouteChosen by rememberUpdatedState(onRouteChosen)
    val density = LocalDensity.current
    val insetPx = with(density) { if (immersiveWorld) 0f else 8.dp.toPx() }
    val pieceTapRadiusPx = with(density) { 32.dp.toPx() }
    val routeTapRadiusPx = with(density) { 26.dp.toPx() }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263D2C)),
        shape = if (immersiveWorld) androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
            else MaterialTheme.shapes.medium
    ) {
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
                            val routeHit = liveRouteOrigin?.let { startNode ->
                                BoardRouteHitTest.select(
                                    tap = tap,
                                    origin = startNode,
                                    destinations = liveRouteDestinations,
                                    canvas = liveViewportSize,
                                    insetPx = insetPx,
                                    zoom = liveZoom,
                                    translation = liveTranslation,
                                    touchRadiusPx = routeTapRadiusPx,
                                    immersiveWorld = immersiveWorld
                                )
                            }
                            if (routeHit != null) {
                                liveRouteChosen(routeHit)
                            } else if (animateTokens) {
                                // Preserve prior tap-to-recenter behavior outside choices.
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
                                worldFrame.left, worldFrame.right, nextZoom),
                            CameraGeometry.boundTranslation(nextTranslation.y, canvasDimensions.height.toFloat(),
                                worldFrame.top, worldFrame.bottom, nextZoom)
                        )
                        cameraMode = BoardCameraMode.MANUAL
                    }
                }
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
                    .then(if (immersiveWorld) Modifier else Modifier.padding(8.dp))
                    .onSizeChanged { canvasDimensions = it }
                    .graphicsLayer {
                        scaleX = cameraZoom
                        scaleY = cameraZoom
                        translationX = actualTranslation.x
                        translationY = actualTranslation.y
                    }
            ) {
            val frame = BoardWorldViewport.frame(
                IntSize(size.width.toInt(), size.height.toInt()), immersiveWorld
            )
            val left = frame.left
            val top = frame.top
            val drawWidth = frame.width
            val drawHeight = frame.height
            fun position(id: String): Offset {
                val node = board.node(id)
                return Offset(left + node.x * drawWidth, top + node.y * drawHeight)
            }
            // Artwork is strictly below all navigable routes, nodes, pieces and
            // hit targets. A missing art slot preserves the tested ART-08 drawing.
            // Both use the same normalized board frame and camera transform.
            if (useOverheadArtwork && !wovenOverhead && courseOverheadStyle != null &&
                environmentAtlas != null && EnvironmentAtlas.validDimensions(environmentAtlas.width, environmentAtlas.height)) {
                drawSiblingCourseOverhead(courseOverheadStyle, siblingAccents, environmentAtlas,
                    left, top, drawWidth, drawHeight)
            } else if (wovenOverhead) {
                // Same graph, different VIEWPOINT: overhead trees, rock,
                // rooftops and clearings rather than a landscape horizon.
                drawOverheadWoodlandPrint(
                    nodes = board.nodes,
                    features = overheadDecor,
                    bridges = overheadBridges,
                    left = left, top = top, width = drawWidth, height = drawHeight,
                    environmentAtlas = environmentAtlas,
                    environmentAccents = environmentAccents
                )
            } else if (boardBackdrop == null) {
                drawWoodlandBoardArt(
                    boardId = board.id,
                    left = left, top = top, width = drawWidth, height = drawHeight
                )
            } else {
                drawImage(
                    image = boardBackdrop,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(boardBackdrop.width, boardBackdrop.height),
                    dstOffset = IntOffset(left.toInt(), top.toInt()),
                    dstSize = IntSize(drawWidth.toInt().coerceAtLeast(1),
                        drawHeight.toInt().coerceAtLeast(1)),
                    filterQuality = FilterQuality.Medium
                )
            }
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
            // Longer authored courses have more route nodes in the same viewport.
            // Reduce plain-node and trail thickness just for this dense atlas.
            val denseBoard = DecisionLab.baseId(board.id) == CrossroadsCourse.LONG_ID
            // Shared board geography never changes with the event seed.
            board.nodes.forEach { node ->
                val from = position(node.id)
                node.next.forEach { dest ->
                    val route = when {
                        board.node(dest).route != "neutral" -> board.node(dest).route
                        node.route != "neutral" -> node.route
                        else -> "neutral"
                    }
                    if (effectiveTrails == CrossroadsTrailStyle.ARTWORK && trailAtlas != null &&
                        immersiveWorld && boardLook == CrossroadsBoardLook.ILLUSTRATED) {
                        // VISUAL-83: actual live roads are carved at Canvas resolution,
                        // not stretched from a 128x32 atlas at 2-4x zoom.
                        // Original coordinates, route colors and tap hit geometry stay identical.
                        drawCrossroadsCarvedTrail(
                            from = from,
                            to = position(dest),
                            route = route,
                            dense = denseBoard,
                            visibilityScale = roadVisibilityScale
                        )
                    } else if (effectiveTrails == CrossroadsTrailStyle.ARTWORK && trailAtlas != null) {
                        // The miniature Full Map deliberately keeps its earlier
                        // detailed route atlas for a consistent whole-course overview.
                        drawCrossroadsIllustratedTrail(
                            from = from,
                            to = position(dest),
                            route = route,
                            dense = denseBoard,
                            atlas = trailAtlas,
                            visibilityScale = roadVisibilityScale
                        )
                    } else {
                        drawCrossroadsTrail(
                            from = from,
                            to = position(dest),
                            route = route,
                            dense = denseBoard,
                            printed = effectiveTrails == CrossroadsTrailStyle.PRINTED,
                            visibilityScale = roadVisibilityScale
                        )
                    }
                }
            }
            // Finish the wood roads at real graph junctions instead of letting
            // multiple painted endcaps pile up into an accidental timber star.
            // Draw BELOW the existing symbols and pawns; this has no effect on
            // route geometry, node counts, or pointer hit testing.
            if (immersiveWorld && boardLook == CrossroadsBoardLook.ILLUSTRATED &&
                effectiveTrails == CrossroadsTrailStyle.ARTWORK && trailAtlas != null
            ) {
                board.nodes.forEach { node ->
                    if (CrossroadsJunctionHubGeometry.eligible(
                            kind = node.kind,
                            immersive = immersiveWorld,
                            illustrated = true,
                            paintedRoad = true
                        )
                    ) {
                        val radius = CrossroadsJunctionHubGeometry.radius(
                            nearestNodeSpacing =
                                nearestSpaceSpacing[node.id] ?: Float.POSITIVE_INFINITY,
                            worldSide = drawWidth,
                            denseCourse = denseBoard
                        )
                        drawCrossroadsWoodenJunctionHub(position(node.id), radius)
                    }
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
                val radius = (when {
                    node.kind == "fork" -> if (denseBoard) 11f else 12f
                    node.kind == "start" || node.kind == "finish" -> 13f
                    denseBoard && node.kind == "plain" -> 4.75f
                    denseBoard && node.kind == "event" -> 6.25f
                    else -> 7f
                }) * nodeVisibilityScale
                // VISUAL-83: Start and Finish feel like physical sockets for
                // the travelers, not two ordinary dots. Shared graph centers
                // and the original S/F labels remain authoritative.
                if (immersiveWorld &&
                    boardLook == CrossroadsBoardLook.ILLUSTRATED &&
                    (node.kind == "start" || node.kind == "finish")) {
                    drawCarvedStartFinishBase(
                        center = center,
                        radius = CarvedWaypointGeometry.socketRadius(
                            baseRadius = radius * 1.1f,
                            neighborDistanceNormalized =
                                nearestSpaceSpacing[node.id] ?: Float.POSITIVE_INFINITY,
                            worldSide = drawWidth
                        ),
                        isFinish = node.kind == "finish"
                    )
                }
                // The painted artwork is purely decorative. Unknown nodes,
                // start/finish, and missing art retain the exact old marker.
                val paintedType = if (showPaintedMarkers)
                    woodlandMarkerFor(node.kind, node.eventPool) else null
                if (paintedType != null && markerAtlas != null) {
                    drawCrossroadsPaintedMarker(center, radius, paintedType, markerAtlas)
                    if (node.kind == "fork") {
                        // Keep the authored "?" semantic label visible without
                        // covering the actual fork icon inside the medallion.
                        val labelAt = center + Offset(radius * 0.59f, radius * 0.62f)
                        drawCircle(Forest, radius = radius * 0.45f, center = labelAt)
                        drawCircle(Color(0xFFF8EDCF), radius = radius * 0.45f,
                            center = labelAt,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f))
                        drawIntoCanvas { canvas ->
                            val paint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                this.color = android.graphics.Color.WHITE
                                textSize = 8.5f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            canvas.nativeCanvas.drawText("?", labelAt.x,
                                labelAt.y + 3f, paint)
                        }
                    }
                } else {
                    // Preserve old shapes as the QA comparison and art fallback.
                    if (node.kind == "finish") {
                        drawCircle(Color(0xFFFFE8B6).copy(alpha = 0.55f),
                            radius = 22f, center = center)
                        drawCircle(Color(0xFFAC7C3B).copy(alpha = 0.77f),
                            radius = 18f, center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f))
                    } else if (node.kind == "start") {
                        drawCircle(Color(0xFFDEE9D5).copy(alpha = 0.56f),
                            radius = 19f, center = center)
                        drawCircle(Forest.copy(alpha = 0.48f),
                            radius = 16.5f, center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f))
                    }
                    // QA prototype: regular playable spaces look like carved
                    // wooden board tokens, not flat white pixels. Draw only on
                    // the illustrated LIVE board; preserve the full-map overview,
                    // special fork/event symbols and all existing game logic.
                    if (BuildConfig.DEBUG && immersiveWorld &&
                        boardLook == CrossroadsBoardLook.ILLUSTRATED &&
                        (node.kind == "plain" || node.kind == "merge")
                    ) {
                        val diskRadius = CrossroadsBoardSpaceGeometry.radius(
                            originalRadius = radius,
                            nearestSpacingNormalized =
                                nearestSpaceSpacing[node.id] ?: Float.POSITIVE_INFINITY,
                            squareWorldSidePx = drawWidth,
                            liveWorld = true
                        )
                        drawCrossroadsTactileSpace(center, diskRadius)
                    } else {
                        when (effectiveTrails) {
                            CrossroadsTrailStyle.ARTWORK -> if (trailAtlas != null) {
                                drawCrossroadsIllustratedJunction(center, radius, trailAtlas)
                            }
                            CrossroadsTrailStyle.PRINTED -> {
                                drawCircle(Color(0xFF334C3C).copy(alpha = 0.66f),
                                    radius = radius + 3.2f, center = center)
                                drawCircle(Color(0xFFF7EBD2),
                                    radius = radius + 1.8f, center = center)
                            }
                            CrossroadsTrailStyle.SIMPLE -> Unit
                        }
                        drawCircle(color, radius, center)
                        drawCircle(Color(0xFF9E896B), radius, center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.3f))
                    }
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
            }
            // Quietly trace each player's actual chosen journey.
            state.players.forEachIndexed { playerIndex, player ->
                val trail = PlayerColors[playerIndex].copy(alpha = 0.50f)
                player.traversed.zipWithNext().forEach { (from, to) ->
                    drawLine(trail, position(from), position(to), strokeWidth = 3f, cap = StrokeCap.Round)
                }
            }
            // Route highlights are ONLY for the current legal human choice.
            // These interactive signs follow the original graph edges exactly;
            // they do not invent paths or displace scenic artwork.
            if (routeChoiceIds.isNotEmpty() && state.phase == Phase.ROUTE &&
                !isTurnTransitioning) {
                val start = position(state.players[state.playerIndex].position)
                drawCircle(
                    Color(0xFFFFEAC0).copy(alpha = 0.88f),
                    radius = if (immersiveWorld) 30f else 17f,
                    center = start,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = if (immersiveWorld) 3.8f else 2.8f
                    )
                )
                routeChoiceIds.forEach { candidate ->
                    val end = position(candidate)
                    val ring = when (board.node(candidate).route) {
                        "risky" -> Color(0xFFAE773B)
                        "mischief" -> Color(0xFF8A6D9B)
                        else -> Color(0xFF4A7650)
                    }
                    // Painted map comes first. Only emphasize the first part
                    // of an available branch: no opaque UI tape across scenery.
                    val (cueStart, cueEnd) = BoardRouteHitTest.cueSegment(start, end)
                    val outlineWidth = if (immersiveWorld) 16f else 7.5f
                    val colorWidth = if (immersiveWorld) 6.5f else 3f
                    drawLine(Color(0xFFF5E9C8).copy(alpha = 0.72f),
                        cueStart, cueEnd, strokeWidth = outlineWidth,
                        cap = StrokeCap.Round)
                    drawLine(ring.copy(alpha = 0.93f),
                        cueStart, cueEnd, strokeWidth = colorWidth,
                        cap = StrokeCap.Round)
                    drawCircle(Color(0xFFFFF7DD),
                        radius = if (immersiveWorld) 25f else 15f,
                        center = end,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = if (immersiveWorld) 4.8f else 3f
                        ))
                    drawCircle(ring,
                        radius = if (immersiveWorld) 21f else 12f,
                        center = end,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = if (immersiveWorld) 3.8f else 2.4f
                        ))
                }
            }
            // Use animated on-screen coordinates to spread clustered tokens.
            // Snapshot positions can already be several spaces ahead mid-animation.
            state.players.forEachIndexed { i, _ ->
                val group = displayed.indices.filter { index ->
                    (displayed[index] - displayed[i]).getDistance() < 0.012f
                }
                val rank = group.indexOf(i)
                val offset = CrossroadsPiecePlacement.offset(rank, group.size, gamePieceScale)
                val marker = Offset(
                    left + displayed[i].x * drawWidth + offset.x,
                    top + displayed[i].y * drawHeight + offset.y -
                        BoardMotionAccent.hopOffsetPx(hopProgress[i].value, gamePieceScale)
                )
                val highlighted = i == highlightedSeat
                // Besides the end-of-move ripple, gently signal EACH actual
                // authored space as the character hops onto it. No additional
                // animation clock or movement delay is introduced.
                val stepContact = BoardMotionAccent.stepContactPulse(
                    hopProgress[i].value,
                    animateTokens && hopProgress[i].isRunning
                )
                val landingStrength = maxOf(landingGlow[i].value, stepContact)
                val tokenRadius = (if (highlighted) 12.5f else 11f) *
                    gamePieceScale * BoardMotionAccent.arrivalScale(landingStrength)
                if (landingStrength > 0.001f) {
                    val alpha = BoardMotionAccent.arrivalRippleAlpha(landingStrength)
                    drawCircle(
                        Color(0xFFFFF4D7).copy(alpha = alpha),
                        radius = tokenRadius * (1.65f + (1f - landingStrength) * 0.9f),
                        center = marker,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.3f * gamePieceScale
                        )
                    )
                    drawCircle(
                        PlayerColors[i].copy(alpha = alpha * 0.65f),
                        radius = tokenRadius * (1.98f + (1f - landingStrength) * 0.8f),
                        center = marker,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.1f * gamePieceScale
                        )
                    )
                }
                drawCrossroadsPiece(
                    center = marker,
                    radius = tokenRadius,
                    seatColor = PlayerColors[i],
                    highlighted = highlighted,
                    traveling = highlighted && isTurnTransitioning
                )
                // Local art lives only inside this token's colored/rimmed seat.
                // Game-state positions, seat index, and readable number stay authoritative.
                if (drawWoodlandPortraits && woodlandTokenAtlas != null && i < 4) {
                    val diameter = (tokenRadius * 1.88f).toInt().coerceAtLeast(1)
                    drawImage(
                        image = woodlandTokenAtlas,
                        srcOffset = IntOffset(i * 128, 0),
                        srcSize = IntSize(128, 128),
                        dstOffset = IntOffset(
                            (marker.x - diameter / 2f).toInt(),
                            (marker.y - diameter / 2f).toInt()
                        ),
                        dstSize = IntSize(diameter, diameter),
                        filterQuality = FilterQuality.Medium
                    )
                    // A separate number medallion lets the character remain visible.
                    val numberAt = marker + Offset(tokenRadius * 0.58f, tokenRadius * 0.62f)
                    drawCircle(
                        color = Color(0xFF35553E), radius = tokenRadius * 0.59f,
                        center = numberAt
                    )
                    drawCircle(
                        color = Color(0xFFF7F0DE), radius = tokenRadius * 0.59f,
                        center = numberAt,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.4f * gamePieceScale
                        )
                    )
                } else {
                    seatImages.getOrNull(i)?.let { art ->
                        val diameter = (tokenRadius * 1.35f).toInt().coerceAtLeast(1)
                        drawImage(
                            image = art,
                            srcSize = IntSize(art.width, art.height),
                            dstOffset = IntOffset(
                                (marker.x - diameter / 2f).toInt(),
                                (marker.y - diameter / 2f).toInt()
                            ),
                            dstSize = IntSize(diameter, diameter),
                            filterQuality = FilterQuality.Medium
                        )
                    }
                }
                val numberX = if (drawWoodlandPortraits) marker.x + tokenRadius * 0.58f
                    else marker.x
                val numberY = if (drawWoodlandPortraits) marker.y + tokenRadius * 0.62f
                    else marker.y
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = (if (drawWoodlandPortraits) 9.4f else 12f) * gamePieceScale
                        isAntiAlias = true
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        setShadowLayer(1.6f, 0f, 0.6f,
                            android.graphics.Color.argb(110, 40, 34, 24))
                    }
                    canvas.nativeCanvas.drawText("${i + 1}", numberX,
                        numberY + (if (drawWoodlandPortraits) 3.2f else 4f) * gamePieceScale,
                        paint)
                }
            }
            }
            // VISUAL-82: the static slim wooden board edge sits OUTSIDE the
            // camera transform. It frames the illustrated world like a
            // tabletop game without moving routes or intercepting gestures.
            if (immersiveWorld) {
                TabletopBoardRim()
            }
            // A conventional game-camera control floats above the world.
            // It never changes engine state, route selection, or saved matches.
            if (immersiveWorld && animateTokens) {
                Surface(
                    onClick = {
                        cameraMode = BoardCameraMode.FOLLOW
                        manualZoom = 1f
                        manualTranslation = Offset.Zero
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                        .padding(top = if (height < 440.dp) 94.dp else 116.dp, end = 12.dp)
                        .size(48.dp)
                        .semantics { contentDescription = "Follow active player" },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color(0xEA233B2B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, Color(0xFFE7D3A4)
                    ),
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⌖", color = Color(0xFFF8EED7),
                            fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

        }
    }
}
