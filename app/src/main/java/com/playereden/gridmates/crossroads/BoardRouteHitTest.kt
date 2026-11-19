package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.playereden.crossroads.core.BoardNode
import kotlin.math.max

/** Visual-only hit testing. Callers must supply the engine's legal route nodes. */
internal object BoardRouteHitTest {
    /**
     * Paint just the beginning of each legal branch to keep the original
     * woodland road visible. Tapping still uses the entire legal graph edge.
     */
    fun cueSegment(origin: Offset, end: Offset): Pair<Offset, Offset> =
        (origin + (end - origin) * 0.14f) to
            (origin + (end - origin) * 0.70f)

    fun screenPosition(
        node: BoardNode,
        canvas: IntSize,
        insetPx: Float,
        zoom: Float,
        translation: Offset,
        immersiveWorld: Boolean = false
    ): Offset {
        val cx = canvas.width / 2f
        val cy = canvas.height / 2f
        val boardPoint = BoardWorldViewport.frame(canvas, immersiveWorld).position(node)
        return Offset(
            insetPx + cx + (boardPoint.x - cx) * zoom + translation.x,
            insetPx + cy + (boardPoint.y - cy) * zoom + translation.y
        )
    }

    /**
     * Tap a legal outgoing painted trail, not the shared fork origin.
     * The closest eligible segment or exit marker wins only when unambiguous.
     * Distances are SCREEN pixels, so 1× and 3× keep similar touch comfort.
     */
    fun select(
        tap: Offset,
        origin: BoardNode,
        destinations: List<BoardNode>,
        canvas: IntSize,
        insetPx: Float,
        zoom: Float,
        translation: Offset,
        touchRadiusPx: Float,
        immersiveWorld: Boolean = false
    ): String? {
        if (destinations.isEmpty() || canvas.width <= 36 || canvas.height <= 36 ||
            !tap.x.isFinite() || !tap.y.isFinite() ||
            !zoom.isFinite() || zoom <= 0f ||
            !translation.x.isFinite() || !translation.y.isFinite() ||
            !touchRadiusPx.isFinite() || touchRadiusPx <= 0f) return null

        val start = screenPosition(origin, canvas, insetPx, zoom, translation, immersiveWorld)
        // The fork itself is shared by all options and must never pick a path.
        if ((tap - start).getDistance() < max(20f, touchRadiusPx * 0.76f)) {
            return null
        }

        val matches = destinations.mapNotNull { destination ->
            val end = screenPosition(destination, canvas, insetPx, zoom, translation, immersiveWorld)
            val vector = end - start
            val lengthSquared = vector.x * vector.x + vector.y * vector.y
            if (lengthSquared < 1f) return@mapNotNull null
            val projection = (tap - start)
            val fraction = (
                (projection.x * vector.x + projection.y * vector.y) / lengthSquared
            ).coerceIn(0.34f, 1f)
            val nearestOnTrail = start + vector * fraction
            val distance = minOf(
                (tap - end).getDistance(),
                (tap - nearestOnTrail).getDistance()
            )
            if (distance > touchRadiusPx) null else destination.id to distance
        }.sortedBy { it.second }

        if (matches.isEmpty()) return null
        if (matches.size > 1 &&
            matches[1].second - matches[0].second < max(9f, touchRadiusPx * 0.32f)) {
            return null // crowded fork: use the accessible choice dock
        }
        return matches[0].first
    }
}
