package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class GamblerPersonalityV2Test {
    private fun race(own:Int,other:Int,shield:Boolean=false,final:Boolean=false)=
        BotRaceAssessment.assess(0,listOf(BotRacerView(0,own,shield),
            BotRacerView(1,other,false)),40,final)

    @Test fun visiblePushBonusDoesNotAlwaysOverrideProtection() {
        val options=listOf("gamble","brace")
        val leading=race(10,12)
        assertEquals("gamble",GamblerPersonalityV1.chooseEncounter(options,leading,1,false,2))
        assertEquals("brace",GamblerPersonalityV2.chooseEncounter(options,leading,1,false,2))
        assertEquals("gamble",GamblerPersonalityV2.chooseEncounter(options,leading,3,false,2))
        assertEquals("gamble",GamblerPersonalityV2.chooseEncounter(options,leading,1,true,2))
        assertEquals("gamble",GamblerPersonalityV2.chooseEncounter(options,race(10,12,true),1,false,2))
        assertEquals("gamble",GamblerPersonalityV2.chooseEncounter(options,race(10,12,final=true),1,false,2))
        assertEquals("gamble",GamblerPersonalityV2.chooseEncounter(options,race(10,9),1,false,2))
    }

    @Test fun travelAndShelterRemainIdenticalToG1() {
        val options=listOf("supplies","cover")
        for(own in 1..40) for(other in 1..40) for(shield in listOf(false,true))
            for(final in listOf(false,true)) {
                val view=race(own,other,shield,final)
                assertEquals(GamblerPersonalityV1.shouldPush(view),GamblerPersonalityV2.shouldPush(view))
                assertEquals(GamblerPersonalityV1.chooseEncounter(options,view,2,false,2),
                    GamblerPersonalityV2.chooseEncounter(options,view,2,false,2))
            }
    }
}
