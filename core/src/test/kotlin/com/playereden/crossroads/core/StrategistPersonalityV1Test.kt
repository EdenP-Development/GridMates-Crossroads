package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class StrategistPersonalityV1Test {
    private fun race(own:Int,other:Int,final:Boolean=false)=
        BotRaceAssessment.assess(0,listOf(BotRacerView(0,own,false),
            BotRacerView(1,other,false)),40,final)

    @Test fun travelRespondsToImmediateFinishThreat() {
        assertFalse(StrategistPersonalityV1.shouldPush(race(4,3)))
        assertTrue(StrategistPersonalityV1.shouldPush(race(7,4)))
        assertFalse(StrategistPersonalityV1.shouldPush(race(40,40)))
        assertTrue(StrategistPersonalityV1.shouldPush(race(6,0,true)))
    }

    @Test fun routeBonusRequiresUninterruptedMovement() {
        val finish=RouteHorizon("finish",2,RouteHorizonStop.FINISH,emptyList())
        val fork=RouteHorizon("fork",2,RouteHorizonStop.FORK,listOf("low","high"))
        assertEquals(10.0,StrategistPersonalityV1.routeScore(10.0,finish,race(5,6),true),0.0)
        assertEquals(7.0,StrategistPersonalityV1.routeScore(10.0,finish,race(5,6),false),0.0)
        assertEquals(4.0,StrategistPersonalityV1.routeScore(10.0,finish,race(5,0,true),false),0.0)
        assertEquals(9.25,StrategistPersonalityV1.routeScore(10.0,fork,race(20,20),false),0.0)
    }

    @Test fun targetingValuesDenyingSteadyFinishOverRawSetback() {
        val urgent=BotTargetView(1,4,false,5)
        val larger=BotTargetView(2,5,false,9)
        assertEquals(2,AdaptiveBotTargetR4.choose(listOf(urgent,larger)))
        assertEquals(1,StrategistPersonalityV1.chooseTarget(listOf(urgent,larger)))
        assertEquals(2,StrategistPersonalityV1.chooseTarget(listOf(urgent.copy(shield=true),larger)))
        assertNull(StrategistPersonalityV1.chooseTarget(emptyList()))
    }

    @Test fun targetingIsDeterministicAndLegal() {
        for(a in 1..12) for(b in 1..12) for(delta in 0..6) {
            val targets=listOf(BotTargetView(1,a,false,a+delta),BotTargetView(2,b,true,b))
            val choice=StrategistPersonalityV1.chooseTarget(targets)
            assertTrue(choice in targets.map { it.seat })
            assertEquals(choice,StrategistPersonalityV1.chooseTarget(targets.reversed()))
        }
    }
}
