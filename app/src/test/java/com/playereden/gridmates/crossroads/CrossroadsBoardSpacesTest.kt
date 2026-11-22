package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.BoardNode
import org.junit.Assert.*
import org.junit.Test

/** GAMEFEEL-64: visibility is proportional to actual authored board spacing. */
class CrossroadsBoardSpacesTest {
    private fun node(id: String, x: Float, y: Float, kind: String = "plain") =
        BoardNode(id, "Meadow", kind, emptyList(), x, y)

    @Test fun nearestSpacingCountsAllNodeKinds() {
        val space = node("A", 0.2f, 0.2f)
        val fork = node("F", 0.25f, 0.2f, "fork")
        val remote = node("B", 0.8f, 0.8f)
        assertEquals(0.05f,
            CrossroadsBoardSpaceGeometry.nearestSpacing(space, listOf(space, fork, remote)),
            .0001f)
        assertEquals(Float.POSITIVE_INFINITY,
            CrossroadsBoardSpaceGeometry.nearestSpacing(space, listOf(space)),
            0f)
    }

    @Test fun classicCourseSpacesGrowWhenTheyHaveRoom() {
        val normal = CrossroadsBoardSpaceGeometry.radius(10f, .08f, 1400f, true)
        assertEquals(14.5f, normal, .0001f)
        assertTrue(normal > 10f)
    }

    @Test fun cedarwoodDensityCapsSpacingToPreventCoinOverlaps() {
        val nearestGapPx = 0.012f * 1400f
        val dense = CrossroadsBoardSpaceGeometry.radius(7.4f, .012f, 1400f, true)
        assertTrue("Dense road must not carry oversized markers", dense < 7.4f * 1.45f)
        assertTrue("Nearby circles need to leave visual air between them",
            dense * 2f <= nearestGapPx * 0.67f)
        assertTrue(dense >= 3.5f)
    }

    @Test fun overviewNeverEnlargesSpaces() {
        assertEquals(7f, CrossroadsBoardSpaceGeometry.radius(7f, .20f, 1400f, false),
            .0001f)
        assertEquals(4.75f, CrossroadsBoardSpaceGeometry.radius(4.75f, .005f, 1400f,
            false), .0001f)
    }

    @Test fun invalidSpacingDoesNotProduceNaNOrCrash() {
        val singleSpace = CrossroadsBoardSpaceGeometry.radius(
            8f, Float.POSITIVE_INFINITY, 1400f, true)
        assertEquals(11.6f, singleSpace, .0001f)
        assertTrue(CrossroadsBoardSpaceGeometry.radius(8f, Float.NaN, 1400f, true).isFinite())
        assertEquals(0f, CrossroadsBoardSpaceGeometry.radius(0f, .1f, 1400f, true),
            .0001f)
    }
}
