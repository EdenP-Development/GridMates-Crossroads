package com.playereden.crossroads.core

/** Frozen R3 heuristic: visible options and movement only, no hidden draws. */
internal object AdaptiveBotEncounterR3 {
    fun choose(options: List<String>, race: BotRaceAwareness, remaining: Int,
               steady: Boolean, pushBonus: Int): String {
        val own = race.own
        val gap = race.gapToLeader ?: 0
        if ("supplies" in options && "cover" in options) {
            // Cover gives one extra step; supplies gives two.
            // Prefer the extra movement when it can reach the finish only with supplies.
            return if (own.shield || race.finalRoundPending ||
                own.distance == remaining + 2 || gap >= 3) "supplies" else "cover"
        }
        if ("gamble" in options && "brace" in options) {
            // Bracing ends movement. Steady can block the hazard retreat;
            // Push may give a visible success bonus, but does not block retreat.
            return if (steady || own.shield || race.finalRoundPending ||
                gap >= 3 || (pushBonus > 0 && gap > 0) ||
                (remaining >= own.distance && own.distance > 0)) "gamble" else "brace"
        }
        return options.first()
    }
}
