package com.playereden.crossroads.core

/** Experimental travel policy R1. Pure, no access to hidden draws or RNG. */
internal object AdaptiveBotTravelR1 {
    fun shouldPush(view: BotRaceAwareness): Boolean {
        val gap = view.gapToLeader ?: 0
        // A final equal-turn chance can reward excess movement even if tied.
        if (view.finalRoundPending) return true
        // Short remaining distance makes a steady roll a reasonable safe option
        // when not trailing; this is a heuristic, not a probability optimum.
        if (view.own.distance <= 3 && gap <= 0) return false
        if (view.stage == BotRaceStage.OPENING) return gap >= 3
        if (view.stage == BotRaceStage.ENDGAME) return gap > 0
        return gap >= 2 || (gap == 1 && view.own.shield)
    }
}
