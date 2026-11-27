package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class GuardianRelaxedCandidateV1Test {
    @Test fun onlyBoundedNonEndgameTravelChoicesCanChange() {
        var changed=0
        var states=0
        for(own in 1..40) for(other in 0..40)
            for(shield in listOf(false,true)) for(finalRound in listOf(false,true)) {
                val race=BotRaceAssessment.assess(0,listOf(
                    BotRacerView(0,own,shield),BotRacerView(1,other,false)),40,finalRound)
                val standard=GuardianPersonalityV1.shouldPush(race)
                val relaxed=GuardianRelaxedCandidateV1.shouldPush(race)
                val expectedChange=standard && !finalRound &&
                    race.stage!=BotRaceStage.ENDGAME && (own-other) in 5..7
                assertEquals(expectedChange,standard!=relaxed)
                assertFalse(!standard && relaxed)
                if(expectedChange) changed++
                states++
            }
        assertEquals(6560,states)
        assertTrue("Candidate must differ in reachable assessment states",changed>0)
    }

    @Test fun preservesUrgentTravelAndRecoveryAtLargeGaps() {
        fun race(own:Int,other:Int,finalRound:Boolean=false)=
            BotRaceAssessment.assess(0,listOf(BotRacerView(0,own,false),
                BotRacerView(1,other,false)),40,finalRound)
        assertTrue(GuardianPersonalityV1.shouldPush(race(30,24)))
        assertFalse(GuardianRelaxedCandidateV1.shouldPush(race(30,24)))
        assertTrue(GuardianRelaxedCandidateV1.shouldPush(race(30,22)))
        assertTrue(GuardianRelaxedCandidateV1.shouldPush(race(10,4)))
        assertTrue(GuardianRelaxedCandidateV1.shouldPush(race(30,24,true)))
        assertFalse(GuardianRelaxedCandidateV1.shouldPush(race(4,2,true)))
    }
}
