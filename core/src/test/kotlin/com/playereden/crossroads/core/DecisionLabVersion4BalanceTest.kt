package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Matched-configuration v3/v4 experiment on unmodified course geometry.
 * Only v4 successful Push + hazard Gamble adds 2 steps.
 * Equal seeds are not identical downstream RNG after different route outcomes.
 */
class DecisionLabVersion4BalanceTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val originals = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private enum class Travel { STEADY, PUSH }
    private enum class Route { SHORTEST, SAFEST }

    private fun risk(risk: String?): Int = when (risk) {
        "low" -> 0
        "medium" -> 1
        "high" -> 2
        else -> 3
    }

    private fun distances(board: BoardDefinition): Map<String, Int> {
        val memo = mutableMapOf<String, Int>()
        fun count(id: String): Int {
            if (id == board.finish) return 0
            memo[id]?.let { return it }
            return (1 + board.node(id).next.minOf { count(it) }).also { memo[id] = it }
        }
        board.nodes.forEach { count(it.id) }
        return memo + (board.finish to 0)
    }

    data class Sample(
        val course: String, val seats: Int, val humanSeat: Int, val seed: Int,
        val version: String, val travel: String, val route: String, val humanWon: Boolean,
        val winnerSeat: Int, val rolls: Int, val humanRolls: Int,
        val forks: Int, val gambleAttempts: Int, val bonusSuccesses: Int, val actions: Int
    ) {
        fun csv() = listOf(course, seats, humanSeat + 1, seed, version,
            travel, route, humanWon, winnerSeat + 1, rolls, humanRolls,
            forks, gambleAttempts, bonusSuccesses, actions).joinToString(",")
    }

    private fun play(
        original: BoardDefinition, seats: Int, humanSeat: Int, seed: Int,
        version4: Boolean, travel: Travel, route: Route,
        distance: Map<String, Int>
    ): Sample {
        val board = if (version4) DecisionLab.variantV4(original)
                    else DecisionLab.variantV3(original)
        val game = GameSession(board, events, seats, seed, setOf(humanSeat))
        var rolls = 0
        var humanRolls = 0
        var forks = 0
        var gambles = 0
        var bonuses = 0
        var actions = 0
        while (game.phase != Phase.FINISHED && actions < 2600) {
            actions++
            if (game.phase == Phase.ROLL) rolls++
            if (game.players[game.currentIndex].isBot) {
                game.botStep()
                continue
            }
            when (game.phase) {
                Phase.ROLL -> {
                    humanRolls++
                    if (travel == Travel.STEADY) game.rollSteady()
                    else game.rollBold()
                }
                Phase.ROUTE -> {
                    forks++
                    val options = game.availableRoutes()
                    val risks = board.forkAt(game.players[humanSeat].position)
                        ?.choices?.associate { it.to to it.risk }.orEmpty()
                    val selected = if (route == Route.SHORTEST) {
                        options.minWith(compareBy<BoardNode> { distance.getValue(it.id) }
                            .thenBy { risk(risks[it.id]) })
                    } else {
                        options.minWith(compareBy<BoardNode> { risk(risks[it.id]) }
                            .thenBy { distance.getValue(it.id) })
                    }
                    game.chooseRoute(selected.id)
                }
                Phase.ENCOUNTER -> {
                    val legal = game.availableEncounterChoices()
                    if ("supplies" in legal) game.chooseEncounter("supplies")
                    else {
                        gambles++
                        val hasBonus = game.pendingPushHazardBonus > 0
                        game.chooseEncounter("gamble")
                        if (hasBonus && game.snapshot().encounters.lastOrNull()
                                ?.outcome?.contains("Push risk bonus +2") == true) bonuses++
                    }
                }
                Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                Phase.FINISHED -> Unit
            }
        }
        assertEquals("Stall: ${original.id}/$seats/$humanSeat/$seed/$version4/$travel/$route",
            Phase.FINISHED, game.phase)
        val winner = requireNotNull(game.winnerIndex)
        assertEquals(board.finish, game.players[winner].position)
        return Sample(original.id, seats, humanSeat, seed,
            if (version4) "v4" else "v3", travel.name, route.name,
            winner == humanSeat, winner, rolls, humanRolls, forks, gambles, bonuses, actions)
    }

    @Test fun compareMatchedV3AndV4UnderEverySeatAndTactic() {
        val rows = buildList {
            for (board in originals) {
                val distance = distances(board)
                for (seats in listOf(2, 4)) for (humanSeat in 0 until seats)
                    for (seed in 1..24) for (travel in Travel.entries)
                        for (route in Route.entries) {
                            add(play(board, seats, humanSeat, seed,
                                false, travel, route, distance))
                            add(play(board, seats, humanSeat, seed,
                                true, travel, route, distance))
                        }
            }
        }
        assertEquals(3456, rows.size)
        assertTrue(rows.all { it.rolls > 0 })
        assertTrue(rows.filter { it.version == "v3" }.all { it.bonusSuccesses == 0 })
        assertTrue(rows.filter { it.travel == "STEADY" }.all { it.bonusSuccesses == 0 })

        val out = File(root, "core/build/reports/gameplay-73")
        check(out.exists() || out.mkdirs())
        File(out, "lab_v3_vs_v4_samples.csv").writeText(
            "course,seats,human_seat_1_based,seed,version,travel,route," +
                "human_won,winner_seat_1_based,all_rolls,human_rolls,human_forks," +
                "human_gamble_attempts,push_success_bonuses,all_actions\n" +
                rows.joinToString("\n") { it.csv() } + "\n"
        )
        fun winPercent(group: List<Sample>) =
            100.0 * group.count { it.humanWon } / group.size
        val summary = mutableListOf<String>()
        summary += "GAMEPLAY-73 — Matched tactical policy v3/v4 evaluation"
        summary += "3,456 simulations: 3 courses x (2+4 mirrored seats) x 24 seeds" +
            " x 2 travel styles x 2 route styles x 2 versions."
        summary += "V4-only intervention: successful Push + high-risk Gamble advance gains +2."
        summary += "Other event probabilities, AI, boards, seat order and policies unchanged."
        summary += "No human duration, fun, fairness or statistical significance established."
        summary += "Same seed does not prevent diverging future RNG consumption after choices."
        summary += ""
        summary += "COURSE SEATS TRAVEL ROUTE V3_WIN% V4_WIN% DELTA_PP V3_AVG_ROLLS V4_AVG_ROLLS V4_BONUSES"
        for (board in originals) for (seats in listOf(2, 4))
            for (travel in Travel.entries) for (route in Route.entries) {
                val relevant = rows.filter { it.course == board.id && it.seats == seats &&
                    it.travel == travel.name && it.route == route.name }
                val v3 = relevant.filter { it.version == "v3" }
                val v4 = relevant.filter { it.version == "v4" }
                summary += "%-28s %d %-6s %-8s %7.1f %7.1f %+8.1f %12.2f %12.2f %d".format(
                    board.id, seats, travel.name, route.name,
                    winPercent(v3), winPercent(v4), winPercent(v4) - winPercent(v3),
                    v3.map { it.rolls }.average(), v4.map { it.rolls }.average(),
                    v4.sumOf { it.bonusSuccesses }
                )
            }
        summary += ""
        summary += "The test deliberately holds human encounter response at SUPPLIES/GAMBLE."
        summary += "This does not prove other humans will prefer that encounter policy."
        summary += "Compare actual device races before considering any release rule changes."
        File(out, "lab_v3_vs_v4_summary.txt").writeText(summary.joinToString("\n") + "\n")
        println(summary.joinToString("\n"))
    }
}
