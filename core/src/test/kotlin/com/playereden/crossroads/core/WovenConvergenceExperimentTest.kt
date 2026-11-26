package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Test-only Woven geometry candidates; never register or ship these boards. */
class WovenConvergenceExperimentTest {
    private fun spaced(board: BoardDefinition, added: Int): BoardDefinition {
        require(added in 0..3)
        val source = board.node("R3")
        require(source.next == listOf("F2"))
        val end = board.node("F2")
        val ids = (1..added).map { "spacing_test_$it" }
        val extras = ids.mapIndexed { index, id ->
            val t = (index+1f)/(added+1f)
            BoardNode(id,source.region,"plain",
                listOf(ids.getOrNull(index+1) ?: end.id),
                source.x+(end.x-source.x)*t,source.y+(end.y-source.y)*t,
                source.route)
        }
        val low = board.node("L4")
        require(low.next == listOf("F3"))
        val lowIds = (1..added).map { "convergence_low_$it" }
        val lowExtras = lowIds.mapIndexed { index,id ->
            val t = (index+1f)/(added+1f)
            BoardNode(id,low.region,"plain",listOf(lowIds.getOrNull(index+1) ?: end.id),
                low.x+(end.x-low.x)*t,low.y+(end.y-low.y)*t,low.route)
        }
        return board.copy(id=board.id+"_convergence_$added",
            nodes=board.nodes.map { when(it.id) {
                source.id -> it.copy(next=listOf(ids.firstOrNull() ?: end.id))
                low.id -> it.copy(next=listOf(lowIds.firstOrNull() ?: end.id))
                else -> it
            } }+extras+lowExtras)
    }
    @Test fun compareConvergenceWithoutChangingEncounterRules() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val base = GameData.board(File(root,"data/boards/woodland_woven_trail.json").readText())
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val rows = mutableListOf<String>()
        for (extra in listOf(-1,0,1,2,3)) {
            val board = DecisionLab.variantV5(if(extra==-1) base else spaced(base,extra))
            for (policy in listOf(BotPolicyVersion.LEGACY_V1,BotPolicyVersion.ADAPTIVE_V2_R2,
                BotPolicyVersion.ADAPTIVE_V2_R4)) for (seats in listOf(2,4)) for (seed in 1..24) {
                val game = GameSession(board,events,seats,seed,emptySet(),policy)
                var actions=0
                var rolls=0
                var junctions=0
                var available=0
                var mediumObserved=0
                var targets=0
                while(game.phase!=Phase.FINISHED && actions<1500) {
                    if(game.phase==Phase.ROLL) rolls++
                    if(game.phase==Phase.ROUTE && game.players[game.currentIndex].position=="F2") {
                        val actor=game.players[game.currentIndex]
                        val before=actor.traversed.size
                        junctions++
                        if(game.routeEncounterAvailable) available++
                        game.botStep()
                        if(actor.traversed.getOrNull(before)=="M1") mediumObserved++
                        if(game.phase==Phase.TARGET) targets++
                    } else game.botStep()
                    actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                assertTrue(available<=junctions)
                if(extra>=0) assertTrue("Converging routes must reach F2",junctions>0)
                assertTrue(targets<=available)
                assertEquals(game.snapshot(),GameSession.restore(board,events,
                    game.checkpoint("spacing-$seed",1L)).snapshot())
                rows += listOf(extra,policy,seats,seed,actions,rolls,junctions,available,
                    mediumObserved,targets,requireNotNull(game.winnerIndex)+1).joinToString(",")
            }
        }
        assertEquals(720,rows.size)
        val out=File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"woven_convergence_experiment.csv").writeText(
            "extra_spaces_or_original_minus_one,policy,seats,seed,actions,rolls,junctions,available,medium_observed,targets,winner_seat\n"+
            rows.joinToString("\n")+"\n")
    }
}
