package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class BotRouteLookaheadTest {
    private fun node(id: String, vararg next: String) =
        BoardNode(id, "forest", "path", next.toList(), 0f, 0f)

    private val board = BoardDefinition("test", "start", "finish", listOf(
        node("start","a","b"), node("a","finish"), node("b","c"),
        node("c","d","e"), node("d","finish"), node("e","finish"), node("finish")
    ), listOf(ForkDefinition("c", choices = listOf(
        ForkChoice("d","Shelter","low"), ForkChoice("e","Hazard","high")
    ))))

    @Test fun finishAndBudgetAreDistinguished() {
        val finish = BotRouteLookahead.inspect(board,"a",2)
        assertEquals(RouteHorizonStop.FINISH,finish.stop)
        assertEquals(2,finish.enteredSpaces)
        assertEquals(RouteHorizonStop.BUDGET,BotRouteLookahead.inspect(board,"a",1).stop)
    }

    @Test fun secondForkIsVisibleOnlyIfMovementRemains() {
        val fork = BotRouteLookahead.inspect(board,"b",3)
        assertEquals(RouteHorizonStop.FORK,fork.stop)
        assertEquals(2,fork.enteredSpaces)
        assertEquals(listOf("low","high"),fork.nextForkRisks)
        assertEquals(RouteHorizonStop.BUDGET,BotRouteLookahead.inspect(board,"b",2).stop)
    }

    @Test fun malformedCycleDoesNotHang() {
        val cyclic = BoardDefinition("cycle","start","finish",
            listOf(node("start","a"),node("a","b"),node("b","a"),node("finish")))
        assertEquals(RouteHorizonStop.CYCLE,BotRouteLookahead.inspect(cyclic,"a",8).stop)
    }
}
