package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.Phase
import com.playereden.crossroads.core.Player
import com.playereden.crossroads.core.Snapshot
import org.junit.Assert.*
import org.junit.Test

class TurnFlowGateTest {
    private val seats = listOf(
        Player("You", false, "S", mutableListOf("S")),
        Player("AI 1", true, "S", mutableListOf("S"))
    )

    private fun state(
        index: Int = 1,
        phase: Phase = Phase.ROLL,
        players: List<Player> = seats
    ) = Snapshot(
        revision = 0,
        phase = phase,
        playerIndex = index,
        players = players,
        remainingSteps = 0,
        die = null,
        lastMessage = "",
        winnerIndex = null,
        turn = 2,
        lastEventId = null,
        encounters = emptyList()
    )

    @Test fun aiOnlyMovesWhenReadyAndNoReaderIsOpen() {
        val ai = state()
        assertTrue(TurnFlowGate.mayAdvanceAi(ai, turnReady = true, readingOrMenuOpen = false, appInForeground = true))
        assertFalse(TurnFlowGate.mayAdvanceAi(ai, turnReady = false, readingOrMenuOpen = false, appInForeground = true))
        assertFalse(TurnFlowGate.mayAdvanceAi(ai, turnReady = true, readingOrMenuOpen = true, appInForeground = true))
        assertFalse(TurnFlowGate.mayAdvanceAi(null, turnReady = true, readingOrMenuOpen = false, appInForeground = true))
    }

    @Test fun humansNeverAdvanceAutomaticallyIncludingPassAndPlay() {
        val human = state(index = 0)
        assertFalse(TurnFlowGate.mayAdvanceAi(human, turnReady = true, readingOrMenuOpen = false, appInForeground = true))
        val local = state(index = 1, players = seats.mapIndexed { i, player ->
            player.copy(name = "Player ${i + 1}", isBot = false)
        })
        assertFalse(TurnFlowGate.mayAdvanceAi(local, turnReady = true, readingOrMenuOpen = false, appInForeground = true))
        assertFalse(TurnFlowGate.mayAdvanceAi(local, turnReady = true, readingOrMenuOpen = true, appInForeground = true))
    }

    @Test fun finishedMatchesNeverAdvanceAndOpenRouteIsStillAnAiDecision() {
        assertFalse(
            TurnFlowGate.mayAdvanceAi(
                state(phase = Phase.FINISHED), turnReady = true, readingOrMenuOpen = false, appInForeground = true
            )
        )
        // When not paused, the existing engine still resolves bot route decisions.
        assertTrue(
            TurnFlowGate.mayAdvanceAi(
                state(phase = Phase.ROUTE), turnReady = true, readingOrMenuOpen = false, appInForeground = true
            )
        )
        assertFalse(
            TurnFlowGate.mayAdvanceAi(
                state(phase = Phase.TARGET), turnReady = true, readingOrMenuOpen = true, appInForeground = true
            )
        )
    }
    @Test fun backgroundAppSuspendsAiWithoutChangingItsTurn() {
        val pendingBot = state(index = 1)
        assertTrue(TurnFlowGate.mayAdvanceAi(
            pendingBot, turnReady = true, readingOrMenuOpen = false,
            appInForeground = true
        ))
        assertFalse(TurnFlowGate.mayAdvanceAi(
            pendingBot, turnReady = true, readingOrMenuOpen = false,
            appInForeground = false
        ))
        // Foreground return is allowed to resume the same pending bot command.
        assertTrue(TurnFlowGate.mayAdvanceAi(
            pendingBot, turnReady = true, readingOrMenuOpen = false,
            appInForeground = true
        ))
        // A reopened reading screen still wins over foreground status.
        assertFalse(TurnFlowGate.mayAdvanceAi(
            pendingBot, turnReady = true, readingOrMenuOpen = true,
            appInForeground = true
        ))
    }

}
