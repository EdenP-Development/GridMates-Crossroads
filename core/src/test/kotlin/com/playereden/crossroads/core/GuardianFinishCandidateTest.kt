package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class GuardianFinishCandidateTest {
    @Test fun candidateMatchesItsOwnReplayAndUsesExpectedTravelChoice() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val board=DecisionLab.variantV5(GameData.board(File(root,
            "design/prototypes/woodland_woven_convergence_v02.json").readText()))
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        var recommendations=0
        for(seed in 1..24) for(focal in 0..3) {
            val lineup=(0..3).associateWith {
                if(it==focal) BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1
                else BotPolicyVersion.GUARDIAN_V1
            }
            val game=GameSession(board,events,4,seed,emptySet(),botSeatPolicies=lineup)
            repeat(4) { if(game.phase!=Phase.FINISHED) game.botStep() }
            val checkpoint=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                game.checkpoint("guardian-finish-$seed-$focal",1L)))
            val restored=GameSession.restore(board,events,checkpoint)
            assertEquals(lineup,checkpoint.botSeatPolicies)
            assertEquals(game.snapshot(),restored.snapshot())
            var actions=4
            while(game.phase!=Phase.FINISHED && actions++<1500) {
                val recommendation=if(game.currentIndex==focal)
                    game.directFinishOpportunity()?.preferPush() else null
                game.botStep()
                restored.botStep()
                if(recommendation!=null) {
                    recommendations++
                    assertEquals(if(recommendation) MatchActionKind.BOLD_ROLL
                        else MatchActionKind.STEADY_ROLL,game.lastBotDecision?.kind)
                }
                assertEquals(game.lastBotDecision,restored.lastBotDecision)
                assertEquals(game.snapshot(),restored.snapshot())
            }
            assertEquals(Phase.FINISHED,game.phase)
            val rematch=restored.freshRematch(seed+100)
            for(seat in 0..3) assertEquals(lineup.getValue(seat),rematch.policyForBotSeat(seat))
        }
        assertTrue("No recommendations exercised",recommendations>0)
    }
}
