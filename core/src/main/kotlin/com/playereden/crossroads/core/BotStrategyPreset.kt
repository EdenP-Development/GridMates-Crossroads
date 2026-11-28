package com.playereden.crossroads.core

/**
 * Experimental strategy selection, not a calibrated difficulty scale.
 * Existing policies are immutable; selection happens before a match starts.
 * Persist the resolved seat policies, never reinterpret saves through presets.
 */
enum class BotStrategyPreset(val policy: BotPolicyVersion) {
    CLASSIC(BotPolicyVersion.LEGACY_V1),
    TRAVELER(BotPolicyVersion.ADAPTIVE_V2_R1),
    SCOUT(BotPolicyVersion.ADAPTIVE_V2_R2),
    GUARDIAN(BotPolicyVersion.ADAPTIVE_V2_R3),
    STRATEGIST(BotPolicyVersion.ADAPTIVE_V2_R4),
    GAMBLER(BotPolicyVersion.GAMBLER_V1)
}

object BotStrategyLineup {
    /** Null represents a human. No randomness, no implicit difficulty claims. */
    fun resolve(seats: List<BotStrategyPreset?>): Map<Int,BotPolicyVersion> {
        require(seats.size in 2..4)
        return seats.mapIndexedNotNull { index, preset ->
            preset?.let { index to it.policy }
        }.toMap()
    }
}
