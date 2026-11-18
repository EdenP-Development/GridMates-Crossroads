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
    val y: Float,
    val route: String = "neutral",
    val eventPool: String = "all"
)
@Serializable data class ForkChoice(
    val to: String,
    val label: String,
    val risk: String,
    val description: String = ""
)
@Serializable data class ForkDefinition(
    val id: String,
    val title: String = "Crossroads",
    val choices: List<ForkChoice> = emptyList()
)
@Serializable data class BoardDefinition(
    val id: String,
    val start: String,
    val finish: String,
    val nodes: List<BoardNode>,
    val forks: List<ForkDefinition> = emptyList()
) {
    val index: Map<String, BoardNode> by lazy { nodes.associateBy { it.id } }
    fun node(id: String): BoardNode = requireNotNull(index[id]) { "Unknown board node $id" }
    fun forkAt(id: String): ForkDefinition? = forks.firstOrNull { it.id == id }
}
@Serializable data class EventEffect(val type: String, val spaces: Int)
@Serializable data class EventDefinition(
    val id: String,
    val regions: List<String>,
    val weight: Int,
    val effect: EventEffect,
    val pools: List<String> = listOf("all"),
    val description: String = ""
)
@Serializable data class EventCatalogue(val events: List<EventDefinition>)
object GameData {
    private val json = Json { ignoreUnknownKeys = true }
    fun board(source: String): BoardDefinition = json.decodeFromString<BoardDefinition>(source)
    fun events(source: String): EventCatalogue = json.decodeFromString<EventCatalogue>(source)
}

/** A short, display-ready memory of an actual encountered event. */
@Serializable
data class EncounterNote(
    val turn: Int,
    val actor: String,
    val title: String,
    val outcome: String,
    val kind: String
)
@Serializable
enum class Phase { ROLL, ROUTE, TARGET, FINISHED, ENCOUNTER }
@Serializable
data class Player(
    val name: String,
    val isBot: Boolean,
    var position: String,
    val traversed: MutableList<String>,
    var shield: Boolean = false
)
@Serializable
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
    val lastEventId: String?,
    val encounters: List<EncounterNote>
)
