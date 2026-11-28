package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Test harness only: explicitly recorded legal travel actions, not a new saved policy. */
class RoundStartAwarenessExperimentTest {
    @Test fun compareGuardianTravelAtEqualTurnBoundaries() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val boards=listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json",
            "data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json",
            "design/prototypes/woodland_woven_convergence_v02.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val rows=mutableListOf<String>()
        for(board in boards) for(seed in 1..64) {
            val baseline=GameSession(board,events,2,seed,emptySet(),BotPolicyVersion.GUARDIAN_V1)
            val live=GameSession(board,events,2,seed,emptySet(),BotPolicyVersion.GUARDIAN_V1)
            val candidate=GameSession(board,events,2,seed,emptySet(),BotPolicyVersion.GUARDIAN_V1)
            val courseDistance=baseline.observedRaceAwareness().own.distance
            fun play(game:GameSession,mode:String,control:GameSession?=null) {
                var boundary=listOf<BotRacerView>()
                val rolls=IntArray(2)
                val pushes=IntArray(2)
                var changed=0
                var actions=0
                while(game.phase!=Phase.FINISHED && actions<1500) {
                    if(game.phase==Phase.ROLL) {
                        val seat=game.currentIndex
                        val actual=game.observedRaceAwareness()
                        if(seat==0) boundary=listOf(actual.own)+actual.opponents
                        val view=if(mode=="round_start" && !actual.finalRoundPending) {
                            // Freeze relative positions only. Current own distance, shields,
                            // and final-round information remain authoritative.
                            val aligned=boundary.map { prior ->
                                if(prior.seat==seat) actual.own else prior.copy(
                                    shield=actual.opponents.single { it.seat==prior.seat }.shield)
                            }
                            // If mischief moved the acting racer since the boundary, retain
                            // both live positions rather than mix incompatible turn states.
                            if(boundary.single { it.seat==seat }.distance!=actual.own.distance) actual
                            else BotRaceAssessment.assess(seat,aligned,courseDistance,false)
                        } else actual
                        val push=GuardianPersonalityV1.shouldPush(view)
                        if(push!=GuardianPersonalityV1.shouldPush(actual)) changed++
                        rolls[seat]++
                        if(push) pushes[seat]++
                        if(push) game.rollBold() else game.rollSteady()
                    } else game.botStep()
                    control?.let {
                        it.botStep()
                        assertEquals(it.snapshot(),game.snapshot())
                    }
                    actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                assertEquals(rolls[0],rolls[1])
                val saved=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                    game.checkpoint("round-start-$mode-$seed",1L)))
                assertEquals(game.snapshot(),GameSession.restore(board,events,saved).snapshot())
                rows+=listOf(board.id,seed,mode,requireNotNull(game.winnerIndex)+1,
                    rolls[0],rolls[1],pushes[0],pushes[1],changed,actions).joinToString(",")
            }
            play(live,"live",baseline)
            play(candidate,"round_start")
        }
        assertEquals(640,rows.size)
        val out=File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"round_start_awareness.csv").writeText(
            "course,seed,mode,winner,rolls_1,rolls_2,push_1,push_2,changed,actions\n"+
            rows.joinToString("\n")+"\n")
    }
}
