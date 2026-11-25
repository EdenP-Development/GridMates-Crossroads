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
    @Test fun controlledMischiefReachesTargetAndReplays() {
        // Both exits have the same advertised risk and the only event is a prank.
        // A Steady opening roll reaches the fork without relying on any random draw.
        fun node(id: String, vararg next: String) =
            BoardNode(id,"forest","path",next.toList(),0f,0f)
        val board = DecisionLab.variantV5(BoardDefinition("r4-target-fixture","start","finish",
            listOf(node("start","a","b"),node("a","c"),node("b","c"),
                node("c","d"),node("d","e"),node("e","finish"),node("finish")),
            listOf(ForkDefinition("start",choices=listOf(
                ForkChoice("a","Mischief A","medium"),ForkChoice("b","Mischief B","medium"))))))
        val events = EventCatalogue(listOf(EventDefinition("fixture_prank",listOf("forest"),
            1,EventEffect("prank",2),listOf("mischief"))))
        val game = GameSession(board,events,4,17,emptySet(),BotPolicyVersion.ADAPTIVE_V2_R4)
        game.botStep()
        assertEquals(Phase.ROUTE,game.phase)
        game.botStep()
        assertEquals(Phase.TARGET,game.phase)
        assertEquals(listOf(1,2,3),game.availableTargets())
        val saved = MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
            game.checkpoint("forced-target",1L)))
        val mirror = GameSession.restore(board,events,saved)
        game.botStep()
        mirror.botStep()
        assertEquals(game.snapshot(),mirror.snapshot())
        assertEquals(1,game.currentIndex)
        assertEquals(Phase.ROLL,game.phase)
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
        // Natural target frequency is diagnostic, not a guaranteed property of this course.
        println("R4 natural Woven target phases: $targets")
    }
}
