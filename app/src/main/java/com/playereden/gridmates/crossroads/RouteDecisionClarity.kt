package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.BoardDefinition
import com.playereden.crossroads.core.DecisionLab

/** Public graph distance only, not estimated turns, odds or a guaranteed result. */
internal data class RouteDistance(val steps: Int, val extra: Int) {
    fun label(): String = "At least $steps ${if (steps == 1) "space" else "spaces"} to Finish" +
        if (extra > 0) " · +$extra vs shortest" else " · Shortest distance"
}

internal object RouteDecisionClarity {
    fun compare(board: BoardDefinition, from: String): Map<String, RouteDistance> {
        val distances = board.node(from).next.associateWith { to ->
            1 + DecisionLab.minimumSpacesToFinish(board, to)
        }
        val minimum = distances.values.minOrNull() ?: return emptyMap()
        return distances.mapValues { (_, steps) -> RouteDistance(steps, steps - minimum) }
    }
}
