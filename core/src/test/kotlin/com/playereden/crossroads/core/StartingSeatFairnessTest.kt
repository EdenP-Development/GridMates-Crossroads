package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Identical policies isolate seat exposure from personality strength. */
class StartingSeatFairnessTest {
    @Test fun compareFirstArrivalAndEqualRoundWinners() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val boards = listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json",
            "data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json",
            "design/prototypes/woodland_woven_convergence_v02.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val policies = listOf(BotPolicyVersion.LEGACY_V1, BotPolicyVersion.ADAPTIVE_V2_R1,
            BotPolicyVersion.ADAPTIVE_V2_R2, BotPolicyVersion.GAMBLER_V1,
            BotPolicyVersion.GUARDIAN_V1, BotPolicyVersion.STRATEGIST_V1)
        val rows = mutableListOf<String>()
        for (board in boards) for (policy in policies) for (seats in 2..4) for(seed in 1..64) {
            val game = GameSession(board,events,seats,seed,emptySet(),policy)
            val rolls = IntArray(seats)
            var actions = 0
            while(game.phase != Phase.FINISHED && actions < 1500) {
                if(game.phase == Phase.ROLL) rolls[game.currentIndex]++
                game.botStep()
                actions++
            }
            assertEquals(Phase.FINISHED,game.phase)
            val receipt = requireNotNull(game.fairFinishReceipt)
            val first = game.players.indexOfFirst { it.name == receipt.firstFinisherName }
            val winner = requireNotNull(game.winnerIndex)
            assertTrue(first in 0 until seats)
            assertTrue(winner in 0 until seats)
            assertTrue(rolls.all { it > 0 })
            rows += (listOf(board.id,policy,seats,seed,first+1,winner+1,
                receipt.finishers,receipt.tiedOnWinningSteps,actions) +
                List(4) { rolls.getOrNull(it)?.toString() ?: "" }).joinToString(",")
        }
        assertEquals(5760,rows.size)
        val out = File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"starting_seat_fairness.csv").writeText(
            "course,policy,seats,seed,first_seat,winner_seat,finishers,tied_best,actions,rolls_1,rolls_2,rolls_3,rolls_4\n"+
            rows.joinToString("\n")+"\n")
    }
}
