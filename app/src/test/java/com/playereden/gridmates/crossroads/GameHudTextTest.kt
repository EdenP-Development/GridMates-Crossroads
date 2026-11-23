package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.Phase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameHudTextTest {
    @Test fun gameHudUsesShortActionStageLabels() {
        assertEquals("CHOOSE YOUR PACE", gameHudActionHeading(Phase.ROLL))
        assertEquals("PICK YOUR PATH", gameHudActionHeading(Phase.ROUTE))
        assertEquals("MAKE YOUR CHOICE", gameHudActionHeading(Phase.ENCOUNTER))
        assertEquals("CHOOSE A TARGET", gameHudActionHeading(Phase.TARGET))
        assertEquals("JOURNEY COMPLETE", gameHudActionHeading(Phase.FINISHED))
    }
    @Test fun compactActionCopyRetainsRealEncounterConsequences() {
        assertEquals(EncounterActionCopy("SUPPLIES", "+2 spaces"), encounterActionCopy("supplies"))
        assertEquals(EncounterActionCopy("COVER", "+1 & shield"), encounterActionCopy("cover"))
        assertEquals(EncounterActionCopy("GAMBLE", "Face hazard"), encounterActionCopy("gamble"))
        assertEquals(EncounterActionCopy("BRACE", "Stop & shield"), encounterActionCopy("brace"))
        assertEquals(EncounterActionCopy("OTHER", "Choose"), encounterActionCopy("other"))
    }

    @Test fun crossroadsNarrationIsAvailableInDetailsWithoutTakingBoardSpace() {
        assertEquals(false, showCollapsedActionNote(Phase.ROUTE, false))
        assertEquals(false, showCollapsedActionNote(Phase.ROUTE, true))
        assertEquals(false, showCollapsedActionNote(Phase.ENCOUNTER, true))
        assertEquals(true, showCollapsedActionNote(Phase.ENCOUNTER, false))
        assertEquals(true, showCollapsedActionNote(Phase.ROLL, false))
        assertEquals(true, showCollapsedActionNote(Phase.TARGET, false))
        assertEquals(true, showCollapsedActionNote(Phase.FINISHED, false))
    }

    @Test fun theCompactDockStillFitsAllPrimaryActionsOnShortPhones() {
        assertTrue(gameActionDockMinimumHeight(Phase.ROLL).value >= 200f)
        assertTrue(gameActionDockMinimumHeight(Phase.ROUTE).value >= 240f)
        assertTrue(gameActionDockMinimumHeight(Phase.ENCOUNTER).value >= 270f)
        assertTrue(gameActionDockMinimumHeight(Phase.TARGET).value >= 245f)
        assertTrue(gameActionDockMinimumHeight(Phase.FINISHED).value >= 170f)
    }

    @Test fun routeAndRollStaySmallButEncountersGetMoreRoom() {
        assertEquals(0.32f, gameActionDockMaxFraction(Phase.ROLL), 0.001f)
        assertEquals(0.32f, gameActionDockMaxFraction(Phase.ROUTE), 0.001f)
        assertEquals(0.37f, gameActionDockMaxFraction(Phase.ENCOUNTER), 0.001f)
        assertEquals(0.39f, gameActionDockMaxFraction(Phase.TARGET), 0.001f)
    }
}
