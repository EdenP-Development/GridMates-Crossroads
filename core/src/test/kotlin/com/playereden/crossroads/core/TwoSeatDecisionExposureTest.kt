package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Observe decision exposure without changing any policy, random draw or rule. */
class TwoSeatDecisionExposureTest {
    @Test fun compareTravelDecisionsAndPublicStateBySeat() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val boards=listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json",
            "data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json",
            "design/prototypes/woodland_woven_convergence_v02.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val policies=BotPersonalityV1.entries.map { it.policy }
        val rows=mutableListOf<String>()
        for(board in boards) for(policy in policies) for(seed in 1..64) {
            val observed=GameSession(board,events,2,seed,emptySet(),policy)
            val control=GameSession(board,events,2,seed,emptySet(),policy)
            var actions=0
            while(observed.phase!=Phase.FINISHED && actions<1500) {
                val seat=observed.currentIndex
                val roll=observed.phase==Phase.ROLL
                val view=if(roll) observed.observedRaceAwareness() else null
                val finish=if(roll) observed.directFinishOpportunity() else null
                if(view!=null) assertEquals(view,observed.observedRaceAwareness())
                observed.botStep()
                control.botStep()
                assertEquals(control.snapshot(),observed.snapshot())
                assertEquals(control.lastBotDecision,observed.lastBotDecision)
                if(view!=null) {
                    val push=when(observed.lastBotDecision?.kind) {
                        MatchActionKind.BOLD_ROLL -> true
                        MatchActionKind.STEADY_ROLL -> false
                        else -> error("Invalid travel receipt")
                    }
                    rows+=listOf(board.id,policy,seed,actions,seat+1,view.own.distance,
                        view.gapToLeader,view.stage,view.finalRoundPending,push,
                        finish?.distance ?: "",finish?.steadySuccesses ?: "",
                        finish?.pushSuccesses ?: "",finish?.preferPush()?.toString() ?: "").joinToString(",")
                }
                actions++
            }
            assertEquals(Phase.FINISHED,observed.phase)
        }
        assertTrue(rows.isNotEmpty())
        val out=File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"two_seat_decision_exposure.csv").writeText(
            "course,policy,seed,action,seat,distance,gap,stage,final_round,push,finish_distance,steady_successes,push_successes,recommended_push\n"+
            rows.joinToString("\n")+"\n")
    }
}
