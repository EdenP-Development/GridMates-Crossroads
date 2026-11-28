package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class AdaptiveBotTravelR1Test {
    private fun view(own: Int, other: Int, shield: Boolean = false, final: Boolean = false) =
        BotRaceAssessment.assess(0, listOf(BotRacerView(0, own, shield),
            BotRacerView(1, other, false)), 40, final)

    @Test fun travelRespondsToRaceContext() {
        assertFalse(AdaptiveBotTravelR1.shouldPush(view(40,39)))
        assertTrue(AdaptiveBotTravelR1.shouldPush(view(40,36)))
        assertFalse(AdaptiveBotTravelR1.shouldPush(view(20,19)))
        assertTrue(AdaptiveBotTravelR1.shouldPush(view(20,19,true)))
        assertTrue(AdaptiveBotTravelR1.shouldPush(view(20,18)))
        assertTrue(AdaptiveBotTravelR1.shouldPush(view(8,5)))
        assertFalse(AdaptiveBotTravelR1.shouldPush(view(2,3)))
        assertTrue(AdaptiveBotTravelR1.shouldPush(view(2,0,final = true)))
    }

    @Test fun explicitExperimentalPolicyRoundTripsAndPreservesFuture() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val board = DecisionLab.variantV5(GameData.board(
            File(root, "data/boards/woodland_woven_trail.json").readText()))
        val events = GameData.events(File(root, "data/events/woven_events_v02.json").readText())
        for (seed in 1..12) {
            val game = GameSession(board, events, 3, seed, emptySet(), BotPolicyVersion.ADAPTIVE_V2_R1)
            repeat(12) { if (game.phase != Phase.FINISHED) game.botStep() }
            val wire = MatchCheckpointCodec.encode(game.checkpoint("adaptive-$seed", 1L))
            assertTrue(wire.contains("ADAPTIVE_V2_R1"))
            val mirror = GameSession.restore(board, events, MatchCheckpointCodec.decode(wire))
            assertEquals(BotPolicyVersion.ADAPTIVE_V2_R1, mirror.botPolicy)
            var steps = 0
            while (game.phase != Phase.FINISHED && steps++ < 1500) {
                game.botStep()
                mirror.botStep()
                assertEquals(game.snapshot(), mirror.snapshot())
            }
            assertEquals(Phase.FINISHED, game.phase)
            assertEquals(BotPolicyVersion.ADAPTIVE_V2_R1, game.freshRematch(seed + 100).botPolicy)
        }
        assertEquals(BotPolicyVersion.LEGACY_V1, GameSession(board, events).botPolicy)
    }
}
