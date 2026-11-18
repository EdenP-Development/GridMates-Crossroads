package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** QA-only new rules. No previously shipped course's fingerprint changes. */
class DecisionLabTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val boards = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private val started = 1794931200000L

    @Test fun labBoardsHaveDistinctFingerprintsAndOldGamesStayLegacy() {
        for (base in boards) {
            val lab = DecisionLab.variant(base)
            assertEquals(base.id, DecisionLab.baseId(lab.id))
            assertTrue(DecisionLab.enabled(lab))
            assertFalse(DecisionLab.enabled(base))
            assertEquals(base.nodes, lab.nodes)
            assertEquals(base.forks, lab.forks)
            assertNotEquals(
                MatchCheckpointCodec.fingerprint(base, events),
                MatchCheckpointCodec.fingerprint(lab, events)
            )
            val existing = GameSession(base, events, seed = 61)
            existing.roll()
            val checkpoint = existing.checkpoint("legacy-${base.id}", started)
            assertEquals(existing.snapshot(),
                GameSession.restore(base, events, checkpoint).snapshot())
            val legacyPayload = MatchCheckpointCodec.encode(checkpoint)
            assertFalse(legacyPayload.contains("STEADY_ROLL"))
            assertFalse(legacyPayload.contains("BOLD_ROLL"))
            try {
                existing.rollSteady()
                fail("Legacy board accepted tactical input")
            } catch (_: IllegalArgumentException) { }
            try {
                GameSession.restore(lab, events, checkpoint)
                fail("Lab accepted legacy checkpoint")
            } catch (_: IllegalArgumentException) { }
        }
    }

    @Test fun tacticalDiceActionsAreExplicitReproducibleAndOldRollIsRejected() {
        val board = DecisionLab.variant(boards.first())
        val game = GameSession(board, events, seats = 2, seed = 101,
            humanIndices = setOf(0, 1))
        try {
            game.roll()
            fail("Lab silently accepted old roll")
        } catch (_: IllegalArgumentException) { }
        game.rollSteady()
        assertEquals(3, game.die)
        assertEquals(game.snapshot(),
            GameSession.restore(board, events,
                game.checkpoint("lab-steady", started)).snapshot())
        val coded = MatchCheckpointCodec.encode(game.checkpoint("lab-steady", started))
        assertTrue(coded.contains("STEADY_ROLL"))
        assertEquals(game.snapshot(),
            GameSession.restore(board, events, MatchCheckpointCodec.decode(coded)).snapshot())

        val push = GameSession(board, events, seats = 2, seed = 101,
            humanIndices = setOf(0, 1))
        push.rollBold()
        assertTrue(requireNotNull(push.die) in 1..6)
        assertEquals(push.snapshot(),
            GameSession.restore(board, events,
                push.checkpoint("lab-push", started)).snapshot())
        assertNotEquals(game.checkpoint("lab-steady", started).actions,
            push.checkpoint("lab-push", started).actions)
    }

    @Test fun verifiedRiskEncounterOccursAtFirstRouteCommitment() {
        // First fork is two spaces from Start on Quick, hence Steady reaches it.
        val base = boards.first()
        for (risk in listOf("high", "low")) {
            val firstForkAtStart = base.copy(nodes = base.nodes.map { node ->
                if (node.id == base.start) node.copy(next = listOf("F1")) else node
            })
            val board = DecisionLab.variant(firstForkAtStart)
            val game = GameSession(board, events, seats = 2, seed = 31,
                humanIndices = setOf(0, 1))
            game.rollSteady()
            assertEquals(Phase.ROUTE, game.phase)
            val fork = requireNotNull(board.forkAt(game.players[0].position))
            val selected = fork.choices.first { it.risk == risk }
            assertNull(game.lastEventId)
            game.chooseRoute(selected.to)
            assertNotNull("Entering $risk failed to trigger a route encounter",
                game.lastEventId)
            val draw = events.events.single { it.id == game.lastEventId }
            assertTrue(draw.pools.contains(if (risk == "high") "hazard" else "shelter"))
            assertEquals(game.snapshot(),
                GameSession.restore(board, events,
                    game.checkpoint("route-${risk}", started)).snapshot())
        }
    }

    @Test fun seededPlaytestModesAlwaysFinishAndReplayForAllSeatingSizes() {
        for (base in boards) for (seats in 2..4) for (seed in 1..55) {
            val board = DecisionLab.variant(base)
            val game = GameSession(board, events, seats = seats, seed = seed,
                humanIndices = setOf(0))
            var steps = 0
            while (game.phase != Phase.FINISHED && steps++ < 2700) {
                if (game.players[game.currentIndex].isBot) {
                    game.botStep()
                } else {
                    when (game.phase) {
                        Phase.ROLL -> if (seed % 2 == 0) game.rollSteady()
                            else game.rollBold()
                        Phase.ROUTE -> game.chooseRoute(
                            game.availableRoutes()[seed % game.availableRoutes().size].id
                        )
                        Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                        Phase.FINISHED -> Unit
                    }
                }
                if (seed <= 4) {
                    val checkpoint = game.checkpoint(
                        "test-${base.id}-$seats-$seed", started)
                    val replayed = GameSession.restore(board, events,
                        MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(checkpoint)))
                    assertEquals(game.snapshot(), replayed.snapshot())
                }
            }
            assertEquals("Lab stalled ${base.id} $seats seed $seed",
                Phase.FINISHED, game.phase)
            assertEquals(board.finish,
                game.players[requireNotNull(game.winnerIndex)].position)
        }
    }
}
