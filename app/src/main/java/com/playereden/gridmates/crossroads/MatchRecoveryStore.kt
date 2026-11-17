package com.playereden.gridmates.crossroads

import android.content.SharedPreferences
import com.playereden.crossroads.core.BoardDefinition
import com.playereden.crossroads.core.EventCatalogue
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
    private val catalogue: EventCatalogue
) {
    companion object {
        private const val ACTIVE_KEY = "active_match_checkpoint_v1"
        private const val LAST_COMPLETED_ID = "last_completed_match_id_v1"
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
            val game = GameSession.restore(board, catalogue, checkpoint)
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
        }
        return editor.commit()
    }

    /** Only intentional Leave/Main menu actions discard the saved race. */
    fun discard(): Boolean = prefs.edit().remove(ACTIVE_KEY).commit()
}
