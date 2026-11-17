package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class GameSessionTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val board = GameData.board(File(root, "data/boards/woodland_woven_trail.json").readText())
    private val events = GameData.events(File(root, "data/events/woven_events_v02.json").readText())

    @Test fun fixtureHasForksAndFinish() {
        assertEquals("START", board.start)
        assertEquals("FINISH", board.finish)
        assertEquals(38, board.nodes.size)
        assertEquals(5, board.nodes.count { it.kind == "fork" })
        assertEquals(20, events.events.size)
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
    @Test fun routePoolsAreDistinctAndCurated() {
        val spaces = board.nodes.filter { it.kind == "event" }
        assertTrue(spaces.any { it.eventPool == "hazard" })
        assertTrue(spaces.any { it.eventPool == "shelter" })
        assertTrue(spaces.any { it.eventPool == "mischief" })
        spaces.forEach { space ->
            assertTrue("Missing eligible events at ${space.id}",
                events.events.count { space.region in it.regions && space.eventPool in it.pools } >= 2)
        }
        assertTrue(events.events.any { it.pools.contains("hazard") && it.effect.type == "retreat" })
        assertFalse(events.events.any { it.pools.contains("shelter") && it.effect.type == "retreat" })
    }

    @Test fun forksHaveDifferentRoutesAndNoFakeChoiceLabels() {
        assertEquals(5, board.forks.size)
        board.forks.forEach { fork ->
            assertEquals(board.node(fork.id).next.toSet(), fork.choices.map { it.to }.toSet())
            assertEquals(fork.choices.size, fork.choices.map { it.label }.toSet().size)
            fork.choices.forEach {
                assertTrue(it.description.isNotBlank())
                assertEquals(it.risk, when (board.node(it.to).route) {
                    "risky" -> "high"
                    "safe" -> "low"
                    "mischief" -> "medium"
                    else -> "invalid"
                })
            }
        }
    }

    @Test fun encountersAreRetainedAfterOtherPlayersRoll() {
        for (seed in 1..250) {
            val g = GameSession(board, events, seats = 3, seed = seed, humanIndices = emptySet())
            var steps = 0
            while (g.snapshot().encounters.isEmpty() && g.phase != Phase.FINISHED && steps++ < 500) g.botStep()
            if (g.snapshot().encounters.isEmpty()) continue
            val encounter = g.snapshot().encounters.last()
            repeat(12) {
                if (g.phase != Phase.FINISHED) g.botStep()
            }
            assertTrue("Lost event history seed $seed", g.snapshot().encounters.contains(encounter))
            assertTrue(encounter.title.isNotBlank())
            assertTrue(encounter.actor.isNotBlank())
            return
        }
        fail("No encounter in 250 seeded games")
    }

    @Test fun encounterHistoryHasBoundedSize() {
        for (seed in 1..50) {
            val g = GameSession(board, events, 4, seed, humanIndices = emptySet())
            var actions = 0
            while (g.phase != Phase.FINISHED && actions++ < 1000) {
                g.botStep()
                assertTrue(g.snapshot().encounters.size <= 16)
            }
        }
    }

    @Test fun seedReplayIncludesIdenticalEncounterLog() {
        fun simulate(): Pair<Int?, List<EncounterNote>> {
            val g = GameSession(board, events, 4, 48294, humanIndices = emptySet())
            repeat(1000) { if(g.phase != Phase.FINISHED) g.botStep() }
            assertEquals(Phase.FINISHED,g.phase)
            return g.winnerIndex to g.snapshot().encounters
        }
        assertEquals(simulate(), simulate())
    }

}

