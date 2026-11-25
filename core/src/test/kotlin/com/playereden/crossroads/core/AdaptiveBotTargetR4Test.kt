package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class AdaptiveBotTargetR4Test {
    @Test fun meaningfulSetbackCanBeatShieldedLeader() {
        assertEquals(2,AdaptiveBotTargetR4.choose(listOf(
            BotTargetView(1,12,true,12),BotTargetView(2,13,false,16))))
    }
    @Test fun distantOpponentDoesNotDistractFromImmediateThreat() {
        assertEquals(1,AdaptiveBotTargetR4.choose(listOf(
            BotTargetView(1,2,true,2),BotTargetView(2,20,false,25))))
    }
    @Test fun noLegalTargetsAndStableTies() {
        assertNull(AdaptiveBotTargetR4.choose(emptyList()))
        assertEquals(1,AdaptiveBotTargetR4.choose(listOf(
            BotTargetView(2,10,false,12),BotTargetView(1,10,false,12))))
    }
    @Test fun explicitPolicyCompletesAndReplays() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val board = DecisionLab.variantV5(GameData.board(File(root,
            "data/boards/woodland_woven_trail.json").readText()))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        var targets = 0
        for (seed in 1..64) {
            val game = GameSession(board,events,4,seed,emptySet(),BotPolicyVersion.ADAPTIVE_V2_R4)
            var ticks = 0
            while (game.phase != Phase.FINISHED && ticks++ < 1500) {
                if (game.phase == Phase.TARGET) {
                    targets++
                    val saved = game.checkpoint("r4-target-$seed",1L)
                    val mirror = GameSession.restore(board,events,saved)
                    game.botStep(); mirror.botStep()
                    assertEquals(game.snapshot(),mirror.snapshot())
                } else game.botStep()
            }
            assertEquals(Phase.FINISHED,game.phase)
            val saved = MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(game.checkpoint("r4-$seed",1L)))
            assertEquals(BotPolicyVersion.ADAPTIVE_V2_R4,saved.botPolicy)
            assertEquals(game.snapshot(),GameSession.restore(board,events,saved).snapshot())
        }
        assertTrue("Seed matrix must exercise targeting",targets > 0)
    }
}
