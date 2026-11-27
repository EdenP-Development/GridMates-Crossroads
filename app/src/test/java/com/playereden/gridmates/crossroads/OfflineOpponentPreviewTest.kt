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

    @Test fun unavailableTierCannotResolve() {
        try {
            OfflineOpponentPreview.policies(true,false,board,2,mapOf(1 to
                BotOpponentConfigurationV1(BotPersonalityV1.SCOUT,BotDifficultyV1.RELAXED)))
            fail("Uncalibrated tier accepted")
        } catch(expected:IllegalArgumentException) { }
    }
}
