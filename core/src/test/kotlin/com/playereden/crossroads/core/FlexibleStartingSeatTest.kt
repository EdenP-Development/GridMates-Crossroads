package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class FlexibleStartingSeatTest {
    private val root=File(System.getProperty("crossroads.repoDir"))
    private val base=GameData.board(File(root,"data/boards/woodland_brookside_quick.json").readText())
    private val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
    private val lab=DecisionLab.variantV5(base)

    @Test fun everyStartRestoresAndFinishesWithEqualTurns() {
        for(seats in 2..4) for(first in 0 until seats) for(seed in 1..12) {
            val policies=(1 until seats).associateWith { BotPolicyVersion.GUARDIAN_V1 }
            val game=GameSession(lab,events,seats,seed,setOf(0),botSeatPolicies=policies,startingSeat=first)
            assertEquals(first,game.currentIndex)
            assertFalse(game.players[0].isBot)
            val rolls=IntArray(seats)
            val order=StartingOrderV1(seats,first)
            var actions=0
            while(game.phase!=Phase.FINISHED && actions<1500) {
                if(game.phase==Phase.ROLL) {
                    assertEquals(order.seatAt(rolls.sum()%seats),game.currentIndex)
                    rolls[game.currentIndex]++
                }
                if(game.players[game.currentIndex].isBot) game.botStep()
                else when(game.phase) {
                    Phase.ROLL -> game.rollSteady()
                    Phase.ROUTE -> game.chooseRoute(game.availableRoutes().first().id)
                    Phase.ENCOUNTER -> game.chooseEncounter(game.availableEncounterChoices().first())
                    Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                    Phase.FINISHED -> Unit
                }
                actions++
                if(actions==4) {
                    val encoded=MatchCheckpointCodec.encode(game.checkpoint("rotated-$seed",1L))
                    assertEquals(first!=0,encoded.contains("\"startingSeat\""))
                    val restored=GameSession.restore(lab,events,MatchCheckpointCodec.decode(encoded))
                    assertEquals(first,restored.startingSeat)
                    assertEquals(game.snapshot(),restored.snapshot())
                }
            }
            assertEquals(Phase.FINISHED,game.phase)
            assertTrue(rolls.all { it==rolls[0] })
            val receipt=requireNotNull(game.fairFinishReceipt)
            val firstFinisher=game.players.indexOfFirst { it.name==receipt.firstFinisherName }
            assertEquals(order.hasLaterTurn(firstFinisher),receipt.laterSeatsGotFinalChance)
            val checkpoint=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(game.checkpoint("finished-$seed",1L)))
            val restored=GameSession.restore(lab,events,checkpoint)
            assertEquals(game.snapshot(),restored.snapshot())
            assertEquals(receipt,restored.fairFinishReceipt)
            val rematch=restored.freshRematch(seed+100)
            assertEquals(first,rematch.currentIndex)
            assertFalse(rematch.players[0].isBot)
            for((seat,policy) in policies) assertEquals(policy,rematch.policyForBotSeat(seat))
            val rotated=restored.freshRematch(seed+100,rotateStarter=true)
            assertEquals((first+1)%seats,rotated.startingSeat)
            assertEquals(rotated.startingSeat,rotated.currentIndex)
            assertFalse(rotated.players[0].isBot)
            for((seat,policy) in policies) assertEquals(policy,rotated.policyForBotSeat(seat))
            val rotatedSaved=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(rotated.checkpoint("next-$seed",2L)))
            val rotatedRestored=GameSession.restore(lab,events,rotatedSaved)
            assertEquals(rotated.startingSeat,rotatedRestored.startingSeat)
            assertEquals(rotated.snapshot(),rotatedRestored.snapshot())
        }
    }

    @Test fun rotatingRematchRejectsUnfinishedOrIneligibleRaces() {
        fun rejects(action:()->Unit) {
            try { action(); fail("Invalid rematch accepted") }
            catch(expected:IllegalArgumentException) { }
        }
        rejects { GameSession(lab,events).freshRematch(8,rotateStarter=true) }
        for(course in listOf(base,lab)) {
            val humans=if(course==lab) setOf(0,1) else setOf(0)
            val game=GameSession(course,events,2,47,humans)
            var actions=0
            while(game.phase!=Phase.FINISHED && actions++<1500) {
                if(game.players[game.currentIndex].isBot) game.botStep()
                else when(game.phase) {
                    Phase.ROLL -> if(course==lab) game.rollSteady() else game.roll()
                    Phase.ROUTE -> game.chooseRoute(game.availableRoutes().first().id)
                    Phase.ENCOUNTER -> game.chooseEncounter(game.availableEncounterChoices().first())
                    Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                    Phase.FINISHED -> Unit
                }
            }
            assertEquals(Phase.FINISHED,game.phase)
            rejects { game.freshRematch(8,rotateStarter=true) }
            assertEquals(0,game.freshRematch(8).startingSeat)
        }
    }

    @Test fun defaultOmittedAndInvalidStartsRejected() {
        val game=GameSession(lab,events)
        val saved=game.checkpoint("legacy",1L)
        assertFalse(MatchCheckpointCodec.encode(saved).contains("\"startingSeat\""))
        assertEquals(0,GameSession.restore(lab,events,saved).startingSeat)
        fun rejects(action:()->Unit) {
            try { action(); fail("Invalid start accepted") }
            catch(expected:IllegalArgumentException) { }
        }
        rejects { GameSession(base,events,startingSeat=1) }
        rejects { GameSession(lab,events,startingSeat=2) }
        rejects { GameSession.restore(lab,events,saved.copy(startingSeat=1)) }
    }
}
