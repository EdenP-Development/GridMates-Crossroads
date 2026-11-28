package com.playereden.gridmates.crossroads

import org.junit.Assert.assertEquals
import org.junit.Test

class PublicVisualProfileTest {
    @Test fun publicDefaultsIgnoreQaPreferences() {
        for (trails in listOf(false, true))
            for (pieces in listOf(false, true))
                for (markers in listOf(false, true))
                    assertEquals(PublicVisualProfile(true, true, true),
                        PublicVisualProfile.resolve(false, trails, pieces, markers))
    }
    @Test fun qaPreservesAllVisualComparisons() {
        for (trails in listOf(false, true))
            for (pieces in listOf(false, true))
                for (markers in listOf(false, true))
                    assertEquals(PublicVisualProfile(trails, pieces, markers),
                        PublicVisualProfile.resolve(true, trails, pieces, markers))
    }
}
