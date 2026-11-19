package com.playereden.gridmates.crossroads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRaceSummaryTest {
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

    @Test fun passAndPlayModeAndTacticalRuleStatusStayVisible() {
        assertEquals("4 people · Pass & Play · offline · Tactical rules ON",
            homeRaceSummary(4, passAndPlay = true, tactical = true))
        assertEquals("2 people · Pass & Play · offline",
            homeRaceSummary(2, passAndPlay = true, tactical = false))
    }
}
