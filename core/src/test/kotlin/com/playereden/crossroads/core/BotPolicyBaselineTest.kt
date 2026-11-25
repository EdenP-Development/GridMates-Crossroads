package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Baseline only: exercise the shipped bot policy, never change its RNG or actions. */
class BotPolicyBaselineTest {
    @Test fun allBotRacesCompleteAndReplayAcrossCoursesAndRuleVersions() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val events = GameData.events(File(root, "data/events/woven_events_v02.json").readText())
        val originals = listOf("woodland_brookside_quick", "woodland_woven_trail",
            "woodland_cedarwood_long").map {
            GameData.board(File(root, "data/boards/$it.json").readText())
        }
        val regional = GameData.board(File(root, "design/prototypes/woodland_woven_regional_v05.json").readText())
        val boards = originals + originals.map { DecisionLab.variantV5(it) } +
            listOf(DecisionLab.variantV5(regional))
        val rows = mutableListOf<String>()
        for (board in boards) for (seats in 2..4) for (seed in 1..12) {
            val game = GameSession(board, events, seats, seed, emptySet())
            val counts = mutableMapOf<Phase, Int>()
            var actions = 0
            while (game.phase != Phase.FINISHED && actions < 1500) {
                val phase = game.phase
                counts[phase] = (counts[phase] ?: 0) + 1
                game.botStep()
                actions++
            }
            assertEquals("Stalled: ${board.id}/$seats/$seed", Phase.FINISHED, game.phase)
            val winner = requireNotNull(game.winnerIndex)
            assertEquals(board.finish, game.players[winner].position)
            val checkpoint = game.checkpoint("baseline-$seed", 1L)
            val restored = GameSession.restore(board, events, checkpoint)
            assertEquals(game.snapshot(), restored.snapshot())
            rows += listOf(board.id, seats, seed, winner + 1, actions,
                counts[Phase.ROLL] ?: 0, counts[Phase.ROUTE] ?: 0,
                counts[Phase.ENCOUNTER] ?: 0, counts[Phase.TARGET] ?: 0).joinToString(",")
        }
        assertEquals(252, rows.size)
        val out = File(root, "core/build/reports/ai2-baseline")
        check(out.exists() || out.mkdirs())
        File(out, "bot_policy_baseline.csv").writeText(
            "course,seats,seed,winner_seat,actions,rolls,routes,encounters,targets\n" +
                rows.joinToString("\n") + "\n")
    }
}
