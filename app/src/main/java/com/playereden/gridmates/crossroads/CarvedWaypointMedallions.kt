package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Physical start/finish sockets for miniature travelers. All decoration sits
 * UNDER the existing engine-authored S/F letter and player pieces.
 */
internal object CarvedWaypointGeometry {
    // Decorative rim only: preserve the central S/F label and existing socket footprint.
    fun rimMarks(isFinish: Boolean): List<Float> =
        if (isFinish) listOf(30f, 90f, 150f, 210f, 270f, 330f)
        else listOf(35f, 145f, 215f, 325f)

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
    drawCircle(if (isFinish) Color(0xFFD3AE63) else Color(0xFFA88A58),
        radius = radius * 0.98f, center = center)
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
    // Trailhead: four leaf-like engravings; destination: six brass sunburst marks.
    // All ink remains within the existing ring, behind the authoritative label.
    CarvedWaypointGeometry.rimMarks(isFinish).forEach { degrees ->
        val radians = degrees * kotlin.math.PI / 180.0
        val direction = Offset(kotlin.math.cos(radians).toFloat(), kotlin.math.sin(radians).toFloat())
        val tangent = Offset(-direction.y, direction.x)
        val at = center + direction * (radius * .82f)
        val ink = if (isFinish) Color(0xFF9A682D) else Color(0xFF426446)
        drawLine(ink, at - tangent * (radius * .07f), at + tangent * (radius * .07f),
            strokeWidth = radius * .065f, cap = StrokeCap.Round)
        if (!isFinish) {
            drawLine(ink, at, at + direction * (radius * .075f),
                strokeWidth = radius * .045f, cap = StrokeCap.Round)
        }
    }
}
