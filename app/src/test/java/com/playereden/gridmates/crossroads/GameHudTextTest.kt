package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.Phase
import org.junit.Assert.assertEquals
import org.junit.Test

class GameHudTextTest {
    @Test fun gameHudUsesShortActionStageLabels() {
        assertEquals("CHOOSE YOUR PACE", gameHudActionHeading(Phase.ROLL))
        assertEquals("PICK YOUR PATH", gameHudActionHeading(Phase.ROUTE))
        assertEquals("MAKE YOUR CHOICE", gameHudActionHeading(Phase.ENCOUNTER))
        assertEquals("CHOOSE A TARGET", gameHudActionHeading(Phase.TARGET))
        assertEquals("JOURNEY COMPLETE", gameHudActionHeading(Phase.FINISHED))
    }
}
