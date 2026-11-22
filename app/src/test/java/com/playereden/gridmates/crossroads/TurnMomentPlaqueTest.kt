package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.Phase
import org.junit.Assert.assertEquals
import org.junit.Test

/** Labels are pure presentation: they cannot change the next legal action. */
class TurnMomentPlaqueTest {
    @Test fun eachTurnPhaseHasAnUnambiguousReadableMoment() {
        assertEquals("DICE ROLL", turnMomentName(Phase.ROLL, false))
        assertEquals("AT A CROSSROADS", turnMomentName(Phase.ROUTE, false))
        assertEquals("MISCHIEF", turnMomentName(Phase.TARGET, false))
        assertEquals("YOUR CHOICE", turnMomentName(Phase.ENCOUNTER, false))
        assertEquals("ENCOUNTER", turnMomentName(Phase.ENCOUNTER, false, botTurn = true))
        assertEquals("FINISH LINE", turnMomentName(Phase.FINISHED, false))
        assertEquals("ON THE TRAIL", turnMomentName(Phase.ROLL, true))
        assertEquals("ON THE TRAIL", turnMomentName(Phase.FINISHED, true))
    }

    @Test fun automatedEncounterCannotAskTheHumanToChoose() {
        val enginePrompt = "AI 1 entered High Ridge. Choose how to respond."
        assertEquals(
            "AI 1 is resolving an encounter…",
            turnActionSummary(Phase.ENCOUNTER, true, "AI 1", enginePrompt)
        )
        assertEquals(
            "Latest action · " + enginePrompt,
            turnActionSummary(Phase.ENCOUNTER, false, "You", enginePrompt)
        )
        assertEquals(
            "Latest action · AI 1 moved ahead.",
            turnActionSummary(Phase.ROLL, true, "AI 1", "AI 1 moved ahead.")
        )
    }
}
