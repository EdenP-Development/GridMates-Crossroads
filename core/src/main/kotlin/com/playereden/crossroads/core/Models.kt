package com.playereden.crossroads.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * These are design fixtures, not locked production balance.
 * The Graph and Events remain independent of Android/UI.
 */
@Serializable data class BoardNode(
    val id: String,
    val region: String,
    val kind: String,
    val next: List<String> = emptyList(),
    val x: Float,
    val y: Float
)
@Serializable data class BoardDefinition(
    val id: String,
    val start: String,
    val finish: String,
    val nodes: List<BoardNode>
) {
    val index: Map<String, BoardNode> by lazy { nodes.associateBy { it.id } }
    fun node(id: String): BoardNode = requireNotNull(index[id]) { "Unknown board node $id" }
}
@Serializable data class EventEffect(val type: String, val spaces: Int)
@Serializable data class EventDefinition(
    val id: String,
    val regions: List<String>,
    val weight: Int,
    val effect: EventEffect
)
@Serializable data class EventCatalogue(val events: List<EventDefinition>)
object GameData {
    private val json = Json { ignoreUnknownKeys = true }
    fun board(source: String): BoardDefinition = json.decodeFromString<BoardDefinition>(source)
    fun events(source: String): EventCatalogue = json.decodeFromString<EventCatalogue>(source)
}

enum class Phase { ROLL, ROUTE, TARGET, FINISHED }
data class Player(
    val name: String,
    val isBot: Boolean,
    var position: String,
    val traversed: MutableList<String>,
    var shield: Boolean = false
)
data class Snapshot(
    val revision: Int,
    val phase: Phase,
    val playerIndex: Int,
    val players: List<Player>,
    val remainingSteps: Int,
    val die: Int?,
    val lastMessage: String,
    val winnerIndex: Int?,
    val turn: Int,
    val lastEventId: String?
)
