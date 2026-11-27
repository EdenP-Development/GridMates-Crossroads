package com.playereden.crossroads.core

import java.security.MessageDigest
import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * SAVE-16: source actions, not a mutable snapshot of the rules engine's PRNG.
 *
 * A checkpoint is reconstructed by replaying every committed action from the
 * original seed, then comparing the entire authoritative snapshot. This keeps
 * PRNG state and bot choices correct without Android or reflection.
 *
 * This is intentionally a versioned local-only format. Updates that change
 * the rules or bundled board/events must explicitly opt into migration; older
 * incompatible checkpoints are rejected rather than resumed with changed odds.
 */
@Serializable
enum class MatchActionKind { ROLL, ROUTE, TARGET, BOT, STEADY_ROLL, BOLD_ROLL, ENCOUNTER }

@Serializable
data class MatchAction(
    val kind: MatchActionKind,
    val routeId: String? = null,
    val targetIndex: Int? = null
)

/** Stable serialized identifier; never change the behavior of an existing policy. */
@Serializable
enum class BotPolicyVersion { LEGACY_V1, ADAPTIVE_V2_R1, ADAPTIVE_V2_R2, ADAPTIVE_V2_R3, ADAPTIVE_V2_R4, GAMBLER_V1, GAMBLER_V2, GUARDIAN_V1, STRATEGIST_V1 }

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MatchCheckpoint(
    val formatVersion: Int,
    val matchId: String,
    val startedAtEpochMillis: Long,
    val boardId: String,
    val contentFingerprint: String,
    val seed: Int,
    val seats: Int,
    val humanIndices: List<Int>,
    val actions: List<MatchAction>,
    val expected: Snapshot,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val botPolicy: BotPolicyVersion = BotPolicyVersion.LEGACY_V1,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val botSeatPolicies: Map<Int, BotPolicyVersion> = emptyMap()
)

object MatchCheckpointCodec {
    const val FORMAT_VERSION = 1
    const val MAX_ACTIONS = 2_000
    const val MAX_CHECKPOINT_CHARS = 350_000

    private val codec = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
    }

    fun encode(checkpoint: MatchCheckpoint): String =
        codec.encodeToString(checkpoint)

    fun decode(payload: String): MatchCheckpoint {
        require(payload.isNotBlank() && payload.length <= MAX_CHECKPOINT_CHARS) {
            "Invalid checkpoint payload size"
        }
        return codec.decodeFromString<MatchCheckpoint>(payload)
    }

    fun fingerprint(board: BoardDefinition, catalogue: EventCatalogue): String {
        val definitions = codec.encodeToString(board) + "\n" + codec.encodeToString(catalogue)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(definitions.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}
