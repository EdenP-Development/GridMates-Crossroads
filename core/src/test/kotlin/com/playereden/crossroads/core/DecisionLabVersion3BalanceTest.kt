package com.playereden.crossroads.core

import java.io.File
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

/**
 * Decision opportunities vs button mashing, with every human seat represented.
 * These synthetic policies are deliberately simple, not validated human skill.
 */
class DecisionLabVersion3BalanceTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val boards = listOf(
        "woodland_brookside_quick.json", "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val catalogue = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private enum class Policy { RANDOM, FAST, CAUTIOUS, ADAPTIVE }
    data class Sample(
        val course: String, val seats: Int, val seat: Int, val seed: Int,
        val policy: String, val version: String, val won: Boolean,
        val rolls: Int, val routes: Int, val encounterChoices: Int, val actions: Int
    ) {
        fun csv() = listOf(course, seats, seat + 1, seed, policy, version,
            won, rolls, routes, encounterChoices, actions).joinToString(",")
    }

    private fun distanceMap(board: BoardDefinition): Map<String, Int> {
        val cache = mutableMapOf<String, Int>()
        fun distance(id: String): Int {
            if (id == board.finish) return 0
            cache[id]?.let { return it }
            val value = 1 + board.node(id).next.minOf { distance(it) }
            cache[id] = value
            return value
        }
        board.nodes.forEach { distance(it.id) }
        return cache + (board.finish to 0)
    }

    private fun play(original: BoardDefinition, seats: Int, seat: Int,
                     seed: Int, policy: Policy, v3: Boolean,
                     distance: Map<String, Int>): Sample {
        val board = if (v3) DecisionLab.variantV3(original)
                    else DecisionLab.variantV2(original)
        val game = GameSession(board, catalogue, seats, seed, setOf(seat))
        val choices = Random(seed * 83 + seats * 37 + seat * 17)
        var rolls = 0
        var routes = 0
        var encounters = 0
        var actions = 0
        while (game.phase != Phase.FINISHED && actions++ < 2900) {
            if (game.players[game.currentIndex].isBot) {
                game.botStep()
                continue
            }
            val own = distance.getValue(game.players[seat].position)
            val nearest = game.players.indices.filter { it != seat }
                .minOf { distance.getValue(game.players[it].position) }
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
                    val available = game.availableRoutes()
                    val risk = board.forkAt(game.players[seat].position)?.choices
                        ?.associate { it.to to it.risk }.orEmpty()
                    fun safeChoice() = available.minWith(compareBy<BoardNode> {
                        when (risk[it.id]) {
                            "low" -> 0
                            "medium" -> 1
                            "high" -> 2
                            else -> 3
                        }
                    }.thenBy { distance.getValue(it.id) })
                    val chosen = when (policy) {
                        Policy.RANDOM -> available[choices.nextInt(available.size)]
                        Policy.FAST -> available.minBy { distance.getValue(it.id) }
                        Policy.CAUTIOUS -> safeChoice()
                        Policy.ADAPTIVE -> if (leading) safeChoice()
                            else available.minBy { distance.getValue(it.id) }
                    }
                    game.chooseRoute(chosen.id)
                }
                Phase.ENCOUNTER -> {
                    encounters++
                    val allowed = game.availableEncounterChoices()
                    val result = when (policy) {
                        Policy.RANDOM -> allowed[choices.nextInt(allowed.size)]
                        Policy.FAST -> if ("supplies" in allowed) "supplies" else "gamble"
                        Policy.CAUTIOUS -> if ("cover" in allowed) "cover" else "brace"
                        Policy.ADAPTIVE -> if (leading) {
                            if ("cover" in allowed) "cover" else "brace"
                        } else {
                            if ("supplies" in allowed) "supplies" else "gamble"
                        }
                    }
                    game.chooseEncounter(result)
                }
                Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                Phase.FINISHED -> Unit
            }
        }
        assertEquals("Unfinished ${original.id}/$seats/$seat/$seed/$policy/$v3",
            Phase.FINISHED, game.phase)
        return Sample(original.id, seats, seat, seed, policy.name,
            if (v3) "v3" else "v2", game.winnerIndex == seat,
            rolls, routes, encounters, actions)
    }

    @Test fun pairedVersionTestDoesNotHideWorsePolicies() {
        val rows = buildList {
            for (board in boards) {
                val distances = distanceMap(board)
                for (seats in listOf(2, 4)) for (seat in 0 until seats)
                    for (seed in 1..16) for (policy in Policy.entries) {
                        add(play(board, seats, seat, seed, policy, false, distances))
                        add(play(board, seats, seat, seed, policy, true, distances))
                    }
            }
        }
        assertEquals(2304, rows.size)
        val dir = File(root, "core/build/reports/encounter-lab-v3")
        check(dir.exists() || dir.mkdirs())
        File(dir, "v2_vs_v3_mirrored_samples.csv").writeText(
            "course,seats,human_seat,seed,policy,version,human_won,human_rolls,human_routes,human_encounter_choices,all_actions\n" +
                rows.joinToString("\n") { it.csv() } + "\n"
        )
        val lines = mutableListOf<String>()
        lines += "GAMEPLAY-37 — INTERACTIVE ENCOUNTER LAB v3"
        lines += "2,304 synthetic races; 1,152 paired scenarios, 16 seeds/course/seat/policy."
        lines += "Mirrored human seats: all 2 seats and all 4 seats, against unchanged built-in bots."
        lines += "v2 keeps automated event outcomes; v3 allows shelter & hazard responses."
        lines += "Not actual session minutes, proof of balance, user enjoyment or causal skill lift."
        lines += "Later RNG streams diverge after choices; sample sizes remain small."
        lines += ""
        lines += "COURSE                       SEATS POLICY     v2_WIN% v3_WIN% DELTA_PP V3_AVG_DECISIONS"
        for (board in boards) for (seats in listOf(2, 4)) for (policy in Policy.entries) {
            val group = rows.filter { it.course == board.id && it.seats == seats &&
                it.policy == policy.name }
            val previous = group.filter { it.version == "v2" }
            val next = group.filter { it.version == "v3" }
            fun wins(a: List<Sample>) = 100.0 * a.count { it.won } / a.size
            lines += "%-28s %5d %-9s %7.1f %7.1f %+8.1f %16.2f".format(
                board.id, seats, policy.name, wins(previous), wins(next),
                wins(next) - wins(previous),
                next.map { it.encounterChoices }.average()
            )
        }
        lines += ""
        lines += "The count of encounter choices is evidence of agency opportunities,"
        lines += "not evidence that the optimal decision is obvious, worthwhile or fun."
        File(dir, "v2_vs_v3_mirrored_summary.txt").writeText(
            lines.joinToString("\n") + "\n")
        println(lines.joinToString("\n"))
    }
}
