package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class AdaptiveBotRouteR2Test {
    private val race = BotRaceAssessment.assess(0,
        listOf(BotRacerView(0,10,false),BotRacerView(1,14,false)),40,false)
    private val budget = RouteHorizon("a",2,RouteHorizonStop.BUDGET,emptyList())
    @Test fun shelterMomentumAndSpentEncounterAffectRouteScore() {
        fun score(risk: String, available: Boolean, steady: Boolean) =
            AdaptiveBotRouteR2.score(9,risk,budget,race,available,steady)
        assertTrue(score("low",true,true) < score("low",true,false))
        assertTrue(score("low",true,false) < score("high",true,false))
        assertEquals(score("low",false,true),score("high",false,true),0.0)
    }
    @Test fun reachableFinishGetsBonusOnlyWithoutPendingEncounter() {
        val finish = budget.copy(stop = RouteHorizonStop.FINISH)
        assertTrue(AdaptiveBotRouteR2.score(2,"high",finish,race,false,false) <
            AdaptiveBotRouteR2.score(2,"high",budget,race,false,false))
        assertEquals(AdaptiveBotRouteR2.score(2,"high",finish,race,true,false),
            AdaptiveBotRouteR2.score(2,"high",budget,race,true,false),0.0)
    }
    @Test fun r2ReplaysWithoutChangingDefault() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val board = DecisionLab.variantV5(GameData.board(File(root,
            "data/boards/woodland_woven_trail.json").readText()))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        for (seed in 1..12) {
            val game = GameSession(board,events,3,seed,emptySet(),BotPolicyVersion.ADAPTIVE_V2_R2)
            repeat(12) { if (game.phase != Phase.FINISHED) game.botStep() }
            val wire = MatchCheckpointCodec.encode(game.checkpoint("r2-$seed",1L))
            val mirror = GameSession.restore(board,events,MatchCheckpointCodec.decode(wire))
            assertEquals(BotPolicyVersion.ADAPTIVE_V2_R2,mirror.botPolicy)
            var ticks = 0
            while (game.phase != Phase.FINISHED && ticks++ < 1500) {
                game.botStep(); mirror.botStep()
                assertEquals(game.snapshot(),mirror.snapshot())
            }
            assertEquals(Phase.FINISHED,game.phase)
        }
        assertEquals(BotPolicyVersion.LEGACY_V1,GameSession(board,events).botPolicy)
    }
}
