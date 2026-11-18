package com.playereden.crossroads.core

import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class CrossroadsProgressCodecTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val classic = GameData.board(File(root, "data/boards/woodland_woven_trail.json").readText())
    private val quick = GameData.board(File(root, "data/boards/woodland_brookside_quick.json").readText())
    private val events = GameData.events(File(root, "data/events/woven_events_v02.json").readText())
    private val boards = mapOf(classic.id to classic, quick.id to quick)

    private fun assertInvalid(raw: String) {
        try {
            CrossroadsProgressCodec.verify(raw, boards, events)
            fail("Untrusted or incompatible import was accepted")
        } catch (_: IllegalArgumentException) {
            // Expected. Decode and checkpoint replay may throw IllegalArgumentException.
        } catch (_: kotlinx.serialization.SerializationException) {
            // Expected for malformed JSON.
        }
    }

    @Test fun offlineQuickAndClassicRaceBackupsRoundTripExactly() {
        for (board in listOf(classic, quick)) for (seats in 2..4) {
            val game = GameSession(board, events, seats, 550 + seats, setOf(0))
            game.roll()
            val cp = game.checkpoint("backup-${board.id}-$seats", 1794931200000L)
            val bundle = CrossroadsProgressBundle(completed = 17, wins = 4, activeMatch = cp,
                lastCompletedMatchId = "previous-finish", lastFinishedAtEpochMillis = 1794000000000L)
            val text = CrossroadsProgressCodec.encode(bundle)
            val imported = CrossroadsProgressCodec.verify(text, boards, events)
            assertEquals(bundle, imported.bundle)
            assertEquals(game.snapshot(), imported.restoredMatch!!.snapshot())
            assertEquals(board.id, imported.restoredMatch.board.id)
        }
    }

    @Test fun recordsOnlyBackupAlsoImportsWithoutCreatingAMatch() {
        val bundle = CrossroadsProgressBundle(completed = 31, wins = 8)
        val restored = CrossroadsProgressCodec.verify(CrossroadsProgressCodec.encode(bundle), boards, events)
        assertEquals(31, restored.bundle.completed)
        assertEquals(8, restored.bundle.wins)
        assertNull(restored.restoredMatch)
    }

    @Test fun finishedMatchKeepsReceiptSoItCannotDoubleCountOnReopen() {
        val game = GameSession(quick, events, seats = 2, seed = 41, humanIndices = emptySet())
        var actions = 0
        while (game.phase != Phase.FINISHED && actions++ < 1500) game.botStep()
        assertEquals(Phase.FINISHED, game.phase)
        val cp = game.checkpoint("finished-one", 1794931200000L)
        val valid = CrossroadsProgressBundle(completed = 6, wins = 2, activeMatch = cp,
            lastCompletedMatchId = cp.matchId, lastFinishedAtEpochMillis = 1794931320000L)
        assertEquals(game.snapshot(), CrossroadsProgressCodec.verify(
            CrossroadsProgressCodec.encode(valid), boards, events).restoredMatch!!.snapshot())
        val missingReceipt = valid.copy(lastCompletedMatchId = null, lastFinishedAtEpochMillis = 0L)
        assertInvalid(CrossroadsProgressCodec.encode(missingReceipt))
        val wrongReceipt = valid.copy(lastCompletedMatchId = "another-game")
        assertInvalid(CrossroadsProgressCodec.encode(wrongReceipt))
    }

    @Test fun importsRejectBadCountersWrongCourseAndTamperedCheckpoints() {
        assertInvalid(Json.encodeToString(
            CrossroadsProgressBundle(completed = 2, wins = 3)))
        assertInvalid(Json.encodeToString(
            CrossroadsProgressBundle(completed = -1, wins = 0)))
        assertInvalid(Json.encodeToString(
            CrossroadsProgressBundle(completed = 2, wins = 1, version = 99)))
        assertInvalid("{}")
        assertInvalid("a".repeat(CrossroadsProgressCodec.MAX_DOCUMENT_CHARS + 1))
        val game = GameSession(classic, events, seats = 2, seed = 22)
        game.roll()
        val cp = game.checkpoint("tampered", 1794931200000L)
        val withCorruptSnapshot = cp.copy(expected = cp.expected.copy(revision = 9999))
        assertInvalid(Json.encodeToString(CrossroadsProgressBundle(
            completed = 1, wins = 0, activeMatch = withCorruptSnapshot)))
        val missingCourse = cp.copy(boardId = "not-a-bundled-course")
        assertInvalid(Json.encodeToString(CrossroadsProgressBundle(
            completed = 1, wins = 0, activeMatch = missingCourse)))
    }
}
