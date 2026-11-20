package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

/** Visual rules are deterministic; the actual roll is always owned by GameSession. */
class TabletopDiceFinishTest {
    @Test fun eachSixSidedFaceHasCorrectUniquePips() {
        for (face in 1..6) {
            val pips = TabletopDiePips.positions(face)
            assertEquals("A die's face must have the matching pip count", face, pips.size)
            assertEquals("A die's pips must not overlap exactly", face, pips.distinct().size)
            assertTrue("All pips must stay inside the raised face", pips.all { (x, y) ->
                x in .20f.. .80f && y in .20f.. .80f
            })
        }
    }

    @Test fun decorativePreviewClampsInvalidValuesWithoutNewRng() {
        assertEquals(TabletopDiePips.positions(1), TabletopDiePips.positions(-100))
        assertEquals(TabletopDiePips.positions(6), TabletopDiePips.positions(100))
        assertEquals(listOf(.5f to .5f), TabletopDiePips.positions(1))
        assertTrue(TabletopDiePips.positions(3).contains(.5f to .5f))
        assertTrue(TabletopDiePips.positions(5).contains(.5f to .5f))
    }

    @Test fun winnerSealAndHeadlinesDependOnActualWinnerIdentityNotSeatIndex() {
        assertEquals("You reached the finish!", CrossroadsFinishCopy.headline("You"))
        assertTrue(CrossroadsFinishCopy.isLocalWinner("You"))
        assertEquals("AI 1 wins!", CrossroadsFinishCopy.headline("AI 1"))
        assertEquals("Player 4 wins!", CrossroadsFinishCopy.headline("Player 4"))
        assertFalse(CrossroadsFinishCopy.isLocalWinner("AI 1"))
        assertFalse(CrossroadsFinishCopy.isLocalWinner("Player 4"))
    }
}
