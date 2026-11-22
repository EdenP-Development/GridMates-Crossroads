package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Pre-release measurement, NOT a substitute for human play.
 * All seats use the existing deterministic AI; no engine or data changes.
 */
class BalanceAuditTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val classic = GameData.board(
        File(root, "data/boards/woodland_woven_trail.json").readText()
    )
    private val quick = GameData.board(
        File(root, "data/boards/woodland_brookside_quick.json").readText()
    )
    private val long = GameData.board(
        File(root, "data/boards/woodland_cedarwood_long.json").readText()
    )
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private val eventType = events.events.associate { it.id to it.effect.type }

    data class Sample(
        val course: String,
        val seats: Int,
        val seed: Int,
        val winner: Int,
        val rolls: Int,
        val actions: Int,
        val encounters: Int,
        val advances: Int,
        val setbacks: Int,
        val shields: Int,
        val pranks: Int
    ) {
        fun csv(): String = listOf(
            course, seats, seed, winner + 1, rolls, actions, encounters,
            advances, setbacks, shields, pranks
        ).joinToString(",")
    }

    private fun play(board: BoardDefinition, seats: Int, seed: Int): Sample {
        val game = GameSession(board, events, seats, seed, humanIndices = emptySet())
        var actions = 0
        var rolls = 0
        var encounters = 0
        var advances = 0
        var setbacks = 0
        var shields = 0
        var pranks = 0
        while (game.phase != Phase.FINISHED && actions < 1_200) {
            val previousPhase = game.phase
            val previousEventId = game.lastEventId
            if (previousPhase == Phase.ROLL) rolls++
            game.botStep()
            actions++
            // An encounter may resolve during ROLL or ROUTE. It may draw the
            // same event ID on another turn, so phase is part of detection.
            val drawn = game.lastEventId
            if (drawn != null &&
                (previousPhase == Phase.ROLL ||
                    previousPhase == Phase.ROUTE && previousEventId == null)
            ) {
                encounters++
                when (eventType.getValue(drawn)) {
                    "advance" -> advances++
                    "retreat" -> setbacks++
                    "shield" -> shields++
                    "prank" -> pranks++
                }
            }
        }
        assertEquals(
            "Race soft-locked: " + board.id + " seats " + seats + " seed " + seed,
            Phase.FINISHED, game.phase
        )
        assertTrue("No dice rolled", rolls > 0)
        val winner = requireNotNull(game.winnerIndex)
        assertEquals(board.finish, game.players[winner].position)
        return Sample(
            board.id, seats, seed, winner, rolls, actions, encounters,
            advances, setbacks, shields, pranks
        )
    }

    private fun percentile(values: List<Int>, percentile: Int): Int {
        val sorted = values.sorted()
        return sorted[((sorted.size - 1) * percentile / 100.0).toInt()]
    }

    @Test fun deterministicCourseAndSeatBalanceAudit() {
        val samples = buildList {
            for (board in listOf(quick, classic, long)) {
                for (seats in 2..4) {
                    for (seed in 1..240) add(play(board, seats, seed))
                }
            }
        }
        assertEquals(2_160, samples.size)
        assertTrue("No encounters were observed", samples.any { it.encounters > 0 })

        val destination = File(root, "core/build/reports/balance")
        check(destination.exists() || destination.mkdirs())
        File(destination, "crossroads_balance_samples.csv").writeText(
            "course,seats,seed,winner_seat_1_based,dice_rolls,ai_actions,encounters,advances,retreats,shields,pranks\n" +
                samples.joinToString("\n") { it.csv() } + "\n"
        )
        val lines = mutableListOf<String>()
        lines += "CROSSROADS BALANCE-31: deterministic all-AI race audit"
        lines += "Sample: 240 fixed seeds per course and seat count; 2160 completed races"
        lines += "Dice rolls are turn actions, NOT elapsed minutes"
        lines += "AI choices do not predict human route selection or enjoyment"
        lines += ""
        lines += "%-28s %5s %8s %8s %9s %12s %9s %11s".format(
            "Course", "Seats", "Median", "P90", "Min-Max", "Avg events", "Seat1 win", "Wins/seat"
        )
        val meanRolls = mutableMapOf<Pair<String, Int>, Double>()
        for (course in listOf(quick.id, classic.id, long.id)) {
            for (seats in 2..4) {
                val group = samples.filter { it.course == course && it.seats == seats }
                val rolls = group.map { it.rolls }
                meanRolls[course to seats] = rolls.average()
                val wins = (0 until seats).map { seat -> group.count { it.winner == seat } }
                assertEquals(group.size, wins.sum())
                val share = wins.first() * 100.0 / group.size
                lines += "%-28s %5d %8d %8d %4d-%-4d %12.2f %8.1f%% %s".format(
                    course, seats, percentile(rolls, 50), percentile(rolls, 90),
                    rolls.minOrNull()!!, rolls.maxOrNull()!!,
                    group.map { it.encounters }.average(), share, wins.joinToString("/")
                )
                if (share > (100.0 / seats) * 1.8) {
                    lines += "    REVIEW: seat-one advantage merits human-play balancing"
                }
            }
        }
        lines += ""
        lines += "Quick average dice-roll count relative to Classic (lower is shorter):"
        for (seats in 2..4) {
            val ratio = 100 * meanRolls.getValue(quick.id to seats) /
                meanRolls.getValue(classic.id to seats)
            lines += "  " + seats + " seats: %.1f%%".format(ratio)
            if (ratio >= 100) lines += "    REVIEW: Quick did not prove shorter"
        }
        lines += ""
        lines += "Long average dice-roll count relative to Classic (higher is longer):"
        for (seats in 2..4) {
            val ratio = 100 * meanRolls.getValue(long.id to seats) /
                meanRolls.getValue(classic.id to seats)
            lines += "  " + seats + " seats: %.1f%%".format(ratio)
            if (ratio <= 125) lines += "    REVIEW: Long was not substantially longer"
        }
        lines += ""
        lines += "Encounter totals across all 2160 races:"
        lines += "  draws: " + samples.sumOf { it.encounters }
        lines += "  advances: " + samples.sumOf { it.advances }
        lines += "  retreats: " + samples.sumOf { it.setbacks }
        lines += "  shields: " + samples.sumOf { it.shields }
        lines += "  pranks: " + samples.sumOf { it.pranks }
        lines += ""
        lines += "This deterministic sample has no timing/user-satisfaction evidence."
        lines += "Do not change saved-game probabilities or board content without migration."
        File(destination, "crossroads_balance_summary.txt").writeText(
            lines.joinToString("\n") + "\n"
        )
        println(lines.joinToString("\n"))
    }
}
