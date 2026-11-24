package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class BotRouteDistancesTest {
    private fun legacy(board: BoardDefinition, id: String, seen: Set<String> = emptySet()): Int {
        if (id == board.finish) return 0
        if (id in seen) return 100_000
        val next = board.node(id).next
        return if (next.isEmpty()) 100_000 else 1 + next.minOf { legacy(board,it,seen+id) }
    }
    @Test fun cachePreservesLegacyDistancesAcrossAllCoursesAndVersions() {
        val root = File(System.getProperty("crossroads.repoDir"))
        for (name in listOf("woodland_brookside_quick","woodland_woven_trail","woodland_cedarwood_long")) {
            val base = GameData.board(File(root,"data/boards/$name.json").readText())
            for (board in listOf(base,DecisionLab.variant(base),DecisionLab.variantV2(base),
                DecisionLab.variantV3(base),DecisionLab.variantV4(base),DecisionLab.variantV5(base))) {
                val cache = BotRouteDistances(board)
                val expected = board.nodes.associate { it.id to legacy(board,it.id) }
                for (node in board.nodes + board.nodes.reversed()) {
                    assertEquals("${board.id} ${node.id}",expected.getValue(node.id),cache.distance(node.id))
                }
            }
        }
    }
    @Test fun malformedCycleKeepsLegacyGuard() {
        val board = BoardDefinition("cycle","A","F",listOf(
            BoardNode("A","meadow","fork",listOf("B","F"),0f,0f),
            BoardNode("B","meadow","plain",listOf("A"),.5f,.5f),
            BoardNode("F","meadow","finish",emptyList(),1f,1f)
        ))
        val cache = BotRouteDistances(board)
        for (id in listOf("A","B","F","B","A")) {
            assertEquals(legacy(board,id),cache.distance(id))
        }
    }
}
