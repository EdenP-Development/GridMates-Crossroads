package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.Phase
import com.playereden.crossroads.core.Snapshot

/**
 * UI-only turn pacing policy. The rules engine remains authoritative.
 * Reading/inspection screens and backgrounded app lifecycle pause AI without
 * blocking a human's pending fork or adding a new turn-confirmation button.
 */
internal object TurnFlowGate {
    fun mayAdvanceAi(
        state: Snapshot?,
        turnReady: Boolean,
        readingOrMenuOpen: Boolean,
        appInForeground: Boolean
    ): Boolean =
        state != null &&
            turnReady &&
            appInForeground &&
            !readingOrMenuOpen &&
            state.phase != Phase.FINISHED &&
            state.players[state.playerIndex].isBot
}
