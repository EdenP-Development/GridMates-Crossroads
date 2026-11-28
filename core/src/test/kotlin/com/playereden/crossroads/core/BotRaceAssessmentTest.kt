package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class BotRaceAssessmentTest {
    private fun racer(seat: Int, distance: Int, shield: Boolean = false) =
        BotRacerView(seat, distance, shield)

    @Test fun openingTieAndStableOpponentOrder() {
        val view = BotRaceAssessment.assess(1,
            listOf(racer(2, 40), racer(1, 40, true), racer(0, 40)), 40, false)
        assertEquals(BotRaceStage.OPENING, view.stage)
        assertEquals(BotRaceStanding.TIED, view.standing)
        assertEquals(0, view.gapToLeader)
        assertEquals(listOf(0,2), view.opponents.map { it.seat })
        assertTrue(view.own.shield)
    }

    @Test fun signedGapAndThreatRankingUseVisibleDistances() {
        val view = BotRaceAssessment.assess(0,
            listOf(racer(0, 25), racer(1, 19), racer(2, 22, true)), 40, false)
        assertEquals(BotRaceStage.MIDGAME, view.stage)
        assertEquals(BotRaceStanding.TRAILING, view.standing)
        assertEquals(6, view.gapToLeader)
        assertEquals(1, view.nearestOpponent?.seat)
        val leading = BotRaceAssessment.assess(1,
            listOf(racer(0, 25), racer(1, 19)), 40, false)
        assertEquals(BotRaceStanding.LEADING, leading.standing)
        assertEquals(-6, leading.gapToLeader)
    }

    @Test fun opponentNearFinishOrFinalRoundMakesRaceEndgame() {
        val racers = listOf(racer(0, 33), racer(1, 5))
        assertEquals(BotRaceStage.ENDGAME, BotRaceAssessment.assess(0, racers, 40, false).stage)
        val finalRound = BotRaceAssessment.assess(0,
            listOf(racer(0, 33), racer(1, 30)), 40, true)
        assertEquals(BotRaceStage.ENDGAME, finalRound.stage)
        assertTrue(finalRound.finalRoundPending)
    }
}
