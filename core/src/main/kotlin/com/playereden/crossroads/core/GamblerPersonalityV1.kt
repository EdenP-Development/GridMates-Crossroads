package com.playereden.crossroads.core

/** Candidate personality, not yet registered as an executable bot policy.
 * Decisions depend only on visible race state and available legal actions.
 */
internal object GamblerPersonalityV1 {
    fun shouldPush(race: BotRaceAwareness): Boolean {
        val gap = race.gapToLeader ?: 0
        if (race.finalRoundPending) return true
        // A reliable finish is preferable to a needless stumble.
        if (race.own.distance <= 4 && gap <= 0) return false
        // When a steady roll cannot finish, exploit the potential extra movement.
        if (race.stage == BotRaceStage.ENDGAME) return gap >= 0 || race.own.distance > 4
        // Protect a substantial lead, otherwise seek upside.
        return gap >= -2
    }

    fun chooseEncounter(options: List<String>, race: BotRaceAwareness, remaining: Int,
                        steady: Boolean, pushBonus: Int): String {
        require(options.isNotEmpty())
        require(remaining >= 0 && pushBonus >= 0)
        val gap = race.gapToLeader ?: 0
        if ("supplies" in options && "cover" in options) {
            return if (!race.finalRoundPending && !race.own.shield &&
                gap <= -3 && race.own.distance > remaining + 2) "cover" else "supplies"
        }
        if ("gamble" in options && "brace" in options) {
            // Steady protects against a retreat; a visible push bonus adds upside.
            return if (steady || pushBonus > 0 || race.finalRoundPending ||
                gap >= -2 || remaining >= race.own.distance) "gamble" else "brace"
        }
        return options.first()
    }
}
