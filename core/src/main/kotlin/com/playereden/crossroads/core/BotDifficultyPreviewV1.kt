package com.playereden.crossroads.core

/** Offline QA selection only. Experimental IDs remain replayable, but are
 * not automatically eligible for a new match. No public UI wiring yet.
 */
object BotDifficultyPreviewV1 {
    fun available(personality: BotPersonalityV1): List<BotDifficultyV1> =
        if (personality == BotPersonalityV1.GUARDIAN)
            listOf(BotDifficultyV1.STANDARD, BotDifficultyV1.ADVANCED)
        else listOf(BotDifficultyV1.STANDARD)

    fun resolve(configuration: BotOpponentConfigurationV1): BotPolicyVersion {
        require(configuration.difficulty in available(configuration.personality)) {
            "Difficulty is not eligible for offline preview"
        }
        return when(configuration.difficulty) {
            BotDifficultyV1.STANDARD -> configuration.personality.policy
            BotDifficultyV1.ADVANCED -> BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1
            BotDifficultyV1.RELAXED -> error("Relaxed preview not available")
        }
    }

    /** Null represents a human; retain actual seat numbers. */
    fun resolveLineup(seats: List<BotOpponentConfigurationV1?>): Map<Int,BotPolicyVersion> {
        require(seats.size in 2..4)
        return seats.mapIndexedNotNull { seat, configuration ->
            configuration?.let { seat to resolve(it) }
        }.toMap()
    }
}
