package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class FinishOpportunityAuditTest {
    @Test fun observeExistingPersonalitiesWithoutChangingMatches() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val boards=listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json",
            "data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json",
            "design/prototypes/woodland_woven_convergence_v02.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val rows=mutableListOf<String>()
        var matches=0
        for(board in boards) for(personality in BotPersonalityV1.entries)
            for(seats in listOf(2,4)) for(seed in 1..24) {
                val observed=GameSession(board,events,seats,seed,emptySet(),botPolicy=personality.policy)
                val control=GameSession(board,events,seats,seed,emptySet(),botPolicy=personality.policy)
                var actions=0
                while(observed.phase!=Phase.FINISHED && actions<1500) {
                    val actor=observed.currentIndex
                    val phase=observed.phase
                    val pending=observed.finalRoundPending
                    val assessment=observed.directFinishOpportunity()
                    assertEquals(assessment,observed.directFinishOpportunity())
                    assertEquals(control.snapshot(),observed.snapshot())
                    observed.botStep()
                    control.botStep()
                    assertEquals(control.lastBotDecision,observed.lastBotDecision)
                    assertEquals(control.snapshot(),observed.snapshot())
                    if(phase==Phase.ROLL) {
                        val pushed=when(observed.lastBotDecision?.kind) {
                            MatchActionKind.BOLD_ROLL -> true
                            MatchActionKind.STEADY_ROLL -> false
                            else -> error("Missing travel receipt")
                        }
                        val actual=assessment?.let {
                            if(pushed) it.pushSuccesses else it.steadySuccesses
                        }
                        val best=assessment?.let { maxOf(it.steadySuccesses,it.pushSuccesses) }
                        val gain=if(best!=null && actual!=null) best-actual else null
                        if(gain!=null) assertTrue(gain in 0..6)
                        rows+=listOf(board.id,personality,seats,seed,actions,actor+1,
                            pending,assessment?.distance ?: "",assessment?.steadySuccesses ?: "",
                            assessment?.pushSuccesses ?: "",if(pushed) "push" else "steady",
                            assessment?.preferPush()?.let { if(it) "push" else "steady" } ?: "",
                            gain ?: "").joinToString(",")
                    } else assertNull(assessment)
                    actions++
                }
                assertEquals(Phase.FINISHED,observed.phase)
                matches++
            }
        assertEquals(1440,matches)
        assertTrue(rows.isNotEmpty())
        val out=File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"finish_opportunity_audit.csv").writeText(
            "course,personality,seats,seed,action,actor_seat,final_round,distance,steady_successes,push_successes,actual,recommended,gain_outcomes\n"+
            rows.joinToString("\n")+"\n")
        val eligible=rows.count{it.split(",")[7].isNotEmpty()}
        assertTrue("No eligible finish opportunities",eligible>0)
        println("Finish opportunity audit: matches=$matches rolls=${rows.size} eligible=$eligible")
    }
}
