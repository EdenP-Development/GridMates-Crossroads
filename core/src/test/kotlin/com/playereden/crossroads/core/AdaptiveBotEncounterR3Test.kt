package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class AdaptiveBotEncounterR3Test {
    private fun race(own: Int, other: Int, shield: Boolean = false, final: Boolean = false) =
        BotRaceAssessment.assess(0,listOf(BotRacerView(0,own,shield),
            BotRacerView(1,other,false)),40,final)
    @Test fun visibleEncounterTradeoffs() {
        val shelter = listOf("supplies","cover")
        val hazard = listOf("gamble","brace")
        assertEquals("cover",AdaptiveBotEncounterR3.choose(shelter,race(20,22),2,false,0))
        assertEquals("supplies",AdaptiveBotEncounterR3.choose(shelter,race(4,6),2,false,0))
        assertEquals("supplies",AdaptiveBotEncounterR3.choose(shelter,race(20,22,true),2,false,0))
        assertEquals("brace",AdaptiveBotEncounterR3.choose(hazard,race(20,22),2,false,0))
        assertEquals("gamble",AdaptiveBotEncounterR3.choose(hazard,race(20,22),2,true,0))
        assertEquals("gamble",AdaptiveBotEncounterR3.choose(hazard,race(20,19),2,false,1))
        assertEquals("gamble",AdaptiveBotEncounterR3.choose(hazard,race(4,0,final=true),2,false,0))
    }
    @Test fun explicitPolicyReplaysAndCompletes() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val board = DecisionLab.variantV5(GameData.board(File(root,
            "data/boards/woodland_woven_trail.json").readText()))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        var encounters = 0
        for (seed in 1..12) {
            val game = GameSession(board,events,3,seed,emptySet(),BotPolicyVersion.ADAPTIVE_V2_R3)
            var ticks = 0
            while (game.phase != Phase.FINISHED && ticks++ < 1500) {
                if (game.phase == Phase.ENCOUNTER) encounters++
                game.botStep()
            }
            assertEquals(Phase.FINISHED,game.phase)
            val saved = MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(game.checkpoint("r3-$seed",1L)))
            val mirror = GameSession.restore(board,events,saved)
            assertEquals(BotPolicyVersion.ADAPTIVE_V2_R3,mirror.botPolicy)
            assertEquals(game.snapshot(),mirror.snapshot())
        }
        assertTrue(encounters > 0)
    }
}
