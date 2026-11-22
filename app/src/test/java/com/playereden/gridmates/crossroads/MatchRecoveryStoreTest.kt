package com.playereden.gridmates.crossroads

import android.content.SharedPreferences
import com.playereden.crossroads.core.*
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class MatchRecoveryStoreTest {
    private val dataRoot = listOf(File("data"), File("../data"))
        .first { File(it, "boards/woodland_woven_trail.json").isFile }
    private val board = GameData.board(
        File(dataRoot, "boards/woodland_woven_trail.json").readText()
    )
    private val events = GameData.events(
        File(dataRoot, "events/woven_events_v02.json").readText()
    )
    private val quick = GameData.board(
        File(dataRoot, "boards/woodland_brookside_quick.json").readText()
    )
    private val cedarwood = GameData.board(
        File(dataRoot, "boards/woodland_cedarwood_long.json").readText()
    )

    private fun finishMatch(game: GameSession, onAction: () -> Unit) {
        var actions = 0
        while (game.phase != Phase.FINISHED && actions++ < 1500) {
            if (game.players[game.currentIndex].isBot) {
                game.botStep()
            } else {
                when (game.phase) {
                    Phase.ROLL -> game.roll()
                    Phase.ROUTE -> game.chooseRoute(game.availableRoutes().first().id)
                    Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                    Phase.ENCOUNTER -> error("Older rules must not enter Lab v3 encounters")
                    Phase.FINISHED -> Unit
                }
            }
            onAction()
        }
        assertEquals(Phase.FINISHED, game.phase)
    }

    @Test fun completedMatchTimeAndStatsSurviveReopenWithoutDoubleCounting() {
        for (passAndPlay in listOf(false, true)) {
            val prefs = MemoryPreferences()
            val store = MatchRecoveryStore(prefs, board, events)
            val humans = if (passAndPlay) setOf(0, 1, 2) else setOf(0)
            val match = GameSession(board, events, seats = 3, seed = 79,
                humanIndices = humans)
            val id = if (passAndPlay) "local-test" else "solo-test"
            val beganAt = System.currentTimeMillis() - 600_000L
            assertTrue(store.persist(match, id, beganAt))
            finishMatch(match) { assertTrue(store.persist(match, id, beganAt)) }

            val recordedFinish = prefs.getLong("last_completed_at_epoch_millis_v1", 0L)
            assertTrue(recordedFinish >= beganAt)
            val duration = store.completedElapsedSeconds(id, beganAt)
            assertEquals((recordedFinish - beganAt) / 1000L, duration)
            val expectedWins = if (!passAndPlay && match.winnerIndex == 0) 1 else 0
            assertEquals(1, prefs.getInt("completed", 0))
            assertEquals(expectedWins, prefs.getInt("wins", 0))

            // Repeated render and an Activity recreation cannot add stats
            // or replace the original completion timestamp.
            val restarted = MatchRecoveryStore(prefs, board, events)
            val restored = requireNotNull(restarted.load().match)
            assertEquals(match.snapshot(), restored.game.snapshot())
            repeat(3) {
                assertTrue(restarted.persist(restored.game, id, beganAt))
                assertEquals(duration, restarted.completedElapsedSeconds(id, beganAt))
            }
            assertEquals(recordedFinish,
                prefs.getLong("last_completed_at_epoch_millis_v1", 0L))
            assertEquals(1, prefs.getInt("completed", 0))
            assertEquals(expectedWins, prefs.getInt("wins", 0))
        }
    }

    @Test fun newCourseDoesNotInvalidateOldClassicSaveAndQuickCanResume() {
        val prefs = MemoryPreferences()
        val legacy = MatchRecoveryStore(prefs, board, events)
        val classicMatch = GameSession(board, events, seats = 3, seed = 151)
        val started = System.currentTimeMillis() - 12_000L
        classicMatch.roll()
        assertTrue(legacy.persist(classicMatch, "existing-classic-save", started))

        val both = MatchRecoveryStore(prefs, board, events,
            mapOf(board.id to board, quick.id to quick))
        val existing = requireNotNull(both.load().match)
        assertEquals("existing-classic-save", existing.matchId)
        assertEquals(classicMatch.snapshot(), existing.game.snapshot())
        assertEquals(board.id, existing.game.board.id)

        // A deliberate new race replaces the single active slot, not the
        // saved match counter or the signed QA installation identity.
        val quickMatch = GameSession(quick, events, seats = 2, seed = 77,
            humanIndices = setOf(0, 1))
        quickMatch.roll()
        assertTrue(both.persist(quickMatch, "new-quick-save", started + 1))
        val resumed = requireNotNull(
            MatchRecoveryStore(prefs, board, events,
                mapOf(board.id to board, quick.id to quick)).load().match
        )
        assertEquals(quick.id, resumed.game.board.id)
        assertEquals(quickMatch.snapshot(), resumed.game.snapshot())
        assertEquals(0, prefs.getInt("completed", 0))
        assertEquals(0, prefs.getInt("wins", 0))
    }

    @Test fun missingCourseNeverSilentlyOverwritesAnExistingCheckpoint() {
        val prefs = MemoryPreferences()
        val both = MatchRecoveryStore(prefs, board, events,
            mapOf(board.id to board, quick.id to quick))
        val quickMatch = GameSession(quick, events, seats = 2, seed = 12)
        val started = System.currentTimeMillis() - 1000L
        assertTrue(both.persist(quickMatch, "unknown-on-old-app", started))
        val olderApp = MatchRecoveryStore(prefs, board, events)
        assertTrue(olderApp.load().invalidSavedMatch)
        assertFalse(olderApp.load().match != null)
        assertEquals(quickMatch.snapshot(),
            requireNotNull(both.load().match).game.snapshot())
    }

    @Test fun manualBackupCanMoveClassicOrQuickSaveToASeparateApp() {
        for (course in listOf(board, quick)) {
            val originalPrefs = MemoryPreferences()
            val original = MatchRecoveryStore(originalPrefs, board, events,
                mapOf(board.id to board, quick.id to quick))
            val game = GameSession(course, events, seats = 3, seed = 37,
                humanIndices = setOf(0))
            game.roll()
            val started = System.currentTimeMillis() - 1000L
            assertTrue(original.persist(game, "portable-match", started))
            originalPrefs.edit().putInt("completed", 42).putInt("wins", 7)
                .putString("last_completed_match_id_v1", "old-completed")
                .putLong("last_completed_at_epoch_millis_v1", started - 3000).commit()
            val backup = original.exportProgress()

            // Different SharedPreferences instance represents the other package sandbox.
            val targetPrefs = MemoryPreferences()
            val newApp = MatchRecoveryStore(targetPrefs, board, events,
                mapOf(board.id to board, quick.id to quick))
            val inspected = newApp.previewImport(backup)
            assertEquals(42, inspected.bundle.completed)
            assertEquals(7, inspected.bundle.wins)
            assertTrue(newApp.importProgress(inspected))
            assertEquals(42, targetPrefs.getInt("completed", 0))
            assertEquals(7, targetPrefs.getInt("wins", 0))
            assertEquals("old-completed",
                targetPrefs.getString("last_completed_match_id_v1", null))
            val recovered = requireNotNull(newApp.load().match)
            assertEquals(course.id, recovered.game.board.id)
            assertEquals(game.snapshot(), recovered.game.snapshot())
            assertEquals(42, originalPrefs.getInt("completed", 0))
            assertEquals(7, originalPrefs.getInt("wins", 0))
        }
    }

    @Test fun invalidBackupDoesNotMutateExistingPrefsAndRecordsOnlyClearsSavedMatch() {
        val prefs = MemoryPreferences()
        val store = MatchRecoveryStore(prefs, board, events,
            mapOf(board.id to board, quick.id to quick))
        val game = GameSession(quick, events, seats = 2, seed = 59)
        val started = System.currentTimeMillis() - 1000
        assertTrue(store.persist(game, "current-progress", started))
        prefs.edit().putInt("completed", 5).putInt("wins", 1).commit()
        val activeBefore = prefs.getString("active_match_checkpoint_v1", null)

        try {
            store.previewImport("{\"completed\":999,\"wins\":1000}")
            fail("Accepted malformed backup")
        } catch (_: RuntimeException) { }
        assertEquals(activeBefore, prefs.getString("active_match_checkpoint_v1", null))
        assertEquals(5, prefs.getInt("completed", 0))
        assertEquals(1, prefs.getInt("wins", 0))

        val recordsOnly = CrossroadsProgressBundle(completed = 9, wins = 3)
        val verified = store.previewImport(CrossroadsProgressCodec.encode(recordsOnly))
        assertTrue(store.importProgress(verified))
        assertNull(prefs.getString("active_match_checkpoint_v1", null))
        assertEquals(9, prefs.getInt("completed", 0))
        assertEquals(3, prefs.getInt("wins", 0))
    }

    @Test fun longRaceResumeAndBackupKeepThePreviousCoursesAvailable() {
        val boards = mapOf(board.id to board, quick.id to quick, cedarwood.id to cedarwood)
        val prefs = MemoryPreferences()
        val store = MatchRecoveryStore(prefs, board, events, boards)
        val race = GameSession(cedarwood, events, seats = 4, seed = 202,
            humanIndices = setOf(0))
        race.roll()
        val start = System.currentTimeMillis() - 4000L
        assertTrue(store.persist(race, "cedarwood-race", start))
        val afterUpdate = MatchRecoveryStore(prefs, board, events, boards)
        assertEquals(race.snapshot(), requireNotNull(afterUpdate.load().match).game.snapshot())

        val portable = afterUpdate.exportProgress()
        val newPrefs = MemoryPreferences()
        val publicCopy = MatchRecoveryStore(newPrefs, board, events, boards)
        assertTrue(publicCopy.importProgress(publicCopy.previewImport(portable)))
        val moved = requireNotNull(publicCopy.load().match)
        assertEquals(cedarwood.id, moved.game.board.id)
        assertEquals(race.snapshot(), moved.game.snapshot())
        assertEquals(0, newPrefs.getInt("wins", 0))
        assertEquals(0, newPrefs.getInt("completed", 0))

        // The earlier app sees an unknown course but never erases its bytes.
        val oldStore = MatchRecoveryStore(prefs, board, events,
            mapOf(board.id to board, quick.id to quick))
        assertTrue(oldStore.load().invalidSavedMatch)
        assertEquals(race.snapshot(),
            requireNotNull(afterUpdate.load().match).game.snapshot())
    }

    @Test fun decisionLabReplayBackupAndNoOfficialStatContamination() {
        val lab = DecisionLab.variant(quick)
        val boards = mapOf(board.id to board, quick.id to quick, lab.id to lab)
        val prefs = MemoryPreferences()
        prefs.edit().putInt("completed", 8).putInt("wins", 6).commit()
        val storage = MatchRecoveryStore(prefs, board, events, boards)
        val start = System.currentTimeMillis() - 45_000L
        val game = GameSession(lab, events, seats = 2, seed = 2727,
            humanIndices = setOf(0))
        assertTrue(storage.persist(game, "lab-playtest", start))
        var actions = 0
        var resumeObserved = false
        while (game.phase != Phase.FINISHED && actions++ < 800) {
            if (game.players[game.currentIndex].isBot) game.botStep()
            else when (game.phase) {
                Phase.ROLL -> if (actions % 2 == 0) game.rollSteady() else game.rollBold()
                Phase.ROUTE -> game.chooseRoute(game.availableRoutes().first().id)
                Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                Phase.ENCOUNTER -> error("Older rules must not enter Lab v3 encounters")
                Phase.FINISHED -> Unit
            }
            assertTrue(storage.persist(game, "lab-playtest", start))
            if (!resumeObserved && actions > 4) {
                val restored = requireNotNull(storage.load().match)
                assertEquals(game.snapshot(), restored.game.snapshot())
                val transfer = storage.exportProgress()
                val imported = MatchRecoveryStore(MemoryPreferences(),
                    board, events, boards)
                assertTrue(imported.importProgress(imported.previewImport(transfer)))
                assertEquals(game.snapshot(),
                    requireNotNull(imported.load().match).game.snapshot())
                resumeObserved = true
            }
        }
        assertTrue(resumeObserved)
        assertEquals(Phase.FINISHED, game.phase)
        assertEquals(8, prefs.getInt("completed", 0))
        assertEquals(6, prefs.getInt("wins", 0))
        assertTrue(storage.completedElapsedSeconds("lab-playtest", start) >= 0L)
        val completed = prefs.getLong("last_completed_at_epoch_millis_v1", 0L)
        assertTrue(completed >= start)
        assertTrue(storage.persist(game, "lab-playtest", start))
        assertEquals(completed, prefs.getLong("last_completed_at_epoch_millis_v1", 0L))
        assertEquals(8, prefs.getInt("completed", 0))

        val older = MatchRecoveryStore(prefs, board, events,
            mapOf(board.id to board, quick.id to quick))
        assertTrue(older.load().invalidSavedMatch)
        assertEquals(game.snapshot(), requireNotNull(storage.load().match).game.snapshot())
    }

    @Test fun qa1312LabV1AndNewLabV2CanRestoreSideBySideWithoutChangingOfficialRecords() {
        val v1 = DecisionLab.variant(quick)
        val v2 = DecisionLab.variantV2(quick)
        val known = mapOf(board.id to board, quick.id to quick,
            v1.id to v1, v2.id to v2)
        val prefs = MemoryPreferences()
        prefs.edit().putInt("completed", 12).putInt("wins", 5).commit()
        val store = MatchRecoveryStore(prefs, board, events, known)
        val start = System.currentTimeMillis() - 15_000L

        val old = GameSession(v1, events, 2, 1337, setOf(0))
        old.rollSteady()
        assertTrue(store.persist(old, "existing-v1", start))
        val legacyLoaded = requireNotNull(store.load().match)
        assertEquals(v1.id, legacyLoaded.game.board.id)
        assertEquals(old.snapshot(), legacyLoaded.game.snapshot())

        val newer = GameSession(v2, events, 2, 1337, setOf(0))
        newer.rollSteady()
        assertTrue(store.persist(newer, "new-v2", start))
        val newLoaded = requireNotNull(store.load().match)
        assertEquals(v2.id, newLoaded.game.board.id)
        assertEquals(newer.snapshot(), newLoaded.game.snapshot())
        val portable = store.exportProgress()
        val imported = MatchRecoveryStore(MemoryPreferences(),
            board, events, known)
        assertTrue(imported.importProgress(imported.previewImport(portable)))
        assertEquals(newer.snapshot(),
            requireNotNull(imported.load().match).game.snapshot())
        assertEquals(12, prefs.getInt("completed", 0))
        assertEquals(5, prefs.getInt("wins", 0))
    }

    @Test fun v3EncounterPendingCheckpointResumesWithoutAFreeChoice() {
        val v1 = DecisionLab.variant(quick)
        val v2 = DecisionLab.variantV2(quick)
        val v3 = DecisionLab.variantV3(quick)
        val registry = mapOf(board.id to board, quick.id to quick,
            v1.id to v1, v2.id to v2, v3.id to v3)
        val prefs = MemoryPreferences()
        prefs.edit().putInt("wins", 7).putInt("completed", 11).commit()
        val store = MatchRecoveryStore(prefs, board, events, registry)
        val game = GameSession(v3, events, 2, 61, setOf(0))
        game.rollSteady()
        assertEquals(Phase.ROUTE, game.phase)
        game.chooseRoute(game.availableRoutes().first { route ->
            quick.forkAt(game.players[0].position)?.choices
                ?.firstOrNull { it.to == route.id }?.risk == "low"
        }.id)
        assertEquals(Phase.ENCOUNTER, game.phase)
        assertTrue(store.persist(game, "pending-v3-encounter",
            System.currentTimeMillis() - 10_000L))
        val loaded = requireNotNull(store.load().match).game
        assertEquals(Phase.ENCOUNTER, loaded.phase)
        assertEquals(game.snapshot(), loaded.snapshot())
        assertEquals(game.availableEncounterChoices(), loaded.availableEncounterChoices())

        val backup = store.exportProgress()
        val another = MatchRecoveryStore(MemoryPreferences(), board, events, registry)
        assertTrue(another.importProgress(another.previewImport(backup)))
        assertEquals(loaded.snapshot(), requireNotNull(another.load().match).game.snapshot())
        loaded.chooseEncounter("cover")
        assertTrue(store.persist(loaded, "pending-v3-encounter",
            System.currentTimeMillis() - 10_000L))
        assertEquals(7, prefs.getInt("wins", 0))
        assertEquals(11, prefs.getInt("completed", 0))
        val stored = requireNotNull(store.load().match).game
        assertEquals(loaded.snapshot(), stored.snapshot())
        assertTrue(stored.availableEncounterChoices().isEmpty())
    }

    @Test fun firstCompletedMatchMayBeAnExperimentalLabRaceAndStillExport() {
        val labs = listOf(
            DecisionLab.variant(quick), DecisionLab.variantV2(quick),
            DecisionLab.variantV3(quick), DecisionLab.variantV4(quick),
            DecisionLab.variantV5(quick)
        )
        val known = (listOf(board, quick, cedarwood) + labs).associateBy { it.id }
        for (lab in labs) {
            val prefs = MemoryPreferences()
            val store = MatchRecoveryStore(prefs, board, events, known)
            val started = System.currentTimeMillis() - 60_000L
            val match = GameSession(lab, events, 2, seed = 62,
                humanIndices = emptySet())
            var actions = 0
            while (match.phase != Phase.FINISHED && actions++ < 1500) {
                match.botStep()
            }
            assertEquals("Never finished ${lab.id}", Phase.FINISHED, match.phase)
            assertTrue(store.persist(match, "lab-only-${lab.id}", started))
            assertEquals(0, prefs.getInt("completed", 0))
            assertEquals(0, prefs.getInt("wins", 0))

            val backup = store.exportProgress()
            val freshPrefs = MemoryPreferences()
            val fresh = MatchRecoveryStore(freshPrefs, board, events, known)
            val preview = fresh.previewImport(backup)
            assertEquals(0, preview.bundle.completed)
            assertEquals(0, preview.bundle.wins)
            assertTrue(fresh.importProgress(preview))
            val restored = requireNotNull(fresh.load().match).game
            assertEquals(match.snapshot(), restored.snapshot())
            assertEquals(match.fairFinishReceipt, restored.fairFinishReceipt)
            val timestamp = freshPrefs.getLong("last_completed_at_epoch_millis_v1", 0L)
            assertTrue(timestamp >= started)
            repeat(3) {
                assertTrue(fresh.persist(restored, "lab-only-${lab.id}", started))
                assertEquals(0, freshPrefs.getInt("completed", 0))
                assertEquals(0, freshPrefs.getInt("wins", 0))
                assertEquals(timestamp,
                    freshPrefs.getLong("last_completed_at_epoch_millis_v1", 0L))
            }
        }
    }

    @Test fun fairFinishPendingAndCompletedTransferPreservesReceiptAndZeroStats() {
        // Three links to FINISH: Steady leaves exactly one unused step.
        val base = BoardDefinition(
            "save78-finish", "START", "FINISH", listOf(
                BoardNode("START", "meadow", "start", listOf("A"), 0f, 0f),
                BoardNode("A", "meadow", "plain", listOf("B"), .2f, .1f),
                BoardNode("B", "meadow", "plain", listOf("FINISH"), .4f, .1f),
                BoardNode("FINISH", "meadow", "finish", emptyList(), .6f, .1f)
            )
        )
        val v4 = DecisionLab.variantV4(base)
        val v5 = DecisionLab.variantV5(base)
        val known = mapOf(board.id to board, v4.id to v4, v5.id to v5)
        val initial = MemoryPreferences()
        val oldApp = MatchRecoveryStore(initial, board, events, known)
        val start = System.currentTimeMillis() - 15_000L
        val game = GameSession(v5, events, 2, seed = 13,
            humanIndices = setOf(0, 1))
        game.rollSteady()
        assertEquals(Phase.ROLL, game.phase)
        assertTrue(game.finalRoundPending)
        assertNull(game.fairFinishReceipt)
        assertTrue(oldApp.persist(game, "pending-final-round", start))
        val waiting = requireNotNull(oldApp.load().match).game
        assertTrue(waiting.finalRoundPending)
        assertEquals(game.snapshot(), waiting.snapshot())
        assertNull(waiting.fairFinishReceipt)

        val newPrefs = MemoryPreferences()
        val newApp = MatchRecoveryStore(newPrefs, board, events, known)
        assertTrue(newApp.importProgress(newApp.previewImport(oldApp.exportProgress())))
        val resumed = requireNotNull(newApp.load().match).game
        assertTrue(resumed.finalRoundPending)
        assertEquals(game.snapshot(), resumed.snapshot())

        resumed.rollSteady() // Tie: first arrival, seat 0, wins.
        assertEquals(Phase.FINISHED, resumed.phase)
        assertEquals(0, resumed.winnerIndex)
        assertEquals(2, resumed.fairFinishReceipt?.finishers)
        assertTrue(resumed.fairFinishReceipt?.tiedOnWinningSteps == true)
        assertTrue(newApp.persist(resumed, "pending-final-round", start))
        assertEquals(0, newPrefs.getInt("completed", 0))
        assertEquals(0, newPrefs.getInt("wins", 0))
        val receipt = requireNotNull(resumed.fairFinishReceipt)
        val recordedFinish = newPrefs.getLong("last_completed_at_epoch_millis_v1", 0L)
        val archived = newApp.exportProgress()
        val another = MatchRecoveryStore(MemoryPreferences(), board, events, known)
        assertTrue(another.importProgress(another.previewImport(archived)))
        val reopened = requireNotNull(another.load().match).game
        assertEquals(resumed.snapshot(), reopened.snapshot())
        assertEquals(receipt, reopened.fairFinishReceipt)
        repeat(3) {
            assertTrue(newApp.persist(resumed, "pending-final-round", start))
            assertEquals(recordedFinish,
                newPrefs.getLong("last_completed_at_epoch_millis_v1", 0L))
            assertEquals(0, newPrefs.getInt("completed", 0))
        }
        // Lab v4's established first-to-finish behavior is untouched.
        val prior = GameSession(v4, events, 2, seed = 13, humanIndices = setOf(0, 1))
        prior.rollSteady()
        assertEquals(Phase.FINISHED, prior.phase)
        assertNull(prior.fairFinishReceipt)
        assertEquals(prior.snapshot(),
            GameSession.restore(v4, events,
                MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                    prior.checkpoint("existing-v4", start)))).snapshot())
    }

    @Test fun olderFinishedCheckpointGetsOneTimeElapsedFallback() {
        val prefs = MemoryPreferences()
        val store = MatchRecoveryStore(prefs, board, events)
        val game = GameSession(board, events, seats = 2, seed = 1337,
            humanIndices = emptySet())
        val id = "old-qa-save"
        val start = System.currentTimeMillis() - 100_000L
        finishMatch(game) { }
        assertTrue(store.persist(game, id, start))
        // Simulate QA1138: it had the finished match ID but no finish timestamp.
        prefs.edit().remove("last_completed_at_epoch_millis_v1").commit()
        val frozen = store.completedElapsedSeconds(id, start)
        assertTrue(frozen >= 100L)
        assertTrue(prefs.getLong("last_completed_at_epoch_millis_v1", 0L) > 0L)
        assertEquals(frozen, MatchRecoveryStore(prefs, board, events)
            .completedElapsedSeconds(id, start))
    }
}

/** Tiny in-memory SharedPreferences implementation for deterministic JVM tests. */
private class MemoryPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any>()
    override fun getAll(): MutableMap<String, *> = data.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? =
        data[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        (data[key] as? Set<*>)?.filterIsInstance<String>()?.toMutableSet() ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = data[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = data[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = data[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        data[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = data.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) = Unit
    override fun edit(): SharedPreferences.Editor = Edit()

    private inner class Edit : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clearAll = false
        override fun putString(key: String?, value: String?): SharedPreferences.Editor =
            apply { if (key != null) pending[key] = value }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor =
            apply { if (key != null) pending[key] = values?.toSet() }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor =
            apply { if (key != null) pending[key] = value }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor =
            apply { if (key != null) pending[key] = value }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor =
            apply { if (key != null) pending[key] = value }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor =
            apply { if (key != null) pending[key] = value }
        override fun remove(key: String?): SharedPreferences.Editor =
            apply { if (key != null) pending[key] = null }
        override fun clear(): SharedPreferences.Editor =
            apply { clearAll = true }
        override fun commit(): Boolean {
            if (clearAll) data.clear()
            pending.forEach { (key, value) ->
                if (value == null) data.remove(key) else data[key] = value
            }
            return true
        }
        override fun apply() { commit() }
    }
}
