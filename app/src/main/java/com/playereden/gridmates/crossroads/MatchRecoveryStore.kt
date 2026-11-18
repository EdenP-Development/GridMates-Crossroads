package com.playereden.gridmates.crossroads

import android.content.SharedPreferences
import com.playereden.crossroads.core.BoardDefinition
import com.playereden.crossroads.core.EventCatalogue
import com.playereden.crossroads.core.CrossroadsProgressBundle
import com.playereden.crossroads.core.CrossroadsProgressCodec
import com.playereden.crossroads.core.VerifiedCrossroadsProgress
import com.playereden.crossroads.core.GameSession
import com.playereden.crossroads.core.MatchCheckpointCodec
import com.playereden.crossroads.core.Phase

/**
 * One local in-progress match, in the app's existing QA-only preference file.
 * A single synchronous SharedPreferences transaction checkpoints each accepted
 * authoritative action, and—when finishing—counts the completed match exactly
 * once. No account, network or outside-app access is added.
 *
 * This is deliberately a small QA persistence layer, not cloud sync or a
 * guarantee against device storage failure, clearing data, or corrupt saves.
 */
internal class MatchRecoveryStore(
    private val prefs: SharedPreferences,
    private val board: BoardDefinition,
    private val catalogue: EventCatalogue,
    // Old QA checkpoints have always carried boardId. Resolve by ID before
    // restoring so enabling a new course never invalidates an old Woven Trail save.
    private val knownBoards: Map<String, BoardDefinition> = mapOf(board.id to board)
) {
    companion object {
        private const val ACTIVE_KEY = "active_match_checkpoint_v1"
        private const val LAST_COMPLETED_ID = "last_completed_match_id_v1"
        private const val LAST_FINISHED_AT = "last_completed_at_epoch_millis_v1"
    }

    data class Restored(
        val game: GameSession,
        val matchId: String,
        val startedAtEpochMillis: Long
    )

    data class LoadResult(val match: Restored?, val invalidSavedMatch: Boolean)

    fun load(): LoadResult {
        val payload = prefs.getString(ACTIVE_KEY, null) ?: return LoadResult(null, false)
        return try {
            val checkpoint = MatchCheckpointCodec.decode(payload)
            val savedBoard = requireNotNull(knownBoards[checkpoint.boardId]) {
                "Saved course isn't bundled with this version"
            }
            val game = GameSession.restore(savedBoard, catalogue, checkpoint)
            LoadResult(
                Restored(game, checkpoint.matchId, checkpoint.startedAtEpochMillis),
                invalidSavedMatch = false
            )
        } catch (_: RuntimeException) {
            // Never create a half-restored session, and never silently erase a
            // rejected checkpoint. A new game explicitly replaces the old data.
            LoadResult(null, invalidSavedMatch = true)
        }
    }

    /**
     * Save the latest complete rules action before the UI advances its revision.
     * If this action ended the match, update stats in the SAME atomic preference
     * commit as the final checkpoint; restoring a finished game cannot double count.
     */
    fun persist(game: GameSession, matchId: String, startedAtEpochMillis: Long): Boolean {
        val checkpoint = game.checkpoint(matchId, startedAtEpochMillis)
        val payload = MatchCheckpointCodec.encode(checkpoint)
        if (payload.length > MatchCheckpointCodec.MAX_CHECKPOINT_CHARS) return false

        val editor = prefs.edit().putString(ACTIVE_KEY, payload)
        if (game.phase == Phase.FINISHED &&
            prefs.getString(LAST_COMPLETED_ID, null) != matchId
        ) {
            editor.putInt("completed", prefs.getInt("completed", 0) + 1)
            editor.putInt("wins", prefs.getInt("wins", 0) +
                if (game.winnerIndex == 0 && game.players.count { !it.isBot } == 1) 1 else 0)
            editor.putString(LAST_COMPLETED_ID, matchId)
            // Save the actual rules finish moment alongside the finished match
            // and record. Reopening the completed game must not grow its timer.
            editor.putLong(LAST_FINISHED_AT, System.currentTimeMillis())
        }
        return editor.commit()
    }

    /**
     * Frozen elapsed time for the finished race, including time spent paused.
     * Older QA finished saves have no completion timestamp; freeze their elapsed
     * time on the first post-upgrade opening instead of letting it grow forever.
     */
    fun completedElapsedSeconds(matchId: String, startedAtEpochMillis: Long): Long {
        require(startedAtEpochMillis > 0L)
        val lastId = prefs.getString(LAST_COMPLETED_ID, null)
        val savedFinish = if (lastId == matchId)
            prefs.getLong(LAST_FINISHED_AT, 0L) else 0L
        val finishedAt = if (savedFinish >= startedAtEpochMillis) {
            savedFinish
        } else {
            val fallback = System.currentTimeMillis()
            if (lastId == matchId) {
                prefs.edit().putLong(LAST_FINISHED_AT, fallback).commit()
            }
            fallback
        }
        return ((finishedAt - startedAtEpochMillis).coerceAtLeast(0L) / 1000L)
    }

    /**
     * Export the existing record and optional complete offline match. No hidden
     * filesystem path, account, or server is used; Android's file picker owns
     * the destination and the resulting document is plain text JSON.
     */
    fun exportProgress(): String {
        val checkpoint = prefs.getString(ACTIVE_KEY, null)?.let { encoded ->
            val parsed = MatchCheckpointCodec.decode(encoded)
            val chosenBoard = requireNotNull(knownBoards[parsed.boardId]) {
                "Saved course is no longer available"
            }
            GameSession.restore(chosenBoard, catalogue, parsed)
            parsed
        }
        val bundle = CrossroadsProgressBundle(
            completed = prefs.getInt("completed", 0),
            wins = prefs.getInt("wins", 0),
            activeMatch = checkpoint,
            lastCompletedMatchId = prefs.getString(LAST_COMPLETED_ID, null),
            lastFinishedAtEpochMillis = prefs.getLong(LAST_FINISHED_AT, 0L)
        )
        val payload = CrossroadsProgressCodec.encode(bundle)
        // A backup must be restorable with the exact data shipped in this APK.
        CrossroadsProgressCodec.verify(payload, knownBoards, catalogue)
        return payload
    }

    fun previewImport(payload: String): VerifiedCrossroadsProgress =
        CrossroadsProgressCodec.verify(payload, knownBoards, catalogue)

    /**
     * The caller must obtain explicit replace confirmation before invoking.
     * No state is touched unless the *entire* backup passes strict validation.
     * One synchronous editor commit writes the profile and match atomically.
     */
    fun importProgress(verified: VerifiedCrossroadsProgress): Boolean {
        val payload = CrossroadsProgressCodec.encode(verified.bundle)
        val safe = CrossroadsProgressCodec.verify(payload, knownBoards, catalogue).bundle
        val editor = prefs.edit()
            .putInt("completed", safe.completed)
            .putInt("wins", safe.wins)
        val checkpoint = safe.activeMatch
        if (checkpoint == null) {
            editor.remove(ACTIVE_KEY)
        } else {
            editor.putString(ACTIVE_KEY, MatchCheckpointCodec.encode(checkpoint))
        }
        if (safe.lastCompletedMatchId == null) {
            editor.remove(LAST_COMPLETED_ID).remove(LAST_FINISHED_AT)
        } else {
            editor.putString(LAST_COMPLETED_ID, safe.lastCompletedMatchId)
                .putLong(LAST_FINISHED_AT, safe.lastFinishedAtEpochMillis)
        }
        return editor.commit()
    }

    /** Only intentional Leave/Main menu actions discard the saved race. */
    fun discard(): Boolean = prefs.edit().remove(ACTIVE_KEY).commit()
}
