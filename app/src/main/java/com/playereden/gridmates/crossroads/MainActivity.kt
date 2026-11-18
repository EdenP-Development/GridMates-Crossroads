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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
        mapOf(
            classicBoard.id to classicBoard,
            quickBoard.id to quickBoard,
            longBoard.id to longBoard
        )
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
        mutableStateOf(initialRecovery.match?.game?.board?.id ?: classicBoard.id)
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
                rollingFace = previewFaces[step % previewFaces.size]
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
                            game.roll() // Exactly one real roll, never during preview.
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
    fun start(seats: Int, passAndPlay: Boolean) {
        if (lanRole != LanRole.OFFLINE) leaveGame()
        val humans = if (passAndPlay) (0 until seats).toSet() else setOf(0)
        val newGame = GameSession(knownBoards.getValue(selectedCourseId), catalogue, seats = seats,
            seed = java.util.Random().nextInt(), humanIndices = humans)
        activeMatchId = java.util.UUID.randomUUID().toString()
        matchStartedAtEpochMillis = System.currentTimeMillis()
        startedFromCheckpoint = false
        persistGame(newGame)
        session = newGame
        revision++
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

    fun rematch() {
        val current = session ?: return
        if (lanRole == LanRole.HOST) {
            if (!lanConnected) return
            val next = GameSession(current.board, catalogue, seats = current.players.size,
                seed = java.util.Random().nextInt(), humanIndices = setOf(0, 1))
            activeMatchId = java.util.UUID.randomUUID().toString()
            matchStartedAtEpochMillis = System.currentTimeMillis()
            startedFromCheckpoint = false
            session = next
            lanHost?.publish(next.checkpoint(activeMatchId, matchStartedAtEpochMillis))
            revision++
        } else if (lanRole == LanRole.OFFLINE) {
            start(current.players.size, current.players.count { !it.isBot } > 1)
        }
    }

    // The OS Back action follows the same protection as Leave Match. Dialogs
    // (including the 3-way junction) own Back while they are visible.
    val localIsCurrentSeat = state != null && (
        lanRole == LanRole.OFFLINE ||
            (lanRole == LanRole.HOST && state.playerIndex == 0 && lanConnected) ||
            (lanRole == LanRole.GUEST && state.playerIndex == 1 && lanConnected)
    )
    val threeWayDialogVisible = game != null && state?.phase == Phase.ROUTE &&
        localIsCurrentSeat && !state.players[state.playerIndex].isBot &&
        game.availableRoutes().size > 2 && turnReady &&
        !routeDialogDismissed && !inspectBoard && !confirmExit && !showGuide
    BackHandler(
        enabled = lanScreen == LanScreen.NONE && game != null && state != null &&
            !threeWayDialogVisible && !showGuide && !showJournal &&
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
        Column(
            modifier = Modifier.fillMaxSize().background(Cream)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (BuildConfig.DEBUG) "CROSSROADS · QA" else "CROSSROADS",
                    style = MaterialTheme.typography.labelLarge, color = Forest)
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
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(CrossroadsCourse.title(board.id),
                            fontWeight = FontWeight.Bold, color = Forest,
                            style = MaterialTheme.typography.titleMedium)
                        Text(CrossroadsCourse.subtitle(board.id),
                            color = Bark, style = MaterialTheme.typography.labelSmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { inspectBoard = true }) {
                            Text("Full map", color = Forest)
                        }
                    Box {
                        IconButton(
                            onClick = { showMatchOptions = true },
                            modifier = Modifier.size(48.dp).semantics {
                                contentDescription = "Match options"
                            }
                        ) { Text("⋯", fontSize = 25.sp, color = Forest) }
                        DropdownMenu(
                            expanded = showMatchOptions,
                            onDismissRequest = { showMatchOptions = false },
                            containerColor = Cream
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
                    if (lanRole != LanRole.OFFLINE) {
                        Text(
                            if (lanRole == LanRole.HOST) "Nearby game · You are Player 1"
                            else "Nearby game · You are Player 2",
                            style = MaterialTheme.typography.labelMedium,
                            color = Forest
                        )
                    }
                    if (lanRole != LanRole.OFFLINE && !lanMessage.isNullOrBlank()) {
                        Text(lanMessage!!, style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF955B33))
                    }
                    val round = (state.turn - 1) / state.players.size + 1
                    val currentName = state.players[state.playerIndex].name
                    val trackedName = state.players[
                        cameraTrackIndex.coerceIn(state.players.indices)
                    ].name
                    Text(
                        if (state.phase == Phase.FINISHED && !turnReady) "Approaching the finish…"
                        else if (state.phase == Phase.FINISHED) "Journey complete!"
                        else if (!turnReady && !rollingDie) {
                            if (state.players[cameraTrackIndex.coerceIn(state.players.indices)].name == "You")
                                "You're on the move…"
                            else "$trackedName is on the move…"
                        }
                        else if (currentName == "You") "Round $round · Your turn"
                        else "Round $round · $currentName's turn",
                        fontWeight = FontWeight.Bold, color = Forest, fontSize = 16.sp,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                    // A single helpful cue replaces any extra Pass & Play
                    // handoff dialog: next player can roll immediately.
                    if (lanRole == LanRole.OFFLINE &&
                        state.players.all { !it.isBot } && state.turn > 1 &&
                        state.phase == Phase.ROLL && turnReady && !rollingDie
                    ) {
                        Text(
                            "Pass the phone to $currentName",
                            style = MaterialTheme.typography.labelMedium,
                            color = Bark
                        )
                    }
                    val initialInstructions = state.turn == 1 &&
                        state.phase == Phase.ROLL && state.die == null &&
                        state.encounters.isEmpty()
                    Text(
                        if (initialInstructions) "Roll the die to begin your journey."
                        else "Latest action · ${state.lastMessage}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (recoveryWarning != null) {
                        Text(recoveryWarning!!, style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF955B33))
                    }
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
                    if (state.phase == Phase.ROUTE && localIsCurrentSeat &&
                        !state.players[state.playerIndex].isBot &&
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
                        Text(if (winner == "You") "You reached the finish!"
                            else "$winner reached the finish!",
                            fontWeight = FontWeight.SemiBold, color = Forest)
                        Button(onClick = { showResults = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("View match results")
                        }
                    } else if (lanRole != LanRole.OFFLINE &&
                        (!lanConnected || lanPendingAction ||
                            state.playerIndex != (if (lanRole == LanRole.HOST) 0 else 1))
                    ) {
                        Text(
                            if (!lanConnected) "Waiting for your partner to reconnect…"
                            else if (lanPendingAction) "Waiting for the host to confirm your move…"
                            else "Waiting for ${state.players[state.playerIndex].name} to play…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Bark
                        )
                    } else if (state.players[state.playerIndex].isBot) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Playing automatically…",
                            style = MaterialTheme.typography.bodySmall, color = Bark)
                    } else {
                        when (state.phase) {
                            Phase.ROLL -> {
                                Button(
                                    onClick = { rollingDie = true },
                                    enabled = !rollingDie && turnReady &&
                                         (lanRole == LanRole.OFFLINE ||
                                             (lanConnected && !lanPendingAction)),
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
                                val fork = board.forkAt(state.players[state.playerIndex].position)
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
                                            onClick = { localAction(LanAction(LanActionType.ROUTE, route = route.id)) },
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
                                    OutlinedButton(onClick = { localAction(LanAction(LanActionType.TARGET, target = target)) },
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
        if (state.phase == Phase.ROUTE && localIsCurrentSeat &&
            !state.players[state.playerIndex].isBot &&
            game.availableRoutes().size > 2 && turnReady &&
            !routeDialogDismissed && !inspectBoard && !confirmExit && !showGuide) {
            val junction = board.forkAt(state.players[state.playerIndex].position)
            // Keep all three route options readable, without an Android system
            // purple/lavender dialog. Only the choices scroll on short phones;
            // the title and Inspect full map action stay accessible.
            Dialog(
                onDismissRequest = { routeDialogDismissed = true },
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                junction?.title ?: "Choose your path",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )
                            Text(
                                "Three paths · ${state.remainingSteps} " +
                                    "${if (state.remainingSteps == 1) "step" else "steps"} left",
                                style = MaterialTheme.typography.bodySmall,
                                color = Bark
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                game.availableRoutes().forEach { route ->
                                    val choice = junction?.choices?.firstOrNull { it.to == route.id }
                                    OutlinedButton(
                                        onClick = { localAction(LanAction(LanActionType.ROUTE, route = route.id)) },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Forest
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp, Color(0xFFB5A88F)
                                        ),
                                        contentPadding = PaddingValues(
                                            horizontal = 11.dp, vertical = 11.dp
                                        )
                                    ) {
                                        RouteChoiceSummary(
                                            label = choice?.label ?: route.id,
                                            description = choice?.description ?: "Follow this trail",
                                            risk = choice?.risk
                                        )
                                    }
                                }
                            }
                            OutlinedButton(
                                // Hide the three-way popup while the full map
                                // is inspected; reopening the decision must
                                // keep the same authoritative dice and routes.
                                onClick = { inspectBoard = true },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, Color(0xFFB5A88F)
                                )
                            ) {
                                Text("Inspect full map")
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
            AlertDialog(
                onDismissRequest = { inspectBoard = false },
                containerColor = Cream,
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(CrossroadsCourse.title(board.id), color = Forest,
                            style = MaterialTheme.typography.titleLarge)
                        Text("Route overview", color = Bark,
                            style = MaterialTheme.typography.labelMedium)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // The prior 385dp portrait viewport made this same
                        // route appear far taller/narrower than the live board.
                        // A width-based viewport keeps course proportions
                        // more consistent on small and large phones.
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val mapHeight = (maxWidth * 0.94f).coerceIn(245.dp, 328.dp)
                            BoardCanvas(board, state, height = mapHeight, tokenScale = 1.35f,
                                animateTokens = false, matchKey = game)
                        }
                        Text("Amber: hazard  •  Green: shelter  •  Purple: mischief",
                            style = MaterialTheme.typography.bodySmall, color = Bark)
                        Text("This map is for inspection; close it to make your next move.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { inspectBoard = false }) {
                        Text(
                            if (state.phase == Phase.ROUTE &&
                                localIsCurrentSeat && game.availableRoutes().size > 2 &&
                                !routeDialogDismissed) "Back to route choices"
                            else "Back to game"
                        )
                    }
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
                                courseId = board.id,
                                elapsedSeconds = finishedElapsedSeconds,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Button(
                                onClick = { rematch() },
                                enabled = lanRole != LanRole.GUEST &&
                                    (lanRole != LanRole.HOST || lanConnected),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Text(if (lanRole == LanRole.GUEST) "Host starts next match"
                                    else "Rematch")
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
    val cameraDescription = "${CrossroadsCourse.title(board.id)} board. $focusPlayerName in $focusRegion."
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
            BoardCameraMode.FOLLOW -> CrossroadsCameraPreset.followZoom(
                state.phase, state.turn, animateTokens
            )
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
    val focusedOffset = CrossroadsPiecePlacement.offset(
        focusedRank.coerceAtLeast(0), focusedGroup.size.coerceAtLeast(1), tokenScale
    )
    val livePieceLocation by rememberUpdatedState(
        Offset(
            boardPadding + active.x * (viewportWidth - 2f * boardPadding).coerceAtLeast(0f) + focusedOffset.x,
            boardPadding + active.y * (viewportHeight - 2f * boardPadding).coerceAtLeast(0f) + focusedOffset.y
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
            // Longer authored courses have more route nodes in the same viewport.
            // Reduce plain-node and trail thickness just for this dense atlas.
            val denseBoard = board.id == CrossroadsCourse.LONG_ID
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
                    drawLine(laneColor, from, position(dest),
                        strokeWidth = if (denseBoard) 7.5f else 11f,
                        cap = StrokeCap.Round)
                    drawLine(Color(0xFFFCF8ED), from, position(dest),
                        strokeWidth = if (denseBoard) 2.0f else 3f,
                        cap = StrokeCap.Round)
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
                val radius = when {
                    node.kind == "fork" -> if (denseBoard) 11f else 12f
                    node.kind == "start" || node.kind == "finish" -> 13f
                    denseBoard && node.kind == "plain" -> 4.75f
                    denseBoard && node.kind == "event" -> 6.25f
                    else -> 7f
                }
                // Tabletop start/finish emblems are distinguishable even when
                // pawns are clustered near the entrance or camera is fit-to-map.
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
                val offset = CrossroadsPiecePlacement.offset(rank, group.size, tokenScale)
                val marker = Offset(
                    left + displayed[i].x * drawWidth + offset.x,
                    top + displayed[i].y * drawHeight + offset.y
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
