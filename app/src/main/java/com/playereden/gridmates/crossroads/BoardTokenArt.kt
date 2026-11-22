package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Original tabletop-style piece shared by the live board and its overview.
 * Decorative drawing only: the numeric seat labels and authoritative positions
 * still come from the game snapshot in MainActivity.
 */
internal fun DrawScope.drawCrossroadsPiece(
    center: Offset,
    radius: Float,
    seatColor: Color,
    highlighted: Boolean,
    traveling: Boolean
) {
    if (radius <= 0f) return

    // Each piece reads as a tactile, slightly raised game token on parchment.
    // Keep the shadow/halo under the number and away from the fork risk symbols.
    drawCircle(
        color = Color(0xFF514837).copy(alpha = 0.23f),
        radius = radius * 1.13f,
        center = center + Offset(0f, radius * 0.25f)
    )
    if (highlighted) {
        drawCircle(
            color = Color(0xFFFDF8E7).copy(alpha = if (traveling) 0.97f else 0.87f),
            radius = radius * 1.51f,
            center = center,
            style = Stroke(width = radius * 0.20f)
        )
        drawCircle(
            color = seatColor.copy(alpha = if (traveling) 0.78f else 0.60f),
            radius = radius * 1.69f,
            center = center,
            style = Stroke(width = radius * 0.13f)
        )
    }
    // Warm engraved rim and raised colored enamel. The numbered center remains
    // unobstructed, so colorblind players can still distinguish each seat.
    drawCircle(Color(0xFF765F4B).copy(alpha = 0.62f), radius * 1.17f, center)
    drawCircle(Color(0xFFD5B778), radius * 1.08f, center)
    drawCircle(Color(0xFFFFF8E6), radius * 1.00f, center)
    drawCircle(seatColor, radius * 0.88f, center + Offset(0f, -radius * 0.06f))
    drawCircle(
        Color(0xFF493A2C).copy(alpha = 0.20f),
        radius * 0.93f, center,
        style = Stroke(width = radius * 0.07f)
    )

    // Soft highlights make the token legible against both dark forest and
    // pale meadow without adding another icon or changing the player's color.
    drawCircle(
        color = Color.White.copy(alpha = 0.23f),
        radius = radius * 0.47f,
        center = center + Offset(-radius * 0.24f, -radius * 0.31f)
    )
    drawCircle(
        color = Color(0xFF3A302C).copy(alpha = 0.15f),
        radius = radius * 0.72f,
        center = center + Offset(radius * 0.08f, radius * 0.20f),
        style = Stroke(width = radius * 0.11f)
    )
}

/** A missing atlas or non-QA build must keep the familiar numbered pieces. */
internal fun shouldShowWoodlandToken(
    isQa: Boolean, requested: Boolean, atlasLoaded: Boolean
): Boolean = isQa && requested && atlasLoaded

/**
 * Keep portraits as an enlargement detail, not tiny, overlapping dots on
 * the fit-to-board map. The stable colored/numbered coin is always available.
 *
 * Zoom is camera-only; no game state, token location or hit radius changes.
 */
internal fun shouldDrawWoodlandPortrait(
    artAvailable: Boolean,
    cameraZoom: Float
): Boolean = artAvailable && cameraZoom.isFinite() && cameraZoom >= 1.65f
