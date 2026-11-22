package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

/** Game-feel feedback cannot make the game wait or hide selection changes. */
class CrossroadsTabletopFeedbackTest {
    @Test fun pressingGivesImmediateSubtleScaleWithoutExcessiveMovement() {
        assertEquals(1f, CrossroadsTabletopFeedback.scale(false), .0001f)
        assertEquals(.975f, CrossroadsTabletopFeedback.scale(true), .0001f)
        assertTrue(CrossroadsTabletopFeedback.scale(true) >= .96f)
        assertTrue(CrossroadsTabletopFeedback.scale(true) < 1f)
    }

    @Test fun homeSetupSummaryStillStatesActualRaceType() {
        assertEquals(
            "Solo vs AI · 2 racers · offline",
            homeRaceSummary(2, passAndPlay = false, tactical = false)
        )
        assertEquals(
            "3 people · Pass & Play · offline · Tactical rules ON",
            homeRaceSummary(3, passAndPlay = true, tactical = true)
        )
    }
}
