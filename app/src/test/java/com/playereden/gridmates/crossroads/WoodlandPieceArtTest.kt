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
}
