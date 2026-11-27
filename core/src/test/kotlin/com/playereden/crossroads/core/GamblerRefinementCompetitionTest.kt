package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** G1 versus G2 with mirrored focal seats and unchanged encounter odds. */
class GamblerRefinementCompetitionTest {
    @Test fun compareRefinementInRealMatches() {
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
        for(board in boards) for(seats in listOf(2,4)) for(seed in 1..48)
            for(focal in 0 until seats) {
                val lineup=(0 until seats).associateWith {
                    if(it==focal) BotPolicyVersion.GAMBLER_V2 else BotPolicyVersion.GAMBLER_V1
                }
                val game=GameSession(board,events,seats,seed,emptySet(),botSeatPolicies=lineup)
                val counts=List(seats){mutableMapOf<String,Int>()}
                var actions=0
                while(game.phase!=Phase.FINISHED && actions<1500) {
                    val actor=game.currentIndex
                    val phase=game.phase
                    game.botStep()
                    val receipt=game.lastBotDecision
                    val key=when(phase) {
                        Phase.ROLL -> when(receipt?.kind) {
                            MatchActionKind.STEADY_ROLL -> "steady"
                            MatchActionKind.BOLD_ROLL -> "push"
                            else -> error("Unexpected travel")
                        }
                        Phase.ENCOUNTER -> {
                            assertEquals(MatchActionKind.ENCOUNTER,receipt?.kind)
                            requireNotNull(receipt?.routeId)
                        }
                        Phase.ROUTE -> "route"
                        Phase.TARGET -> "target"
                        Phase.FINISHED -> error("Unexpected finish")
                    }
                    counts[actor][key]=(counts[actor][key]?:0)+1
                    actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                assertEquals(actions,counts.sumOf{it.values.sum()})
                val checkpoint=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                    game.checkpoint("g2-$seed",1L)))
                assertEquals(lineup,checkpoint.botSeatPolicies)
                assertEquals(game.snapshot(),GameSession.restore(board,events,checkpoint).snapshot())
                for(seat in 0 until seats) {
                    rows+=(listOf(board.id,seats,seed,focal+1,seat+1,lineup.getValue(seat),
                        if(game.winnerIndex==seat) 1 else 0,actions)+
                        listOf("steady","push","supplies","cover","gamble","brace","route","target").map{counts[seat][it]?:0}).joinToString(",")
                }
            }
        assertEquals(4800,rows.size)
        val out=File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"gambler_refinement.csv").writeText(
            "course,seats,seed,g2_seat,actor_seat,policy,won,match_actions,steady,push,supplies,cover,gamble,brace,route,target\n"+
            rows.joinToString("\n")+"\n")
    }
}
