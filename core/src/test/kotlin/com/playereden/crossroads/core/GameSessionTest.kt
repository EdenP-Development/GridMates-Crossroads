package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class GameSessionTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val board = GameData.board(File(root, "data/boards/woodland_quick_example.json").readText())
    private val events = GameData.events(File(root, "data/events/core_events_proposed.json").readText())

    @Test fun fixtureHasForksAndFinish() {
        assertEquals("START", board.start)
        assertEquals("FINISH", board.finish)
        assertEquals(40, board.nodes.size)
        assertEquals(3, board.nodes.count { it.kind == "fork" })
        assertEquals(14, events.events.size)
    }

    @Test fun onlyLegalRouteCanBeChosen() {
        val game = GameSession(board, events, seed = 4)
        var steps = 0
        while (game.phase != Phase.ROUTE && game.phase != Phase.FINISHED && steps++ < 100) {
            when (game.phase) {
                Phase.ROLL -> game.roll()
                Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                else -> Unit
            }
        }
        assertEquals(Phase.ROUTE, game.phase)
        try {
            game.chooseRoute("DOES_NOT_EXIST")
            fail("Illegal route accepted")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test fun allBotsFinishEveryRaceWithoutSoftlock() {
        for (seats in 2..4) for (seed in 1..100) {
            val game = GameSession(board, events, seats, seed, humanIndices = emptySet())
            var actions = 0
            while (game.phase != Phase.FINISHED && actions++ < 1500) game.botStep()
            assertEquals("Failed seed $seed seats $seats", Phase.FINISHED, game.phase)
            assertNotNull(game.winnerIndex)
            assertEquals(board.finish, game.players[game.winnerIndex!!].position)
        }
    }

    @Test fun identicalSeedAndActionsProduceIdenticalResult() {
        fun play(): List<String> {
            val game = GameSession(board, events, 4, 8192, humanIndices = emptySet())
            val log = mutableListOf<String>()
            repeat(2000) {
                if (game.phase == Phase.FINISHED) return@repeat
                game.botStep()
                log.add("${game.snapshot().phase}:${game.snapshot().playerIndex}:${game.snapshot().players.map { p -> p.position }}:${game.lastEventId}")
            }
            assertEquals(Phase.FINISHED, game.phase)
            return log
        }
        assertEquals(play(), play())
    }

    @Test fun humanMustChooseAtFork() {
        val game = GameSession(board, events, 2, 22, humanIndices = setOf(0))
        game.roll()
        if (game.phase == Phase.ROUTE) {
            assertEquals(2, game.availableRoutes().size)
            game.chooseRoute(game.availableRoutes().first().id)
        }
        assertTrue(game.revision >= 1)
    }
}
