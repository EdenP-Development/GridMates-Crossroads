package com.playereden.crossroads.core

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Small, versioned household LAN protocol. The host alone rolls dice. */
const val CROSSROADS_LAN_VERSION = 1

@Serializable
enum class LanActionType { ROLL, ROUTE, TARGET }

@Serializable
data class LanAction(
    val type: LanActionType,
    val route: String? = null,
    val target: Int? = null
)

@Serializable
data class LanPacket(
    val type: String,
    val version: Int = CROSSROADS_LAN_VERSION,
    val roomId: String = "",
    val code: String = "",
    val token: String = "",
    val checkpoint: MatchCheckpoint? = null,
    val action: LanAction? = null,
    val message: String = ""
)

object LanWire {
    const val MAX_FRAME_BYTES = 524288
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = false }

    fun read(input: InputStream): LanPacket {
        try {
            val size = DataInputStream(input).readInt()
            if (size !in 1..MAX_FRAME_BYTES) throw IOException("Invalid room message size")
            val bytes = ByteArray(size)
            DataInputStream(input).readFully(bytes)
            return json.decodeFromString<LanPacket>(bytes.toString(Charsets.UTF_8)).also {
                if (it.type.length !in 1..24 || it.roomId.length > 100 ||
                    it.code.length > 12 || it.token.length > 128 ||
                    it.message.length > 300 || it.version != CROSSROADS_LAN_VERSION
                ) throw IOException("Incompatible room message")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Unreadable room message", e)
        }
    }

    fun write(output: OutputStream, packet: LanPacket) {
        val bytes = json.encodeToString(packet).toByteArray(Charsets.UTF_8)
        if (bytes.size !in 1..MAX_FRAME_BYTES) throw IOException("Room message too large")
        DataOutputStream(output).apply { writeInt(bytes.size); write(bytes); flush() }
    }
}

/**
 * Single source of truth: the host may move seat 0, the connected guest seat 1.
 * Rejected actions never consume a die, change a route or mutate the snapshot.
 */
object LanTurnAuthority {
    fun apply(game: GameSession, seat: Int, action: LanAction) {
        require(seat in 0..1 && seat < game.players.size) { "Invalid LAN seat" }
        require(!game.players[seat].isBot && game.currentIndex == seat) {
            "It isn't your turn"
        }
        when (action.type) {
            LanActionType.ROLL -> {
                require(game.phase == Phase.ROLL && action.route == null && action.target == null)
                game.roll()
            }
            LanActionType.ROUTE -> {
                require(game.phase == Phase.ROUTE && action.target == null)
                val route = requireNotNull(action.route)
                require(game.availableRoutes().any { it.id == route }) { "Unavailable route" }
                game.chooseRoute(route)
            }
            LanActionType.TARGET -> {
                require(game.phase == Phase.TARGET && action.route == null)
                val target = requireNotNull(action.target)
                require(target in game.availableTargets()) { "Unavailable target" }
                game.chooseTarget(target)
            }
        }
    }
}
