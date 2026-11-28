package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class BotDifficultyPreviewV1Test {
    @Test fun previewEligibilityIsExplicitAndExhaustive() {
        var accepted=0
        for(personality in BotPersonalityV1.entries)
            for(difficulty in BotDifficultyV1.entries) {
                val config=BotOpponentConfigurationV1(personality,difficulty)
                val expected=when {
                    difficulty==BotDifficultyV1.STANDARD -> personality.policy
                    personality==BotPersonalityV1.GUARDIAN &&
                        difficulty==BotDifficultyV1.ADVANCED ->
                        BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1
                    else -> null
                }
                assertEquals(expected!=null,difficulty in BotDifficultyPreviewV1.available(personality))
                if(expected!=null) {
                    assertEquals(expected,BotDifficultyPreviewV1.resolve(config))
                    accepted++
                } else {
                    try { BotDifficultyPreviewV1.resolve(config); fail("Unsupported preview accepted") }
                    catch(expected:IllegalArgumentException) { }
                }
            }
        assertEquals(7,accepted)
    }

    @Test fun mixedLineupPreservesHumanSeatsAndResolvedIds() {
        val advanced=BotOpponentConfigurationV1(BotPersonalityV1.GUARDIAN,BotDifficultyV1.ADVANCED)
        val scout=BotOpponentConfigurationV1(BotPersonalityV1.SCOUT)
        assertEquals(mapOf(1 to BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1,
            3 to BotPolicyVersion.ADAPTIVE_V2_R2),
            BotDifficultyPreviewV1.resolveLineup(listOf(null,advanced,null,scout)))
        for(size in listOf(0,1,5)) {
            try { BotDifficultyPreviewV1.resolveLineup(List(size){null}); fail("Invalid seats accepted") }
            catch(expected:IllegalArgumentException) { }
        }
    }

    @Test fun experimentalScoutIdsRemainResolvableOnlyThroughExperimentContract() {
        val config=BotOpponentConfigurationV1(BotPersonalityV1.SCOUT,BotDifficultyV1.RELAXED)
        assertEquals(BotPolicyVersion.SCOUT_RELAXED_V1,BotOpponentPolicyV1.resolve(config))
        try { BotDifficultyPreviewV1.resolveLineup(listOf(null,config)); fail("Experimental tier leaked") }
        catch(expected:IllegalArgumentException) { }
    }
}
