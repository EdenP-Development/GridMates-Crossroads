package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Quick is a separate authored graph, not faster animation or altered dice odds. */
class QuickCourseTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val quick = GameData.board(File(root, "data/boards/woodland_brookside_quick.json").readText())
    private val classic = GameData.board(File(root, "data/boards/woodland_woven_trail.json").readText())
    private val events = GameData.events(File(root, "data/events/woven_events_v02.json").readText())
    private val started = 1794931200000L

    private fun pathLengths(board: BoardDefinition, id: String = board.start): List<Int> {
        if (id == board.finish) return listOf(0)
        return board.node(id).next.flatMap { next -> pathLengths(board, next).map { it + 1 } }
    }

    @Test fun brooksideIsClearlyShorterButStillOffersRealRouteChoices() {
        assertNotEquals(classic.id, quick.id)
        assertEquals("START", quick.start)
        assertEquals("FINISH", quick.finish)
        assertEquals(24, quick.nodes.size)
        assertEquals(2, quick.forks.size)
        assertEquals(3, quick.forkAt("F2")!!.choices.size)
        val classicLengths = pathLengths(classic)
        val quickLengths = pathLengths(quick)
        assertTrue(quickLengths.isNotEmpty())
        assertTrue(quickLengths.maxOrNull()!! < classicLengths.maxOrNull()!!)
        assertTrue(quickLengths.minOrNull()!! < classicLengths.minOrNull()!!)
        assertTrue(quickLengths.toSet().size > 1)
        assertTrue(quick.nodes.all { it.x in 0.0f..1.0f && it.y in 0.0f..1.0f })
        assertEquals(quick.nodes.size, quick.nodes.map { it.id }.toSet().size)
        assertTrue(quick.nodes.all { it.id == quick.finish || it.next.isNotEmpty() })
        quick.nodes.flatMap { it.next }.forEach { assertTrue(quick.index.containsKey(it)) }
    }

    @Test fun quickRoutesHaveHonestRiskAndPlayableRegionalEvents() {
        val allowedRoutes = mapOf("risky" to "high", "safe" to "low", "mischief" to "medium")
        quick.forks.forEach { fork ->
            assertEquals(quick.node(fork.id).next.toSet(), fork.choices.map { it.to }.toSet())
            assertTrue(fork.choices.all { it.label.isNotBlank() && it.description.isNotBlank() })
            fork.choices.forEach { choice ->
                assertEquals(allowedRoutes[quick.node(choice.to).route], choice.risk)
            }
        }
        quick.nodes.filter { it.kind == "event" }.forEach { node ->
            val eligible = events.events.filter {
                node.region in it.regions && node.eventPool in it.pools && it.weight > 0
            }
            assertTrue("Event pool empty at ${node.id}", eligible.size >= 2)
        }
    }

    @Test fun allQuickRaceConfigurationsFinishAndReplayWithoutChangingRules() {
        for (seats in 2..4) {
            for (seed in 1..65) {
                val game = GameSession(quick, events, seats = seats, seed = seed, humanIndices = emptySet())
                var actions = 0
                while (game.phase != Phase.FINISHED && actions++ < 1500) game.botStep()
                assertEquals("Quick softlock seed=$seed seats=$seats", Phase.FINISHED, game.phase)
                assertEquals(quick.finish, game.players[requireNotNull(game.winnerIndex)].position)
                if (seed <= 6) {
                    val checkpoint = game.checkpoint("quick-$seats-$seed", started)
                    assertEquals(quick.id, checkpoint.boardId)
                    assertEquals(game.snapshot(), GameSession.restore(quick, events, checkpoint).snapshot())
                    try {
                        GameSession.restore(classic, events, checkpoint)
                        fail("Quick checkpoint restored using Classic board")
                    } catch (_: IllegalArgumentException) { }
                }
            }
        }
    }
}
