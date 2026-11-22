package com.playereden.crossroads.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * Explicit, user-controlled local backup for Android QA -> production transfers.
 * Not a secret, signed entitlement, cloud save, multiplayer state, or automatic sync.
 * Both apps must include compatible authored boards/events for saved-match replay.
 */
@Serializable
data class CrossroadsProgressBundle(
    val kind: String = "gridmates-crossroads-progress",
    val version: Int = 1,
    val completed: Int,
    val wins: Int,
    val activeMatch: MatchCheckpoint? = null,
    val lastCompletedMatchId: String? = null,
    val lastFinishedAtEpochMillis: Long = 0L
)

data class VerifiedCrossroadsProgress(
    val bundle: CrossroadsProgressBundle,
    val restoredMatch: GameSession?
)

object CrossroadsProgressCodec {
    const val MAX_DOCUMENT_CHARS = 500_000
    const val MAX_DOCUMENT_BYTES = 524_288
    private const val MAX_RECORD_COUNT = 1_000_000
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = false }

    fun encode(bundle: CrossroadsProgressBundle): String {
        require(bundle.kind == "gridmates-crossroads-progress" && bundle.version == 1)
        require(bundle.completed in 0..MAX_RECORD_COUNT &&
            bundle.wins in 0..bundle.completed)
        return json.encodeToString(bundle).also {
            require(it.length <= MAX_DOCUMENT_CHARS && it.toByteArray(Charsets.UTF_8).size <= MAX_DOCUMENT_BYTES)
        }
    }

    /**
     * Validate *everything* before calling the Android atomic preference commit.
     * Replays the complete seed and action log against the original board.
     */
    fun verify(
        payload: String,
        boards: Map<String, BoardDefinition>,
        events: EventCatalogue
    ): VerifiedCrossroadsProgress {
        require(payload.isNotBlank() && payload.length <= MAX_DOCUMENT_CHARS)
        require(payload.toByteArray(Charsets.UTF_8).size <= MAX_DOCUMENT_BYTES)
        // Default Kotlin serialization values are convenient for writing but
        // not enough to distinguish a real backup from arbitrary JSON.
        val envelope = json.parseToJsonElement(payload).jsonObject
        require("kind" in envelope && "version" in envelope) {
            "Missing Crossroads backup identifier"
        }
        val value = json.decodeFromString<CrossroadsProgressBundle>(payload)
        require(value.kind == "gridmates-crossroads-progress" && value.version == 1) {
            "Not a supported Crossroads backup"
        }
        require(value.completed in 0..MAX_RECORD_COUNT &&
            value.wins in 0..value.completed) { "Invalid local record" }
        require(value.lastCompletedMatchId == null ||
            value.lastCompletedMatchId.isNotBlank() &&
            value.lastCompletedMatchId.length <= 100)
        require(value.lastFinishedAtEpochMillis >= 0)
        if (value.lastCompletedMatchId == null) {
            require(value.lastFinishedAtEpochMillis == 0L)
        }
        val game = value.activeMatch?.let { checkpoint ->
            val board = requireNotNull(boards[checkpoint.boardId]) {
                "This backup uses a course unavailable in this version"
            }
            val restored = GameSession.restore(board, events, checkpoint)
            if (restored.phase == Phase.FINISHED) {
                // Without the matching completion sentinel, reopening a finished
                // race could incorrectly count a second win/match.
                require(value.lastCompletedMatchId == checkpoint.matchId &&
                    value.lastFinishedAtEpochMillis >= checkpoint.startedAtEpochMillis) {
                    "Finished match receipt is missing or inconsistent"
                }
                // Experimental Lab races are deliberately excluded from official
                // completion/win totals. Their finished checkpoint + matching
                // receipt are still valid even for a brand-new profile with zero
                // official races. Original-rules finishes still require a count.
                if (!restored.playtestRules) require(value.completed > 0)
            }
            restored
        }
        return VerifiedCrossroadsProgress(value, game)
    }
}
