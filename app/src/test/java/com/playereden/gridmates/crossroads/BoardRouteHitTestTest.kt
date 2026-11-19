package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.playereden.crossroads.core.BoardNode
import org.junit.Assert.*
import org.junit.Test

class BoardRouteHitTestTest {
    private val fork = BoardNode("fork", "Forest", "fork",
        listOf("left", "right"), 0.5f, 0.5f)
    private val left = BoardNode("left", "Meadow", "event",
        emptyList(), 0.25f, 0.33f, route = "safe")
    private val right = BoardNode("right", "River", "event",
        emptyList(), 0.75f, 0.33f, route = "risky")
    private val canvas = IntSize(400, 400)
    private val noTranslation = Offset.Zero
    private val inset = 8f
    private val radius = 25f

    private fun choose(tap: Offset, options: List<BoardNode> = listOf(left, right),
        zoom: Float = 1f, pan: Offset = noTranslation): String? =
        BoardRouteHitTest.select(tap, fork, options, canvas, inset, zoom, pan, radius)

    @Test fun eachGraphLegalExitCanBeSelectedByTappingItsMarker() {
        val leftAt = BoardRouteHitTest.screenPosition(left, canvas, inset, 1f, noTranslation)
        val rightAt = BoardRouteHitTest.screenPosition(right, canvas, inset, 1f, noTranslation)
        assertEquals("left", choose(leftAt))
        assertEquals("right", choose(rightAt))
    }

    @Test fun tappingTrailSegmentWorksWithoutTouchingItsNode() {
        val start = BoardRouteHitTest.screenPosition(fork, canvas, inset, 1f, noTranslation)
        val end = BoardRouteHitTest.screenPosition(left, canvas, inset, 1f, noTranslation)
        val midpoint = start + (end - start) * 0.68f
        assertEquals("left", choose(midpoint))
    }

    @Test fun cameraZoomAndTranslationAreAccountedFor() {
        val pan = Offset(43f, -17f)
        val chosen = BoardRouteHitTest.screenPosition(right, canvas, inset, 2.2f, pan)
        assertEquals("right", choose(chosen, zoom = 2.2f, pan = pan))
        // The original, untransformed target is now NOT at the display position.
        assertNull(choose(
            BoardRouteHitTest.screenPosition(right, canvas, inset, 1f, noTranslation),
            zoom = 2.2f, pan = pan
        ))
    }

    @Test fun forkCenterAndOutsideTapsAreNotActions() {
        val start = BoardRouteHitTest.screenPosition(fork, canvas, inset, 1f, noTranslation)
        assertNull(choose(start))
        assertNull(choose(Offset(9f, 11f)))
        assertNull(choose(Offset(-100f, 100f)))
    }

    @Test fun ambiguousOrUnavailableOptionsNeverAutoChoose() {
        val leftAt = BoardRouteHitTest.screenPosition(left, canvas, inset, 1f, noTranslation)
        assertNull(choose(leftAt, options = emptyList()))
        val overlapping = left.copy(id = "duplicate")
        assertNull(choose(leftAt, options = listOf(left, overlapping)))
    }

    @Test fun invalidCameraAndViewportDataFailClosed() {
        val point = Offset(190f, 190f)
        assertNull(BoardRouteHitTest.select(point, fork, listOf(left), IntSize.Zero,
            inset, 1f, noTranslation, radius))
        assertNull(choose(point, zoom = Float.NaN))
        assertNull(choose(point, pan = Offset(Float.NaN, 0f)))
        assertNull(BoardRouteHitTest.select(point, fork, listOf(left), canvas,
            inset, 1f, noTranslation, 0f))
    }
}
