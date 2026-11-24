package com.playereden.crossroads.core

/**
 * Cache the existing shortest-distance calculation for this immutable board.
 * Invalid cyclic graphs retain the previous path-dependent cycle guard.
 * No RNG, scores, legal actions or serialized state enter this helper.
 */
internal class BotRouteDistances(private val board: BoardDefinition) {
    private val memo = mutableMapOf<String, Int>()
    private val visiting = mutableSetOf<String>()
    private var cyclic = false

    fun distance(id: String): Int {
        if (cyclic) return legacyDistance(id, emptySet())
        val result = visit(id)
        return if (cyclic) legacyDistance(id, emptySet()) else result
    }

    private fun visit(id: String): Int {
        if (id == board.finish) return 0
        memo[id]?.let { return it }
        if (!visiting.add(id)) {
            cyclic = true
            return 100_000
        }
        val next = board.node(id).next
        val result = if (next.isEmpty()) 100_000 else 1 + next.minOf { visit(it) }
        visiting.remove(id)
        if (!cyclic) memo[id] = result
        return result
    }

    private fun legacyDistance(id: String, seen: Set<String>): Int {
        if (id == board.finish) return 0
        if (id in seen) return 100_000
        val next = board.node(id).next
        return if (next.isEmpty()) 100_000 else 1 + next.minOf { legacyDistance(it, seen + id) }
    }
}
