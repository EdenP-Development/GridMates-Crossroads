package com.playereden.crossroads.core

/** R2 route heuristic, frozen for replay. Lower score is preferred. */
internal object AdaptiveBotRouteR2 {
    fun score(distance: Int, risk: String?, horizon: RouteHorizon,
              race: BotRaceAwareness, encounterAvailable: Boolean,
              steadyShelterBonus: Boolean): Double {
        val leading = race.standing == BotRaceStanding.LEADING
        val riskCost = if (!encounterAvailable) 0.0 else when (risk) {
            "high" -> if (leading) 2.1 else 0.55
            "medium" -> if (leading) 0.8 else 0.35
            "low" -> (if (leading) -1.6 else -0.35) -
                (if (steadyShelterBonus) 1.0 else 0.0)
            else -> 0.0
        }
        // Geometry is not a guarantee when a route encounter can alter movement.
        val finishBonus = if (horizon.stop == RouteHorizonStop.FINISH &&
            !encounterAvailable) 3.0 else 0.0
        val choiceBonus = if (horizon.stop == RouteHorizonStop.FORK &&
            !encounterAvailable && "low" in horizon.nextForkRisks) 0.25 else 0.0
        return distance + riskCost - finishBonus - choiceBonus
    }
}
