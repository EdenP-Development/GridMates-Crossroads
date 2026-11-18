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
