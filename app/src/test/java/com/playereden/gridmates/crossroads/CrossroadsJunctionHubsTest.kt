package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

/** Art-only junction caps must not invent spaces, routes or full-map markers. */
class CrossroadsJunctionHubsTest {
    @Test fun onlyLiveIllustratedPaintedJunctionsUseHubs() {
        val g = CrossroadsJunctionHubGeometry
        assertTrue(g.eligible("fork", true, true, true))
        assertTrue(g.eligible("merge", true, true, true))
        assertFalse(g.eligible("plain", true, true, true))
        assertFalse(g.eligible("event", true, true, true))
        assertFalse(g.eligible("start", true, true, true))
        assertFalse(g.eligible("finish", true, true, true))
        assertFalse(g.eligible("fork", false, true, true)) // full map
        assertFalse(g.eligible("fork", true, false, true)) // classic/flat view
        assertFalse(g.eligible("fork", true, true, false)) // painted art unavailable
    }

    @Test fun comfortableCrossingsBecomeDistinctWoodenHubs() {
        val g = CrossroadsJunctionHubGeometry
        assertEquals(29f, g.radius(.1f, 1500f, false), .0001f)
        assertEquals(24f, g.radius(.1f, 1500f, true), .0001f)
    }

    @Test fun denseCourseCapsHubToClosestAuthoredNode() {
        val g = CrossroadsJunctionHubGeometry
        val shortGap = .032f * 1500f
        val cap = g.radius(.032f, 1500f, true)
        assertTrue(cap > 8f)
        assertTrue(cap < 24f)
        assertTrue(cap <= shortGap * .35f + .0001f)
        assertTrue(2f * cap < shortGap)
    }

    @Test fun invalidOrTinyGeometryCannotCreateEnormousHubs() {
        val g = CrossroadsJunctionHubGeometry
        assertEquals(0f, g.radius(.1f, 0f, false), .0001f)
        assertEquals(0f, g.radius(.1f, Float.NaN, false), .0001f)
        assertEquals(29f, g.radius(Float.POSITIVE_INFINITY, 1500f, false), .0001f)
        assertEquals(0f, g.radius(.0001f, 1000f, true), .0001f)
        assertFalse(g.radius(.04f, 1400f, true).isNaN())
    }
}
