package com.playereden.crossroads.core

/** Visible graph only. Stop at the next decision; never predict hidden event draws. */
internal enum class RouteHorizonStop { FINISH, FORK, BUDGET, DEAD_END, CYCLE }
internal data class RouteHorizon(
    val routeId: String,
    val enteredSpaces: Int,
    val stop: RouteHorizonStop,
    val nextForkRisks: List<String>
)

internal object BotRouteLookahead {
    fun inspect(board: BoardDefinition, routeId: String, movementBudget: Int): RouteHorizon {
        require(movementBudget > 0)
        var node = board.node(routeId)
        var entered = 1
        val seen = mutableSetOf(node.id)
        fun result(stop: RouteHorizonStop, risks: List<String> = emptyList()) =
            RouteHorizon(routeId, entered, stop, risks)
        while (true) {
            if (node.id == board.finish) return result(RouteHorizonStop.FINISH)
            // No route choice is required after movement is exhausted.
            if (entered >= movementBudget) return result(RouteHorizonStop.BUDGET)
            if (node.next.isEmpty()) return result(RouteHorizonStop.DEAD_END)
            if (node.next.size > 1) {
                val risks = board.forkAt(node.id)?.choices.orEmpty()
                    .filter { it.to in node.next }.map { it.risk }
                return result(RouteHorizonStop.FORK, risks)
            }
            val next = node.next.single()
            if (!seen.add(next)) return result(RouteHorizonStop.CYCLE)
            node = board.node(next)
            entered++
        }
    }
}
