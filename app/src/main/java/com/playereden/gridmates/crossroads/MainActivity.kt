package com.playereden.gridmates.crossroads

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.crossroads.core.*
import kotlinx.coroutines.delay

private val Forest = Color(0xFF48684D)
private val Cream = Color(0xFFF7F0DE)
private val Bark = Color(0xFF80664B)
private val PlayerColors = listOf(Color(0xFFC75C49), Color(0xFF3D729F), Color(0xFF729B60), Color(0xFFB48A36))

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
    var matches by remember { mutableIntStateOf(prefs.getInt("completed", 0)) }
    var wins by remember { mutableIntStateOf(prefs.getInt("wins", 0)) }
    var session by remember { mutableStateOf<GameSession?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    val game = session
    val state = remember(game, revision) { game?.snapshot() }
    // QA only: wall-clock timing includes reading, map inspection and background pauses.
    val matchStartedAt = remember(game) { android.os.SystemClock.elapsedRealtime() }
    var showJournal by remember(game) { mutableStateOf(false) }
    var confirmExit by remember(game) { mutableStateOf(false) }
    var inspectBoard by remember(game) { mutableStateOf(false) }
    var rollingDie by remember(game) { mutableStateOf(false) }
    var speed by remember { mutableStateOf(PresentationSpeed.COZY) }
    var transitionDelay by remember(game) { mutableLongStateOf(0L) }
    var turnReady by remember(game) { mutableStateOf(true) }
    var routeDialogDismissed by remember(game) { mutableStateOf(false) }
    LaunchedEffect(game, state?.revision) { routeDialogDismissed = false }
    var rollingFace by remember(game) { mutableIntStateOf(1) }
    var recentReveal by remember(game) { mutableStateOf<EncounterNote?>(null) }
    val latestNote = state?.encounters?.lastOrNull()
    LaunchedEffect(game, latestNote) {
        if (latestNote != null) {
            recentReveal = latestNote
            delay(2100)
            recentReveal = null
        }
    }
    LaunchedEffect(game, rollingDie) {
        if (game != null && rollingDie) {
            // Visual dice cycling is presentation only: the rules engine draws the
            // real die exactly once after the animation; no new RNG affects matches.
            repeat(speed.dieFrames) { step ->
                rollingFace = (step * 5) % 6 + 1
                delay(speed.dieFrameMillis)
            }
            if (game.phase == Phase.ROLL && !game.players[game.currentIndex].isBot) {
                val before = game.snapshot()
                game.roll() // Exactly one real engine roll; visual faces use no RNG.
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
            turnReady = true
            if (state.phase != Phase.FINISHED && state.players[state.playerIndex].isBot) {
                delay(speed.aiBreathMillis)
                val before = game.snapshot()
                game.botStep()
                transitionDelay = PresentationPacing.waitForMovement(before, game.snapshot(), speed)
                turnReady = false
                revision++
            }
        }
    }
    LaunchedEffect(game, state?.phase) {
        if (game != null && state?.phase == Phase.FINISHED) {
            matches++
            if (state.winnerIndex == 0) wins++
            prefs.edit().putInt("completed", matches).putInt("wins", wins).apply()
        }
    }
    fun start(seats: Int) {
        session = GameSession(board, catalogue, seats = seats, seed = java.util.Random().nextInt(), humanIndices = setOf(0))
        revision++
    }
    fun update(block: () -> Unit) {
        val before = game?.snapshot()
        block()
        if (before != null && game != null) {
            transitionDelay = PresentationPacing.waitForMovement(before, game.snapshot(), speed)
            turnReady = false
        }
        revision++
    }

    if (game == null || state == null) {
        Column(
            modifier = Modifier.fillMaxSize().background(Cream)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("GRIDMATES", style = MaterialTheme.typography.labelLarge, color = Forest)
            Text("Crossroads", fontSize = 31.sp, fontWeight = FontWeight.Bold, color = Forest)
            Text("Every path is a choice.", fontSize = 19.sp)
            Text("Follow winding woodland roads, choose your route, and see what the journey brings. First to the finish wins.")
            HorizontalDivider()
            Text("Play against AI", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text("Choose the total number of players, including you.", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (seats in 2..4) {
                    Button(onClick = { start(seats) }, modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 12.dp)) {
                        Text("$seats players", textAlign = TextAlign.Center)
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAE1CA)),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Your local record", fontWeight = FontWeight.Bold)
                    Text("$wins ${if (wins == 1) "win" else "wins"} • $matches completed ${if (matches == 1) "match" else "matches"}")
                }
            }
            Text("QA playtest • Solo vs AI • One handcrafted board", style = MaterialTheme.typography.bodySmall, color = Bark)
        }
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
                TextButton(onClick = { speed = if (speed == PresentationSpeed.COZY)
                    PresentationSpeed.BRISK else PresentationSpeed.COZY }) {
                    Text(if (speed == PresentationSpeed.COZY) "Pace: Cozy" else "Pace: Brisk",
                        style = MaterialTheme.typography.labelMedium)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.players.forEachIndexed { i, player ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = if (i == state.playerIndex && state.phase != Phase.FINISHED)
                            Color(0xFFE6E9D6) else Color(0xFFF0E9D9),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(12.dp).background(PlayerColors[i], androidx.compose.foundation.shape.CircleShape))
                            Spacer(Modifier.width(5.dp))
                            Text(player.name, fontSize = 12.sp, fontWeight =
                                if (i == state.playerIndex) FontWeight.Bold else FontWeight.Normal)
                            if (player.shield) Text(" 🛡", fontSize = 11.sp)
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Woodland Crossing", fontWeight = FontWeight.Bold, color = Forest)
                    TextButton(
                        onClick = { inspectBoard = true },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text("Inspect map", style = MaterialTheme.typography.labelMedium)
                    }
                }
                BoardCanvas(board, state, matchKey = game, stepMillis = speed.stepMillis)
                if (state.encounters.isNotEmpty()) {
                    val latest = state.encounters.last()
                    val symbol = when (latest.kind) {
                        "setback" -> "⚠️"
                        "mischief" -> "🎭"
                        else -> "🍀"
                    }
                    val encounterTint = when (latest.kind) {
                        "setback" -> Color(0xFFF0E0C8)
                        "mischief" -> Color(0xFFE9DEED)
                        else -> Color(0xFFE1E9D7)
                    }
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = encounterTint)) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("$symbol  LATEST ENCOUNTER", style = MaterialTheme.typography.labelSmall, color = Forest)
                            Text("${latest.actor} · ${latest.title}", fontWeight = FontWeight.SemiBold)
                            Text(latest.outcome, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    TextButton(onClick = { showJournal = !showJournal }) {
                        Text(if (showJournal) "Hide journey journal" else "See journey journal (${state.encounters.size})")
                    }
                    if (showJournal) {
                        state.encounters.asReversed().take(8).forEach { note ->
                            Text("Turn ${note.turn} · ${note.actor}: ${note.title} — ${note.outcome}",
                                style = MaterialTheme.typography.bodySmall, color = Bark)
                        }
                    }
                } else {
                    Text("Amber: hazards   •   Green: shelter   •   Purple: mischief",
                        style = MaterialTheme.typography.bodySmall, color = Bark)
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
                    Text(
                        if (state.phase == Phase.FINISHED) "Journey complete!"
                        else if (state.playerIndex == 0) "Round $round · Your turn"
                        else "Round $round · $currentName's turn",
                        fontWeight = FontWeight.Bold, color = Forest, fontSize = 16.sp
                    )
                    Text(state.lastMessage, style = MaterialTheme.typography.bodyMedium)
                    if (!turnReady && state.phase != Phase.FINISHED && !rollingDie) {
                        Text("Following the trail…", style = MaterialTheme.typography.labelSmall,
                            color = Bark)
                    }
                    if (state.phase != Phase.FINISHED) {
                        AnimatedContent(
                            targetState = recentReveal,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "encounter-reveal"
                        ) { note ->
                            if (note != null) {
                                val revealTint = when (note.kind) {
                                    "setback" -> Color(0xFFF0E0C8)
                                    "mischief" -> Color(0xFFE9DEED)
                                    else -> Color(0xFFE1E9D7)
                                }
                                val mark = when (note.kind) {
                                    "setback" -> "⚠"
                                    "mischief" -> "🎭"
                                    else -> "🍀"
                                }
                                Surface(color = revealTint, shape = MaterialTheme.shapes.medium) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text("$mark  ${note.actor} · ${note.title}",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text(note.outcome, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                    if (state.die != null && state.phase != Phase.FINISHED) {
                        Text(
                            "🎲 ${state.die}  ·  ${state.remainingSteps} ${if (state.remainingSteps == 1) "step" else "steps"} remaining",
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
                        )
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
                        Text(if (state.winnerIndex == 0) "You won! 🏆" else "${state.players[state.winnerIndex!!].name} won!",
                            fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Forest)
                        val elapsedSeconds = ((android.os.SystemClock.elapsedRealtime() -
                            matchStartedAt) / 1000L).coerceAtLeast(0L)
                        Text(
                            "Match time: ${elapsedSeconds / 60}m ${elapsedSeconds % 60}s · ${(state.turn - 1) / state.players.size + 1} rounds",
                            style = MaterialTheme.typography.bodySmall, color = Bark
                        )
                        Button(onClick = { start(state.players.size) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Rematch")
                        }
                        OutlinedButton(onClick = { session = null }, modifier = Modifier.fillMaxWidth()) {
                            Text("Back to menu")
                        }
                    } else if (state.playerIndex != 0) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Your opponents are taking their turns…",
                            style = MaterialTheme.typography.bodySmall, color = Bark)
                    } else {
                        when (state.phase) {
                            Phase.ROLL -> {
                                Button(
                                    onClick = { rollingDie = true },
                                    enabled = !rollingDie && turnReady,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (rollingDie) "🎲 $rollingFace  Rolling…" else "🎲 Roll the die")
                                }
                            }
                            Phase.ROUTE -> {
                                val fork = board.forkAt(state.players[0].position)
                                Text(fork?.title ?: "Choose your route", fontWeight = FontWeight.SemiBold, color = Forest)
                                if (fork != null) {
                                    Text("Choose a path • routes have different risks and rewards",
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
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text(choice?.label ?: route.id, fontWeight = FontWeight.SemiBold)
                                                Text(choice?.description ?: "Follow this trail",
                                                    style = MaterialTheme.typography.bodySmall)
                                            }
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
                    if (state.phase != Phase.FINISHED) {
                        TextButton(onClick = { confirmExit = true }, modifier = Modifier.align(Alignment.End)) {
                            Text("Leave match", color = Bark)
                        }
                    }
                }
            }
        }
        // A dedicated scrollable chooser avoids the third option being hidden
        // under the fixed action pane on smaller handsets.
        if (state.phase == Phase.ROUTE && state.playerIndex == 0 &&
            game.availableRoutes().size > 2 && turnReady &&
            !routeDialogDismissed && !inspectBoard && !confirmExit) {
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
                            val routeTint = when (choice?.risk) {
                                "high" -> Color(0xFFAC854B)
                                "medium" -> Color(0xFF9A79A5)
                                else -> Forest
                            }
                            OutlinedButton(
                                onClick = { update { game.chooseRoute(route.id) } },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(Modifier.size(12.dp).background(routeTint,
                                        androidx.compose.foundation.shape.CircleShape))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(choice?.label ?: route.id,
                                            fontWeight = FontWeight.SemiBold)
                                        Text(choice?.description ?: "Follow this trail",
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                }
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
        if (confirmExit) {
            AlertDialog(
                onDismissRequest = { confirmExit = false },
                title = { Text("Leave this match?") },
                text = { Text("This match is not saved yet. Your completed-match record will be kept.") },
                confirmButton = {
                    TextButton(onClick = { session = null; confirmExit = false }) { Text("Leave") }
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
    stepMillis: Int = PresentationSpeed.COZY.stepMillis
) {
    // The new GameSession object resets visual positions after Rematch.
    val motion = remember(matchKey, board.id, state.players.size, animateTokens) {
        List(state.players.size) { i ->
            val node = board.node(if (animateTokens) board.start else state.players[i].position)
            Animatable(Offset(node.x, node.y), Offset.VectorConverter)
        }
    }
    val landingGlow = remember(matchKey, board.id, state.players.size, animateTokens) {
        List(state.players.size) { Animatable(0f) }
    }
    val displayed = state.players.mapIndexed { i, player ->
        val path = player.traversed.toList()
        // The animation observes the rendered route history, never changes the rules engine.
        var previous by remember(matchKey, board.id, i, animateTokens) {
            mutableStateOf(if (animateTokens) listOf(board.start) else path)
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
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE4D0))) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height).padding(8.dp)) {
            val left = 18f
            val top = 18f
            val drawWidth = size.width - left * 2
            val drawHeight = size.height - top * 2
            fun position(id: String): Offset {
                val node = board.node(id)
                return Offset(left + node.x * drawWidth, top + node.y * drawHeight)
            }
            // Very light thematic paper washes preserve path readability without external art.
            drawRoundRect(
                color = Color(0xFFABC99B).copy(alpha = 0.12f),
                topLeft = Offset(left, top + drawHeight * 0.12f),
                size = androidx.compose.ui.geometry.Size(drawWidth * 0.35f, drawHeight * 0.76f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
            )
            drawRoundRect(
                color = Color(0xFF799B81).copy(alpha = 0.10f),
                topLeft = Offset(left + drawWidth * 0.34f, top + drawHeight * 0.05f),
                size = androidx.compose.ui.geometry.Size(drawWidth * 0.37f, drawHeight * 0.90f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
            )
            drawRoundRect(
                color = Color(0xFFD3AE7B).copy(alpha = 0.12f),
                topLeft = Offset(left + drawWidth * 0.69f, top + drawHeight * 0.06f),
                size = androidx.compose.ui.geometry.Size(drawWidth * 0.31f, drawHeight * 0.89f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
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
            if (state.phase == Phase.ROUTE) {
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
            drawCircle(Forest.copy(alpha = 0.7f), radius = 17f * tokenScale,
                center = Offset(left + displayed[state.playerIndex].x * drawWidth,
                    top + displayed[state.playerIndex].y * drawHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.3f))
            state.players.forEachIndexed { i, player ->
                val common = state.players.count { it.position == player.position }
                val rank = state.players.take(i).count { it.position == player.position }
                val center = position(player.position)
                val dx = if (common == 1) 0f else (rank - (common - 1) / 2f) * 18f * tokenScale
                val dy = if (common == 1) -14f * tokenScale else -17f * tokenScale
                val marker = Offset(left + displayed[i].x * drawWidth + dx,
                    top + displayed[i].y * drawHeight + dy)
                val tokenRadius = (if (i == state.playerIndex) 12.5f else 11f) * tokenScale
                drawCircle(PlayerColors[i], tokenRadius, marker)
                drawCircle(Color.White, tokenRadius, marker,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.3f))
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 12f * tokenScale
                        isAntiAlias = true
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    canvas.nativeCanvas.drawText("${i + 1}", marker.x, marker.y + 4f * tokenScale, paint)
                }
            }
        }
    }
}
