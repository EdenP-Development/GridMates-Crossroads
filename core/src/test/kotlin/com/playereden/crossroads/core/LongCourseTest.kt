package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** A new long course: rules and old board fingerprints are unchanged. */
class LongCourseTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val long = GameData.board(
        File(root, "data/boards/woodland_cedarwood_long.json").readText())
    private val classic = GameData.board(
        File(root, "data/boards/woodland_woven_trail.json").readText())
    private val quick = GameData.board(
        File(root, "data/boards/woodland_brookside_quick.json").readText())
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText())
    private val started = 1794931200000L

    private fun distances(board: BoardDefinition, id: String = board.start,
                          depth: Int = 0): List<Int> {
        require(depth < 125) { "Loop or unexpectedly deep route at $id" }
        if (id == board.finish) return listOf(depth)
        return board.node(id).next.flatMap { next -> distances(board, next, depth + 1) }
    }

    @Test fun cedarwoodHasLongDistinctFiniteRoutes() {
        assertNotEquals(quick.id, long.id)
        assertNotEquals(classic.id, long.id)
        assertEquals(101, long.nodes.size)
        assertEquals(6, long.forks.size)
        assertEquals(2, long.forks.count { it.choices.size == 3 })
        assertTrue(long.nodes.all { it.x in 0f..1f && it.y in 0f..1f })
        assertEquals(long.nodes.size, long.nodes.map { it.id }.toSet().size)
        assertTrue(long.nodes.all { it.id == long.finish || it.next.isNotEmpty() })
        long.nodes.flatMap { it.next }.forEach { assertTrue(long.index.containsKey(it)) }
        val all = distances(long)
        assertEquals(144, all.size)
        assertEquals(54, all.minOrNull())
        assertEquals(66, all.maxOrNull())
        assertTrue(all.minOrNull()!! > distances(classic).maxOrNull()!!)
        assertTrue(all.minOrNull()!! > distances(quick).maxOrNull()!!)
        assertTrue(all.distinct().size > 4)
    }

    @Test fun noDecorativeCrossingLooksLikeAFakeLegalJunction() {
        val edges = long.nodes.flatMap { from ->
            from.next.map { id -> from to long.node(id) }
        }
        fun side(a: BoardNode, b: BoardNode, c: BoardNode): Float =
            (b.x - a.x) * (c.y - a.y) -
                (b.y - a.y) * (c.x - a.x)
        val fakeCrossings = mutableListOf<String>()
        for (i in edges.indices) for (j in i + 1 until edges.size) {
            val (a, b) = edges[i]
            val (c, d) = edges[j]
            if (a.id == c.id || a.id == d.id || b.id == c.id || b.id == d.id) continue
            // A proper line-segment intersection without a graph node is a
            // misleading visual fork: users should never be asked to guess.
            val p = side(a, b, c)
            val q = side(a, b, d)
            val r = side(c, d, a)
            val t = side(c, d, b)
            if (p * q < -0.00000001f && r * t < -0.00000001f) {
                fakeCrossings += "${a.id}->${b.id} crosses ${c.id}->${d.id}"
            }
        }
        assertTrue(fakeCrossings.joinToString("; "), fakeCrossings.isEmpty())
    }

    @Test fun everyBranchHasAnHonestRiskAndPlayEnabledEncounterPool() {
        val risks = mapOf("risky" to "high", "safe" to "low", "mischief" to "medium")
        long.forks.forEach { fork ->
            val paths = long.node(fork.id).next
            assertEquals(paths.toSet(), fork.choices.map { it.to }.toSet())
            assertTrue(fork.choices.all { it.label.isNotBlank() && it.description.isNotBlank() })
            fork.choices.forEach { choice ->
                assertEquals(risks[long.node(choice.to).route], choice.risk)
            }
        }
        val eventsOnCourse = long.nodes.filter { it.kind == "event" }
        assertTrue(eventsOnCourse.size >= 20)
        eventsOnCourse.forEach { node ->
            val choices = events.events.filter {
                node.region in it.regions && node.eventPool in it.pools && it.weight > 0
            }
            assertTrue("Empty Long event pool at ${node.id}", choices.size >= 2)
        }
    }

    @Test fun twoToFourSeatLongMatchesFinishAndRestoreExactly() {
        for (seats in 2..4) for (seed in 1..55) {
            val game = GameSession(long, events, seats = seats, seed = seed,
                humanIndices = emptySet())
            var actions = 0
            while (game.phase != Phase.FINISHED && actions++ < 2400) game.botStep()
            assertEquals("Long race stalled: $seats seats seed $seed", Phase.FINISHED, game.phase)
            assertEquals(long.finish, game.players[requireNotNull(game.winnerIndex)].position)
            if (seed <= 6) {
                val checkpoint = game.checkpoint("cedarwood-$seats-$seed", started)
                assertEquals(long.id, checkpoint.boardId)
                assertEquals(game.snapshot(),
                    GameSession.restore(long, events, checkpoint).snapshot())
                for (wrongBoard in listOf(classic, quick)) {
                    try {
                        GameSession.restore(wrongBoard, events, checkpoint)
                        fail("Long match incorrectly restored on ${wrongBoard.id}")
                    } catch (_: IllegalArgumentException) { }
                }
            }
        }
    }
}
