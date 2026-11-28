package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Regional course comparison under current V5 rules. Synthetic policy play only. */
class RegionalTacticalAuditTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val originals = listOf(
        "data/boards/woodland_woven_trail.json",
        "design/prototypes/woodland_woven_regional_v04.json",
        "design/prototypes/woodland_woven_regional_v05.json"
    ).map { GameData.board(File(root, it).readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )

    private enum class Travel { STEADY, PUSH }
    private enum class Route { SHORTEST, SAFEST }

    internal data class Sample(
        val course: String,
        val seats: Int,
        val humanSeat: Int,
        val seed: Int,
        val travel: String,
        val route: String,
        val humanWon: Boolean,
        val winnerSeat: Int,
        val totalRolls: Int,
        val humanRolls: Int,
        val humanForks: Int,
        val riskDistanceConflicts: Int,
        val highRiskRoutes: Int,
        val lowRiskRoutes: Int,
        val humanEncounterChoices: Int,
        val totalActions: Int
    ) {
        fun csv() = listOf(
            course, seats, humanSeat + 1, seed, travel, route, humanWon,
            winnerSeat + 1, totalRolls, humanRolls, humanForks,
            riskDistanceConflicts, highRiskRoutes, lowRiskRoutes,
            humanEncounterChoices, totalActions
        ).joinToString(",")
    }

    private fun rank(risk: String?): Int = when (risk) {
        "low" -> 0
        "medium" -> 1
        "high" -> 2
        else -> 3
    }

    /**
     * (risk rank, minimum remaining graph distance).
     * A conflict exists only when no option is BOTH shortest and safest.
     * This identifies a simple visible tradeoff, not proof of equal balance.
     */
    private fun riskDistanceConflict(options: List<Pair<Int, Int>>): Boolean {
        if (options.size < 2) return false
        val bestRisk = options.minOf { it.first }
        val bestDistance = options.minOf { it.second }
        return options.none { it.first == bestRisk && it.second == bestDistance }
    }

    @Test fun riskDistanceTradeoffDetectionIsConservative() {
        assertTrue(riskDistanceConflict(listOf(0 to 11, 2 to 9)))
        assertFalse(riskDistanceConflict(listOf(0 to 9, 2 to 11)))
        assertFalse(riskDistanceConflict(listOf(0 to 9, 0 to 11)))
        assertFalse(riskDistanceConflict(listOf(0 to 9)))
        assertTrue(riskDistanceConflict(listOf(2 to 6, 1 to 8, 0 to 10)))
        assertFalse(riskDistanceConflict(listOf(2 to 6, 0 to 6, 0 to 10)))
    }

    private fun distances(board: BoardDefinition): Map<String, Int> {
        val memo = mutableMapOf<String, Int>()
        fun count(nodeId: String): Int {
            if (nodeId == board.finish) return 0
            memo[nodeId]?.let { return it }
            val next = board.node(nodeId).next
            require(next.isNotEmpty()) { "Unexpected dead end: $nodeId" }
            return (1 + next.minOf { count(it) }).also { memo[nodeId] = it }
        }
        board.nodes.forEach { count(it.id) }
        return memo + (board.finish to 0)
    }

    private fun play(
        original: BoardDefinition, seats: Int, humanSeat: Int,
        seed: Int, travel: Travel, route: Route, distance: Map<String, Int>
    ): Sample {
        val board = DecisionLab.variantV5(original)
        val game = GameSession(board, events, seats, seed, setOf(humanSeat))
        var humanRolls = 0
        var totalRolls = 0
        var humanForks = 0
        var conflicts = 0
        var highRisk = 0
        var lowRisk = 0
        var encounters = 0
        var actions = 0

        while (game.phase != Phase.FINISHED && actions < 2600) {
            actions++
            if (game.phase == Phase.ROLL) totalRolls++
            if (game.players[game.currentIndex].isBot) {
                game.botStep()
                continue
            }
            when (game.phase) {
                Phase.ROLL -> {
                    humanRolls++
                    if (travel == Travel.STEADY) game.rollSteady() else game.rollBold()
                }
                Phase.ROUTE -> {
                    humanForks++
                    val options = game.availableRoutes()
                    require(options.size >= 2)
                    val fork = board.forkAt(game.players[humanSeat].position)
                    val risks = fork?.choices?.associate { it.to to it.risk }.orEmpty()
                    val ranked = options.map { rank(risks[it.id]) to distance.getValue(it.id) }
                    if (riskDistanceConflict(ranked)) conflicts++

                    // Both policies have deterministic, documented tie-breaks:
                    // SHORTEST breaks equal lengths toward safety;
                    // SAFEST breaks equal risks toward the shorter course.
                    val choice = if (route == Route.SHORTEST) {
                        options.minWith(compareBy<BoardNode> { distance.getValue(it.id) }
                            .thenBy { rank(risks[it.id]) })
                    } else {
                        options.minWith(compareBy<BoardNode> { rank(risks[it.id]) }
                            .thenBy { distance.getValue(it.id) })
                    }
                    when (risks[choice.id]) {
                        "low" -> lowRisk++
                        "high" -> highRisk++
                    }
                    game.chooseRoute(choice.id)
                }
                Phase.ENCOUNTER -> {
                    encounters++
                    val legal = game.availableEncounterChoices()
                    // Hold the response policy constant across all four axes.
                    val response = if ("supplies" in legal) "supplies" else "gamble"
                    require(response in legal)
                    game.chooseEncounter(response)
                }
                Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                Phase.FINISHED -> Unit
            }
        }

        assertEquals(
            "Softlock: ${original.id}, seats=$seats, humanSeat=$humanSeat, " +
                "seed=$seed, travel=$travel, route=$route",
            Phase.FINISHED, game.phase
        )
        val winner = requireNotNull(game.winnerIndex)
        assertEquals(board.finish, game.players[winner].position)
        assertTrue("A race requires at least one roll", totalRolls > 0)
        assertTrue(conflicts <= humanForks)
        return Sample(
            original.id, seats, humanSeat, seed, travel.name, route.name,
            winner == humanSeat, winner, totalRolls, humanRolls,
            humanForks, conflicts, highRisk, lowRisk, encounters, actions
        )
    }

    private fun rate(numerator: Int, denominator: Int): Double =
        if (denominator == 0) 0.0 else 100.0 * numerator / denominator

    private fun median(values: List<Int>): Int {
        val sorted = values.sorted()
        return sorted[(sorted.size - 1) / 2]
    }

    @Test fun mirroredSeatsExposeTradeoffsInsteadOfAssumingFairness() {
        val rows = buildList {
            for (board in originals) {
                val distance = distances(board)
                for (seats in listOf(2, 3, 4)) for (humanSeat in 0 until seats)
                    for (seed in 1..20) for (travel in Travel.entries)
                        for (route in Route.entries) {
                            add(play(board, seats, humanSeat, seed, travel, route, distance))
                        }
            }
        }
        assertEquals(2160, rows.size)
        assertTrue(rows.all { it.totalRolls > 0 })
        assertTrue(rows.all { it.riskDistanceConflicts <= it.humanForks })

        val out = File(root, "core/build/reports/gameplay-109")
        check(out.exists() || out.mkdirs())
        File(out, "tactical_factorial_samples.csv").writeText(
            "course,seats,human_seat_1_based,seed,travel_policy,route_policy," +
                "human_won,winner_seat_1_based,all_rolls,human_rolls,human_forks," +
                "risk_distance_conflict_forks,chosen_high_risk,chosen_low_risk," +
                "human_encounter_choices,all_actions\n" +
                rows.joinToString("\n") { it.csv() } + "\n"
        )

        val lines = mutableListOf<String>()
        lines += "GAMEPLAY-109 — Lab v5 independent tactical-axis diagnostic"
        lines += "2160 synthetic races: 3 boards x (2+3+4 mirrored human seats) x " +
            "20 fixed seeds x 2 travel styles x 2 route styles."
        lines += "The fixed human encounter response is always SUPPLIES or GAMBLE."
        lines += "Other seats always use the existing state-aware built-in bots."
        lines += "No shared RNG sequence is guaranteed after policy choices diverge."
        lines += "All rollout durations below are DICE ROLLS, NOT seconds or minutes."
        lines += "No human skill, satisfaction, fairness or statistical confidence is proven."
        lines += ""
        lines += "COURSE                       SEATS TRAVEL ROUTE     WIN% MEDIAN_ALL_ROLLS AVG_HUMAN_FORKS ZERO_FORK% CONFLICTS/FORKS"
        for (board in originals) for (seats in listOf(2, 3, 4))
            for (travel in Travel.entries) for (route in Route.entries) {
                val group = rows.filter {
                    it.course == board.id && it.seats == seats &&
                        it.travel == travel.name && it.route == route.name
                }
                val forks = group.sumOf { it.humanForks }
                val conflicts = group.sumOf { it.riskDistanceConflicts }
                lines += "%-28s %5d %-6s %-8s %5.1f%% %16d %15.2f %10.1f%% %d/%d".format(
                    board.id, seats, travel.name, route.name,
                    rate(group.count { it.humanWon }, group.size),
                    median(group.map { it.totalRolls }),
                    group.map { it.humanForks }.average(),
                    rate(group.count { it.humanForks == 0 }, group.size),
                    conflicts, forks
                )
            }

        lines += ""
        lines += "MIRRORED HUMAN STARTING SEATS (win count / sampled matches):"
        for (board in originals) for (seats in listOf(2, 3, 4)) {
            val group = rows.filter { it.course == board.id && it.seats == seats }
            val wins = (0 until seats).joinToString(" | ") { seat ->
                val sample = group.filter { it.humanSeat == seat }
                "seat ${seat + 1}: ${sample.count { it.humanWon }}/${sample.size}"
            }
            lines += "${board.id} / $seats seats: $wins"
        }

        lines += ""
        lines += "HOW TO INTERPRET:"
        lines += "- SHORTEST and SAFEST differ only in route selection/tie-breaking."
        lines += "- STEADY and PUSH differ only in their own travel action."
        lines += "- A conflict fork has no option that is both shortest and lowest risk."
        lines += "- Conflict count measures visible tradeoff opportunities, not strategic value."
        lines += "- Zero-fork races include games where the bot wins before any human fork."
        lines += "- Small seeded samples expose hypotheses, not statistical proof."
        lines += "- Do not adjust board graphs, dice odds or checkpoint fingerprints from this alone."
        File(out, "tactical_factorial_summary.txt").writeText(
            lines.joinToString("\n") + "\n"
        )
        println(lines.joinToString("\n"))
    }
}
