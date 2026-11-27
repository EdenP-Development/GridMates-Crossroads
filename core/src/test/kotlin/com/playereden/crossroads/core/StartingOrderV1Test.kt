package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class StartingOrderV1Test {
    @Test fun everyStartingSeatPreservesIdentityAndRoundBoundary() {
        for(seats in 2..4) for(first in 0 until seats) {
            val order=StartingOrderV1(seats,first)
            val sequence=(0 until seats).map(order::seatAt)
            assertEquals(first,sequence.first())
            assertEquals((0 until seats).toSet(),sequence.toSet())
            assertEquals(order.last,sequence.last())
            for((ordinal,seat) in sequence.withIndex()) {
                assertEquals(ordinal,order.ordinal(seat))
                assertEquals(sequence[(ordinal+1)%seats],order.next(seat))
                assertEquals(ordinal<seats-1,order.hasLaterTurn(seat))
            }
            assertEquals(first,order.next(order.last))
        }
    }

    @Test fun defaultOrderMatchesHistoricalSeatSemantics() {
        for(seats in 2..4) {
            val order=StartingOrderV1(seats)
            assertEquals(seats-1,order.last)
            for(seat in 0 until seats) {
                assertEquals(seat,order.ordinal(seat))
                assertEquals(seat<seats-1,order.hasLaterTurn(seat))
            }
        }
    }

    @Test fun repeatedRotationGivesEveryIdentityOneStart() {
        for(seats in 2..4) for(first in 0 until seats) {
            val original=StartingOrderV1(seats,first)
            var order=original
            val starts=mutableSetOf<Int>()
            repeat(seats) {
                assertTrue(starts.add(order.first))
                order=order.rotated()
            }
            assertEquals(original,order)
        }
    }

    @Test fun invalidSeatsAndOrdinalsAreRejected() {
        fun rejects(action: () -> Unit) {
            try { action(); fail("Accepted invalid order") }
            catch(expected: IllegalArgumentException) { }
        }
        rejects { StartingOrderV1(1) }
        rejects { StartingOrderV1(5) }
        rejects { StartingOrderV1(3,-1) }
        rejects { StartingOrderV1(3,3) }
        val order=StartingOrderV1(3,2)
        rejects { order.ordinal(-1) }
        rejects { order.seatAt(3) }
        rejects { order.next(3) }
    }
}
