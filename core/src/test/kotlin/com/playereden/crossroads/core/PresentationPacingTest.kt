package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class PresentationPacingTest {
    private fun state(route: List<String>, events: List<EncounterNote> = emptyList()): Snapshot =
        Snapshot(
            revision = 1, phase = Phase.ROLL, playerIndex = 0,
            players = listOf(Player("You", false, route.last(), route.toMutableList())),
            remainingSteps = 0, die = null, lastMessage = "", winnerIndex = null,
            turn = 1, lastEventId = events.lastOrNull()?.title, encounters = events
        )

    @Test fun cameraTracksPreviousTurnMoverNotNewActivePlayer() {
        val before = state(listOf("S", "A")).copy(
            players = listOf(
                Player("You", false, "A", mutableListOf("S", "A")),
                Player("Bot", true, "S", mutableListOf("S"))
            ), playerIndex = 0
        )
        val after = before.copy(
            players = listOf(
                Player("You", false, "B", mutableListOf("S", "A", "B")),
                Player("Bot", true, "S", mutableListOf("S"))
            ), playerIndex = 1
        )
        assertEquals(0, PresentationPacing.mover(before, after))
        assertEquals(1, PresentationPacing.mover(after,
            after.copy(players = listOf(
                after.players[0],
                Player("Bot", true, "C", mutableListOf("S", "C"))
            ))))
    }

    @Test fun movementIsTimedPerActualNodeNotTheRolledNumber() {
        val first = state(listOf("S"))
        val last = state(listOf("S", "A", "B", "C"))
        assertEquals(3, PresentationPacing.stepsMoved(first, last))
        assertEquals(3L * 205L + 220L,
            PresentationPacing.waitForMovement(first, last, PresentationSpeed.COZY))
    }

    @Test fun setbacksHaveTheSameVisibleStepCount() {
        val before = state(listOf("S", "A", "B", "C", "D"))
        val after = state(listOf("S", "A", "B"))
        assertEquals(2, PresentationPacing.stepsMoved(before, after))
    }

    @Test fun eventRevealAddsVisualTimeWithoutTouchingGameState() {
        val a = state(listOf("S", "A"))
        val event = EncounterNote(1, "You", "Friendly scarecrow", "Gained a shield", "benefit")
        val b = state(listOf("S", "A"), listOf(event))
        assertEquals(480L, PresentationPacing.waitForMovement(a, b, PresentationSpeed.COZY))
        assertEquals(0L, PresentationPacing.waitForMovement(a, a, PresentationSpeed.COZY))
    }

    @Test fun briskModeIsFasterButCannotChangeRoutesOrEvents() {
        val before = state(listOf("S", "A"))
        val note = EncounterNote(1, "You", "Shortcut", "Moved ahead 2 spaces", "benefit")
        val after = state(listOf("S", "A", "B", "C"), listOf(note))
        val cozy = PresentationPacing.waitForMovement(before, after, PresentationSpeed.COZY)
        val brisk = PresentationPacing.waitForMovement(before, after, PresentationSpeed.BRISK)
        assertTrue(brisk < cozy)
        assertEquals(after.players[0].traversed, listOf("S", "A", "B", "C"))
        assertEquals(listOf(note), after.encounters)
    }
}
