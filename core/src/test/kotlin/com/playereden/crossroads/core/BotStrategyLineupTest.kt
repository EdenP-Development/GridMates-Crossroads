package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class BotStrategyLineupTest {
    @Test fun resolvesMixedLineupWithoutAssigningHumanPolicy() {
        assertEquals(mapOf(1 to BotPolicyVersion.ADAPTIVE_V2_R2,
            2 to BotPolicyVersion.ADAPTIVE_V2_R3,3 to BotPolicyVersion.ADAPTIVE_V2_R4),
            BotStrategyLineup.resolve(listOf(null,BotStrategyPreset.SCOUT,
                BotStrategyPreset.GUARDIAN,BotStrategyPreset.STRATEGIST)))
        assertTrue(BotStrategyLineup.resolve(listOf(null,null)).isEmpty())
        assertEquals(mapOf(1 to BotPolicyVersion.LEGACY_V1),
            BotStrategyLineup.resolve(listOf(null,BotStrategyPreset.CLASSIC)))
    }
    @Test fun invalidSeatCountsRejected() {
        for (size in listOf(0,1,5)) {
            try {
                BotStrategyLineup.resolve(List(size){BotStrategyPreset.CLASSIC})
                fail("Invalid lineup size accepted")
            } catch (_: IllegalArgumentException) {}
        }
    }
    @Test fun mixedPresetMatchRestoresResolvedPolicies() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val board=DecisionLab.variantV5(GameData.board(File(root,
            "design/prototypes/woodland_woven_convergence_v02.json").readText()))
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val lineup=BotStrategyLineup.resolve(listOf(BotStrategyPreset.CLASSIC,
            BotStrategyPreset.SCOUT,BotStrategyPreset.GUARDIAN,BotStrategyPreset.STRATEGIST))
        for(seed in 1..24) {
            val game=GameSession(board,events,4,seed,emptySet(),botSeatPolicies=lineup)
            var actions=0
            while(game.phase!=Phase.FINISHED && actions++<1500) game.botStep()
            assertEquals(Phase.FINISHED,game.phase)
            val saved=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(game.checkpoint("preset-$seed",1L)))
            assertEquals(lineup,saved.botSeatPolicies)
            val restored=GameSession.restore(board,events,saved)
            assertEquals(game.snapshot(),restored.snapshot())
            val rematch=restored.freshRematch(seed+100)
            assertEquals(lineup,(0..3).associateWith{rematch.policyForBotSeat(it)})
        }
    }
}
