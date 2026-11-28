package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class GamblerIntegrationTest {
    @Test fun actualGamblerUsesDistinctOpeningAndRecovers() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val board=DecisionLab.variantV5(GameData.board(File(root,
            "design/prototypes/woodland_woven_convergence_v02.json").readText()))
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val lineup=BotStrategyLineup.resolve(listOf(BotStrategyPreset.GAMBLER,BotStrategyPreset.SCOUT))
        for(seed in 1..24) {
            val game=GameSession(board,events,2,seed,emptySet(),botSeatPolicies=lineup)
            game.botStep()
            assertEquals(MatchActionKind.BOLD_ROLL,game.lastBotDecision?.kind)
            val midpoint=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(game.checkpoint("gambler-$seed",1L)))
            assertEquals(lineup,midpoint.botSeatPolicies)
            val recovered=GameSession.restore(board,events,midpoint)
            assertEquals(game.snapshot(),recovered.snapshot())
            var actions=1
            while(game.phase!=Phase.FINISHED && actions++<1500) {
                game.botStep(); recovered.botStep()
                assertEquals(game.lastBotDecision,recovered.lastBotDecision)
                assertEquals(game.snapshot(),recovered.snapshot())
            }
            assertEquals(Phase.FINISHED,game.phase)
            assertEquals(BotPolicyVersion.GAMBLER_V1,recovered.freshRematch(seed+100).policyForBotSeat(0))
        }
    }
}
