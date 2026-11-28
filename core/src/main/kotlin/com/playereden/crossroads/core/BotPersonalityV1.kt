package com.playereden.crossroads.core

/** Selected initial offline tactical roster; not a difficulty ranking.
 * Historical BotStrategyPreset mappings remain frozen for comparison.
 * Resolve only when creating a new match, never during checkpoint restore.
 */
enum class BotPersonalityV1(val policy: BotPolicyVersion) {
    CLASSIC(BotPolicyVersion.LEGACY_V1),
    TRAVELER(BotPolicyVersion.ADAPTIVE_V2_R1),
    SCOUT(BotPolicyVersion.ADAPTIVE_V2_R2),
    GAMBLER(BotPolicyVersion.GAMBLER_V1),
    GUARDIAN(BotPolicyVersion.GUARDIAN_V1),
    STRATEGIST(BotPolicyVersion.STRATEGIST_V1)
}

object BotPersonalityLineupV1 {
    fun resolve(seats: List<BotPersonalityV1?>): Map<Int,BotPolicyVersion> {
        require(seats.size in 2..4)
        return seats.mapIndexedNotNull { seat, personality ->
            personality?.let { seat to it.policy }
        }.toMap()
    }
}
