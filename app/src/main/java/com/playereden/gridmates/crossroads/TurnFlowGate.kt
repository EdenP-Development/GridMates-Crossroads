package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.Phase
import com.playereden.crossroads.core.Snapshot

/**
 * UI-only turn pacing policy. The rules engine remains authoritative.
 * Reading/inspection surfaces pause AI without blocking a human's pending
 * fork, or introducing a new turn-confirmation button.
 */
internal object TurnFlowGate {
    fun mayAdvanceAi(
        state: Snapshot?,
        turnReady: Boolean,
        readingOrMenuOpen: Boolean
    ): Boolean =
        state != null &&
            turnReady &&
            !readingOrMenuOpen &&
            state.phase != Phase.FINISHED &&
            state.players[state.playerIndex].isBot
}
