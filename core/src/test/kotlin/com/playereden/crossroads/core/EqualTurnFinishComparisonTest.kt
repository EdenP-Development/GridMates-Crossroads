package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Diagnostic, not a forced success claim: identical-bot v4 versus v5 races.
 * All old v4 mechanics remain except v5 finishes the current round fairly.
 */
class EqualTurnFinishComparisonTest {
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
        val course: String, val version: String, val seats: Int, val seed: Int,
        val winnerSeat: Int, val dieActions: Int, val botActions: Int,
        val finalRoundTriggered: Boolean, val extraRollsAfterFirstFinish: Int,
        val finishersAtEnd: Int
    ) {
        fun csv() = listOf(course, version, seats, seed, winnerSeat + 1,
            dieActions, botActions, finalRoundTriggered,
            extraRollsAfterFirstFinish, finishersAtEnd).joinToString(",")
    }

    private fun run(base: BoardDefinition, version: String, seats: Int, seed: Int): Sample {
        val board = if (version == "v5") DecisionLab.variantV5(base)
                    else DecisionLab.variantV4(base)
        val game = GameSession(board, events, seats, seed, emptySet())
        var allActions = 0
        var allRolls = 0
        var extraRolls = 0
        var pendingSeen = false
        while (game.phase != Phase.FINISHED && allActions < 2500) {
            if (game.phase == Phase.ROLL) {
                allRolls++
                if (game.finalRoundPending) extraRolls++
            }
            if (game.finalRoundPending) pendingSeen = true
            game.botStep()
            if (game.finalRoundPending) pendingSeen = true
            allActions++
        }
        assertEquals("Softlock: ${base.id}/$version/$seats/$seed",
            Phase.FINISHED, game.phase)
        val winner = requireNotNull(game.winnerIndex)
        assertEquals(board.finish, game.players[winner].position)
        assertTrue(allRolls > 0)
        assertTrue(allActions >= allRolls)
        if (version == "v4") {
            assertFalse(game.finalRoundPending)
            assertFalse(pendingSeen)
            assertEquals(0, extraRolls)
        }
        return Sample(base.id, version, seats, seed, winner, allRolls, allActions,
            pendingSeen, extraRolls,
            game.players.count { it.position == board.finish })
    }

    private fun percent(n: Int, d: Int): Double = if (d == 0) 0.0 else 100.0 * n / d

    @Test fun compareFirstSeatFairnessAndAdditionalActionsWithoutPredeterminedVerdict() {
        val rows = buildList {
            for (base in originals) for (seats in 2..4) for (seed in 1..120) {
                add(run(base, "v4", seats, seed))
                add(run(base, "v5", seats, seed))
            }
        }
        assertEquals(2160, rows.size)
        assertTrue(rows.all { it.finishersAtEnd in 1..it.seats })
        assertTrue(rows.filter { it.version == "v4" }.all { it.finishersAtEnd == 1 })
        assertTrue(rows.filter { it.version == "v5" }.all {
            !it.finalRoundTriggered || it.extraRollsAfterFirstFinish > 0
        })

        val dir = File(root, "core/build/reports/gameplay-75")
        check(dir.exists() || dir.mkdirs())
        File(dir, "v4_v5_equal_turn_samples.csv").writeText(
            "course,version,seats,seed,winner_seat_1_based,die_actions," +
                "bot_actions,final_round_triggered,extra_rolls_after_first_finish," +
                "finishers_at_end\n" +
                rows.joinToString("\n") { it.csv() } + "\n"
        )
        val lines = mutableListOf<String>()
        lines += "GAMEPLAY-75 — First-to-finish v4 versus equal-turn v5 diagnostic"
        lines += "2,160 identical-bot simulated races: 3 courses x 2/3/4 seats x " +
            "120 seeds x 2 versions."
        lines += "V5: the remaining seats in the finishing round can take one last turn."
        lines += "More unused movement at finish wins when several finish; equal movement favors earliest arrival."
        lines += "The original v4 rules and checkpoints are unchanged."
        lines += "Races are synthetic, not evidence of real human match time or fairness perception."
        lines += ""
        lines += "COURSE                        SEATS V4_SEAT1% V5_SEAT1% V4_SEAT_LAST% V5_SEAT_LAST% V4_AVG_ROLLS V5_AVG_ROLLS V5_FINAL_ROUND% V5_MULTI_FINISH% V5_AVG_EXTRA_ROLLS"
        for (base in originals) for (seats in 2..4) {
            val group = rows.filter { it.course == base.id && it.seats == seats }
            val v4 = group.filter { it.version == "v4" }
            val v5 = group.filter { it.version == "v5" }
            assertEquals(120, v4.size)
            assertEquals(120, v5.size)
            lines += "%-29s %5d %9.1f%% %9.1f%% %12.1f%% %12.1f%% %12.2f %12.2f %14.1f%% %15.1f%% %16.2f".format(
                base.id, seats,
                percent(v4.count { it.winnerSeat == 0 }, v4.size),
                percent(v5.count { it.winnerSeat == 0 }, v5.size),
                percent(v4.count { it.winnerSeat == seats - 1 }, v4.size),
                percent(v5.count { it.winnerSeat == seats - 1 }, v5.size),
                v4.map { it.dieActions }.average(),
                v5.map { it.dieActions }.average(),
                percent(v5.count { it.finalRoundTriggered }, v5.size),
                percent(v5.count { it.finishersAtEnd > 1 }, v5.size),
                v5.map { it.extraRollsAfterFirstFinish }.average()
            )
        }
        lines += ""
        lines += "INTERPRETATION:"
        lines += "- Equal-turn resolution can remove a last-action disadvantage, but does not promise 1/seats winners."
        lines += "- Compare per course; Quick has unusually short, highly first-seat-favored races."
        lines += "- Extra die actions are NOT animation or actual seconds."
        lines += "- Note multi-finisher percentages and whether extra play burdens Quick."
        lines += "- No rule promotion without real Android play, save restoration and owner approval."
        File(dir, "v4_v5_equal_turn_summary.txt").writeText(
            lines.joinToString("\n") + "\n"
        )
        println(lines.joinToString("\n"))
    }
}
