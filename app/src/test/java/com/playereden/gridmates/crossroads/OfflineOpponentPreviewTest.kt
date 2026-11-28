package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.*
import org.junit.Assert.*
import org.junit.Test

class OfflineOpponentPreviewTest {
    private val board=CrossroadsCourse.CLASSIC_ID+DecisionLab.V5_SUFFIX
    private val guardian=BotOpponentConfigurationV1(BotPersonalityV1.GUARDIAN,BotDifficultyV1.ADVANCED)
    private val scout=BotOpponentConfigurationV1(BotPersonalityV1.SCOUT)

    @Test fun onlyDebugSoloV5EnablesOverrides() {
        val choices=mapOf(0 to guardian,1 to scout,2 to guardian,3 to scout)
        assertEquals(mapOf(1 to BotPolicyVersion.ADAPTIVE_V2_R2,
            2 to BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1),
            OfflineOpponentPreview.policies(true,false,board,3,choices))
        assertTrue(OfflineOpponentPreview.policies(false,false,board,3,choices).isEmpty())
        assertTrue(OfflineOpponentPreview.policies(true,true,board,3,choices).isEmpty())
        assertTrue(OfflineOpponentPreview.policies(true,false,CrossroadsCourse.CLASSIC_ID,3,choices).isEmpty())
        assertTrue(OfflineOpponentPreview.policies(true,false,
            CrossroadsCourse.CLASSIC_ID+DecisionLab.V4_SUFFIX,3,choices).isEmpty())
        assertTrue(OfflineOpponentPreview.policies(true,false,board,2,emptyMap()).isEmpty())
    }
    @Test fun forcedConvergenceUsesEffectiveBoardRatherThanToggle() {
        val effective=CrossroadsExperimentalMode.boardId(CrossroadsCourse.CONVERGENCE_ID,false,false)
        assertTrue(OfflineOpponentPreview.enabled(true,false,effective))
        assertEquals("Guardian · Advanced preview",OfflineOpponentPreview.label(guardian))
    }
    @Test fun setupSummaryMatchesActiveSeatsAndEligibility() {
        val choices=mapOf(0 to guardian,1 to scout,3 to guardian)
        assertEquals("AI: Scout · Classic · Guardian · Advanced preview",
            OfflineOpponentPreview.summary(true,false,board,4,choices))
        assertEquals("AI: Scout",OfflineOpponentPreview.summary(true,false,board,2,choices))
        assertNull(OfflineOpponentPreview.summary(true,false,board,2,mapOf(3 to guardian)))
        assertNull(OfflineOpponentPreview.summary(false,false,board,4,choices))
        assertNull(OfflineOpponentPreview.summary(true,true,board,4,choices))
        assertNull(OfflineOpponentPreview.summary(true,false,CrossroadsCourse.CLASSIC_ID,4,choices))
        assertNull(OfflineOpponentPreview.summary(true,false,board,2,emptyMap()))
        assertNull(OfflineOpponentPreview.summary(true,false,board,2,mapOf(1 to
            BotOpponentConfigurationV1(BotPersonalityV1.CLASSIC))))
    }
    @Test fun liveNamesUseResolvedPolicyAndRetainSeatIdentity() {
        assertEquals("2 · Scout",OfflineOpponentPreview.liveName("Bot 2",1,BotPolicyVersion.ADAPTIVE_V2_R2))
        assertEquals("3 · Scout",OfflineOpponentPreview.liveName("Bot 3",2,BotPolicyVersion.ADAPTIVE_V2_R2))
        assertEquals("4 · Guardian+",OfflineOpponentPreview.liveName("Bot 4",3,BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1))
        assertEquals("2 · Classic",OfflineOpponentPreview.liveName("Bot 2",1,BotPolicyVersion.LEGACY_V1))
        assertEquals("You",OfflineOpponentPreview.liveName("You",0,null))
        assertEquals("Bot 2",OfflineOpponentPreview.liveName("Bot 2",1,BotPolicyVersion.GUARDIAN_RELAXED_CANDIDATE_V1))
    }

    @Test fun plateNamesAvoidDuplicatingPawnNumberAndExpandAdvancedForSpeech() {
        assertEquals("Scout", OfflineOpponentPreview.plateName("Bot 2",1,BotPolicyVersion.ADAPTIVE_V2_R2))
        assertEquals("Guardian+", OfflineOpponentPreview.plateName("Bot 4",3,BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1))
        assertEquals("Guardian, Advanced preview", OfflineOpponentPreview.accessiblePlateName(
            "Bot 4",3,BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1))
        assertEquals("You", OfflineOpponentPreview.plateName("You",0,null))
        assertEquals("Bot 2", OfflineOpponentPreview.plateName("Bot 2",1,BotPolicyVersion.GUARDIAN_RELAXED_CANDIDATE_V1))
    }

    @Test fun everyAvailableOpponentHasDistinctStyleCopyWithoutDifficultyPromises() {
        val choices = BotPersonalityV1.entries.flatMap { personality ->
            BotDifficultyPreviewV1.available(personality).map { difficulty ->
                BotOpponentConfigurationV1(personality, difficulty)
            }
        }
        assertEquals(7, choices.size)
        val descriptions = choices.map { OfflineOpponentPreview.description(it) }
        assertEquals(choices.size, descriptions.distinct().size)
        assertTrue(descriptions.all { it.isNotBlank() && it.length <= 80 })
        assertTrue(descriptions.none { it.contains("easy", ignoreCase = true) ||
            it.contains("harder", ignoreCase = true) || it.contains("guarantee", ignoreCase = true) })
        assertEquals("Tests a different approach to finishing the race.",
            OfflineOpponentPreview.description(guardian))
        assertEquals("Guardian · Advanced preview", OfflineOpponentPreview.label(guardian))
    }

    @Test fun unavailableTierCannotResolve() {
        try {
            OfflineOpponentPreview.policies(true,false,board,2,mapOf(1 to
                BotOpponentConfigurationV1(BotPersonalityV1.SCOUT,BotDifficultyV1.RELAXED)))
            fail("Uncalibrated tier accepted")
        } catch(expected:IllegalArgumentException) { }
    }
}
