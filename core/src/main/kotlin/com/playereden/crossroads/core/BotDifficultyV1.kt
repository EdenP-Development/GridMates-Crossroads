package com.playereden.crossroads.core

/** Proposed independent decision-sophistication axis, not calibrated strength. */
enum class BotDifficultyV1 { RELAXED, STANDARD, ADVANCED }

data class BotOpponentConfigurationV1(
    val personality: BotPersonalityV1,
    val difficulty: BotDifficultyV1 = BotDifficultyV1.STANDARD
)

/** Candidate route adjustment. Lower scores are preferable.
 * Standard is an exact identity transformation. No RNG or hidden state.
 * All adjustments are bounded; they cannot change the legal action set.
 */
internal object BotDifficultyRouteV1 {
    fun score(difficulty: BotDifficultyV1, personality: BotPersonalityV1,
              personalityScore: Double, distance: Int, horizon: RouteHorizon,
              encounterAvailable: Boolean): Double {
        require(personalityScore.isFinite() && distance >= 0)
        return when(difficulty) {
            BotDifficultyV1.STANDARD -> personalityScore
            BotDifficultyV1.RELAXED -> {
                // Weaken the distance heuristic while retaining personality's
                // relative risk/route preferences. Never penalize legal finish.
                if (!encounterAvailable && horizon.stop == RouteHorizonStop.FINISH)
                    personalityScore
                else personalityScore - distance * 0.25
            }
            BotDifficultyV1.ADVANCED -> {
                if (encounterAvailable) personalityScore
                else personalityScore - when(horizon.stop) {
                    RouteHorizonStop.FINISH -> 4.0
                    RouteHorizonStop.FORK -> if (horizon.nextForkRisks.distinct().size >= 2)
                        if (personality == BotPersonalityV1.STRATEGIST) 1.0 else 0.5
                        else 0.0
                    else -> 0.0
                }
            }
        }
    }
}

/** Candidate target adjustment, preserving the original personality score.
 * No access to future random draws; shield stripping is not a setback.
 */
internal object BotDifficultyTargetV1 {
    fun score(difficulty: BotDifficultyV1, personalityScore: Double,
              target: BotTargetView): Double {
        require(personalityScore.isFinite())
        val setback = if (target.shield) 0 else
            (target.afterDistance - target.distance).coerceAtLeast(0)
        return when(difficulty) {
            BotDifficultyV1.STANDARD -> personalityScore
            BotDifficultyV1.RELAXED -> personalityScore - setback * 0.5
            BotDifficultyV1.ADVANCED -> personalityScore +
                if (!target.shield && target.distance in 1..4 && target.afterDistance > 4)
                    2.0 else 0.0
        }
    }
}

/** Only implemented combinations resolve; unsupported tiers must not silently
 * fall back to Standard or be exposed as functioning difficulty options. */
object BotOpponentPolicyV1 {
    fun resolve(configuration: BotOpponentConfigurationV1): BotPolicyVersion {
        if (configuration.difficulty == BotDifficultyV1.STANDARD)
            return configuration.personality.policy
        require(configuration.personality == BotPersonalityV1.SCOUT) {
            "Difficulty variant not implemented for this personality"
        }
        return when (configuration.difficulty) {
            BotDifficultyV1.RELAXED -> BotPolicyVersion.SCOUT_RELAXED_V1
            BotDifficultyV1.ADVANCED -> BotPolicyVersion.SCOUT_ADVANCED_V1
            BotDifficultyV1.STANDARD -> configuration.personality.policy
        }
    }
}
