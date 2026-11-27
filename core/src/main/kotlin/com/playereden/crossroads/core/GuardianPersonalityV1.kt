package com.playereden.crossroads.core

/** Unregistered defensive personality candidate. Visible state only. */
internal object GuardianPersonalityV1 {
    fun shouldPush(race: BotRaceAwareness): Boolean {
        val gap = race.gapToLeader ?: 0
        if (race.own.distance <= 4) return false
        if (race.finalRoundPending) return true
        return if (race.stage == BotRaceStage.ENDGAME) gap >= 2 else gap >= 5
    }

    fun chooseEncounter(options: List<String>, race: BotRaceAwareness,
                        remaining: Int, steady: Boolean): String {
        require(options.isNotEmpty())
        require(remaining >= 0)
        val gap = race.gapToLeader ?: 0
        if ("supplies" in options && "cover" in options) {
            return if (race.own.shield || race.finalRoundPending ||
                race.own.distance == remaining + 2 || gap >= 5) "supplies" else "cover"
        }
        if ("gamble" in options && "brace" in options) {
            // Shield protects against mischief, not hazard retreat.
            // Steady footing, unlike shield, can absorb the hazard setback.
            return if (steady || race.finalRoundPending || gap >= 5 ||
                remaining >= race.own.distance) "gamble" else "brace"
        }
        return options.first()
    }

    fun routeScore(baseScore: Double, risk: String?, race: BotRaceAwareness,
                   encounterAvailable: Boolean): Double {
        if (!encounterAvailable || race.finalRoundPending ||
            (race.gapToLeader ?: 0) >= 5) return baseScore
        return baseScore + when(risk) {
            "high" -> 1.5
            "low" -> -1.0
            else -> 0.0
        }
    }
}
