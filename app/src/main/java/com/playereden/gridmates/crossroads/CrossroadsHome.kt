package com.playereden.gridmates.crossroads

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF48684D)
private val Muted = Color(0xFF80664B)
private val Paper = Color(0xFFF7F0DE)

/** An original miniature travel motif, not an interactive or alternate board. */
@Composable
private fun HomeTrailIllustration() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Color(0xFFECE5D4),
        tonalElevation = 2.dp
    ) {
        Canvas(Modifier.fillMaxWidth().height(175.dp)) {
            val inset = 12f
            val w = (size.width - 2f * inset).coerceAtLeast(1f)
            val h = (size.height - 2f * inset).coerceAtLeast(1f)
            fun point(x: Float, y: Float) = Offset(inset + x * w, inset + y * h)
            drawWoodlandBoardArt(
                boardId = "woodland_woven_trail_v02",
                left = inset, top = inset, width = w, height = h
            )
            // A recognizable pair of joining paths, but not the actual Woven Trail.
            // Both lines remain decorative; no labels could mislead about rules.
            val upper = Path().apply {
                val start = point(0.10f, 0.73f)
                moveTo(start.x, start.y)
                val end = point(0.89f, 0.19f)
                cubicTo(
                    point(0.17f, 0.13f).x, point(0.17f, 0.13f).y,
                    point(0.66f, 0.35f).x, point(0.66f, 0.35f).y,
                    end.x, end.y
                )
            }
            val lower = Path().apply {
                val start = point(0.10f, 0.73f)
                moveTo(start.x, start.y)
                val end = point(0.89f, 0.19f)
                cubicTo(
                    point(0.31f, 0.95f).x, point(0.31f, 0.95f).y,
                    point(0.72f, 0.85f).x, point(0.72f, 0.85f).y,
                    end.x, end.y
                )
            }
            listOf(upper, lower).forEachIndexed { i, path ->
                drawPath(path, Color(0xFF735E41).copy(alpha = 0.48f),
                    style = Stroke(width = 14f, cap = StrokeCap.Round))
                drawPath(path, if (i == 0) Color(0xFFFFE9BC) else Color(0xFFE1EACD),
                    style = Stroke(width = 9f, cap = StrokeCap.Round))
            }
            drawCircle(Ink, 12f, point(0.10f, 0.73f))
            drawCircle(Color(0xFFFDF9EB), 7f, point(0.10f, 0.73f))
            drawCircle(Color(0xFFBB8B41), 14f, point(0.89f, 0.19f))
            drawCircle(Color(0xFFFFF3D5), 8f, point(0.89f, 0.19f))
        }
    }
}

@Composable
internal fun CrossroadsHomeScreen(
    wins: Int,
    completed: Int,
    onStart: (Int) -> Unit
) {
    // The first run is a single tap: 1 human + 1 bot. No room codes or setup screens.
    var seats by remember { mutableIntStateOf(2) }

    Column(
        modifier = Modifier.fillMaxSize().background(Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("GRIDMATES · BOARD GAMES",
            style = MaterialTheme.typography.labelLarge, color = Muted)
        Text("Crossroads", fontSize = 33.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Every path is a choice.", style = MaterialTheme.typography.titleMedium,
            color = Muted)
        HomeTrailIllustration()
        Text(
            "Wander through the meadow, forest, and village. Choose your trails, take a chance, and reach the finish first.",
            style = MaterialTheme.typography.bodyMedium, color = Ink
        )
        HorizontalDivider(color = Muted.copy(alpha = 0.24f))
        Text("Ready to play?", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = Ink)
        Text("You and ${seats - 1} ${if (seats == 2) "AI opponent" else "AI opponents"} · offline",
            style = MaterialTheme.typography.bodySmall, color = Muted)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (2..4).forEach { count ->
                FilterChip(
                    selected = count == seats,
                    onClick = { seats = count },
                    label = { Text("$count players", textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Button(
            onClick = { onStart(seats) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text("Start journey", style = MaterialTheme.typography.titleMedium)
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAE1CA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("Your local record", fontWeight = FontWeight.SemiBold, color = Ink)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Column {
                        Text("$wins", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Ink)
                        Text(if (wins == 1) "Win" else "Wins",
                            style = MaterialTheme.typography.labelMedium, color = Muted)
                    }
                    Column {
                        Text("$completed", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Ink)
                        Text("Finished matches",
                            style = MaterialTheme.typography.labelMedium, color = Muted)
                    }
                }
            }
        }
        Text("QA playtest · Solo vs AI · Woodland Crossing",
            style = MaterialTheme.typography.labelSmall, color = Muted)
    }
}
