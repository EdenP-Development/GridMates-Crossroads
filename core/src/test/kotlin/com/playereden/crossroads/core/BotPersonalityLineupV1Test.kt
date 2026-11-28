package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class BotPersonalityLineupV1Test {
    @Test fun selectedRosterIsDistinctAndHistoricalPresetsStayFrozen() {
        assertEquals(6,BotPersonalityV1.entries.size)
        assertEquals(6,BotPersonalityV1.entries.map{it.policy}.toSet().size)
        assertEquals(BotPolicyVersion.GUARDIAN_V1,BotPersonalityV1.GUARDIAN.policy)
        assertEquals(BotPolicyVersion.STRATEGIST_V1,BotPersonalityV1.STRATEGIST.policy)
        assertEquals(BotPolicyVersion.GAMBLER_V1,BotPersonalityV1.GAMBLER.policy)
        assertEquals(BotPolicyVersion.ADAPTIVE_V2_R3,BotStrategyPreset.GUARDIAN.policy)
        assertEquals(BotPolicyVersion.ADAPTIVE_V2_R4,BotStrategyPreset.STRATEGIST.policy)
    }

    @Test fun resolutionPreservesSeatIdentityAndExcludesHumans() {
        assertEquals(mapOf(1 to BotPolicyVersion.GUARDIAN_V1,
            3 to BotPolicyVersion.STRATEGIST_V1),
            BotPersonalityLineupV1.resolve(listOf(null,BotPersonalityV1.GUARDIAN,
                null,BotPersonalityV1.STRATEGIST)))
        assertTrue(BotPersonalityLineupV1.resolve(listOf(null,null)).isEmpty())
        for(size in listOf(0,1,5)) {
            try {
                BotPersonalityLineupV1.resolve(List(size){null})
                fail("Accepted invalid seat count")
            } catch(expected:IllegalArgumentException) { }
        }
    }
}
