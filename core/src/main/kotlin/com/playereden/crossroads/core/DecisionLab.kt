package com.playereden.crossroads.core

/**
 * Opt-in rules sandbox. A different board ID means an experimental checkpoint
 * can never silently resume under the original rules and vice versa.
 *
 * Existing courses, LAN hosts and previous saves retain their original IDs.
 */
object DecisionLab {
    const val SUFFIX = "__decision_lab_v1"
    fun enabled(board: BoardDefinition): Boolean = board.id.endsWith(SUFFIX)
    fun baseId(id: String): String = id.removeSuffix(SUFFIX)

    fun variant(board: BoardDefinition): BoardDefinition {
        require(!enabled(board)) { "Already a Decision Lab board" }
        return board.copy(id = board.id + SUFFIX)
    }
}
