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
    // Original preview was 450 ms (Cozy) and 100 ms (Brisk), too fast
    // for someone to perceive a satisfying roll on an actual phone.
    COZY(205, 220, 480, 180L, 55L, 11),
    BRISK(95, 80, 110, 90L, 50L, 8);

    /**
     * A die starts briskly and visibly settles rather than flashing every
     * frame at a constant rate. Total preview: Cozy 1375ms, Brisk 680ms.
     * UI timing only; neither value nor delay touches GameSession's PRNG.
     */
    fun dieFrameDelayMillis(frame: Int): Long {
        require(frame in 0 until dieFrames)
        return dieFrameMillis + 2L * frame * frame
    }

    fun diePreviewDurationMillis(): Long =
        (0 until dieFrames).sumOf(::dieFrameDelayMillis)
}

/** Shared timing math keeps the animated board and turn gating in sync. */
object PresentationPacing {
    /** Keep the camera on the person who just moved, even if their turn ended. */
    fun mover(before: Snapshot, after: Snapshot): Int =
        before.players.indices.maxByOrNull { index ->
            MoveTrail.steps(before.players[index].traversed,
                after.players[index].traversed).size
        }?.takeIf { index ->
            before.players[index].traversed != after.players[index].traversed
        } ?: before.playerIndex

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
