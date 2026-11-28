package com.playereden.crossroads.core

/** Proposed cyclic order; identity indexes never change. Not wired into GameSession. */
internal data class StartingOrderV1(val seats: Int, val first: Int = 0) {
    init {
        require(seats in 2..4)
        require(first in 0 until seats)
    }
    fun ordinal(seat: Int): Int {
        require(seat in 0 until seats)
        return (seat - first + seats) % seats
    }
    fun seatAt(ordinal: Int): Int {
        require(ordinal in 0 until seats)
        return (first + ordinal) % seats
    }
    val last: Int get() = seatAt(seats - 1)
    fun next(seat: Int): Int {
        require(seat in 0 until seats)
        return (seat + 1) % seats
    }
    fun hasLaterTurn(seat: Int): Boolean = ordinal(seat) < seats - 1
    fun rotated(): StartingOrderV1 = copy(first = next(first))
}
