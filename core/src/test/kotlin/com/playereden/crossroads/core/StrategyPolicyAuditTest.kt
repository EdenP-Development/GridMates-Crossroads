package com.playereden.crossroads.core

import java.io.File
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

/**
 * Is there an advantage to thinking about route risk/distance?
 *
 * Every race has one simulated human, seat 1; other seats use existing
 * botStep(). Opponent behavior and prank target policy are unchanged.
 * This is not a human skill study and uses a first-seat fixed scenario.
 */
class StrategyPolicyAuditTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val boards = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )

    private enum class Policy {
        RANDOM, SHORTEST, SAFEST, ADAPTIVE
    }

    data class Record(
        val board: String,
        val seats: Int,
        val seed: Int,
        val policy: String,
        val won: Boolean,
        val rolls: Int,
        val routeChoices: Int,
        val encounters: Int
    ) {
        fun csv(): String = listOf(
            board, seats, seed, policy, won, rolls, routeChoices, encounters
        ).joinToString(",")
    }

    private fun distances(board: BoardDefinition): Map<String, Int> {
        val cache = mutableMapOf<String, Int>()
        fun distance(id: String, seen: Set<String>): Int {
            if (id == board.finish) return 0
            cache[id]?.let { return it }
            if (id in seen) error("Illegal cycle in ${board.id}")
            val next = board.node(id).next
            val result = if (next.isEmpty()) 100_000
                else 1 + next.minOf { distance(it, seen + id) }
            cache[id] = result
            return result
        }
        board.nodes.forEach { distance(it.id, emptySet()) }
        return cache + (board.finish to 0)
    }

    private fun run(board: BoardDefinition, seats: Int, seed: Int, policy: Policy,
                    distances: Map<String, Int>): Record {
        val game = GameSession(board, events, seats, seed, humanIndices = setOf(0))
        val local = Random(seed * 73 + seats * 7)
        var rolls = 0
        var routes = 0
        var actions = 0
        while (game.phase != Phase.FINISHED && actions++ < 2400) {
            if (game.players[game.currentIndex].isBot) {
                game.botStep()
                continue
            }
            when (game.phase) {
                Phase.ROLL -> {
                    rolls++
                    game.roll()
                }
                Phase.ROUTE -> {
                    routes++
                    val options = game.availableRoutes()
                    val fork = board.forkAt(game.players[0].position)
                    val riskById = fork?.choices?.associate { it.to to it.risk }.orEmpty()
                    val closestOther = game.players.indices.filter { it != 0 }
                        .minOf { distances.getValue(game.players[it].position) }
                    val ahead = distances.getValue(game.players[0].position) < closestOther
                    val chosen = when (policy) {
                        Policy.RANDOM -> options[local.nextInt(options.size)]
                        Policy.SHORTEST -> options.minBy { distances.getValue(it.id) }
                        Policy.SAFEST -> options.minWith(compareBy<BoardNode> {
                            when (riskById[it.id]) {
                                "low" -> 0
                                "medium" -> 1
                                "high" -> 2
                                else -> 3
                            }
                        }.thenBy { distances.getValue(it.id) })
                        Policy.ADAPTIVE -> if (ahead) {
                            options.minWith(compareBy<BoardNode> {
                                when (riskById[it.id]) {
                                    "low" -> 0
                                    "medium" -> 1
                                    "high" -> 2
                                    else -> 3
                                }
                            }.thenBy { distances.getValue(it.id) })
                        } else {
                            options.minBy { distances.getValue(it.id) }
                        }
                    }
                    game.chooseRoute(chosen.id)
                }
                Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                Phase.ENCOUNTER -> error("Encounter phase belongs only to Lab v3")
                Phase.FINISHED -> Unit
            }
        }
        assertEquals("Stalled: ${board.id} $seats $seed $policy", Phase.FINISHED, game.phase)
        return Record(board.id, seats, seed, policy.name, game.winnerIndex == 0,
            rolls, routes, game.snapshot().encounters.size)
    }

    @Test fun reportStrategyVersusRandomRatherThanAssumingItMatters() {
        val records = buildList {
            for (board in boards) {
                val distances = distances(board)
                for (seats in listOf(2, 4)) for (seed in 1..200) {
                    for (policy in Policy.entries) add(run(board, seats, seed, policy, distances))
                }
            }
        }
        assertEquals(4800, records.size)
        val destination = File(root, "core/build/reports/agency")
        check(destination.exists() || destination.mkdirs())
        File(destination, "crossroads_strategy_samples.csv").writeText(
            "course,seats,seed,policy,won,dice_rolls,route_choices,encounter_log_count_capped\n" +
                records.joinToString("\n") { it.csv() } + "\n"
        )

        val lines = mutableListOf<String>()
        lines += "AGENCY-34 STRATEGY AUDIT — does a simple deliberate policy beat random tapping?"
        lines += "All 4800 games use existing rules and bots, 200 seeds per course/seat/policy."
        lines += "Synthetic human seat 1: RANDOM vs SHORTEST vs SAFEST vs ADAPTIVE route choice."
        lines += "ADAPTIVE picks SAFEST when leading by shortest-distance metric, otherwise SHORTEST."
        lines += "All policies roll immediately; prank targets always take the first legal target."
        lines += "No human timing or real player decision quality is measured."
        lines += ""
        lines += "COURSE                         SEATS  RANDOM%  SHORTEST%  SAFEST%  ADAPTIVE%  BEST LIFT PP"
        for (board in boards) for (seats in listOf(2, 4)) {
            val group = records.filter { it.board == board.id && it.seats == seats }
            fun share(policy: Policy): Double {
                val subset = group.filter { it.policy == policy.name }
                return 100.0 * subset.count { it.won } / subset.size
            }
            val random = share(Policy.RANDOM)
            val fastest = share(Policy.SHORTEST)
            val safest = share(Policy.SAFEST)
            val adaptive = share(Policy.ADAPTIVE)
            lines += "%-29s %5d %8.1f %10.1f %8.1f %10.1f %12.1f".format(
                board.id, seats, random, fastest, safest, adaptive,
                maxOf(fastest, safest, adaptive) - random
            )
        }
        lines += ""
        lines += "These policies are intentionally primitive: this tests whether obvious"
        lines += "route principles *can* improve outcomes. It does not prove a skilled"
        lines += "person would achieve any percentage or that the winning policy is fun."
        lines += "Different path choices may consume different subsequent event RNG draws."
        lines += "No engine changes, timing targets, skill claims or balance fixes are implied."
        File(destination, "crossroads_strategy_summary.txt").writeText(lines.joinToString("\n") + "\n")
        println(lines.joinToString("\n"))
    }
}
