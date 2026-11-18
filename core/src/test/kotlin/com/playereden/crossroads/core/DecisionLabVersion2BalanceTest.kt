package com.playereden.crossroads.core

import java.io.File
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

/**
 * Paired, mirrored-seat synthetic strategy comparison between QA1312 Lab v1
 * and Lab v2. We do NOT infer human session times or proof of enjoyable play.
 */
class DecisionLabVersion2BalanceTest {
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

    data class Result(
        val course: String,
        val seats: Int,
        val humanSeat: Int,
        val seed: Int,
        val policy: String,
        val version: String,
        val humanWon: Boolean,
        val humanRolls: Int,
        val humanRoutes: Int,
        val actions: Int
    ) {
        fun csv() = listOf(
            course, seats, humanSeat + 1, seed, policy, version,
            humanWon, humanRolls, humanRoutes, actions
        ).joinToString(",")
    }

    private fun distances(board: BoardDefinition): Map<String, Int> {
        val cache = mutableMapOf<String, Int>()
        fun distance(id: String): Int {
            if (id == board.finish) return 0
            cache[id]?.let { return it }
            val next = board.node(id).next
            require(next.isNotEmpty()) { "No path from $id" }
            val distance = 1 + next.minOf { distance(it) }
            cache[id] = distance
            return distance
        }
        board.nodes.forEach { distance(it.id) }
        return cache + (board.finish to 0)
    }

    private fun play(original: BoardDefinition, seatCount: Int, humanSeat: Int,
                     seed: Int, policy: Policy, secondVersion: Boolean,
                     distances: Map<String, Int>): Result {
        val course = if (secondVersion) DecisionLab.variantV2(original)
                     else DecisionLab.variant(original)
        val game = GameSession(course, events, seatCount, seed, setOf(humanSeat))
        // Policy randomness is distinct from authoritative, saved dice/event RNG.
        val choices = Random(seed * 79 + seatCount * 31 + humanSeat * 19)
        var rolls = 0
        var routes = 0
        var actions = 0
        while (game.phase != Phase.FINISHED && actions++ < 2700) {
            if (game.players[game.currentIndex].isBot) {
                game.botStep()
                continue
            }
            val own = distances.getValue(game.players[humanSeat].position)
            val nearest = game.players.indices.filter { it != humanSeat }
                .minOf { distances.getValue(game.players[it].position) }
            val leading = own < nearest
            when (game.phase) {
                Phase.ROLL -> {
                    rolls++
                    val steady = when (policy) {
                        Policy.RANDOM -> choices.nextBoolean()
                        Policy.FAST -> false
                        Policy.CAUTIOUS -> true
                        Policy.ADAPTIVE -> leading
                    }
                    if (steady) game.rollSteady() else game.rollBold()
                }
                Phase.ROUTE -> {
                    routes++
                    val possible = game.availableRoutes()
                    val risks = course.forkAt(game.players[humanSeat].position)?.choices
                        ?.associate { it.to to it.risk }.orEmpty()
                    fun safer() = possible.minWith(
                        compareBy<BoardNode> {
                            when (risks[it.id]) {
                                "low" -> 0
                                "medium" -> 1
                                "high" -> 2
                                else -> 3
                            }
                        }.thenBy { distances.getValue(it.id) }
                    )
                    val destination = when (policy) {
                        Policy.RANDOM -> possible[choices.nextInt(possible.size)]
                        Policy.FAST -> possible.minBy { distances.getValue(it.id) }
                        Policy.CAUTIOUS -> safer()
                        Policy.ADAPTIVE -> if (leading) safer()
                            else possible.minBy { distances.getValue(it.id) }
                    }
                    game.chooseRoute(destination.id)
                }
                Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                Phase.FINISHED -> Unit
            }
        }
        assertEquals("Softlock $seed / $humanSeat / $seatCount / $policy / $course",
            Phase.FINISHED, game.phase)
        assertEquals(course.finish,
            game.players[requireNotNull(game.winnerIndex)].position)
        return Result(original.id, seatCount, humanSeat, seed, policy.name,
            if (secondVersion) "v2" else "v1",
            game.winnerIndex == humanSeat, rolls, routes, actions)
    }

    @Test fun pairedMirroredSeatReportShowsImprovementOrRegression() {
        val records = buildList {
            for (original in originals) {
                val distance = distances(original)
                for (seatCount in listOf(2, 4)) {
                    for (seat in 0 until seatCount) {
                        for (seed in 1..24) {
                            for (policy in Policy.entries) {
                                add(play(original, seatCount, seat, seed, policy,
                                    false, distance))
                                add(play(original, seatCount, seat, seed, policy,
                                    true, distance))
                            }
                        }
                    }
                }
            }
        }
        assertEquals(3456, records.size)
        val dir = File(root, "core/build/reports/decision-lab-v2")
        check(dir.exists() || dir.mkdirs())
        File(dir, "v1_vs_v2_mirrored_seat_samples.csv").writeText(
            "course,seats,human_seat,seed,policy,lab_version,won,human_rolls,route_decisions,total_actions\n" +
                records.joinToString("\n") { it.csv() } + "\n"
        )

        val lines = mutableListOf<String>()
        lines += "GAMEPLAY-36 — LAB v1 versus v2 mirrored-seat comparisons"
        lines += "3,456 paired synthetic races = 3 courses × (2+4 mirrored human seats) × 24 seeds × 4 policies × 2 rule variants."
        lines += "Both variants use EXACT same original board nodes, events, bot policy, seed and human seat."
        lines += "v2 adds one move step only when Steady enters a low-risk shelter route."
        lines += "Caveat: after action divergence, later RNG consumption may differ."
        lines += "No measured human timing, skill advantage, balance proof or perceived fun."
        lines += ""
        lines += "COURSE                       SEATS POLICY     v1_WIN%  v2_WIN% DELTA_PP  v1_ROLLS v2_ROLLS"
        for (original in originals) for (seats in listOf(2, 4)) {
            for (policy in Policy.entries) {
                val relevant = records.filter {
                    it.course == original.id && it.seats == seats &&
                    it.policy == policy.name
                }
                val old = relevant.filter { it.version == "v1" }
                val updated = relevant.filter { it.version == "v2" }
                fun rate(results: List<Result>) =
                    100.0 * results.count { it.humanWon } / results.size
                lines += "%-28s %5d %-9s %7.1f %8.1f %+8.1f %8.2f %8.2f".format(
                    original.id, seats, policy.name,
                    rate(old), rate(updated), rate(updated) - rate(old),
                    old.map { it.humanRolls }.average(),
                    updated.map { it.humanRolls }.average()
                )
            }
        }
        lines += ""
        lines += "All results span every possible human starting seat, so seat-1 bias"
        lines += "isn't confused with a tactical advantage. Different policies can"
        lines += "compete against the SAME built-in bot behavior, not each other."
        lines += "Do not merge lab v2 into main without real physical playtesting."
        File(dir, "v1_vs_v2_mirrored_seat_summary.txt").writeText(
            lines.joinToString("\n") + "\n"
        )
        println(lines.joinToString("\n"))
    }
}
