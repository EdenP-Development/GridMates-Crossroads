package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Experimental printed-board trail treatment.
 *
 * Trail geometry is ALWAYS sourced from the authoritative BoardDefinition
 * and drawn dynamically on the same Canvas/camera as the tokens.
 * These strokes are not baked into any generated image.
 *
 * The QA menu exposes both variants for a same-board comparison:
 * - printed = layered ink, parchment ribbon and colored stitching
 * - simple  = established light-line renderer from GAMEFEEL-41
 *
 * This is deliberately not a new collectible/theme pack or game rule.
 */
internal fun DrawScope.drawCrossroadsTrail(
    from: Offset,
    to: Offset,
    route: String,
    dense: Boolean,
    printed: Boolean
) {
    val routeInk = when (route) {
        "risky" -> Color(0xFFC29450)
        "safe" -> Color(0xFF83A181)
        "mischief" -> Color(0xFFA18BB1)
        else -> Color(0xFFD4C3A3)
    }
    if (!printed) {
        // Keep the last QA-approved legibility treatment intact for A/B review.
        drawLine(
            Color(0xFF334C3C).copy(alpha = 0.80f), from, to,
            strokeWidth = if (dense) 12f else 15f, cap = StrokeCap.Round
        )
        drawLine(
            routeInk, from, to,
            strokeWidth = if (dense) 8.5f else 11f, cap = StrokeCap.Round
        )
        drawLine(
            Color(0xFFFCF8ED), from, to,
            strokeWidth = if (dense) 2.1f else 3f, cap = StrokeCap.Round
        )
        return
    }

    // A narrow inked shadow and warm paper deck mimic a printed board-game
    // path. Distinct route types show through as colored sidebands and dashes.
    // No random grain: deterministic strokes avoid shimmer while panning.
    val weight = if (dense) 0.88f else 1f
    drawLine(
        Color(0xFF31453A).copy(alpha = 0.66f), from, to,
        strokeWidth = 15f * weight, cap = StrokeCap.Round
    )
    drawLine(
        routeInk.copy(alpha = 0.92f), from, to,
        strokeWidth = 12f * weight, cap = StrokeCap.Round
    )
    drawLine(
        Color(0xFFF6EBD0), from, to,
        strokeWidth = 8.3f * weight, cap = StrokeCap.Round
    )
    drawLine(
        routeInk.copy(alpha = 0.80f), from, to,
        strokeWidth = 1.7f * weight, cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.5f, 8.5f), 0f)
    )
}
