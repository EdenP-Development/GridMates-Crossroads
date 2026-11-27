package com.playereden.crossroads.core

/** Frozen experimental conservative travel candidate. Relaxed strength is a
 * hypothesis, not a guarantee: safer choices may improve win rates.
 * Preserve all existing Guardian endgame and non-travel decisions.
 */
internal object GuardianRelaxedCandidateV1 {
    fun shouldPush(race: BotRaceAwareness): Boolean {
        val standard = GuardianPersonalityV1.shouldPush(race)
        if (!standard || race.finalRoundPending || race.stage == BotRaceStage.ENDGAME)
            return standard
        // More patient in the early/middle race; still react to a large gap.
        return (race.gapToLeader ?: 0) >= 8
    }
}
