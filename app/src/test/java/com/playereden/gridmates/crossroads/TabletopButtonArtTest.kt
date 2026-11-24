package com.playereden.gridmates.crossroads

import org.junit.Assert.assertEquals
import org.junit.Test

class TabletopButtonArtTest {
    @Test fun disabledOverridesPressAndNormalStatesAreStable() {
        assertEquals("primary_normal.png", TabletopButtonArt.filename(true, false))
        assertEquals("primary_pressed.png", TabletopButtonArt.filename(true, true))
        assertEquals("primary_disabled.png", TabletopButtonArt.filename(false, false))
        assertEquals("primary_disabled.png", TabletopButtonArt.filename(false, true))
    }
    @Test fun secondaryStatesAndSelectionHaveStableNames() {
        for (family in TabletopArtFamily.entries) {
            assertEquals("${family.stem}_normal.png", TabletopButtonArt.filename(family, true, false, false))
            assertEquals("${family.stem}_highlight.png", TabletopButtonArt.filename(family, true, false, true))
            assertEquals("${family.stem}_pressed.png", TabletopButtonArt.filename(family, true, true, true))
            assertEquals("${family.stem}_disabled.png", TabletopButtonArt.filename(family, false, true, true))
        }
    }
}
