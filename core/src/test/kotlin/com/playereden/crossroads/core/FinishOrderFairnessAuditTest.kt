package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * GAMEPLAY-74: first-to-finish seat-order diagnostic.
 *
 * All racers are identical built-in bots. This isolates a meaningful property
 * of a race's fixed turn order without inventing a human-skill estimate.
 * The "after winner within four steps" field is a potential lost
 * same-round opportunity, NOT a claim that a later seat would have won.
 *
 * No engine, board, save, statistics, networking or presentation edits.
 */
class FinishOrderFairnessAuditTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val originals = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )

    data class Sample(
        val course: String,
        val version: String,
        val seats: Int,
        val seed: Int,
        val winnerSeat: Int,
        val totalDieActions: Int,
        val totalBotActions: Int,
        val winnerTurn: Int,
        val nearestRivalMinSpaces: Int,
        val rivalsWithinFour: Int,
        val laterSeatsRemaining: Int,
        val laterSeatsWithinFour: Int
    ) {
        fun csv() = listOf(
            course, version, seats, seed, winnerSeat + 1,
            totalDieActions, totalBotActions, winnerTurn,
            nearestRivalMinSpaces, rivalsWithinFour, laterSeatsRemaining,
            laterSeatsWithinFour
        ).joinToString(",")
    }

    private fun play(original: BoardDefinition, version: String, seats: Int, seed: Int): Sample {
        val board = if (version == "v3") DecisionLab.variantV3(original)
            else DecisionLab.variantV4(original)
        val game = GameSession(board, events, seats = seats, seed = seed, humanIndices = emptySet())
        var botActions = 0
        var diceActions = 0
        while (game.phase != Phase.FINISHED && botActions < 2500) {
            if (game.phase == Phase.ROLL) diceActions++
            game.botStep()
            botActions++
        }
        assertEquals("Softlock: ${original.id}/$version/$seats/$seed",
            Phase.FINISHED, game.phase)
        assertTrue(diceActions > 0)
        val winner = requireNotNull(game.winnerIndex)
        assertEquals(board.finish, game.players[winner].position)
        assertEquals(winner, game.currentIndex)

        val rivalMinSpaces = game.players.mapIndexedNotNull { seat, player ->
            if (seat == winner) null
            else DecisionLab.minimumSpacesToFinish(board, player.position)
        }
        val laterDistances = game.players.indices.filter { it > winner }.map { seat ->
            DecisionLab.minimumSpacesToFinish(board, game.players[seat].position)
        }
        assertEquals(seats - 1, rivalMinSpaces.size)
        assertEquals(seats - winner - 1, laterDistances.size)
        return Sample(
            original.id, version, seats, seed, winner, diceActions, botActions, game.turn,
            rivalMinSpaces.min(), rivalMinSpaces.count { it <= 4 },
            laterDistances.size, laterDistances.count { it <= 4 }
        )
    }

    private fun percentage(numerator: Int, denominator: Int): Double =
        if (denominator == 0) 0.0 else 100.0 * numerator / denominator

    @Test fun allIdenticalBotsFinishAndReportWhatLaterSeatsCouldStillAttempt() {
        val rows = buildList {
            for (board in originals) for (version in listOf("v3", "v4"))
                for (seats in 2..4) for (seed in 1..120)
                    add(play(board, version, seats, seed))
        }
        assertEquals(2160, rows.size)
        assertTrue(rows.all { it.totalDieActions > 0 && it.totalBotActions > 0 })
        assertTrue(rows.all { it.rivalsWithinFour in 0 until it.seats })
        assertTrue(rows.all { it.laterSeatsWithinFour <= it.laterSeatsRemaining })
        assertTrue(rows.all { it.nearestRivalMinSpaces >= 0 })

        val dir = File(root, "core/build/reports/gameplay-74")
        check(dir.exists() || dir.mkdirs())
        File(dir, "finish_order_fairness_samples.csv").writeText(
            "course,version,seats,seed,winner_seat_1_based,total_die_actions," +
                "total_bot_actions,global_turn_at_finish,nearest_rival_min_spaces," +
                "rivals_within_four_spaces,later_seats_without_turn,later_seats_within_four_spaces\n" +
                rows.joinToString("\n") { it.csv() } + "\n"
        )

        val lines = mutableListOf<String>()
        lines += "GAMEPLAY-74 — Identical-bot first-finish seat-order audit"
        lines += "2,160 synthetic races: 3 courses x 2 versions (v3/v4) x 2/3/4 seats x 120 seeds."
        lines += "Every racer uses the same existing state-aware bot, not a human policy."
        lines += "A close later seat is NOT a guaranteed finisher; dice/route/events can intervene."
        lines += "Four spaces is the reliable Steady base roll, not an exact finishing probability."
        lines += "Game turn and die-action counts are NOT real elapsed minutes."
        lines += "No changed mechanics or assumed fixes."
        lines += ""
        lines += "COURSE                        VER SEATS FIRST_WIN% LAST_WIN% WINNERS_BY_SEAT AVG_DIE_ACTIONS CLOSE_LATER_MATCHES% CLOSE_LATER_SEATS/TOTAL_LATER"
        for (board in originals) for (version in listOf("v3", "v4"))
            for (seats in 2..4) {
                val group = rows.filter { it.course == board.id &&
                    it.version == version && it.seats == seats }
                assertEquals(120, group.size)
                val wins = (0 until seats).map { seat ->
                    group.count { it.winnerSeat == seat }
                }
                assertEquals(120, wins.sum())
                val laterAvailable = group.sumOf { it.laterSeatsRemaining }
                val laterClose = group.sumOf { it.laterSeatsWithinFour }
                lines += "%-29s %-3s %5d %9.1f%% %8.1f%% %-17s %15.2f %19.1f%% %d/%d".format(
                    board.id, version, seats,
                    percentage(wins.first(), group.size),
                    percentage(wins.last(), group.size),
                    wins.joinToString("/"),
                    group.map { it.totalDieActions }.average(),
                    percentage(group.count { it.laterSeatsWithinFour > 0 }, group.size),
                    laterClose, laterAvailable
                )
            }
        lines += ""
        lines += "WHAT THE NUMBERS CAN SUPPORT:"
        lines += "- Uniform seat distribution is 1 / seats only as a simple fairness reference."
        lines += "- Fixed first-mover order is a plausible structural contributor to gaps;"
        lines += "  AI decisions, stochastic event ordering and course length also matter."
        lines += "- Later-seat-within-four is a counterfactual OPPORTUNITY signal only."
        lines += "- Test an equal-turn final-round option separately before adopting it;"
        lines += "  changing winner rules requires new versioned match/replay identity."
        lines += "- No fairness/skill/fun claim is permitted without real playtests."
        File(dir, "finish_order_fairness_summary.txt").writeText(
            lines.joinToString("\n") + "\n"
        )
        println(lines.joinToString("\n"))
    }
}
