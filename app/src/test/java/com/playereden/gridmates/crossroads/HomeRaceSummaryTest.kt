package com.playereden.gridmates.crossroads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRaceSummaryTest {
    @Test fun pinnedSummaryIdentifiesCourseAndPreservesRulesDisclosure() {
        assertEquals("Brookside Dash · Solo vs AI · 2 racers · offline",
            homeRaceSummary(2, false, false, courseName = "Brookside Dash"))
        assertEquals("Cedarwood Expedition · 4 people · Pass & Play · offline · Tactical + Fair Finish ON",
            homeRaceSummary(4, true, true, true, courseName = "Cedarwood Expedition"))
        assertEquals(homeRaceSummary(2, false, false),
            homeRaceSummary(2, false, false, courseName = " "))
    }


    @Test fun secondaryOptionsHaveConcisePortraitFriendlyLabels() {
        assertEquals("More options ↓", homeOptionsToggleLabel(expanded = false))
        assertEquals("Hide more options ↑", homeOptionsToggleLabel(expanded = true))
    }
    @Test fun defaultSoloSummaryRemainsSimple() {
        val text = homeRaceSummary(2, passAndPlay = false, tactical = false)
        assertEquals("Solo vs AI · 2 racers · offline", text)
        assertFalse(text.contains("Tactical"))
    }

    @Test fun collapsedOptionsNeverHideEnabledExperimentalRules() {
        val text = homeRaceSummary(3, passAndPlay = false, tactical = true)
        assertEquals("Solo vs AI · 3 racers · offline · Tactical rules ON", text)
        assertTrue(text.endsWith("Tactical rules ON"))
    }

    @Test fun pinnedSummaryNamesFairFinishWhenV5IsReallyEnabled() {
        assertEquals("Solo vs AI · 2 racers · offline · Tactical + Fair Finish ON",
            homeRaceSummary(2, passAndPlay = false, tactical = true, fairFinish = true))
        assertEquals("4 people · Pass & Play · offline · Tactical + Fair Finish ON",
            homeRaceSummary(4, passAndPlay = true, tactical = true, fairFinish = true))
    }

    @Test fun rememberedFairFinishPreferenceDoesNotMislabelOriginalRules() {
        // The saved toggle may remain true after Tactical is turned off;
        // normal offline racing must still show the original-rules summary.
        assertEquals("Solo vs AI · 2 racers · offline",
            homeRaceSummary(2, passAndPlay = false, tactical = false, fairFinish = true))
        assertEquals("2 people · Pass & Play · offline",
            homeRaceSummary(2, passAndPlay = true, tactical = false, fairFinish = true))
    }

    @Test fun passAndPlayModeAndTacticalRuleStatusStayVisible() {
        assertEquals("4 people · Pass & Play · offline · Tactical rules ON",
            homeRaceSummary(4, passAndPlay = true, tactical = true))
        assertEquals("2 people · Pass & Play · offline",
            homeRaceSummary(2, passAndPlay = true, tactical = false))
    }
}
