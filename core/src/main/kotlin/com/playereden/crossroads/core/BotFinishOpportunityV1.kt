package com.playereden.crossroads.core

/** Conservative movement-only finish assessment. A failed movement outcome
 * landing on an event may still advance: counts are lower bounds, not full
 * probabilities of winning the game. No RNG or event draws.
 */
internal object BotFinishOpportunityV1 {
    fun corridorDistance(board: BoardDefinition, position: String): Int? {
        if (position == board.finish) return null
        var node = board.node(position)
        val seen = mutableSetOf(position)
        for (distance in 1..8) {
            val next = node.next.singleOrNull() ?: return null
            if (!seen.add(next)) return null
            if (next == board.finish) return distance
            node = board.node(next)
        }
        return null
    }

    /** An existing final-round leader wins ties; later finishers must exceed
     * that leader's unused movement. Without a leader, count direct arrivals.
     */
    fun assess(board: BoardDefinition, position: String,
               leadingExcess: Int? = null): FinishOpportunityV1? {
        require(leadingExcess == null || leadingExcess >= 0)
        val distance = corridorDistance(board, position) ?: return null
        // A leader excess >=8 cannot be beaten by either direct travel action.
        val threshold = if (leadingExcess == null) distance
            else distance + leadingExcess.coerceAtMost(8) + 1
        return FinishOpportunityV1(distance,
            BotTravelOddsV1.steadyFinishOutcomes(threshold),
            BotTravelOddsV1.pushFinishOutcomes(threshold))
    }
}

internal data class FinishOpportunityV1(
    val distance: Int,
    val steadySuccesses: Int,
    val pushSuccesses: Int
) {
    /** Null retains the personality choice when direct counts tie.
     * A recommendation is not a guarantee of match victory.
     */
    fun preferPush(): Boolean? = when {
        pushSuccesses > steadySuccesses -> true
        steadySuccesses > pushSuccesses -> false
        else -> null
    }
}
