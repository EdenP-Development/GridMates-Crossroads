package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Isolate travel choices on a mirrored synthetic seat; all other seats use V1. */
class AdaptiveTravelComparisonTest {
    private data class Row(val course: String, val seats: Int, val seat: Int,
        val seed: Int, val adaptive: Boolean, val won: Boolean, val rolls: Int,
        val pushes: Int, val actions: Int) {
        fun csv() = listOf(course,seats,seat+1,seed,adaptive,won,rolls,pushes,actions).joinToString(",")
    }

    @Test fun mirroredTravelPoliciesProduceComparableSamples() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val boards = listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json","data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val rows = mutableListOf<Row>()
        for (board in boards) {
            val distances = BotRouteDistances(board)
            for (seats in 2..4) for (seat in 0 until seats) for (seed in 1..12)
                for (adaptive in listOf(false,true)) {
                    val game = GameSession(board,events,seats,seed,setOf(seat))
                    var rolls = 0
                    var pushes = 0
                    var actions = 0
                    while (game.phase != Phase.FINISHED && actions < 1500) {
                        actions++
                        if (game.currentIndex != seat) { game.botStep(); continue }
                        val view = BotRaceAssessment.assess(seat,game.players.mapIndexed { i,p ->
                            BotRacerView(i,distances.distance(p.position),p.shield)
                        },distances.distance(board.start),game.finalRoundPending)
                        when (game.phase) {
                            Phase.ROLL -> {
                                rolls++
                                val push = if (adaptive) AdaptiveBotTravelR1.shouldPush(view)
                                    else (view.gapToLeader ?: 0) > 0
                                if (push) { pushes++; game.rollBold() } else game.rollSteady()
                            }
                            Phase.ROUTE -> {
                                // Identical deterministic route/encounter/target controls in both arms.
                                val route = game.availableRoutes().minBy { distances.distance(it.id) }
                                game.chooseRoute(route.id)
                            }
                            Phase.ENCOUNTER -> {
                                val legal = game.availableEncounterChoices()
                                game.chooseEncounter(if ("supplies" in legal) "supplies" else "gamble")
                            }
                            Phase.TARGET -> game.chooseTarget(game.availableTargets().minBy {
                                distances.distance(game.players[it].position)
                            })
                            Phase.FINISHED -> error("Unexpected finished phase")
                        }
                    }
                    assertEquals("Stalled: ${board.id}/$seats/$seat/$seed/$adaptive",Phase.FINISHED,game.phase)
                    assertTrue(pushes <= rolls)
                    rows += Row(board.id,seats,seat,seed,adaptive,game.winnerIndex == seat,rolls,pushes,actions)
                }
        }
        assertEquals(864,rows.size)
        val pairs = rows.groupBy { listOf(it.course,it.seats,it.seat,it.seed) }
        assertEquals(432,pairs.size)
        assertTrue(pairs.values.all { it.size == 2 && it.map { row -> row.adaptive }.toSet().size == 2 })
        val out = File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"travel_samples.csv").writeText("course,seats,seat,seed,adaptive,won,rolls,pushes,actions\n"+
            rows.joinToString("\n"){it.csv()}+"\n")
        val summary = buildString {
            appendLine("AI2 travel-only proxy comparison: 432 paired scenarios, 864 races.")
            appendLine("Same seed and mirrored seat; different choices may diverge RNG consumption.")
            appendLine("Not a full mixed-policy bot tournament; non-travel choices held constant.")
            for ((course,group) in rows.groupBy { it.course }) for (seats in 2..4) {
                val subset = group.filter { it.seats == seats }
                val legacy = subset.filter { !it.adaptive }
                val candidate = subset.filter { it.adaptive }
                appendLine("$course seats=$seats n/arm=${legacy.size} legacy wins=${legacy.count{it.won}} adaptive wins=${candidate.count{it.won}} legacy pushes/rolls=${legacy.sumOf{it.pushes}}/${legacy.sumOf{it.rolls}} adaptive pushes/rolls=${candidate.sumOf{it.pushes}}/${candidate.sumOf{it.rolls}}")
            }
        }
        File(out,"travel_summary.txt").writeText(summary)
        println(summary)
    }
}
