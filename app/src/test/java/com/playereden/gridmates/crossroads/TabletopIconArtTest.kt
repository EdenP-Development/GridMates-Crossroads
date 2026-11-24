package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

class TabletopIconArtTest {
    @Test fun iconInventoryMatchesSourceLibrary() {
        val expected = setOf("home", "multiplayer", "map", "journal", "settings",
            "sound", "music", "dice", "zoom_in", "zoom_out", "expand", "collapse")
        assertEquals(expected, TabletopIconArt.entries.map { it.stem }.toSet())
        assertEquals(12, TabletopIconArt.entries.size)
        for (icon in TabletopIconArt.entries) {
            assertEquals("art/ui/v1/icon_${icon.stem}.png", icon.packagedPath)
            assertTrue(icon.validDimensions(128, 128))
            assertFalse(icon.validDimensions(127, 128))
            assertFalse(icon.validDimensions(128, 129))
        }
    }
}
