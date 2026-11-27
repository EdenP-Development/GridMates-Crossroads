package com.playereden.gridmates.crossroads

/** Setup-only choice; the resolved seat, never the random choice, is saved. */
internal object OfflineStartingSeatPreview {
    const val RANDOM = -1

    fun normalized(choice: Int, seats: Int): Int {
        require(seats in 2..4)
        return if (choice == RANDOM || choice in 0 until seats) choice else 0
    }

    fun nextChoice(choice: Int, seats: Int): Int = when(val current = normalized(choice, seats)) {
        RANDOM -> 0
        seats - 1 -> RANDOM
        else -> current + 1
    }

    fun label(choice: Int, seats: Int): String = when(val current = normalized(choice, seats)) {
        RANDOM -> "Starts: Random"
        0 -> "Starts: You"
        else -> "Starts: Player ${current + 1}"
    }

    fun resolve(debug: Boolean, passAndPlay: Boolean, boardId: String, seats: Int,
                choice: Int, draw: (Int) -> Int): Int {
        val current = normalized(choice, seats)
        if (!OfflineOpponentPreview.enabled(debug, passAndPlay, boardId)) return 0
        return if (current == RANDOM) draw(seats).also { require(it in 0 until seats) } else current
    }
}
