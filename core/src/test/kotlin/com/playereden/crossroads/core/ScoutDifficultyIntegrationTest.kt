package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class ScoutDifficultyIntegrationTest {
    @Test fun onlyImplementedCombinationsResolve() {
        for(personality in BotPersonalityV1.entries) {
            assertEquals(personality.policy,BotOpponentPolicyV1.resolve(
                BotOpponentConfigurationV1(personality)))
            for(difficulty in listOf(BotDifficultyV1.RELAXED,BotDifficultyV1.ADVANCED)) {
                val configuration=BotOpponentConfigurationV1(personality,difficulty)
                if(personality==BotPersonalityV1.SCOUT) {
                    assertEquals(if(difficulty==BotDifficultyV1.RELAXED)
                        BotPolicyVersion.SCOUT_RELAXED_V1 else BotPolicyVersion.SCOUT_ADVANCED_V1,
                        BotOpponentPolicyV1.resolve(configuration))
                } else {
                    try { BotOpponentPolicyV1.resolve(configuration); fail("Unsupported variant resolved") }
                    catch(expected:IllegalArgumentException) { }
                }
            }
        }
    }

    @Test fun scoutTiersRecoverAndPreserveExactDecisions() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val board=DecisionLab.variantV5(GameData.board(File(root,
            "design/prototypes/woodland_woven_convergence_v02.json").readText()))
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        for(seed in 1..24) for(rotation in 0..2) {
            val variants=BotDifficultyV1.entries.map {
                BotOpponentPolicyV1.resolve(BotOpponentConfigurationV1(BotPersonalityV1.SCOUT,it))
            }
            val lineup=(0..2).associateWith { variants[(it-rotation+3)%3] }
            val game=GameSession(board,events,3,seed,emptySet(),botSeatPolicies=lineup)
            repeat(4) { if(game.phase!=Phase.FINISHED) game.botStep() }
            val saved=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                game.checkpoint("scout-difficulty-$seed-$rotation",1L)))
            assertEquals(lineup,saved.botSeatPolicies)
            val restored=GameSession.restore(board,events,saved)
            assertEquals(game.snapshot(),restored.snapshot())
            var actions=4
            while(game.phase!=Phase.FINISHED && actions++<1500) {
                game.botStep();restored.botStep()
                assertEquals(game.lastBotDecision,restored.lastBotDecision)
                assertEquals(game.snapshot(),restored.snapshot())
            }
            assertEquals(Phase.FINISHED,game.phase)
            val rematch=restored.freshRematch(seed+100)
            for(seat in 0..2) assertEquals(lineup.getValue(seat),rematch.policyForBotSeat(seat))
        }
    }
}
