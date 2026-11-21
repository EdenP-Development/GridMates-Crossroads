package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.DecisionLab
import org.junit.Assert.*
import org.junit.Test

class DecisionLabV5PresentationTest {
    @Test fun currentTestModeIdentifiesV5ButKeepsOldResults() {
        val base = CrossroadsCourse.CLASSIC_ID
        assertEquals("Woven Trail", CrossroadsCourse.title(base + DecisionLab.V5_SUFFIX))
        assertEquals("Gameplay 2.0 · Lab v5",
            CrossroadsCourse.subtitle(base + DecisionLab.V5_SUFFIX))
        assertEquals("Gameplay 2.0 · Lab v4",
            CrossroadsCourse.subtitle(base + DecisionLab.V4_SUFFIX))
        assertEquals("First across Woven Trail · Playtest",
            CrossroadsCourse.resultLine(base + DecisionLab.V5_SUFFIX))
        assertEquals("First across Woven Trail",
            CrossroadsCourse.resultLine(base))
    }

    @Test fun conditionalPushHazardRewardIsStillDisclosedInV5() {
        assertEquals("Success: +2 extra",
            encounterActionCopy("gamble", pushHazardSuccessBonus = 2).impact)
        assertEquals("Face hazard", encounterActionCopy("gamble").impact)
    }
}
