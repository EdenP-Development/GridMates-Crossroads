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
}
