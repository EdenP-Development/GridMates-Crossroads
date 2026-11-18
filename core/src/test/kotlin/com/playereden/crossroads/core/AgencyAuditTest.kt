package com.playereden.crossroads.core

import java.io.File
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

/**
 * AGENCY-34: does thinking about a fork affect the race, or can people
 * button-mash with approximately the same outcome?
 *
 * This audits the EXISTING released-to-QA engine. It changes no board,
 * event, RNG, rules, LAN protocol, user data, or checkpoint format.
 *
 * Synthetic player 1 rolls as soon as permitted, chooses routes pseudo-
 * randomly, and targets the first legal opponent. Other seats run existing
 * bots. No result here is a human UX/strategy study.
 */
class AgencyAuditTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val boards = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private val startedAt = 1794931200000L

    data class Outcome(
        val winner: Int,
        val humanRolls: Int,
        val humanForks: Int,
        val humanTargets: Int,
        val allActions: Int
    ) {
        val humanChoices get() = humanForks + humanTargets
    }

    private fun playToFinish(game: GameSession, choices: Random): Outcome {
        var rolls = 0
        var forks = 0
        var targets = 0
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
                    forks++
                    val options = game.availableRoutes()
                    assertTrue(options.size in 2..3)
                    game.chooseRoute(options[choices.nextInt(options.size)].id)
                }
                Phase.TARGET -> {
                    targets++
                    game.chooseTarget(game.availableTargets().first())
                }
                Phase.FINISHED -> Unit
            }
        }
        assertEquals("Non-finishing game: ${game.board.id}", Phase.FINISHED, game.phase)
        val winner = requireNotNull(game.winnerIndex)
        assertEquals(game.board.finish, game.players[winner].position)
        return Outcome(winner, rolls, forks, targets, actions)
    }

    data class AuditRow(
        val boardId: String,
        val seats: Int,
        val seed: Int,
        val won: Boolean,
        val rolls: Int,
        val forks: Int,
        val targets: Int,
        val noFirstForkBeforeFinish: Boolean,
        val humanWinChangesWithFirstChoice: Boolean?,
        val overallWinnerChangesWithFirstChoice: Boolean?,
        val firstForkChoices: Int
    ) {
        fun csv(): String = listOf(
            boardId, seats, seed, won, rolls, forks, targets,
            noFirstForkBeforeFinish,
            humanWinChangesWithFirstChoice?.toString().orEmpty(),
            overallWinnerChangesWithFirstChoice?.toString().orEmpty(),
            firstForkChoices
        ).joinToString(",")
    }

    private fun measure(board: BoardDefinition, seats: Int, seed: Int): AuditRow {
        val game = GameSession(board, events, seats, seed, humanIndices = setOf(0))
        val random = Random(seed * 73 + seats * 7)
        var firstFork: MatchCheckpoint? = null
        var firstOptions: List<String> = emptyList()
        var forks = 0
        var rolls = 0
        var targets = 0
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
                    forks++
                    val options = game.availableRoutes()
                    if (firstFork == null) {
                        firstFork = game.checkpoint("agency-$seats-$seed", startedAt)
                        firstOptions = options.map { it.id }
                    }
                    game.chooseRoute(options[random.nextInt(options.size)].id)
                }
                Phase.TARGET -> {
                    targets++
                    game.chooseTarget(game.availableTargets().first())
                }
                Phase.FINISHED -> Unit
            }
        }

        assertEquals("Baseline stalled: ${board.id} $seats $seed", Phase.FINISHED, game.phase)
        assertEquals(board.finish, game.players[requireNotNull(game.winnerIndex)].position)
        val whatIf = firstFork?.let { checkpoint ->
            firstOptions.map { option ->
                val replay = GameSession.restore(board, events, checkpoint)
                assertEquals(Phase.ROUTE, replay.phase)
                assertEquals(0, replay.currentIndex)
                replay.chooseRoute(option)
                // A fixed, independent post-fork synthetic policy. Each
                // branch continues honestly, with the same seeded rule RNG
                // up until the choice. Later event draws may diverge.
                playToFinish(replay, Random(seed * 137 + seats * 29))
            }
        }

        return AuditRow(
            board.id, seats, seed, game.winnerIndex == 0, rolls, forks, targets,
            firstFork == null,
            whatIf?.map { it.winner == 0 }?.distinct()?.size?.let { it > 1 },
            whatIf?.map { it.winner }?.distinct()?.size?.let { it > 1 },
            firstOptions.size
        )
    }

    @Test fun measureButtonMashingAndConsequentialFirstChoices() {
        val samples = buildList {
            for (board in boards) for (seats in listOf(2, 4)) {
                for (seed in 1..200) add(measure(board, seats, seed))
            }
        }
        assertEquals(1200, samples.size)
        assertTrue(samples.all { it.rolls > 0 })
        assertTrue(samples.all { it.forks >= 0 })
        assertTrue(samples.all { !it.noFirstForkBeforeFinish || it.firstForkChoices == 0 })

        val destination = File(root, "core/build/reports/agency")
        check(destination.exists() || destination.mkdirs())
        File(destination, "crossroads_agency_samples.csv").writeText(
            "course,seats,seed,human_won,human_rolls,human_route_choices,human_prank_choices,finished_before_first_fork,human_win_flips_when_first_route_changes,winner_identity_flips_when_first_route_changes,first_fork_options\n" +
                samples.joinToString("\n") { it.csv() } + "\n"
        )

        val lines = mutableListOf<String>()
        lines += "CROSSROADS AGENCY-34 — button-mashing and first-crossroads leverage"
        lines += "Existing unmodified rules; 200 seeds per course and 2/4-seat match = 1200 baseline races."
        lines += "Seat 1 is simulated by immediate Roll/random route/first legal prank target;"
        lines += "remaining seats use existing built-in bots. No human attention or time is measured."
        lines += "First-fork counterfactuals restore the SAME checkpoint and force each"
        lines += "available first route; later actions are automated and event RNG can diverge."
        lines += ""
        lines += "COURSE                         SEATS  WIN%  AVG ROLLS  AVG FORKS  0 FORKS%  FORK-CHANGES-HUMAN-WIN%  FORK-CHANGES-WINNER%"
        for (board in boards) for (seats in listOf(2, 4)) {
            val group = samples.filter { it.boardId == board.id && it.seats == seats }
            val withChoices = group.filter { it.firstForkChoices > 1 }
            val winFlip = withChoices.count { it.humanWinChangesWithFirstChoice == true }
            val anyFlip = withChoices.count { it.overallWinnerChangesWithFirstChoice == true }
            val share = { n: Int, d: Int -> if (d == 0) 0.0 else 100.0 * n / d }
            lines += "%-29s %5d %5.1f %10.2f %10.2f %8.1f %24.1f %22.1f".format(
                board.id, seats, share(group.count { it.won }, group.size),
                group.map { it.rolls }.average(), group.map { it.forks }.average(),
                share(group.count { it.noFirstForkBeforeFinish }, group.size),
                share(winFlip, withChoices.size), share(anyFlip, withChoices.size)
            )
        }
        lines += ""
        lines += "INTERPRETATION:"
        lines += "- A first-fork win flip means route selection CAN matter under this"
        lines += "  synthetic continuation. It does not measure skill advantage or agency"
        lines += "  over entire human matches."
        lines += "- Few human forks relative to rolls means many turns offer no decision."
        lines += "- Zero-fork matches are actionable: player 1 finished without ever"
        lines += "  choosing a route, or an AI finished before their first fork."
        lines += "- Button mashing and human enjoyment cannot be inferred from this"
        lines += "  alone. Test perceived meaningful choices on phones."
        lines += "- No rule changes or balance conclusions are authorized by this test."

        File(destination, "crossroads_agency_summary.txt").writeText(
            lines.joinToString("\n") + "\n"
        )
        println(lines.joinToString("\n"))
    }
}
