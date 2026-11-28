package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class StrategistIntegrationTest {
    @Test fun mixedPersonalitiesReplayAndRematch() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val board=DecisionLab.variantV5(GameData.board(File(root,
            "design/prototypes/woodland_woven_convergence_v02.json").readText()))
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        for(seed in 1..24) for(focal in 0..3) {
            val policies=listOf(BotPolicyVersion.STRATEGIST_V1, BotPolicyVersion.GUARDIAN_V1,
                BotPolicyVersion.GAMBLER_V1, BotPolicyVersion.ADAPTIVE_V2_R4)
            val lineup=(0..3).associateWith { policies[(it-focal+4)%4] }
            val game=GameSession(board,events,4,seed,emptySet(),botSeatPolicies=lineup)
            repeat(4) {
                if(game.phase!=Phase.FINISHED) game.botStep()
            }
            val saved=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                game.checkpoint("strategist-$seed-$focal",1L)))
            assertEquals(lineup,saved.botSeatPolicies)
            val restored=GameSession.restore(board,events,saved)
            assertEquals(game.snapshot(),restored.snapshot())
            var actions=4
            while(game.phase!=Phase.FINISHED && actions++<1500) {
                game.botStep()
                restored.botStep()
                assertEquals(game.lastBotDecision,restored.lastBotDecision)
                assertEquals(game.snapshot(),restored.snapshot())
            }
            assertEquals(Phase.FINISHED,game.phase)
            val rematch=restored.freshRematch(seed+100)
            for(seat in 0..3) assertEquals(lineup.getValue(seat),rematch.policyForBotSeat(seat))
        }
    }
}
