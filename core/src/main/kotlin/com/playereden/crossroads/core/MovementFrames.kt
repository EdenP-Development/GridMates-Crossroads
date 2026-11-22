package com.playereden.crossroads.core

/**
 * Creates the small sequence of *visible* nodes traversed between UI snapshots.
 * This must not mutate game state, affect dice, or draw additional event outcomes.
 */
object MoveTrail {
    fun steps(before: List<String>, after: List<String>): List<String> {
        if (after.isEmpty()) return emptyList()
        if (before == after) return emptyList()
        if (after.size > before.size && after.take(before.size) == before) {
            return after.drop(before.size)
        }
        if (before.size > after.size && before.take(after.size) == after) {
            // Retreat in reverse along already-traversed nodes.
            return before.drop(after.size).dropLast(1).asReversed() + after.last()
        }
        // Board reload, rematch, or unexpected route history change: move to
        // the authoritative node rather than inventing a route.
        return listOf(after.last())
    }
}
