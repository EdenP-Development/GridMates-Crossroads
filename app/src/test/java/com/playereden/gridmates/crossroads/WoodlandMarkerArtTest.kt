package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

/** Render assets cannot supply route geometry or change risk/event semantics. */
class WoodlandMarkerArtTest {
    @Test fun mapsOnlyKnownVisualWaypoints() {
        assertEquals(WoodlandMarker.FORK, woodlandMarkerFor("fork", null))
        assertEquals(WoodlandMarker.SHELTER, woodlandMarkerFor("event", "shelter"))
        assertEquals(WoodlandMarker.HAZARD, woodlandMarkerFor("event", "hazard"))
        assertEquals(WoodlandMarker.MISCHIEF, woodlandMarkerFor("event", "mischief"))
        assertNull(woodlandMarkerFor("event", "unknown"))
        assertNull(woodlandMarkerFor("plain", null))
        assertNull(woodlandMarkerFor("start", null))
        assertNull(woodlandMarkerFor("finish", null))
    }

    @Test fun atlasCoordinatesStayInsideFourTiles() {
        assertEquals(0, markerAtlasOffset(WoodlandMarker.FORK).x)
        assertEquals(0, markerAtlasOffset(WoodlandMarker.FORK).y)
        assertEquals(128, markerAtlasOffset(WoodlandMarker.SHELTER).x)
        assertEquals(0, markerAtlasOffset(WoodlandMarker.SHELTER).y)
        assertEquals(0, markerAtlasOffset(WoodlandMarker.HAZARD).x)
        assertEquals(128, markerAtlasOffset(WoodlandMarker.HAZARD).y)
        assertEquals(128, markerAtlasOffset(WoodlandMarker.MISCHIEF).x)
        assertEquals(128, markerAtlasOffset(WoodlandMarker.MISCHIEF).y)
        assertEquals("marker_atlas_v1.png", CrossroadsArtSlot.MARKER_ATLAS.filename)
    }

    @Test fun nonDebugClassicAndMissingAssetsUseOldMarkers() {
        assertTrue(woodlandMarkerEnabled(true, true, true, true))
        assertFalse(woodlandMarkerEnabled(false, true, true, true))
        assertFalse(woodlandMarkerEnabled(true, false, true, true))
        assertFalse(woodlandMarkerEnabled(true, true, false, true))
        assertFalse(woodlandMarkerEnabled(true, true, true, false))
    }
}
