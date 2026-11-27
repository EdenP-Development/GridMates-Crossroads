package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Dedicated mirrored Strategist comparisons; original benchmark cohorts stay fixed. */
class StrategistCompetitionTest {
    @Test fun mirroredHeadToHeadAndFourSeatCompetition() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val boards=listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json",
            "data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json",
            "design/prototypes/woodland_woven_convergence_v02.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val opponents=listOf(BotPolicyVersion.LEGACY_V1,BotPolicyVersion.ADAPTIVE_V2_R1,
            BotPolicyVersion.ADAPTIVE_V2_R2,BotPolicyVersion.ADAPTIVE_V2_R3,
            BotPolicyVersion.ADAPTIVE_V2_R4, BotPolicyVersion.GAMBLER_V1, BotPolicyVersion.GUARDIAN_V1)
        val keys=listOf("steady","push","low","medium","high","supplies","cover","gamble","brace","target","target_pass")
        val rows=mutableListOf<String>()
        for(board in boards) for(opponent in opponents) for(seats in listOf(2,4))
            for(seed in 1..24) for(focal in 0 until seats) {
                val lineup=(0 until seats).associateWith {
                    if(it==focal) BotPolicyVersion.STRATEGIST_V1 else opponent
                }
                val game=GameSession(board,events,seats,seed,emptySet(),botSeatPolicies=lineup)
                val counts=List(seats){mutableMapOf<String,Int>()}
                var actions=0
                while(game.phase!=Phase.FINISHED && actions<1500) {
                    val actor=game.currentIndex
                    val phase=game.phase
                    val fork=board.forkAt(game.players[actor].position)
                    game.botStep()
                    val receipt=game.lastBotDecision
                    val key=when(phase) {
                        Phase.ROLL -> when(receipt?.kind) {
                            MatchActionKind.STEADY_ROLL -> "steady"
                            MatchActionKind.BOLD_ROLL -> "push"
                            else -> error("Invalid travel receipt")
                        }
                        Phase.ROUTE -> {
                            assertEquals(MatchActionKind.ROUTE,receipt?.kind)
                            requireNotNull(fork?.choices?.firstOrNull{it.to==receipt?.routeId}).risk
                        }
                        Phase.ENCOUNTER -> {
                            assertEquals(MatchActionKind.ENCOUNTER,receipt?.kind)
                            requireNotNull(receipt?.routeId)
                        }
                        Phase.TARGET -> if(receipt==null) "target_pass" else {
                            assertEquals(MatchActionKind.TARGET,receipt.kind)
                            "target"
                        }
                        Phase.FINISHED -> error("Unexpected finish")
                    }
                    assertTrue(key in keys)
                    counts[actor][key]=(counts[actor][key]?:0)+1
                    actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                assertEquals(actions,counts.sumOf{it.values.sum()})
                val winner=requireNotNull(game.winnerIndex)
                val saved=MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                    game.checkpoint("strategist-competition-$seed",1L)))
                assertEquals(lineup,saved.botSeatPolicies)
                assertEquals(game.snapshot(),GameSession.restore(board,events,saved).snapshot())
                for(seat in 0 until seats) {
                    rows+=(listOf(board.id,opponent,seats,seed,focal+1,seat+1,lineup.getValue(seat),
                        if(winner==seat) 1 else 0,actions)+keys.map{counts[seat][it]?:0}).joinToString(",")
                }
            }
        // 5 courses x 7 opponents x 24 seeds x (2+4) starting seats.
        assertEquals(16800,rows.size)
        val out=File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"strategist_competition.csv").writeText(
            "course,opponent,seats,seed,strategist_seat,actor_seat,policy,won,match_actions,"+keys.joinToString(",")+"\n"+
            rows.joinToString("\n")+"\n")
    }
}
