package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

class CarvedWaypointMedallionsTest {
    @Test fun startAndFinishHaveDistinctStableRimPatterns() {
        val start = CarvedWaypointGeometry.rimMarks(false)
        val finish = CarvedWaypointGeometry.rimMarks(true)
        assertEquals(4, start.size)
        assertEquals(6, finish.size)
        assertNotEquals(start, finish)
        listOf(start, finish).forEach { marks ->
            assertEquals(marks.size, marks.distinct().size)
            assertTrue(marks.all { it >= 0f && it < 360f })
        }
    }

    @Test fun startFinishBaseIsBoundedByNeighborDistanceOnDenseCourse() {
        val original = 13f
        val radius = CarvedWaypointGeometry.socketRadius(original, 0.035f, 960f)
        assertTrue(radius >= original * .72f)
        assertTrue(radius < original * 1.7f)
        assertTrue(radius <= 960f * .035f * .30f)
    }

    @Test fun normalCourseCanUseFullSizeAndFullMapNeverCallsTheseDecorations() {
        val result = CarvedWaypointGeometry.socketRadius(13f, .13f, 960f)
        assertEquals(22.1f, result, .001f)
        assertEquals(0f, CarvedWaypointGeometry.socketRadius(Float.NaN, 0.1f, 960f), .001f)
        assertEquals(0f, CarvedWaypointGeometry.socketRadius(-1f, 0.1f, 960f), .001f)
    }
}
