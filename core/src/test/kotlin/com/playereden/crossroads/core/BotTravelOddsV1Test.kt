package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class BotTravelOddsV1Test {
    @Test fun exactSixOutcomeCounts() {
        assertEquals(listOf(6,4,4,4,4,3,2,1,0),
            (1..9).map(BotTravelOddsV1::pushFinishOutcomes))
        assertEquals(listOf(6,6,6,6,0,0,0,0,0),
            (1..9).map(BotTravelOddsV1::steadyFinishOutcomes))
        assertEquals(28,BotTravelOddsV1.pushMovementTotal())
        assertEquals(10,BotTravelOddsV1.pushExpectedExcessNumerator(4))
        assertEquals(0,BotTravelOddsV1.pushExpectedExcessNumerator(8))
    }
    @Test fun invalidDistanceRejected() {
        for(distance in listOf(0,-1)) {
            try { BotTravelOddsV1.pushFinishOutcomes(distance);fail("Invalid distance accepted") }
            catch(expected:IllegalArgumentException) { }
        }
    }
}
