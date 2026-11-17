package com.playereden.crossroads.core

/**
 * UI timing only: a pace setting never changes dice, route choices,
 * encounter weights, winners, or game state.
 */
enum class PresentationSpeed(
    val stepMillis: Int,
    val landingMillis: Int,
    val encounterMillis: Int,
    val aiBreathMillis: Long,
    val dieFrameMillis: Long,
    val dieFrames: Int
) {
    COZY(205, 220, 480, 180L, 45L, 10),
    BRISK(95, 80, 110, 90L, 25L, 4)
}

/** Shared timing math keeps the animated board and turn gating in sync. */
object PresentationPacing {
    fun stepsMoved(before: Snapshot, after: Snapshot): Int =
        before.players.indices.maxOfOrNull { index ->
            MoveTrail.steps(before.players[index].traversed, after.players[index].traversed).size
        } ?: 0

    fun waitForMovement(before: Snapshot, after: Snapshot, speed: PresentationSpeed): Long {
        val count = stepsMoved(before, after)
        val newEncounter = before.encounters.lastOrNull() != after.encounters.lastOrNull()
        val movement = count.toLong() * speed.stepMillis
        val landing = if (count > 0) speed.landingMillis.toLong() else 0L
        val reveal = if (newEncounter) speed.encounterMillis.toLong() else 0L
        return movement + landing + reveal
    }
}
