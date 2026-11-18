package com.playereden.gridmates.crossroads

import org.junit.Assert.assertEquals
import org.junit.Test

class CrossroadsBoardLookTest {
    @Test fun defaultLookIsIllustrated() {
        assertEquals(CrossroadsBoardLook.ILLUSTRATED, CrossroadsBoardLook.fromStored(null))
        assertEquals(CrossroadsBoardLook.ILLUSTRATED, CrossroadsBoardLook.fromStored(""))
        assertEquals(CrossroadsBoardLook.ILLUSTRATED, CrossroadsBoardLook.fromStored("AUTUMN_UNAVAILABLE"))
    }

    @Test fun selectionCanRoundTripWithoutChangingCourseId() {
        for (look in CrossroadsBoardLook.entries) {
            assertEquals(look, CrossroadsBoardLook.fromStored(look.name))
        }
    }
}
