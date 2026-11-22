package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.playereden.crossroads.core.BoardNode
import kotlin.math.hypot

/**
 * GAMEFEEL-64 prototype: make ordinary authored graph nodes read like small
 * carved tabletop landing spaces rather than tiny dots painted onto scenery.
 *
 * Pure drawing; it does not add spaces to the graph, move pieces, or change
 * legal routes. Initially QA live-world only; full-map remains unchanged.
 */
internal object CrossroadsBoardSpaceGeometry {
    /** Nearest board node of ANY type also protects special markers from overlap. */
    fun nearestSpacing(node: BoardNode, nodes: List<BoardNode>): Float =
        nodes.asSequence().filter { it.id != node.id }
            .map { other -> hypot(node.x - other.x, node.y - other.y) }
            .minOrNull() ?: Float.POSITIVE_INFINITY

    /**
     * Enlarge comfortable Classic/Quick spaces, but never blindly double
     * waypoints in dense Cedarwood layouts. Coordinates remain authoritative.
     */
    fun radius(
        originalRadius: Float,
        nearestSpacingNormalized: Float,
        squareWorldSidePx: Float,
        liveWorld: Boolean
    ): Float {
        if (!liveWorld || originalRadius <= 0f || !originalRadius.isFinite()) {
            return originalRadius
        }
        val preferred = originalRadius * 1.45f
        val spacingCap = if (nearestSpacingNormalized.isFinite() &&
            nearestSpacingNormalized > 0f && squareWorldSidePx.isFinite() &&
            squareWorldSidePx > 0f
        ) nearestSpacingNormalized * squareWorldSidePx * 0.33f
            else preferred
        return minOf(preferred, spacingCap).coerceAtLeast(3.5f)
    }
}

/** A wooden board-space disk: recessed shadow, raised honey rim, warm face. */
internal fun DrawScope.drawCrossroadsTactileSpace(center: Offset, radius: Float) {
    if (!radius.isFinite() || radius <= 0f) return
    drawCircle(
        color = Color(0xFF283D2E).copy(alpha = 0.48f),
        radius = radius * 1.20f,
        center = center + Offset(0f, radius * 0.30f)
    )
    drawCircle(Color(0xFF59452E), radius = radius * 1.17f, center = center)
    drawCircle(Color(0xFFC89E5E), radius = radius * 1.07f, center = center)
    drawCircle(Color(0xFFF8E7BD), radius = radius * 0.83f, center = center)
    drawCircle(Color(0xFFEEE0BC), radius = radius * 0.62f, center = center)
    drawCircle(
        color = Color.White.copy(alpha = 0.43f),
        radius = radius * 0.17f,
        center = center + Offset(-radius * 0.25f, -radius * 0.26f)
    )
}
