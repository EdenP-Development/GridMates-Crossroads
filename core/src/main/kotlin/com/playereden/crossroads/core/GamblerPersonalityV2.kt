package com.playereden.crossroads.core

/** Frozen G2 encounter refinement; G1 remains immutable. */
internal object GamblerPersonalityV2 {
    fun shouldPush(race: BotRaceAwareness): Boolean =
        GamblerPersonalityV1.shouldPush(race)

    fun chooseEncounter(options: List<String>, race: BotRaceAwareness,
                        remaining: Int, steady: Boolean, pushBonus: Int): String {
        require(options.isNotEmpty())
        require(remaining >= 0 && pushBonus >= 0)
        val gap = race.gapToLeader ?: 0
        // Bracing costs the remaining move. Prefer it only when that cost is
        // small, the racer has a cushion, and no steady protection exists.
        if ("brace" in options && "gamble" in options &&
            !race.finalRoundPending && !steady && !race.own.shield &&
            gap <= -2 && remaining <= 1 && race.own.distance > remaining) {
            return "brace"
        }
        return GamblerPersonalityV1.chooseEncounter(options,race,remaining,steady,pushBonus)
    }
}
