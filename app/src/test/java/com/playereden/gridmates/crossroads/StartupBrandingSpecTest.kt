package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

class StartupBrandingSpecTest {
    @Test fun approvedSequenceAndTiming() {
        assertEquals(listOf("Spudacus", "GridMates"), StartupBrandingSpec.stages.map { it.first })
        assertEquals(250, StartupBrandingSpec.FADE_MS)
        assertEquals(1000, StartupBrandingSpec.HOLD_MS)
        assertEquals(3000, StartupBrandingSpec.stages.size *
            (2 * StartupBrandingSpec.FADE_MS + StartupBrandingSpec.HOLD_MS))
    }
    @Test fun onlyColdLaunchWithMotionShowsBranding() {
        for (cold in listOf(false,true)) for (motion in listOf(false,true)) {
            assertEquals(if (cold && motion) 0 else StartupBrandingSpec.stages.size,
                StartupBrandingSpec.initialStage(cold,motion))
        }
    }
}
