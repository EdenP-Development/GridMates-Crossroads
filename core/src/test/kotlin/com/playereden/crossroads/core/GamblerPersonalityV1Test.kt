package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class GamblerPersonalityV1Test {
    private fun race(own:Int,other:Int,shield:Boolean=false,final:Boolean=false)=
        BotRaceAssessment.assess(0,listOf(BotRacerView(0,own,shield),
            BotRacerView(1,other,false)),40,final)

    @Test fun travelSeeksUpsideButProtectsReliableFinish() {
        assertTrue(GamblerPersonalityV1.shouldPush(race(40,40)))
        assertTrue(GamblerPersonalityV1.shouldPush(race(20,21)))
        assertFalse(GamblerPersonalityV1.shouldPush(race(20,25)))
        assertFalse(GamblerPersonalityV1.shouldPush(race(4,5)))
        assertTrue(GamblerPersonalityV1.shouldPush(race(4,5,final=true)))
        assertTrue(GamblerPersonalityV1.shouldPush(race(9,3)))
        assertFalse(AdaptiveBotTravelR1.shouldPush(race(40,40)))
    }

    @Test fun encountersSeekUpsideWithVisibleDefensiveExceptions() {
        val shelter=listOf("supplies","cover")
        val hazard=listOf("gamble","brace")
        assertEquals("supplies",GamblerPersonalityV1.chooseEncounter(shelter,race(20,20),1,false,0))
        assertEquals("cover",GamblerPersonalityV1.chooseEncounter(shelter,race(20,25),1,false,0))
        assertEquals("supplies",GamblerPersonalityV1.chooseEncounter(shelter,race(3,9),1,false,0))
        assertEquals("gamble",GamblerPersonalityV1.chooseEncounter(hazard,race(20,20),1,false,0))
        assertEquals("brace",GamblerPersonalityV1.chooseEncounter(hazard,race(20,25),1,false,0))
        assertEquals("gamble",GamblerPersonalityV1.chooseEncounter(hazard,race(20,25),1,true,0))
        assertEquals("gamble",GamblerPersonalityV1.chooseEncounter(hazard,race(20,25),1,false,2))
        assertEquals("cover",GamblerPersonalityV1.chooseEncounter(listOf("cover"),race(20,20),1,false,0))
    }

    @Test fun choicesAreLegalAcrossVisibleRaceStates() {
        for (own in 1..40) for (other in 1..40)
            for (shield in listOf(false,true)) for (final in listOf(false,true)) {
                val view=race(own,other,shield,final)
                for(options in listOf(listOf("cover","supplies"),listOf("brace","gamble")))
                    assertTrue(GamblerPersonalityV1.chooseEncounter(options,view,2,false,0) in options)
            }
    }
}
