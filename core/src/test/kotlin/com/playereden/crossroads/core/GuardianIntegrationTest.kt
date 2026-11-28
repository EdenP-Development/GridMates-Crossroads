package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class GuardianIntegrationTest {
    @Test fun mixedPersonalitiesRetainDecisionsAfterRecovery() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val board=DecisionLab.variantV5(GameData.board(File(root,
            "design/prototypes/woodland_woven_convergence_v02.json").readText()))
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        for(seed in 1..24) for(focal in 0..1) {
            val lineup=(0..1).associateWith {
                if(it==focal) BotPolicyVersion.GUARDIAN_V1 else BotPolicyVersion.GAMBLER_V1
            }
            val game=GameSession(board,events,2,seed,emptySet(),botSeatPolicies=lineup)
            game.botStep()
            assertEquals(if(focal==0) MatchActionKind.STEADY_ROLL else MatchActionKind.BOLD_ROLL,
                game.lastBotDecision?.kind)
            val saved=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                game.checkpoint("guardian-$seed",1L)))
            assertEquals(lineup,saved.botSeatPolicies)
            val restored=GameSession.restore(board,events,saved)
            assertEquals(game.snapshot(),restored.snapshot())
            var actions=1
            while(game.phase!=Phase.FINISHED && actions++<1500) {
                game.botStep()
                restored.botStep()
                assertEquals(game.lastBotDecision,restored.lastBotDecision)
                assertEquals(game.snapshot(),restored.snapshot())
            }
            assertEquals(Phase.FINISHED,game.phase)
            assertEquals(BotPolicyVersion.GUARDIAN_V1,
                restored.freshRematch(seed+100).policyForBotSeat(focal))
        }
    }
}
