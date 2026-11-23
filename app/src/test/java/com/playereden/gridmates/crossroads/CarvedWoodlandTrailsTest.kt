package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

class CarvedWoodlandTrailsTest {
    @Test fun carvedRoadJointSpacingIsDeterministicAndSparse() {
        val a = carvedRoadLayout(420f, dense = false, visibilityScale = 1.9f)
        val b = carvedRoadLayout(420f, dense = false, visibilityScale = 1.9f)
        assertEquals(a, b)
        assertEquals(1.9f * CARVED_ROAD_INK_RATIO, a.weight, .0001f)
        assertTrue(a.seamFractions.size in 1..5)
        assertTrue(a.seamFractions.zipWithNext().all { (first, second) -> second > first })
        assertTrue(a.seamFractions.all { it > 0f && it < 1f })
    }

    @Test fun artworkStaysSharpWithoutCoveringTheWoodlandScenery() {
        // Previously a full-bright trail at 1.9 zoom occupied 45.6px
        // (24 * 1.9); the owner phone screen showed rails overwhelming art.
        val originalOuterWidth = 24f * 1.9f
        val tunedOuterWidth = 24f * carvedRoadLayout(420f, false, 1.9f).weight
        assertTrue(tunedOuterWidth < originalOuterWidth * .85f)
        assertTrue(tunedOuterWidth > originalOuterWidth * .70f)
        assertTrue("The road remains readable instead of turning into a hairline",
            tunedOuterWidth > 30f)
        assertTrue("The joinery must not produce fake nodes",
            carvedRoadLayout(420f, false, 1.9f).seamFractions.size <= 3)
        assertTrue("Quick/Classic and denser Cedarwood should stay distinct",
            carvedRoadLayout(420f, true, 1.9f).weight <
                carvedRoadLayout(420f, false, 1.9f).weight)
    }

    @Test fun shortAndDenseCourseEdgesDoNotGainFakeSteppingSpaces() {
        assertTrue(carvedRoadLayout(32f, true, 1.8f).seamFractions.isEmpty())
        assertTrue(carvedRoadLayout(50f, false, 1.9f).seamFractions.isEmpty())
        assertTrue(carvedRoadLayout(100f, false, 1.9f).seamFractions.isEmpty())
        assertTrue(carvedRoadLayout(10000f, false, 1.9f).seamFractions.size <= 7)
    }

    @Test fun invalidRoadGeometryCannotPaintOrGenerateDecorativeJoints() {
        listOf(Float.NaN, Float.POSITIVE_INFINITY, -3f, 0f).forEach { length ->
            assertTrue(carvedRoadLayout(length, false, 2f).seamFractions.isEmpty())
            assertEquals(0f, carvedRoadLayout(length, false, 2f).weight, .001f)
        }
        assertEquals(0f, carvedRoadLayout(100f, false, Float.NaN).weight, .001f)
        assertEquals(0f, carvedRoadLayout(100f, false, -1f).weight, .001f)
    }

    @Test fun riskInlaysStayDistinctAndDoNotChangeTheirMeaning() {
        val safe = carvedRoadRiskColor("safe")
        val risky = carvedRoadRiskColor("risky")
        val mischief = carvedRoadRiskColor("mischief")
        assertNotEquals(safe, risky)
        assertNotEquals(risky, mischief)
        assertNotEquals(safe, mischief)
        assertEquals(carvedRoadRiskColor("neutral"), carvedRoadRiskColor("unknown"))
    }
}
