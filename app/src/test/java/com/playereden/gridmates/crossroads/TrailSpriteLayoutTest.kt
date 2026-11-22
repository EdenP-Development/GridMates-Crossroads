package com.playereden.gridmates.crossroads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/** Art tiling is deterministic and must never modify board positions or hit targets. */
class TrailSpriteLayoutTest {
    @Test fun veryShortEdgeUsesOneSafeFallback() {
        val slices = planTrailSpriteSlices(10f, 13.2f)
        assertEquals(1, slices.size)
        assertEquals(0, slices[0].sourceX)
        assertEquals(128, slices[0].sourceWidth)
        assertEquals(10f, slices[0].widthPx, 0.001f)
    }

    @Test fun longEdgeHasTwoCapsAndUnstretchedRepeatedCenters() {
        val length = 220f
        val height = 13.2f
        val slices = planTrailSpriteSlices(length, height)
        assertTrue(slices.size > 3)
        assertEquals(0, slices.first().sourceX)
        assertEquals(16, slices.first().sourceWidth)
        assertEquals(112, slices.last().sourceX)
        assertEquals(16, slices.last().sourceWidth)
        val center = slices.drop(1).dropLast(1)
        assertTrue(center.all { it.sourceX == 16 && it.sourceWidth == 96 })
        assertTrue(center.all { it.widthPx > 0f && it.widthPx <= height * 3f + 0.001f })
        assertEquals(220f, slices.sumOf { it.widthPx.toDouble() }.toFloat(), 0.005f)
        for (pair in slices.zipWithNext()) {
            assertTrue(abs(pair.first.offsetPx + pair.first.widthPx -
                pair.second.offsetPx) < 0.005f)
        }
        assertEquals(length, slices.last().offsetPx + slices.last().widthPx, 0.005f)
    }

    @Test fun denseAndExtremelyLongEdgesRespectStampBudget() {
        val dense = planTrailSpriteSlices(90f, 13.2f * 0.87f)
        assertTrue(dense.size in 3..26)
        val extreme = planTrailSpriteSlices(10_000f, 13.2f)
        assertEquals(26, extreme.size)
        assertEquals(10_000f, extreme.last().offsetPx + extreme.last().widthPx, 0.01f)
    }

    @Test fun invalidLengthsProduceNoDrawCommands() {
        assertTrue(planTrailSpriteSlices(Float.NaN, 13.2f).isEmpty())
        assertTrue(planTrailSpriteSlices(Float.POSITIVE_INFINITY, 13.2f).isEmpty())
        assertTrue(planTrailSpriteSlices(10f, 0f).isEmpty())
        assertTrue(planTrailSpriteSlices(10f, Float.NaN).isEmpty())
    }
}
