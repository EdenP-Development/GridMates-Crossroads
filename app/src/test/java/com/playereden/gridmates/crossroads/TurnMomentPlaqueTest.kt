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
        assertEquals("FINISH LINE", turnMomentName(Phase.FINISHED, false))
        assertEquals("ON THE TRAIL", turnMomentName(Phase.ROLL, true))
        assertEquals("ON THE TRAIL", turnMomentName(Phase.FINISHED, true))
    }
}
