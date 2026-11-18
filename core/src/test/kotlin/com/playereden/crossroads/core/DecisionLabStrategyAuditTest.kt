package com.playereden.crossroads.core

import java.io.File
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

/**
 * Compares the existing rules with the isolated experimental rules under
 * equally seeded 2- and 4-racer all-AI-opponent games.
 *
 * Synthetic player 1 uses the named policy, NOT human skill/UX.
 * No preselected success threshold: if the experiment is unbalanced,
 * report it instead of silently shipping a balance claim.
 */
class DecisionLabStrategyAuditTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val originals = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private enum class Policy { RANDOM, FAST, CAUTIOUS, ADAPTIVE }

    data class Row(
        val base: String,
        val experiment: Boolean,
        val seats: Int,
        val seed: Int,
        val policy: String,
        val winner: Int,
        val personalRolls: Int,
        val routeChoices: Int,
        val finishedActions: Int
    ) {
        fun csv() = listOf(base, experiment, seats, seed, policy,
            winner + 1, personalRolls, routeChoices, finishedActions)
            .joinToString(",")
    }

    private fun distances(board: BoardDefinition): Map<String, Int> {
        val cache = mutableMapOf<String, Int>()
        fun walk(id: String): Int {
            if (id == board.finish) return 0
            cache[id]?.let { return it }
            val next = board.node(id).next
            val length = 1 + next.minOf { walk(it) }
            cache[id] = length
            return length
        }
        board.nodes.forEach { walk(it.id) }
        return cache + (board.finish to 0)
    }

    private fun run(board: BoardDefinition, seats: Int, seed: Int,
                    policy: Policy, experiment: Boolean): Row {
        val actual = if (experiment) DecisionLab.variant(board) else board
        val distances = distances(board)
        val game = GameSession(actual, events, seats = seats, seed = seed,
            humanIndices = setOf(0))
        val choices = Random(seed * 73 + seats * 7)
        var rolls = 0
        var routes = 0
        var actions = 0
        while (game.phase != Phase.FINISHED && actions++ < 2700) {
            if (game.players[game.currentIndex].isBot) {
                game.botStep()
                continue
            }
            val own = distances.getValue(game.players[0].position)
            val opponent = game.players.indices.filter { it != 0 }
                .minOf { distances.getValue(game.players[it].position) }
            val leading = own < opponent
            when (game.phase) {
                Phase.ROLL -> {
                    rolls++
                    if (!experiment) game.roll()
                    else {
                        val steady = when (policy) {
                            Policy.RANDOM -> choices.nextBoolean()
                            Policy.FAST -> false
                            Policy.CAUTIOUS -> true
                            Policy.ADAPTIVE -> leading
                        }
                        if (steady) game.rollSteady() else game.rollBold()
                    }
                }
                Phase.ROUTE -> {
                    routes++
                    val options = game.availableRoutes()
                    val risk = board.forkAt(game.players[0].position)?.choices
                        ?.associate { it.to to it.risk }.orEmpty()
                    val chosen = when (policy) {
                        Policy.RANDOM -> options[choices.nextInt(options.size)]
                        Policy.FAST -> options.minBy { distances.getValue(it.id) }
                        Policy.CAUTIOUS -> options.minWith(compareBy<BoardNode> {
                            when(risk[it.id]) {
                                "low" -> 0
                                "medium" -> 1
                                "high" -> 2
                                else -> 3
                            }
                        }.thenBy { distances.getValue(it.id) })
                        Policy.ADAPTIVE -> if (leading) {
                            options.minWith(compareBy<BoardNode> {
                                when(risk[it.id]) {
                                    "low" -> 0
                                    "medium" -> 1
                                    "high" -> 2
                                    else -> 3
                                }
                            }.thenBy { distances.getValue(it.id) })
                        } else options.minBy { distances.getValue(it.id) }
                    }
                    game.chooseRoute(chosen.id)
                }
                Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                Phase.FINISHED -> Unit
            }
        }
        assertEquals("Softlock on ${board.id} $experiment $seats $seed $policy",
            Phase.FINISHED, game.phase)
        return Row(board.id, experiment, seats, seed, policy.name,
            requireNotNull(game.winnerIndex), rolls, routes, actions)
    }

    @Test fun prototypeMetricsKeepFailedExperimentsVisible() {
        val rows = buildList {
            for (board in originals) for (seats in listOf(2, 4))
                for (seed in 1..120) for (policy in Policy.entries) {
                    add(run(board, seats, seed, policy, experiment = false))
                    add(run(board, seats, seed, policy, experiment = true))
                }
        }
        assertEquals(5760, rows.size)
        val dir = File(root, "core/build/reports/decision-lab")
        check(dir.exists() || dir.mkdirs())
        File(dir, "lab_vs_legacy_samples.csv").writeText(
            "course,experimental,seats,seed,policy,winner_seat_1_based,human_rolls,human_routes,total_actions\n" +
                rows.joinToString("\n") { it.csv() } + "\n"
        )
        val lines = mutableListOf<String>()
        lines += "GAMEPLAY-35 — tactical Decision Lab vs original rules"
        lines += "5,760 synthetic races. 120 seeds × 3 courses × 2/4 seats × 4 human policies × legacy/lab."
        lines += "Steady: 4 reliable steps and one setback cancellation per turn."
        lines += "Push: die 1–2 => one step; 3–6 => five to eight steps."
        lines += "Lab route selection draws one risk-aligned encounter on entry per turn."
        lines += "This is not human play, wall-clock minutes or evidence of fun."
        lines += ""
        lines += "COURSE                         SEATS   MODE       RANDOM% FAST% CAUTIOUS% ADAPTIVE%  AVG ROLLS HUMAN"
        for (board in originals) for (seats in listOf(2, 4))
            for (experiment in listOf(false, true)) {
                val group = rows.filter { it.base == board.id &&
                    it.seats == seats && it.experiment == experiment }
                fun win(policy: Policy) = group.filter { it.policy == policy.name }
                    .let { 100.0 * it.count { row -> row.winner == 0 } / it.size }
                lines += "%-29s %5d   %-9s %6.1f %5.1f %9.1f %9.1f %13.2f".format(
                    board.id, seats, if (experiment) "LAB" else "LEGACY",
                    win(Policy.RANDOM), win(Policy.FAST),
                    win(Policy.CAUTIOUS), win(Policy.ADAPTIVE),
                    group.map { it.personalRolls }.average()
                )
            }
        lines += ""
        lines += "Interpretation: better strategy must be explainable, not merely more"
        lines += "random variation. Do not launch if one style dominates or turns feel tedious."
        lines += "Existing legacy engine, checkpoints, WAN/LAN, original boards and match"
        lines += "statistics are not modified by opting out of the experimental board variant."
        File(dir, "lab_vs_legacy_summary.txt").writeText(lines.joinToString("\n") + "\n")
        println(lines.joinToString("\n"))
    }
}
