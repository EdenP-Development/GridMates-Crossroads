package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class GuardianPersonalityV1Test {
    private fun race(own:Int,other:Int,shield:Boolean=false,final:Boolean=false)=
        BotRaceAssessment.assess(0,listOf(BotRacerView(0,own,shield),
            BotRacerView(1,other,false)),40,final)

    @Test fun travelProtectsProgressButCanCatchUp() {
        assertFalse(GuardianPersonalityV1.shouldPush(race(40,40)))
        assertFalse(GuardianPersonalityV1.shouldPush(race(20,17)))
        assertTrue(GuardianPersonalityV1.shouldPush(race(20,15)))
        assertTrue(GuardianPersonalityV1.shouldPush(race(9,6)))
        assertFalse(GuardianPersonalityV1.shouldPush(race(4,3)))
        assertTrue(GuardianPersonalityV1.shouldPush(race(8,0,final=true)))
    }

    @Test fun defenseDistinguishesShieldFromSteadyProtection() {
        val hazard=listOf("gamble","brace")
        val shelter=listOf("supplies","cover")
        assertEquals("brace",GuardianPersonalityV1.chooseEncounter(hazard,race(20,20,true),2,false))
        assertEquals("gamble",GuardianPersonalityV1.chooseEncounter(hazard,race(20,20),2,true))
        assertEquals("gamble",GuardianPersonalityV1.chooseEncounter(hazard,race(20,15),2,false))
        assertEquals("cover",GuardianPersonalityV1.chooseEncounter(shelter,race(20,20),2,false))
        assertEquals("supplies",GuardianPersonalityV1.chooseEncounter(shelter,race(4,5),2,false))
        assertEquals("supplies",GuardianPersonalityV1.chooseEncounter(shelter,race(20,20,true),2,false))
        assertEquals("gamble",AdaptiveBotEncounterR3.choose(hazard,race(20,20,true),2,false,0))
    }

    @Test fun routePreferenceIsConditionalNotGlobal() {
        assertEquals(11.5,GuardianPersonalityV1.routeScore(10.0,"high",race(20,20),true),0.0)
        assertEquals(9.0,GuardianPersonalityV1.routeScore(10.0,"low",race(20,20),true),0.0)
        assertEquals(10.0,GuardianPersonalityV1.routeScore(10.0,"high",race(20,20),false),0.0)
        assertEquals(10.0,GuardianPersonalityV1.routeScore(10.0,"low",race(20,15),true),0.0)
        assertEquals(10.0,GuardianPersonalityV1.routeScore(10.0,"low",race(20,15,final=true),true),0.0)
    }

    @Test fun encounterChoicesStayLegalAcrossRaceStates() {
        for(own in 1..40) for(other in 1..40) for(shield in listOf(false,true))
            for(final in listOf(false,true)) {
                val view=race(own,other,shield,final)
                for(options in listOf(listOf("supplies","cover"),listOf("gamble","brace")))
                    for(steady in listOf(false,true))
                        assertTrue(GuardianPersonalityV1.chooseEncounter(options,view,2,steady) in options)
            }
    }
}
