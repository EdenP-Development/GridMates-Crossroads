package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class BotFinishOpportunityV1Test {
    private fun board(distance:Int, event:Boolean=false): BoardDefinition {
        val nodes=(0..distance).map { i ->
            BoardNode("n$i","woodland",if(i==distance) "finish" else
                if(event && i==1) "event" else "plain",
                if(i==distance) emptyList() else listOf("n${i+1}"),0f,0f)
        }
        return BoardDefinition("fixture","n0","n$distance",nodes)
    }

    @Test fun corridorDirectArrivalCountsMatchMovementOdds() {
        for(distance in 1..8) {
            val result=requireNotNull(BotFinishOpportunityV1.assess(board(distance),"n0"))
            assertEquals(distance,result.distance)
            assertEquals(BotTravelOddsV1.steadyFinishOutcomes(distance),result.steadySuccesses)
            assertEquals(BotTravelOddsV1.pushFinishOutcomes(distance),result.pushSuccesses)
        }
        assertEquals(false,BotFinishOpportunityV1.assess(board(4),"n0")?.preferPush())
        assertEquals(true,BotFinishOpportunityV1.assess(board(6),"n0")?.preferPush())
        assertNull(BotFinishOpportunityV1.assess(board(1),"n0")?.preferPush())
        assertNull(BotFinishOpportunityV1.assess(board(9),"n0"))
        assertNull(BotFinishOpportunityV1.assess(board(4),"n4"))
    }

    @Test fun laterFinisherMustStrictlyExceedLeader() {
        val b=board(3)
        assertEquals(FinishOpportunityV1(3,0,4),
            BotFinishOpportunityV1.assess(b,"n0",1))
        assertEquals(FinishOpportunityV1(3,0,1),
            BotFinishOpportunityV1.assess(b,"n0",4))
        assertEquals(FinishOpportunityV1(3,0,0),
            BotFinishOpportunityV1.assess(b,"n0",5))
        assertNull(BotFinishOpportunityV1.assess(b,"n0",Int.MAX_VALUE)?.preferPush())
    }

    @Test fun forksCyclesAndDeadEndsAreNotCertifiedCorridors() {
        val b=board(4)
        for(next in listOf(listOf("n1","n2"),listOf("n0"),emptyList())) {
            val modified=b.copy(nodes=b.nodes.map {
                if(it.id=="n0") it.copy(next=next) else it
            })
            assertNull(BotFinishOpportunityV1.assess(modified,"n0"))
        }
    }

    @Test fun eventSpaceDoesNotInterruptDirectArrival() {
        // Events resolve only on landing or route commitment, not passing
        // through a plain corridor. Failed outcomes are not modeled here.
        assertEquals(BotFinishOpportunityV1.assess(board(4),"n0"),
            BotFinishOpportunityV1.assess(board(4,true),"n0"))
    }
}
