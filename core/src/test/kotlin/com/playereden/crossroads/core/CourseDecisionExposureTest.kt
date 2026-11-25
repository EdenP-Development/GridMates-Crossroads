package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Synthetic bot actions, not human duration or proof of strategy balance. */
class CourseDecisionExposureTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private data class Seat(val rolls: Int, val forks: Int, val encounters: Int,
                            val targetChoices: Int, val won: Boolean)
    @Test fun auditAllSeatCountsAndRules() {
        val events = GameData.events(File(root, "data/events/woven_events_v02.json").readText())
        val paths = listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json", "data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_expanded.json",
            "design/prototypes/woodland_woven_regional_v04.json",
            "design/prototypes/woodland_woven_regional_v05.json")
        val rows = mutableListOf("course,rules,seats,seed,seat,rounds,rolls,forks,encounter_choices,target_choices,won")
        val summary = mutableListOf<String>()
        var races = 0
        for (path in paths) {
            val base = GameData.board(File(root, path).readText())
            for ((rules, board) in listOf("original" to base, "v5" to DecisionLab.variantV5(base))) {
                for (seats in 2..4) {
                    val observations = List(seats) { mutableListOf<Seat>() }
                    val rounds = mutableListOf<Int>()
                    for (seed in 1..60) {
                        val game = GameSession(board, events, seats, seed, humanIndices = emptySet())
                        val rolls = IntArray(seats)
                        val forks = IntArray(seats)
                        val encounters = IntArray(seats)
                        val targets = IntArray(seats)
                        var actions = 0
                        while (game.phase != Phase.FINISHED && actions < 2400) {
                            val seat = game.currentIndex
                            when (game.phase) {
                                Phase.ROLL -> rolls[seat]++
                                Phase.ROUTE -> if (game.availableRoutes().size >= 2) forks[seat]++
                                Phase.ENCOUNTER -> if (game.availableEncounterChoices().size >= 2) encounters[seat]++
                                Phase.TARGET -> if (game.availableTargets().size >= 2) targets[seat]++
                                Phase.FINISHED -> Unit
                            }
                            game.botStep()
                            actions++
                        }
                        assertEquals(Phase.FINISHED, game.phase)
                        val winner = requireNotNull(game.winnerIndex)
                        assertEquals(board.finish, game.players[winner].position)
                        val round = (game.turn - 1) / seats + 1
                        rounds.add(round)
                        races++
                        for (seat in 0 until seats) {
                            val value = Seat(rolls[seat], forks[seat], encounters[seat], targets[seat], seat == winner)
                            observations[seat].add(value)
                            rows.add(listOf(base.id, rules, seats, seed, seat + 1, round,
                                value.rolls, value.forks, value.encounters, value.targetChoices, value.won).joinToString(","))
                        }
                    }
                    fun median(values: List<Int>): Double {
                        val sorted = values.sorted()
                        return (sorted[(sorted.size - 1) / 2] + sorted[sorted.size / 2]) / 2.0
                    }
                    summary.add("${base.id} | $rules | $seats seats: rounds min/median/max=${rounds.minOrNull()}/${median(rounds)}/${rounds.maxOrNull()}")
                    observations.forEachIndexed { index, samples ->
                        summary.add("  seat ${index + 1}: wins=${samples.count { it.won }}/60, median rolls=${median(samples.map { it.rolls })}, median forks=${median(samples.map { it.forks })}, median encounter choices=${median(samples.map { it.encounters })}, median target choices=${median(samples.map { it.targetChoices })}, zero forks=${samples.count { it.forks == 0 }}/60, zero non-roll choices=${samples.count { it.forks + it.encounters + it.targetChoices == 0 }}/60")
                    }
                    assertEquals(60, observations.flatten().count { it.won })
                }
            }
        }
        assertEquals(2160, races)
        assertEquals(6481, rows.size)
        val dir = File(root, "core/build/reports/gameplay-104")
        dir.mkdirs()
        File(dir, "seat-exposure.csv").writeText(rows.joinToString("\n") + "\n")
        File(dir, "summary.txt").writeText(summary.joinToString("\n") + "\n")
        println(summary.joinToString("\n"))
    }
}
