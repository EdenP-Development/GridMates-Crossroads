package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Diagnose actual route choices and finish tie frequency without changing rules. */
class BotBalanceDiagnosticsTest {
    @Test fun auditRouteExposureAndFinishReceipts() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val boards = listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json","data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val routeRows = mutableListOf<String>()
        val finishRows = mutableListOf<String>()
        for (board in boards) for (policy in BotPolicyVersion.entries)
            for (seats in listOf(2,4)) for (seed in 1..24) {
                val game = GameSession(board,events,seats,seed,emptySet(),policy)
                var actions = 0
                while (game.phase != Phase.FINISHED && actions < 1500) {
                    if (game.phase == Phase.ROUTE) {
                        val actor = game.currentIndex
                        val player = game.players[actor]
                        val fork = requireNotNull(board.forkAt(player.position))
                        val before = player.traversed.size
                        val available = game.routeEncounterAvailable
                        val steady = game.steadyTravelChosen
                        val legal = game.availableRoutes().map { it.id }.toSet()
                        val medium = fork.choices.filter { it.to in legal && it.risk == "medium" }
                        val eligible = medium.any { choice -> events.events.any {
                            it.weight > 0 && "mischief" in it.pools &&
                                board.node(choice.to).region in it.regions
                        } }
                        game.botStep()
                        // Progress can retreat over the selected edge. Do not infer a choice
                        // when its traversal was erased; report unknown explicitly.
                        val entered = player.traversed.getOrNull(before)
                        val chosen = fork.choices.firstOrNull { it.to == entered }
                        routeRows += listOf(board.id,policy,seats,seed,actor+1,fork.id,
                            available,steady,medium.isNotEmpty(),eligible,
                            chosen?.risk ?: "unknown",game.phase == Phase.TARGET).joinToString(",")
                    } else game.botStep()
                    actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                val receipt = requireNotNull(game.fairFinishReceipt)
                val winner = requireNotNull(game.winnerIndex)
                val first = game.players.indexOfFirst { it.name == receipt.firstFinisherName }
                assertTrue(first >= 0)
                finishRows += listOf(board.id,policy,seats,seed,first+1,winner+1,
                    receipt.finishers,receipt.winningUnusedSteps,
                    receipt.tiedOnWinningSteps,receipt.laterSeatsGotFinalChance).joinToString(",")
            }
        assertEquals(960,finishRows.size)
        assertTrue(routeRows.isNotEmpty())
        val out = File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"route_exposure.csv").writeText(
            "course,policy,seats,seed,actor,fork,encounter_available,steady,medium_offered,medium_pool_exists,observed_risk,target_phase\n"+
            routeRows.joinToString("\n")+"\n")
        File(out,"finish_receipts.csv").writeText(
            "course,policy,seats,seed,first_seat,winner_seat,finishers,unused_steps,tied_best,later_seats_chance\n"+
            finishRows.joinToString("\n")+"\n")
    }
}
