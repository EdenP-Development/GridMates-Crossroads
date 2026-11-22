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

    @Test fun snapshotIsDefensiveAndCannotMoveAuthoritativePieces() {
        val game = GameSession(board, events, seats = 4, seed = 771, humanIndices = setOf(0))
        val saved = game.snapshot()
        saved.players[0].position = "FAKE_SPACE"
        saved.players[0].traversed.add("FAKE_SPACE")
        saved.players[1].shield = true

        val actual = game.snapshot()
        assertEquals(board.start, actual.players[0].position)
        assertEquals(listOf(board.start), actual.players[0].traversed)
        assertFalse(actual.players[1].shield)
        assertEquals(0, actual.revision)
    }

    @Test fun rejectedWrongPhaseActionsDoNotChangeCurrentGame() {
        val game = GameSession(board, events, seats = 3, seed = 251, humanIndices = setOf(0))
        val before = game.snapshot()

        try {
            game.chooseRoute("NOT_A_ROUTE")
            fail("Allowed a route before rolling")
        } catch (_: IllegalArgumentException) {
            // Phase ROUTE was never entered.
        }
        try {
            game.chooseTarget(1)
            fail("Allowed a prank target without a pending prank")
        } catch (_: IllegalArgumentException) {
            // Phase TARGET was never entered.
        }
        try {
            game.botStep()
            fail("Bot was allowed to move during the human turn")
        } catch (_: IllegalArgumentException) {
            // Bot actions cannot take a human turn.
        }
        assertEquals(before, game.snapshot())
    }

    @Test fun finalWinnerAndStateRemainLockedAfterRace() {
        for (seats in 2..4) {
            val game = GameSession(board, events, seats, seed = 197 + seats,
                humanIndices = emptySet())
            var actions = 0
            while (game.phase != Phase.FINISHED && actions++ < 1500) game.botStep()
            assertEquals(Phase.FINISHED, game.phase)
            val finished = game.snapshot()
            assertEquals(board.finish, finished.players[finished.winnerIndex!!].position)

            try {
                game.roll()
                fail("Accepted a roll after the match ended")
            } catch (_: IllegalArgumentException) { }
            try {
                game.chooseRoute(board.start)
                fail("Accepted a route after the match ended")
            } catch (_: IllegalArgumentException) { }
            try {
                game.chooseTarget(0)
                fail("Accepted a target after the match ended")
            } catch (_: IllegalArgumentException) { }
            try {
                game.botStep()
                fail("Accepted a bot action after the match ended")
            } catch (_: IllegalArgumentException) { }

            assertEquals("Terminal snapshot mutated for $seats seats", finished, game.snapshot())
        }
    }

    @Test fun passAndPlayHasDistinctHumanSeatsAndFullTurnFlow() {
        for (seats in 2..4) {
            val game = GameSession(board, events, seats = seats, seed = 2026 + seats,
                humanIndices = (0 until seats).toSet())
            assertEquals((1..seats).map { "Player $it" }, game.players.map { it.name })
            assertTrue(game.players.none { it.isBot })
            val seenTurns = mutableSetOf<Int>()
            var actions = 0
            while (game.phase != Phase.FINISHED && actions++ < 1500) {
                seenTurns += game.currentIndex
                when (game.phase) {
                    Phase.ROLL -> game.roll()
                    Phase.ROUTE -> game.chooseRoute(game.availableRoutes().first().id)
                    Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                    Phase.ENCOUNTER -> error("Encounter phase belongs only to Lab v3")
                    Phase.FINISHED -> Unit
                }
            }
            assertEquals("Pass & Play did not finish with $seats seats",
                Phase.FINISHED, game.phase)
            assertEquals((0 until seats).toSet(), seenTurns)
            assertEquals(board.finish, game.players[game.winnerIndex!!].position)
        }
    }

    @Test fun soloSeatRemainsYouWithBots() {
        for (seats in 2..4) {
            val game = GameSession(board, events, seats = seats, seed = 40 + seats,
                humanIndices = setOf(0))
            assertEquals("You", game.players.first().name)
            assertFalse(game.players.first().isBot)
            assertTrue(game.players.drop(1).all { it.isBot })
            assertEquals(listOf("You") + (1 until seats).map { "AI $it" },
                game.players.map { it.name })
        }
    }

}

