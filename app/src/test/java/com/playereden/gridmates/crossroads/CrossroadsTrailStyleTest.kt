package com.playereden.gridmates.crossroads

import org.junit.Assert.assertEquals
import org.junit.Test

/** Cosmetic trail switching must not mutate authoritative route graph or match data. */
class CrossroadsTrailStyleTest {
    @Test fun qaStylesCycleBetweenArtworkPrintedAndSimple() {
        assertEquals(CrossroadsTrailStyle.PRINTED, CrossroadsTrailStyle.ARTWORK.next())
        assertEquals(CrossroadsTrailStyle.SIMPLE, CrossroadsTrailStyle.PRINTED.next())
        assertEquals(CrossroadsTrailStyle.ARTWORK, CrossroadsTrailStyle.SIMPLE.next())
    }

    @Test fun missingArtAndClassicMapFallbackRemainSafe() {
        assertEquals(CrossroadsTrailStyle.PRINTED,
            effectiveTrailStyle(CrossroadsTrailStyle.ARTWORK, true, false))
        assertEquals(CrossroadsTrailStyle.ARTWORK,
            effectiveTrailStyle(CrossroadsTrailStyle.ARTWORK, true, true))
        assertEquals(CrossroadsTrailStyle.PRINTED,
            effectiveTrailStyle(CrossroadsTrailStyle.PRINTED, true, true))
        assertEquals(CrossroadsTrailStyle.SIMPLE,
            effectiveTrailStyle(CrossroadsTrailStyle.ARTWORK, false, true))
        assertEquals(CrossroadsTrailStyle.SIMPLE,
            effectiveTrailStyle(CrossroadsTrailStyle.SIMPLE, true, false))
    }
}
