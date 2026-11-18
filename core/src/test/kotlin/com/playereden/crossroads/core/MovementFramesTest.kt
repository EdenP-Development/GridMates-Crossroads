package com.playereden.crossroads.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MovementFramesTest {
    @Test fun forwardSequenceUsesEveryTraversedNode() {
        assertEquals(listOf("B", "C", "D"),
            MoveTrail.steps(listOf("START", "A"),
                listOf("START", "A", "B", "C", "D")))
    }
    @Test fun retreatRetracesActualPath() {
        assertEquals(listOf("C", "B"), MoveTrail.steps(
            listOf("START", "A", "B", "C", "D"),
            listOf("START", "A", "B")
        ))
    }
    @Test fun noMovementProducesNoVisualMotion() {
        assertEquals(emptyList<String>(), MoveTrail.steps(listOf("START", "A"),
            listOf("START", "A")))
    }
    @Test fun routeResetSnapsTowardAuthoritativePosition() {
        assertEquals(listOf("NEXT"), MoveTrail.steps(listOf("START", "A", "B"),
            listOf("START", "NEXT")))
    }
}
