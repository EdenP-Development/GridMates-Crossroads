package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.Phase
import org.junit.Assert.*
import org.junit.Test

class BoardFollowTurnPolicyTest {
    private fun recenter(
        phase: Phase = Phase.ROLL,
        turn: Int = 3,
        last: Int = 2,
        immersive: Boolean = true,
        animated: Boolean = true,
        transitioning: Boolean = false
    ) = BoardFollowTurnPolicy.shouldRecenter(
        phase = phase,
        turn = turn,
        lastCenteredTurn = last,
        immersiveWorld = immersive,
        animateTokens = animated,
        isTurnTransitioning = transitioning
    )

    @Test fun readyNewTurnReturnsCameraToActiveTraveler() {
        assertTrue(recenter())
        assertTrue(recenter(turn = 1, last = -1))
    }

    @Test fun doesNotStealManualPanWithinSameTurn() {
        assertFalse(recenter(last = 3))
        assertFalse(recenter(phase = Phase.ROUTE))
        assertFalse(recenter(phase = Phase.ENCOUNTER))
        assertFalse(recenter(phase = Phase.TARGET))
    }

    @Test fun doesNotInterruptMovementOrAReadOnlyFullMap() {
        assertFalse(recenter(transitioning = true))
        assertFalse(recenter(immersive = false))
        assertFalse(recenter(animated = false))
        assertFalse(recenter(phase = Phase.FINISHED))
        assertFalse(recenter(turn = 0))
    }
}
