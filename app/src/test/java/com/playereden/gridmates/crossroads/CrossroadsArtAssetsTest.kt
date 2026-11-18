package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.DecisionLab
import org.junit.Assert.*
import org.junit.Test

/** The optional visual pack must never alter course IDs or player semantics. */
class CrossroadsArtAssetsTest {
    @Test fun artSlotPathsAreStableLocalAndUnique() {
        val slots = CrossroadsArtSlot.entries
        assertEquals(12, slots.size)
        assertEquals(slots.size, slots.map { it.filename }.distinct().size)
        slots.forEach {
            assertTrue(it.filename.matches(Regex("[a-z][a-z0-9_]*\\.png")))
            assertEquals("art/v1/${it.filename}", it.packagedPath)
            assertFalse(it.packagedPath.contains(".."))
        }
    }

    @Test fun artMapsEveryOriginalAndSavedLabCourseToSameBackplate() {
        val courses = listOf(
            "woodland_brookside_quick_v01" to CrossroadsArtSlot.BOARD_BROOKSIDE,
            "woodland_woven_trail_v02" to CrossroadsArtSlot.BOARD_WOVEN,
            "woodland_cedarwood_long_v01" to CrossroadsArtSlot.BOARD_CEDARWOOD
        )
        for ((board, expected) in courses) {
            assertEquals(expected, CrossroadsArtCatalog.backplate(board))
            for (suffix in listOf(
                DecisionLab.SUFFIX, DecisionLab.V2_SUFFIX, DecisionLab.V3_SUFFIX
            )) {
                assertEquals(expected, CrossroadsArtCatalog.backplate(board + suffix))
            }
        }
        assertNull(CrossroadsArtCatalog.backplate("unknown_future_course"))
    }

    @Test fun seatPortraitsNeverEraseRequiredSeatNumbers() {
        assertEquals(CrossroadsArtSlot.TOKEN_SEAT_1, CrossroadsArtCatalog.seat(0))
        assertEquals(CrossroadsArtSlot.TOKEN_SEAT_2, CrossroadsArtCatalog.seat(1))
        assertEquals(CrossroadsArtSlot.TOKEN_SEAT_3, CrossroadsArtCatalog.seat(2))
        assertEquals(CrossroadsArtSlot.TOKEN_SEAT_4, CrossroadsArtCatalog.seat(3))
        assertNull(CrossroadsArtCatalog.seat(-1))
        assertNull(CrossroadsArtCatalog.seat(4))
    }
}
