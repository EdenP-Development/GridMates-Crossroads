package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class MatchCheckpointTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val board = GameData.board(File(root, "data/boards/woodland_woven_trail.json").readText())
    private val catalogue = GameData.events(File(root, "data/events/woven_events_v02.json").readText())
    private val stamp = 1794931200000L

    private fun checkpoint(game: GameSession): MatchCheckpoint =
        MatchCheckpointCodec.decode(
            MatchCheckpointCodec.encode(game.checkpoint("saved-test-match", stamp))
        )

    @Test fun freshAndPendingRollRoundTripExactly() {
        val game = GameSession(board, catalogue, seats = 3, seed = 848,
            humanIndices = setOf(0))
        val fresh = checkpoint(game)
        assertTrue(fresh.actions.isEmpty())
        assertEquals(game.snapshot(), GameSession.restore(board, catalogue, fresh).snapshot())
        game.roll()
        val rolled = checkpoint(game)
        assertEquals(listOf(MatchActionKind.ROLL), rolled.actions.map { it.kind })
        assertEquals(game.snapshot(), GameSession.restore(board, catalogue, rolled).snapshot())
    }

    @Test fun humanForkCanResumeWithRemainingStepsAndIdenticalFuture() {
        val game = GameSession(board, catalogue, seats = 2, seed = 22,
            humanIndices = setOf(0))
        var actions = 0
        while (game.phase != Phase.ROUTE && game.phase != Phase.FINISHED && actions++ < 500) {
            when {
                game.players[game.currentIndex].isBot -> game.botStep()
                game.phase == Phase.ROLL -> game.roll()
                game.phase == Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                else -> error("Unexpected phase")
            }
        }
        assertEquals(Phase.ROUTE, game.phase)
        val saved = checkpoint(game)
        val restored = GameSession.restore(board, catalogue, saved)
        assertEquals(game.remainingSteps, restored.remainingSteps)
        assertEquals(game.availableRoutes().map { it.id }, restored.availableRoutes().map { it.id })
        assertEquals(game.snapshot(), restored.snapshot())
        val selected = game.availableRoutes().first().id
        game.chooseRoute(selected)
        restored.chooseRoute(selected)
        assertEquals(game.snapshot(), restored.snapshot())
    }

    @Test fun pendingMischiefTargetCanBeRestored() {
        var pending: GameSession? = null
        for (seed in 1..160) {
            val game = GameSession(board, catalogue, seats = 4, seed = seed,
                humanIndices = emptySet())
            var steps = 0
            while (game.phase != Phase.TARGET && game.phase != Phase.FINISHED &&
                steps++ < 550) game.botStep()
            if (game.phase == Phase.TARGET) {
                pending = game
                break
            }
        }
        val game = requireNotNull(pending) { "No mischief target in seeded sessions" }
        val restored = GameSession.restore(board, catalogue, checkpoint(game))
        assertEquals(Phase.TARGET, restored.phase)
        assertEquals(game.availableTargets(), restored.availableTargets())
        val target = game.availableTargets().first()
        game.chooseTarget(target)
        restored.chooseTarget(target)
        assertEquals(game.snapshot(), restored.snapshot())
    }

    @Test fun botFutureDiceRoutesAndEventsAreIdenticalAfterResume() {
        for (seats in 2..4) for (seed in 1..12) {
            val game = GameSession(board, catalogue, seats, seed,
                humanIndices = emptySet())
            repeat(23) { if (game.phase != Phase.FINISHED) game.botStep() }
            val save = checkpoint(game)
            // One public command per botStep, never a nested roll/route/target.
            assertTrue(save.actions.all { it.kind == MatchActionKind.BOT })
            val recovered = GameSession.restore(board, catalogue, save)
            assertEquals("Checkpoint mismatch $seats/$seed", game.snapshot(), recovered.snapshot())
            var actions = 0
            while (game.phase != Phase.FINISHED && actions++ < 1500) {
                game.botStep()
                recovered.botStep()
                assertEquals("RNG diverged $seats/$seed after $actions actions",
                    game.snapshot(), recovered.snapshot())
            }
            assertEquals(Phase.FINISHED, recovered.phase)
            val final = checkpoint(recovered)
            assertEquals(game.snapshot(), GameSession.restore(board, catalogue, final).snapshot())
        }
    }

    @Test fun tamperedOrIncompatibleSaveIsRejectedRatherThanPartiallyLoaded() {
        val game = GameSession(board, catalogue, seats = 3, seed = 390,
            humanIndices = emptySet())
        repeat(8) { if (game.phase != Phase.FINISHED) game.botStep() }
        val save = checkpoint(game)
        fun bad(value: MatchCheckpoint) {
            try {
                GameSession.restore(board, catalogue, value)
                fail("Accepted corrupted or incompatible checkpoint")
            } catch (_: IllegalArgumentException) { }
        }
        bad(save.copy(formatVersion = 999))
        bad(save.copy(contentFingerprint = "wrong-map-or-events"))
        bad(save.copy(boardId = "other-course"))
        bad(save.copy(seats = 5))
        bad(save.copy(humanIndices = listOf(0, 0)))
        bad(save.copy(expected = save.expected.copy(turn = save.expected.turn + 1)))
        bad(save.copy(actions = save.actions.dropLast(1)))
        try {
            MatchCheckpointCodec.decode("not JSON")
            fail("Accepted invalid JSON")
        } catch (_: Exception) { }
    }
    @Test fun passAndPlayCheckpointPreservesEveryHumanSeatAndDecision() {
        for (seats in 2..4) {
            val game = GameSession(board, catalogue, seats = seats, seed = 111 + seats,
                humanIndices = (0 until seats).toSet())
            var actions = 0
            while (game.currentIndex == 0 && game.phase != Phase.FINISHED && actions++ < 200) {
                when (game.phase) {
                    Phase.ROLL -> game.roll()
                    Phase.ROUTE -> game.chooseRoute(game.availableRoutes().first().id)
                    Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                    Phase.FINISHED -> Unit
                }
            }
            assertEquals(1, game.currentIndex)
            val recovered = GameSession.restore(board, catalogue, checkpoint(game))
            assertEquals(game.snapshot(), recovered.snapshot())
            assertTrue(recovered.players.none { it.isBot })
            assertEquals((1..seats).map { "Player $it" },
                recovered.players.map { it.name })

            // All players continue from exactly the same point and future RNG.
            var moveCount = 0
            while (game.phase != Phase.FINISHED && moveCount++ < 1200) {
                val before = game.snapshot()
                when (game.phase) {
                    Phase.ROLL -> { game.roll(); recovered.roll() }
                    Phase.ROUTE -> {
                        val route = game.availableRoutes().first().id
                        game.chooseRoute(route)
                        recovered.chooseRoute(route)
                    }
                    Phase.TARGET -> {
                        val target = game.availableTargets().first()
                        game.chooseTarget(target)
                        recovered.chooseTarget(target)
                    }
                    Phase.FINISHED -> Unit
                }
                assertTrue(game.revision > before.revision)
                assertEquals("Local game diverged at $seats seats, action $moveCount",
                    game.snapshot(), recovered.snapshot())
            }
            assertEquals(Phase.FINISHED, game.phase)
        }
    }

}
