package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class BotDifficultyV1Test {
    @Test fun standardPreservesEveryPersonalityScoreExactly() {
        val horizon=RouteHorizon("route",2,RouteHorizonStop.BUDGET,emptyList())
        for(p in BotPersonalityV1.entries) for(score in listOf(-3.5,0.0,8.25,50.0))
            for(distance in 0..40) for(encounter in listOf(false,true)) {
                assertEquals(score,BotDifficultyRouteV1.score(BotDifficultyV1.STANDARD,
                    p,score,distance,horizon,encounter),0.0)
                assertEquals(score,BotDifficultyTargetV1.score(BotDifficultyV1.STANDARD,
                    score,BotTargetView(1,distance,false,distance+2)),0.0)
            }
    }

    @Test fun advancedOnlyRewardsVisibleRouteOpportunities() {
        val finish=RouteHorizon("finish",2,RouteHorizonStop.FINISH,emptyList())
        val fork=RouteHorizon("fork",2,RouteHorizonStop.FORK,listOf("low","high"))
        for(p in BotPersonalityV1.entries) {
            assertEquals(6.0,BotDifficultyRouteV1.score(BotDifficultyV1.ADVANCED,p,10.0,2,finish,false),0.0)
            assertEquals(10.0,BotDifficultyRouteV1.score(BotDifficultyV1.ADVANCED,p,10.0,2,finish,true),0.0)
            assertEquals(10.0,BotDifficultyRouteV1.score(BotDifficultyV1.RELAXED,p,10.0,2,finish,false),0.0)
            assertEquals(8.0,BotDifficultyRouteV1.score(BotDifficultyV1.RELAXED,p,10.0,8,fork,false),0.0)
        }
        assertEquals(9.0,BotDifficultyRouteV1.score(BotDifficultyV1.ADVANCED,
            BotPersonalityV1.STRATEGIST,10.0,8,fork,false),0.0)
    }

    @Test fun targetAdjustmentsRespectShieldAndFinishThreat() {
        val target=BotTargetView(1,4,false,6)
        assertEquals(6.0,BotDifficultyTargetV1.score(BotDifficultyV1.ADVANCED,4.0,target),0.0)
        assertEquals(3.0,BotDifficultyTargetV1.score(BotDifficultyV1.RELAXED,4.0,target),0.0)
        for(difficulty in BotDifficultyV1.entries)
            assertEquals(4.0,BotDifficultyTargetV1.score(difficulty,4.0,target.copy(shield=true)),0.0)
    }

    @Test fun configurationDefaultsToStandardWithoutChangingPolicy() {
        for(p in BotPersonalityV1.entries) {
            assertEquals(BotDifficultyV1.STANDARD,BotOpponentConfigurationV1(p).difficulty)
            for(d in BotDifficultyV1.entries)
                assertEquals(p,BotOpponentConfigurationV1(p,d).personality)
        }
    }
}
