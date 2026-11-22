package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

class CarvedWoodlandTrailsTest {
    @Test fun carvedRoadJointSpacingIsDeterministicAndSparse() {
        val a = carvedRoadLayout(420f, dense = false, visibilityScale = 1.9f)
        val b = carvedRoadLayout(420f, dense = false, visibilityScale = 1.9f)
        assertEquals(a, b)
        assertEquals(1.9f, a.weight, .0001f)
        assertTrue(a.seamFractions.size in 1..7)
        assertTrue(a.seamFractions.zipWithNext().all { (first, second) -> second > first })
        assertTrue(a.seamFractions.all { it > 0f && it < 1f })
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
