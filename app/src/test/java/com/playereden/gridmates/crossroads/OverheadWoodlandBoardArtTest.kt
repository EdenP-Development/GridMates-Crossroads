package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.BoardNode
import org.junit.Assert.*
import org.junit.Test

class OverheadWoodlandBoardArtTest {
    private val graph = listOf(
        BoardNode("start", "meadow", "start", listOf("fork"), .12f, .78f),
        BoardNode("fork", "meadow", "fork", listOf("safe", "risky"), .48f, .50f),
        BoardNode("safe", "forest", "event", listOf("finish"), .74f, .31f),
        BoardNode("risky", "forest", "event", listOf("finish"), .67f, .66f),
        BoardNode("finish", "village", "finish", emptyList(), .92f, .14f)
    )

    @Test fun overheadPrintIsOnlyAnOptInWovenQaPreview() {
        assertTrue(useOverheadBoardPreview(
            "woodland_woven_trail_v02", true, true, true
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_woven_trail_v02", false, true, true
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_woven_trail_v02", true, false, true
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_woven_trail_v02", true, true, false
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_brookside_quick_v01", true, true, true
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_cedarwood_long_v01", true, true, true
        ))
    }

    @Test fun overheadFeaturesStayAwayFromRealRoadsAndMarkers() {
        assertFalse(OverheadScenery.clearOfPlay(.48f, .50f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(.30f, .64f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(.92f, .14f, graph, .067f))
        assertTrue(OverheadScenery.clearOfPlay(.10f, .12f, graph, .067f))
        val ornaments = OverheadScenery.plan(graph)
        assertTrue(ornaments.isNotEmpty())
        ornaments.forEach { place ->
            val margin = if (place.kind == OverheadDecoration.COTTAGE) .084f else .067f
            assertTrue("Scenery cannot masquerade as a legal route or covered node",
                OverheadScenery.clearOfPlay(place.x, place.y, graph, margin))
        }
    }

    @Test fun sceneryIsDeterministicAndBoundedAcrossRecompositions() {
        val a = OverheadScenery.plan(graph)
        val b = OverheadScenery.plan(graph)
        assertEquals(a, b)
        assertTrue(a.size <= 49)
        assertEquals(a.size, a.distinct().size)
        assertTrue(a.all { it.x in 0f..1f && it.y in 0f..1f })
        assertTrue(OverheadScenery.plan(emptyList()).isEmpty())
    }

    @Test fun graphSegmentDistanceHandlesEndpointsAndShortSegments() {
        assertEquals(0f, OverheadScenery.distanceToEdge(.5f, .5f,
            .5f, .5f, .5f, .5f), .001f)
        assertEquals(.2f, OverheadScenery.distanceToEdge(.5f, .7f,
            .2f, .5f, .8f, .5f), .001f)
        assertEquals(.3f, OverheadScenery.distanceToEdge(.9f, .5f,
            .3f, .5f, .6f, .5f), .001f)
    }

    @Test fun invalidOutOfBoundsDecorationPointsNeverPassClearance() {
        assertFalse(OverheadScenery.clearOfPlay(Float.NaN, .5f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(-.1f, .5f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(.5f, 1.1f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(.5f, .5f, graph, 0f))
    }
}
