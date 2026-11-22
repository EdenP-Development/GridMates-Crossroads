package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Physical start/finish sockets for miniature travelers. All decoration sits
 * UNDER the existing engine-authored S/F letter and player pieces.
 */
internal object CarvedWaypointGeometry {
    fun socketRadius(
        baseRadius: Float,
        neighborDistanceNormalized: Float,
        worldSide: Float
    ): Float {
        if (!baseRadius.isFinite() || baseRadius <= 0f) return 0f
        val desired = baseRadius * 1.7f
        if (!neighborDistanceNormalized.isFinite() ||
            neighborDistanceNormalized <= 0f ||
            !worldSide.isFinite() || worldSide <= 0f) return desired
        return minOf(desired, neighborDistanceNormalized * worldSide * 0.29f)
            .coerceAtLeast(baseRadius * 0.72f)
    }
}

internal fun DrawScope.drawCarvedStartFinishBase(
    center: Offset,
    radius: Float,
    isFinish: Boolean
) {
    if (!radius.isFinite() || radius <= 0f) return
    val paint = if (isFinish) Color(0xFFC99D57) else Color(0xFF73936C)
    // Raised wooden socket with a warm metallic rim. Result is not a
    // new selectable node, only a better legible starting/destination place.
    drawCircle(
        Color(0xFF203329).copy(alpha = 0.37f),
        radius = radius * 1.11f,
        center = center + Offset(0f, radius * 0.26f)
    )
    drawCircle(Color(0xFF493D30), radius = radius * 1.05f, center = center)
    drawCircle(Color(0xFFC19D65), radius = radius * 0.98f, center = center)
    drawCircle(Color(0xFFEEE0BD), radius = radius * 0.84f, center = center)
    drawCircle(paint, radius = radius * 0.66f, center = center)
    drawCircle(
        Color(0xFFFFF5D9).copy(alpha = 0.91f),
        radius = radius * 0.72f,
        center = center,
        style = Stroke(width = radius * 0.07f)
    )
    drawCircle(
        Color(0xFF3B4936).copy(alpha = 0.33f),
        radius = radius * 0.90f,
        center = center,
        style = Stroke(width = radius * 0.055f)
    )
}
