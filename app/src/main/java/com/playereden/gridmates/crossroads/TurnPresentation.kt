package com.playereden.gridmates.crossroads

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.crossroads.core.EncounterNote
import com.playereden.crossroads.core.Phase
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode

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

/** Presentation-only stage of a tabletop turn, never a second gameplay state. */
internal fun turnMomentName(phase: Phase, moving: Boolean): String = when {
    moving -> "ON THE TRAIL"
    phase == Phase.ROLL -> "DICE ROLL"
    phase == Phase.ROUTE -> "AT A CROSSROADS"
    phase == Phase.TARGET -> "MISCHIEF"
    phase == Phase.FINISHED -> "FINISH LINE"
    else -> "ON THE TRAIL"
}

/**
 * A compact, tactile-looking turn plaque in the fixed action panel.
 * Gives the die, journey, crossroads and finish different visual beats
 * without extra dialogs, timers or changes to the authoritative engine.
 */
@Composable
internal fun TurnMomentPlaque(
    title: String,
    phase: Phase,
    moving: Boolean,
    seatColor: Color,
    modifier: Modifier = Modifier
) {
    val moment = turnMomentName(phase, moving)
    val target = when (moment) {
        "AT A CROSSROADS" -> Color(0xFFF6E6C5)
        "FINISH LINE" -> Color(0xFFF5E5BB)
        "MISCHIEF" -> Color(0xFFECE0EB)
        "ON THE TRAIL" -> Color(0xFFE7E8D8)
        else -> Color(0xFFE3EBD9)
    }
    val paper by animateColorAsState(
        targetValue = target, animationSpec = tween(durationMillis = 220),
        label = "tabletop-turn-ink"
    )
    Surface(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        color = paper, shape = RoundedCornerShape(13.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Canvas(Modifier.size(37.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.43f
                drawCircle(seatColor.copy(alpha = 0.22f),
                    radius = radius * 1.12f, center = center)
                drawCircle(Color(0xFFFFFCF1), radius = radius, center = center)
                drawCircle(seatColor, radius = radius,
                    center = center, style = Stroke(width = 2.3f))
                when (moment) {
                    "YOUR MOVE" -> {
                        val side = radius * 0.92f
                        drawRoundRect(
                            color = PineInk,
                            topLeft = center - Offset(side/2f, side/2f),
                            size = Size(side, side),
                            cornerRadius = CornerRadius(side * 0.20f)
                        )
                        drawCircle(Color.White, radius * 0.08f,
                            center = center)
                    }
                    "AT A CROSSROADS" -> {
                        val ink = Color(0xFF94612E)
                        drawLine(ink, center + Offset(0f, radius * 0.66f),
                            center + Offset(0f, -radius * 0.16f),
                            strokeWidth = 3.1f)
                        drawLine(ink, center + Offset(0f, -radius * 0.16f),
                            center + Offset(-radius * 0.52f, -radius * 0.61f),
                            strokeWidth = 3.1f)
                        drawLine(ink, center + Offset(0f, -radius * 0.16f),
                            center + Offset(radius * 0.52f, -radius * 0.61f),
                            strokeWidth = 3.1f)
                    }
                    "FINISH LINE" -> {
                        val flag = Path().apply {
                            moveTo(center.x - radius * .25f, center.y - radius * .70f)
                            lineTo(center.x + radius * .56f, center.y - radius * .48f)
                            lineTo(center.x - radius * .25f, center.y - radius * .12f)
                            close()
                        }
                        drawPath(flag, Color(0xFFAD8946))
                        drawLine(PineInk, center + Offset(-radius*.25f, -radius*.72f),
                            center + Offset(-radius*.25f, radius*.70f),
                            strokeWidth = 2.8f)
                    }
                    else -> {
                        drawCircle(PineInk, radius * .19f,
                            center + Offset(0f, -radius*.38f))
                        drawLine(PineInk,
                            center + Offset(0f, -radius*.20f),
                            center + Offset(0f, radius*.52f),
                            strokeWidth = 2.8f)
                        drawLine(PineInk,
                            center + Offset(0f, radius*.10f),
                            center + Offset(-radius*.46f, radius*.44f),
                            strokeWidth = 2.5f)
                        drawLine(PineInk,
                            center + Offset(0f, radius*.10f),
                            center + Offset(radius*.46f, radius*.44f),
                            strokeWidth = 2.5f)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(moment, color = CocoaInk,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(title, color = PineInk,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}
