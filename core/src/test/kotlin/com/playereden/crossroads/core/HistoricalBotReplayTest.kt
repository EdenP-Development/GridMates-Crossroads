package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** The same test source runs against the pinned pre-policy engine and current engine. */
class HistoricalBotReplayTest {
    @Test fun compareHistoricalCheckpointsAndFuture() {
        val mode = System.getenv("CROSSROADS_HISTORY_MODE") ?: return
        require(mode == "generate" || mode == "verify")
        val directory = File(requireNotNull(System.getenv("CROSSROADS_HISTORY_DIR")))
        if (mode == "generate") directory.mkdirs()
        require(directory.isDirectory)
        val root = File(System.getProperty("crossroads.repoDir"))
        val events = GameData.events(File(root, "data/events/woven_events_v02.json").readText())
        val originals = listOf("woodland_brookside_quick", "woodland_woven_trail",
            "woodland_cedarwood_long").map {
            GameData.board(File(root, "data/boards/$it.json").readText())
        }
        val boards = originals + originals.map { DecisionLab.variantV5(it) }
        var compared = 0
        for ((index, board) in boards.withIndex()) for (seats in listOf(2,4)) for (seed in listOf(17,83)) {
            val name = "legacy-$index-$seats-$seed"
            val midFile = File(directory, "$name-mid.json")
            val endFile = File(directory, "$name-end.json")
            val game = if (mode == "generate") {
                GameSession(board, events, seats, seed, emptySet()).also { session ->
                    repeat(12) { if (session.phase != Phase.FINISHED) session.botStep() }
                    midFile.writeText(MatchCheckpointCodec.encode(session.checkpoint(name, 1L)))
                }
            } else {
                val original = midFile.readText()
                assertFalse(original.contains("\"botPolicy\""))
                val saved = MatchCheckpointCodec.decode(original)
                GameSession.restore(board, events, saved).also {
                    assertEquals(saved.expected, it.snapshot())
                    assertEquals(original, MatchCheckpointCodec.encode(it.checkpoint(name, 1L)))
                }
            }
            var ticks = 0
            while (game.phase != Phase.FINISHED && ticks++ < 1500) game.botStep()
            assertEquals(Phase.FINISHED, game.phase)
            val final = MatchCheckpointCodec.encode(game.checkpoint(name, 1L))
            if (mode == "generate") endFile.writeText(final)
            else assertEquals("Historical future drift: $name", endFile.readText(), final)
            compared++
        }
        assertEquals(24, compared)
        println("Historical AI replay $mode: $compared races")
    }
}
