package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.DecisionLab
import org.junit.Assert.*
import org.junit.Test

/** Every visible bonus must be truthful and legacy UI copy must remain intact. */
class DecisionLabV4PresentationTest {
    @Test fun newLabHasCorrectTitleAndExperimentalResultWithoutRenamingCourse() {
        val base = CrossroadsCourse.CLASSIC_ID
        val v3 = base + DecisionLab.V3_SUFFIX
        val v4 = base + DecisionLab.V4_SUFFIX
        assertEquals("Woven Trail", CrossroadsCourse.title(v4))
        assertEquals("Gameplay 2.0 · Lab v4", CrossroadsCourse.subtitle(v4))
        assertEquals("Gameplay 2.0 · Lab v3", CrossroadsCourse.subtitle(v3))
        assertEquals("First across Woven Trail · Playtest",
            CrossroadsCourse.resultLine(v4))
        assertEquals("First across Woven Trail · Playtest",
            CrossroadsCourse.resultLine(v3))
        assertEquals("First across Woven Trail", CrossroadsCourse.resultLine(base))
    }

    @Test fun hazardCardStatesConditionalRewardWithoutGuaranteeingSuccess() {
        assertEquals("Face hazard", encounterActionCopy("gamble").impact)
        assertEquals("Success: +2 extra",
            encounterActionCopy("gamble", pushHazardSuccessBonus = 2).impact)
        assertEquals("Stop & shield",
            encounterActionCopy("brace", pushHazardSuccessBonus = 2).impact)
        assertEquals("+2 spaces",
            encounterActionCopy("supplies", pushHazardSuccessBonus = 2).impact)
    }
}
