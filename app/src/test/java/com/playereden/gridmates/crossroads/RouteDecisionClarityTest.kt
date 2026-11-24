package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.GameData
import com.playereden.crossroads.core.DecisionLab
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class RouteDecisionClarityTest {
    @Test fun distancesIncludeFirstStepAndCompareEveryLegalExit() {
        for (name in listOf("woodland_brookside_quick","woodland_woven_trail","woodland_cedarwood_long")) {
            val file = listOf(File("data/boards/$name.json"), File("../data/boards/$name.json"))
                .first { it.isFile }
            val board = GameData.board(file.readText())
            board.nodes.filter { it.next.size > 1 }.forEach { fork ->
                val choices = RouteDecisionClarity.compare(board, fork.id)
                assertEquals(fork.next.toSet(), choices.keys)
                assertEquals(0, choices.values.minOf { it.extra })
                val shortest = choices.values.minOf { it.steps }
                choices.forEach { (id, distance) ->
                    assertEquals(1 + DecisionLab.minimumSpacesToFinish(board,id),distance.steps)
                    assertEquals(distance.steps-shortest,distance.extra)
                    assertTrue(distance.label().startsWith("At least "))
                    assertEquals(distance.extra == 0, distance.label().contains("Shortest distance"))
                }
            }
            assertTrue(RouteDecisionClarity.compare(board,board.finish).isEmpty())
        }
    }
    @Test fun singularDistanceAndTiesRemainHonest() {
        assertEquals("At least 1 space to Finish · Shortest distance", RouteDistance(1,0).label())
        assertEquals("At least 3 spaces to Finish · +2 vs shortest", RouteDistance(3,2).label())
    }
}
