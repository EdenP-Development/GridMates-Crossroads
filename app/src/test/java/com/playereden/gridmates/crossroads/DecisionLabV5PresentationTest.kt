package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.DecisionLab
import com.playereden.crossroads.core.FairFinishReceipt
import org.junit.Assert.*
import org.junit.Test

class DecisionLabV5PresentationTest {
    @Test fun currentTestModeIdentifiesV5ButKeepsOldResults() {
        val base = CrossroadsCourse.CLASSIC_ID
        assertEquals("Woven Trail", CrossroadsCourse.title(base + DecisionLab.V5_SUFFIX))
        assertEquals("Gameplay 2.0 · Lab v5",
            CrossroadsCourse.subtitle(base + DecisionLab.V5_SUFFIX))
        assertEquals("Gameplay 2.0 · Lab v4",
            CrossroadsCourse.subtitle(base + DecisionLab.V4_SUFFIX))
        assertEquals("Fair finish on Woven Trail · Playtest",
            CrossroadsCourse.resultLine(base + DecisionLab.V5_SUFFIX))
        assertEquals("First across Woven Trail · Playtest",
            CrossroadsCourse.resultLine(base + DecisionLab.V4_SUFFIX))
        assertEquals("First across Woven Trail",
            CrossroadsCourse.resultLine(base))
    }

    @Test fun newMatchModeCanCompareV4AndV5WithoutChangingLegacy() {
        val base = CrossroadsCourse.QUICK_ID
        assertEquals(base, CrossroadsExperimentalMode.boardId(
            base, tactical = false, fairFinish = true))
        assertEquals(base + DecisionLab.V4_SUFFIX,
            CrossroadsExperimentalMode.boardId(base, tactical = true, fairFinish = false))
        assertEquals(base + DecisionLab.V5_SUFFIX,
            CrossroadsExperimentalMode.boardId(base, tactical = true, fairFinish = true))
    }

    @Test fun fairFinishReceiptExplainsWinnerAndEarlierArrivalPrecisely() {
        val sole = FairFinishReceipt(
            firstFinisherName = "You", winnerName = "You",
            winningUnusedSteps = 2, finishers = 1,
            tiedOnWinningSteps = false, laterSeatsGotFinalChance = true
        )
        assertTrue(CrossroadsFinishCopy.fairFinishExplanation(sole)
            .contains("nobody else reached Finish"))

        val lastSeat = sole.copy(
            firstFinisherName = "Player 4", winnerName = "Player 4",
            laterSeatsGotFinalChance = false
        )
        assertTrue(CrossroadsFinishCopy.fairFinishExplanation(lastSeat)
            .contains("final seat"))

        val overtake = sole.copy(
            firstFinisherName = "You", winnerName = "AI 1",
            winningUnusedSteps = 3, finishers = 2
        )
        val overtakeCopy = CrossroadsFinishCopy.fairFinishExplanation(overtake)
        assertTrue(overtakeCopy.contains("You crossed first"))
        assertTrue(overtakeCopy.contains("AI 1 won with 3 unused steps"))
        assertFalse(overtakeCopy.contains("Tied on unused"))

        val tie = overtake.copy(
            winnerName = "You", winningUnusedSteps = 1,
            tiedOnWinningSteps = true
        )
        val tieCopy = CrossroadsFinishCopy.fairFinishExplanation(tie)
        assertTrue(tieCopy.contains("1 unused step."))
        assertTrue(tieCopy.contains("earlier of the tied finishers won"))
    }

    @Test fun conditionalPushHazardRewardIsStillDisclosedInV5() {
        assertEquals("Success: +2 extra",
            encounterActionCopy("gamble", pushHazardSuccessBonus = 2).impact)
        assertEquals("Face hazard", encounterActionCopy("gamble").impact)
    }
}
