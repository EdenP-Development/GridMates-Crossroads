package com.playereden.crossroads.core

/** Unregistered tactical personality candidate. No access to hidden outcomes. */
internal object StrategistPersonalityV1 {
    fun shouldPush(race: BotRaceAwareness): Boolean {
        val gap = race.gapToLeader ?: 0
        val own = race.own.distance
        if (race.finalRoundPending) return own > 4 || gap >= 0
        if (own <= 4) return false
        val imminentOpponent = race.opponents.any { it.distance in 1..4 }
        return if (imminentOpponent) gap > 0
        else AdaptiveBotTravelR1.shouldPush(race)
    }

    fun routeScore(base: Double, horizon: RouteHorizon,
                   race: BotRaceAwareness, encounterAvailable: Boolean): Double {
        // Only an encounter-free corridor can guarantee reaching its endpoint.
        if (encounterAvailable) return base
        if (horizon.stop == RouteHorizonStop.FINISH) {
            return base - if (race.finalRoundPending) 6.0 else 3.0
        }
        // Favor useful choices over a forced corridor when otherwise close.
        if (horizon.stop == RouteHorizonStop.FORK &&
            horizon.nextForkRisks.distinct().size >= 2) return base - 0.75
        return base
    }

    fun chooseTarget(candidates: List<BotTargetView>): Int? {
        val nearest = candidates.minOfOrNull { it.distance } ?: return null
        fun score(t: BotTargetView): Double {
            val setback = (t.afterDistance - t.distance).coerceAtLeast(0)
            val effective = if (t.shield) 0.5 else setback.toDouble()
            // Crossing the four-space steady-finish threshold can deny a
            // reliable finish opportunity; stripping a shield cannot do so.
            val deniedFinish = !t.shield && t.distance in 1..4 && t.afterDistance > 4
            return effective - (t.distance - nearest) * 0.5 +
                if (deniedFinish) 4.0 else 0.0
        }
        return candidates.sortedWith(compareByDescending<BotTargetView> { score(it) }
            .thenBy { it.distance }.thenBy { it.seat }).first().seat
    }
}
