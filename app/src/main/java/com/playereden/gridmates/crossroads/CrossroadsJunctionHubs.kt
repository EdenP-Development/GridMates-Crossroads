package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min

/**
 * GAMEFEEL-68: visual junction caps over the endpoints of painted road sprites.
 * Purely decorative: the actual BoardDefinition coordinates and touch-testing
 * still identify forks/merges, and the painted marker is drawn over this cap.
 */
internal object CrossroadsJunctionHubGeometry {
    fun eligible(
        kind: String,
        immersive: Boolean,
        illustrated: Boolean,
        paintedRoad: Boolean
    ): Boolean =
        immersive && illustrated && paintedRoad && (kind == "fork" || kind == "merge")

    fun radius(
        nearestNodeSpacing: Float,
        worldSide: Float,
        denseCourse: Boolean
    ): Float {
        if (!worldSide.isFinite() || worldSide <= 0f) return 0f
        val desired = if (denseCourse) 24f else 29f
        val separationCap = if (nearestNodeSpacing.isFinite() &&
            nearestNodeSpacing > 0f) {
            // Never let one joint consume half the distance to the next space.
            nearestNodeSpacing * worldSide * 0.35f
        } else desired
        return min(desired, separationCap).coerceAtLeast(0f)
    }
}

/**
 * A restrained carved wooden hub visually joins incoming/outgoing road strips.
 * The outer dark rim hides atlas-edge pileup; the warm center suggests a
 * raised tabletop intersection. Draw BEFORE the authored fork marker/pawns.
 */
internal fun DrawScope.drawCrossroadsWoodenJunctionHub(center: Offset, radius: Float) {
    if (!radius.isFinite() || radius < 8f) return
    drawCircle(
        color = Color(0xFF243B30).copy(alpha = 0.45f),
        radius = radius * 1.10f,
        center = center + Offset(0f, radius * .16f)
    )
    drawCircle(color = Color(0xFF344C3B), radius = radius, center = center)
    drawCircle(color = Color(0xFFB98C50), radius = radius * .89f, center = center)
    drawCircle(color = Color(0xFFE9CF95), radius = radius * .75f, center = center)
    drawCircle(
        color = Color(0xFF805F38).copy(alpha = 0.60f),
        radius = radius * .70f,
        center = center,
        style = Stroke(width = (radius * .045f).coerceAtLeast(.7f))
    )
}
