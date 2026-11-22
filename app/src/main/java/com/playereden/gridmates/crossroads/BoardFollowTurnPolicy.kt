package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.Phase

/**
 * Presentation-only camera recovery after a new turn becomes playable.
 *
 * A player can freely pan/zoom during their current turn. When the NEXT roll
 * becomes ready, resume following the active game piece instead of leaving
 * the camera stranded on an explored junction or empty scenery. Never
 * interrupt the prior player's movement animation to do this.
 */
internal object BoardFollowTurnPolicy {
    fun shouldRecenter(
        phase: Phase,
        turn: Int,
        lastCenteredTurn: Int,
        immersiveWorld: Boolean,
        animateTokens: Boolean,
        isTurnTransitioning: Boolean
    ): Boolean =
        immersiveWorld && animateTokens && !isTurnTransitioning &&
            phase == Phase.ROLL && turn > 0 && turn != lastCenteredTurn
}
