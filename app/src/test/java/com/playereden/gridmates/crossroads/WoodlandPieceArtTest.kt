package com.playereden.gridmates.crossroads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure QA art selection: the game never depends on portraits. */
class WoodlandPieceArtTest {
    @Test fun onlyRequestedAndLoadedQaArtReplacesClassicTokenCenter() {
        assertTrue(shouldShowWoodlandToken(true, true, true))
        assertFalse(shouldShowWoodlandToken(true, false, true))
        assertFalse(shouldShowWoodlandToken(true, true, false))
        assertFalse(shouldShowWoodlandToken(false, true, true))
    }

    @Test fun fourAtlasSlotsCorrespondToTheFourOriginalSeatColors() {
        val expectedOffsets = (0 until 4).map { it * 128 }
        assertEquals(listOf(0, 128, 256, 384), expectedOffsets)
        assertEquals("token_atlas_v1.png", CrossroadsArtSlot.TOKEN_ATLAS.filename)
    }

    @Test fun fitToBoardAlwaysPrefersLegibleNumberedCoins() {
        assertFalse(shouldDrawWoodlandPortrait(true, 1f))
        assertFalse(shouldDrawWoodlandPortrait(true, 1.35f))
        assertFalse(shouldDrawWoodlandPortrait(true, 1.64f))
        assertTrue(shouldDrawWoodlandPortrait(true, 1.65f))
        assertTrue(shouldDrawWoodlandPortrait(true, 3f))
    }

    @Test fun absentArtAndInvalidZoomNeverRenderTinyFaces() {
        assertFalse(shouldDrawWoodlandPortrait(false, 3f))
        assertFalse(shouldDrawWoodlandPortrait(true, Float.NaN))
        assertFalse(shouldDrawWoodlandPortrait(true, Float.POSITIVE_INFINITY))
        assertFalse(shouldDrawWoodlandPortrait(true, -1f))
    }
    @Test fun widerLiveCameraRetainsPortraitsWhenRenderedSizeIsAdequate() {
        assertTrue(shouldDrawWoodlandPortrait(true, 1.15f, true, 50f))
        assertTrue(shouldDrawWoodlandPortrait(true, 1f, true, 44f))
        assertFalse(shouldDrawWoodlandPortrait(true, 1.15f, true, 30f))
        assertFalse(shouldDrawWoodlandPortrait(false, 1.15f, true, 50f))
        assertFalse(shouldDrawWoodlandPortrait(true, 1.15f, true, Float.NaN))
        assertFalse(shouldDrawWoodlandPortrait(true, 1f, false, 50f))
    }
}
