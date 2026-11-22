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
    private val long = GameData.board(File(root, "data/boards/woodland_cedarwood_long.json").readText())
    private val events = GameData.events(File(root, "data/events/woven_events_v02.json").readText())
    private val boards = mapOf(classic.id to classic, quick.id to quick, long.id to long)

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

    @Test fun offlineCourseBackupsRoundTripExactly() {
        for (board in listOf(classic, quick, long)) for (seats in 2..4) {
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

    @Test fun finishedLabOnlyProfileCanExportWithoutInventedOfficialWin() {
        val versions = listOf(
            DecisionLab.variant(quick), DecisionLab.variantV2(quick),
            DecisionLab.variantV3(quick), DecisionLab.variantV4(quick),
            DecisionLab.variantV5(quick)
        )
        val known = boards + versions.associateBy { it.id }
        for (lab in versions) {
            val game = GameSession(lab, events, seats = 2, seed = 71,
                humanIndices = emptySet())
            var commands = 0
            while (game.phase != Phase.FINISHED && commands++ < 1500) game.botStep()
            assertEquals("Unfinished ${lab.id}", Phase.FINISHED, game.phase)
            val cp = game.checkpoint("only-lab-${lab.id}", 1794931200000L)
            val backup = CrossroadsProgressBundle(
                completed = 0, wins = 0, activeMatch = cp,
                lastCompletedMatchId = cp.matchId,
                lastFinishedAtEpochMillis = 1794931260000L
            )
            val verified = CrossroadsProgressCodec.verify(
                CrossroadsProgressCodec.encode(backup), known, events)
            assertEquals(0, verified.bundle.completed)
            assertEquals(0, verified.bundle.wins)
            assertEquals(game.snapshot(), verified.restoredMatch!!.snapshot())
            assertEquals(game.fairFinishReceipt, verified.restoredMatch.fairFinishReceipt)
            // An experimental finish still MUST have an idempotent completion receipt.
            assertInvalidFor(backup.copy(lastCompletedMatchId = null,
                lastFinishedAtEpochMillis = 0L), known)
            assertInvalidFor(backup.copy(lastCompletedMatchId = "wrong-finish"),
                known)
        }
    }

    @Test fun normalFinishedGameStillRequiresRecordedOfficialCompletion() {
        val game = GameSession(quick, events, 2, 41, emptySet())
        var actions = 0
        while (game.phase != Phase.FINISHED && actions++ < 1500) game.botStep()
        assertEquals(Phase.FINISHED, game.phase)
        val cp = game.checkpoint("official-finish", 1794931200000L)
        val withoutRecord = CrossroadsProgressBundle(
            completed = 0, wins = 0, activeMatch = cp,
            lastCompletedMatchId = cp.matchId,
            lastFinishedAtEpochMillis = 1794931260000L
        )
        assertInvalidFor(withoutRecord, boards)
        val withRecord = withoutRecord.copy(completed = 1)
        assertEquals(game.snapshot(), CrossroadsProgressCodec.verify(
            CrossroadsProgressCodec.encode(withRecord), boards, events)
            .restoredMatch!!.snapshot())
    }

    private fun assertInvalidFor(
        bundle: CrossroadsProgressBundle, known: Map<String, BoardDefinition>
    ) {
        try {
            CrossroadsProgressCodec.verify(
                CrossroadsProgressCodec.encode(bundle), known, events)
            fail("Accepted invalid finished-match receipt")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun importsRejectBadCountersWrongCourseAndTamperedCheckpoints() {
        assertInvalid(Json.encodeToString(
            CrossroadsProgressBundle(completed = 2, wins = 3)))
        assertInvalid(Json.encodeToString(
            CrossroadsProgressBundle(completed = -1, wins = 0)))
        assertInvalid(Json.encodeToString(
            CrossroadsProgressBundle(completed = 2, wins = 1, version = 99)))
        assertInvalid("{}")
        assertInvalid("{\"completed\":1,\"wins\":0}")
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
