package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.BotDifficultyPreviewV1
import com.playereden.crossroads.core.BotDifficultyV1
import com.playereden.crossroads.core.BotOpponentConfigurationV1
import com.playereden.crossroads.core.BotPersonalityV1
import com.playereden.crossroads.core.BotPolicyVersion
import com.playereden.crossroads.core.DecisionLab

/** New offline match setup only. Never consult this when restoring a match. */
internal object OfflineOpponentPreview {
    fun enabled(debug: Boolean, passAndPlay: Boolean, effectiveBoardId: String): Boolean =
        debug && !passAndPlay && effectiveBoardId.endsWith(DecisionLab.V5_SUFFIX)

    fun policies(debug: Boolean, passAndPlay: Boolean, effectiveBoardId: String,
                 seats: Int, choices: Map<Int, BotOpponentConfigurationV1>): Map<Int, BotPolicyVersion> {
        require(seats in 2..4)
        if (!enabled(debug, passAndPlay, effectiveBoardId)) return emptyMap()
        return BotDifficultyPreviewV1.resolveLineup(List(seats) { seat ->
            if (seat == 0) null else choices[seat]
        })
    }

    /** Compact summary of active bot seats; never advertise inactive choices. */
    fun summary(debug: Boolean, passAndPlay: Boolean, effectiveBoardId: String,
                seats: Int, choices: Map<Int, BotOpponentConfigurationV1>): String? {
        val active = policies(debug, passAndPlay, effectiveBoardId, seats, choices)
        if (active.isEmpty() || active.values.all { it == BotPolicyVersion.LEGACY_V1 })
            return null
        return (1 until seats).joinToString(" · ", prefix = "AI: ") { seat ->
            val choice = choices[seat]
            if (choice == null) "Classic" else label(choice)
        }
    }

    /** Display only; retain numeric seat identity even for duplicate personalities. */
    fun liveName(original: String, seat: Int, policy: BotPolicyVersion?): String {
        val personality = when(policy) {
            BotPolicyVersion.LEGACY_V1 -> "Classic"
            BotPolicyVersion.ADAPTIVE_V2_R1 -> "Traveler"
            BotPolicyVersion.ADAPTIVE_V2_R2 -> "Scout"
            BotPolicyVersion.GAMBLER_V1 -> "Gambler"
            BotPolicyVersion.GUARDIAN_V1 -> "Guardian"
            BotPolicyVersion.STRATEGIST_V1 -> "Strategist"
            BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1 -> "Guardian+"
            else -> return original
        }
        return "${seat + 1} · $personality"
    }

    fun label(configuration: BotOpponentConfigurationV1): String {
        val name = when(configuration.personality) {
            BotPersonalityV1.CLASSIC -> "Classic"
            BotPersonalityV1.TRAVELER -> "Traveler"
            BotPersonalityV1.SCOUT -> "Scout"
            BotPersonalityV1.GAMBLER -> "Gambler"
            BotPersonalityV1.GUARDIAN -> "Guardian"
            BotPersonalityV1.STRATEGIST -> "Strategist"
        }
        return if(configuration.difficulty == BotDifficultyV1.ADVANCED)
            "$name · Advanced preview" else name
    }
}
