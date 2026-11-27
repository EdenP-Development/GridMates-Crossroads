package com.playereden.crossroads.core

/** Exact movement-only probabilities for visible travel choices.
 * Does not account for encounters, finish tie-breaks, or opponent turns.
 * Never reads the game RNG. Candidate analysis helper, not a policy.
 */
internal object BotTravelOddsV1 {
    private val pushOutcomes = listOf(1, 1, 5, 6, 7, 8)
    fun steadyFinishOutcomes(distance: Int): Int {
        require(distance > 0)
        return if (distance <= 4) 6 else 0
    }
    fun pushFinishOutcomes(distance: Int): Int {
        require(distance > 0)
        return pushOutcomes.count { it >= distance }
    }
    fun pushMovementTotal(): Int = pushOutcomes.sum()
    fun pushExpectedExcessNumerator(distance: Int): Int {
        require(distance > 0)
        return pushOutcomes.sumOf { (it - distance).coerceAtLeast(0) }
    }
}
