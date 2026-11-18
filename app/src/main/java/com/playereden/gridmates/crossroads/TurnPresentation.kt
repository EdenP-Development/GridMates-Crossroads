package com.playereden.gridmates.crossroads

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.crossroads.core.EncounterNote

private val PineInk = Color(0xFF48684D)
private val CocoaInk = Color(0xFF6B5745)
private val DiceOutline = Color(0xFFB4A285)

/**
 * Presentation-only six-sided die. The real dice value still comes exclusively
 * from GameSession.roll(); the preview frames do not use RNG or influence play.
 */
@Composable
internal fun DieFace(
    face: Int,
    modifier: Modifier = Modifier,
    announceFace: Boolean = true
) {
    val shown = face.coerceIn(1, 6)
    // The button's decorative die should not announce an arbitrary preview
    // face (often a 1) before the real roll. The result die stays accessible.
    val dieModifier = if (announceFace)
        modifier.semantics { contentDescription = "Die showing $shown" }
    else modifier.clearAndSetSemantics { }
    Canvas(
        modifier = dieModifier
    ) {
        val side = size.minDimension
        if (side <= 0f) return@Canvas
        val origin = Offset((size.width - side) / 2f, (size.height - side) / 2f)
        val corner = side * 0.18f
        drawRoundRect(
            color = Color(0xFFFFFCF2),
            topLeft = origin,
            size = Size(side, side),
            cornerRadius = CornerRadius(corner)
        )
        drawRoundRect(
            color = DiceOutline,
            topLeft = origin,
            size = Size(side, side),
            cornerRadius = CornerRadius(corner),
            style = Stroke(width = side * 0.035f)
        )
        val dots: List<Pair<Float, Float>> = when (shown) {
            1 -> listOf(0.5f to 0.5f)
            2 -> listOf(0.28f to 0.28f, 0.72f to 0.72f)
            3 -> listOf(0.28f to 0.28f, 0.5f to 0.5f, 0.72f to 0.72f)
            4 -> listOf(0.28f to 0.28f, 0.72f to 0.28f,
                        0.28f to 0.72f, 0.72f to 0.72f)
            5 -> listOf(0.28f to 0.28f, 0.72f to 0.28f,
                        0.5f to 0.5f, 0.28f to 0.72f, 0.72f to 0.72f)
            else -> listOf(0.28f to 0.28f, 0.72f to 0.28f,
                           0.28f to 0.5f, 0.72f to 0.5f,
                           0.28f to 0.72f, 0.72f to 0.72f)
        }
        dots.forEach { (x, y) ->
            drawCircle(
                color = PineInk,
                radius = side * 0.070f,
                center = origin + Offset(side * x, side * y)
            )
        }
    }
}

/**
 * The same risk/name/description hierarchy for two-way and three-way forks.
 * Never imply an exact reward or outcome beyond the published route metadata.
 */
@Composable
internal fun RouteChoiceSummary(label: String, description: String, risk: String?) {
    val name = when (risk) {
        "high" -> "High risk"
        "medium" -> "Medium risk"
        "low" -> "Low risk"
        else -> "Route risk unknown"
    }
    val accent = when (risk) {
        "high" -> Color(0xFF94612E)
        "medium" -> Color(0xFF7A5D94)
        "low" -> Color(0xFF456F49)
        else -> CocoaInk
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Surface(
            color = accent.copy(alpha = 0.12f),
            shape = RoundedCornerShape(9.dp)
        ) {
            Text(
                name,
                color = accent,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold, color = PineInk,
                style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = CocoaInk)
        }
    }
}

/** One transient event result, not a second permanent section below the board. */
@Composable
internal fun EncounterRibbon(note: EncounterNote) {
    val (symbol, tint) = when (note.kind) {
        "setback" -> "⚠" to Color(0xFFF0E0C8)
        "mischief" -> "🎭" to Color(0xFFE9DEED)
        else -> "🍀" to Color(0xFFE1E9D7)
    }
    Surface(color = tint, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(symbol, fontSize = 23.sp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${note.actor} · ${note.title}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PineInk
                )
                Text(note.outcome, color = CocoaInk,
                    style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}
