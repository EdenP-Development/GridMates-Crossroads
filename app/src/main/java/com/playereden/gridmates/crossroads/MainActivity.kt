package com.playereden.gridmates.crossroads

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        val board = GameData.board(assets.open("boards/woodland_quick_example.json").bufferedReader().use { it.readText() })
        val catalogue = GameData.events(assets.open("events/core_events_proposed.json").bufferedReader().use { it.readText() })
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

    LaunchedEffect(game, revision) {
        if (game != null && state != null && state.phase != Phase.FINISHED && state.players[state.playerIndex].isBot) {
            delay(320)
            game.botStep()
            revision++
        }
    }
    LaunchedEffect(game, state?.phase) {
        if (game != null && state?.phase == Phase.FINISHED) {
            matches += 1
            if (state.winnerIndex == 0) wins += 1
            prefs.edit().putInt("completed", matches).putInt("wins", wins).apply()
        }
    }
    fun start(seats: Int) {
        session = GameSession(board, catalogue, seats = seats, seed = java.util.Random().nextInt(), humanIndices = setOf(0))
        revision++
    }
    fun update(block: () -> Unit) {
        block()
        revision++
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Cream).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("GRIDMATES", style = MaterialTheme.typography.labelLarge, color = Forest)
        Text("Crossroads", fontSize = 29.sp, fontWeight = FontWeight.Bold, color = Forest)
        if (game == null || state == null) {
            Text("Every path is a choice.", fontSize = 18.sp)
            Text("A cozy little board-game race. Roll the die, choose your route, and get to the finish before the others.")
            HorizontalDivider()
            Text("Play against AI", fontWeight = FontWeight.SemiBold)
            Text("Choose total players (including you).", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (seats in 2..4) {
                    Button(onClick = { start(seats) }, modifier = Modifier.weight(1f)) {
                        Text("$seats players")
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAE1CA))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Your local record", fontWeight = FontWeight.SemiBold)
                    Text("$wins wins in $matches completed matches")
                    Text("QA prototype: one small board, no Wi-Fi or cosmetics yet.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                for (i in state.players.indices) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(Modifier.size(18.dp).background(PlayerColors[i]))
                        Text(if (i == 0) "You" else "AI $i", fontSize = 12.sp, fontWeight = if (i == state.playerIndex) FontWeight.Bold else FontWeight.Normal)
                        if (state.players[i].shield) Text("Shield", fontSize = 10.sp)
                    }
                }
            }
            BoardCanvas(board, state)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAE1CA))) {
                Column(Modifier.padding(12.dp)) {
                    Text(if (state.phase == Phase.FINISHED) "Journey complete!" else "Turn ${state.turn} • ${if (state.playerIndex == 0) "Your turn" else "AI ${state.playerIndex}'s turn"}", fontWeight = FontWeight.Bold)
                    Text(state.lastMessage, style = MaterialTheme.typography.bodyMedium)
                    if (state.die != null && state.phase != Phase.FINISHED) {
                        Text("🎲 ${state.die}  •  ${state.remainingSteps} steps left")
                    }
                }
            }
            if (state.phase == Phase.FINISHED) {
                Text(if (state.winnerIndex == 0) "You won! 🏆" else "AI ${state.winnerIndex} won!", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Forest)
                Button(onClick = { start(state.players.size) }, modifier = Modifier.fillMaxWidth()) { Text("Rematch") }
                OutlinedButton(onClick = { session = null }, modifier = Modifier.fillMaxWidth()) { Text("Back to menu") }
            } else if (state.playerIndex != 0) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Your opponent is taking a turn…", style = MaterialTheme.typography.bodySmall)
            } else {
                when (state.phase) {
                    Phase.ROLL -> Button(onClick = { update { game.roll() } }, modifier = Modifier.fillMaxWidth()) { Text("Roll the die 🎲") }
                    Phase.ROUTE -> {
                        Text("Crossroads! Choose your path.", fontWeight = FontWeight.Bold, color = Forest)
                        game.availableRoutes().forEach { route ->
                            val fast = route.id.startsWith("U")
                            OutlinedButton(onClick = { update { game.chooseRoute(route.id) } }, modifier = Modifier.fillMaxWidth()) {
                                Text("${if (fast) "Upper shortcut" else "Lower scenic trail"} · ${if (fast) "Faster / riskier" else "Longer / gentler"}")
                            }
                        }
                    }
                    Phase.TARGET -> {
                        Text("Mischief! Choose your opponent.", fontWeight = FontWeight.Bold)
                        game.availableTargets().forEach { target ->
                            OutlinedButton(onClick = { update { game.chooseTarget(target) } }, modifier = Modifier.fillMaxWidth()) {
                                Text("Prank AI $target ${if (state.players[target].shield) "· shielded" else ""}")
                            }
                        }
                    }
                    Phase.FINISHED -> Unit
                }
            }
            OutlinedButton(onClick = { session = null }, modifier = Modifier.fillMaxWidth()) { Text("Leave this match") }
            Text("Prototype • Single device + AI • Fixed course with region-based random events", fontSize = 11.sp, color = Bark)
        }
    }
}

@Composable
private fun BoardCanvas(board: BoardDefinition, state: Snapshot) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE4D0))) {
        Canvas(modifier = Modifier.fillMaxWidth().height(290.dp).padding(8.dp)) {
            val left = 18f
            val top = 18f
            val drawWidth = size.width - left * 2
            val drawHeight = size.height - top * 2
            fun position(id: String): Offset {
                val node = board.node(id)
                return Offset(left + node.x * drawWidth, top + node.y * drawHeight)
            }
            // Shared board geography never changes with the event seed.
            board.nodes.forEach { node ->
                val from = position(node.id)
                node.next.forEach { dest ->
                    drawLine(Color(0xFFD9C6A5), from, position(dest), strokeWidth = 11f, cap = StrokeCap.Round)
                    drawLine(Color(0xFFFCF8ED), from, position(dest), strokeWidth = 3f, cap = StrokeCap.Round)
                }
            }
            board.nodes.forEach { node ->
                val center = position(node.id)
                val color = when (node.kind) {
                    "fork" -> Color(0xFFD7AB50)
                    "event" -> Color(0xFF8B9F7B)
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
            state.players.forEachIndexed { i, player ->
                val common = state.players.count { it.position == player.position }
                val rank = state.players.take(i).count { it.position == player.position }
                val center = position(player.position)
                val dx = if (common == 1) 0f else (rank - (common - 1) / 2f) * 14f
                val dy = if (common == 1) -12f else -14f
                drawCircle(PlayerColors[i], 9f, Offset(center.x + dx, center.y + dy))
                drawCircle(Color.White, 9f, Offset(center.x + dx, center.y + dy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 10f
                        isAntiAlias = true
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    canvas.nativeCanvas.drawText("${i + 1}", center.x + dx, center.y + dy + 3.5f, paint)
                }
            }
        }
    }
}
