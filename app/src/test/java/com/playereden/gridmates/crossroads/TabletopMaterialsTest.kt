package com.playereden.gridmates.crossroads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TabletopMaterialsTest {
    @Test fun numberedPlayerMarkersPreserveWhoIsActingAndShieldStatus() {
        assertEquals("Player 1, You, current player",
            TabletopMaterials.label(0, "You", "current player", false))
        assertEquals("Player 2, AI 1, currently moving, shielded",
            TabletopMaterials.label(1, "AI 1", "currently moving", true))
        assertEquals("Player 4, Player 4, waiting",
            TabletopMaterials.label(3, "Player 4", "waiting", false))
    }

    @Test fun allFourSeatsRemainDistinguishableBeyondColor() {
        val labels = (0 until 4).map { index ->
            TabletopMaterials.label(index, "Player", "waiting", false)
        }
        assertEquals(4, labels.distinct().size)
        labels.forEachIndexed { index, label ->
            assertTrue(label.startsWith("Player " + (index + 1) + ","))
        }
    }

    @Test fun boardRimIsNarrowAndDoesNotTurnIntoAnObscuringOverlay() {
        assertTrue(TabletopMaterials.RAIL_WIDTH_DP >= 5f)
        assertTrue(TabletopMaterials.RAIL_WIDTH_DP <= 12f)
        assertTrue(TabletopMaterials.SCORE_PAWN_DIAMETER_DP >= 24f)
        assertTrue(TabletopMaterials.SCORE_PAWN_DIAMETER_DP <= 32f)
    }
}
