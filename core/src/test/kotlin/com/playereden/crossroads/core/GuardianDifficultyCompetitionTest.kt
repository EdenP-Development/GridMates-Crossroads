package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Mirrored Guardian tier comparisons, with every ordered focal/opponent pair. */
class GuardianDifficultyCompetitionTest {
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
        val policies=listOf(BotPolicyVersion.GUARDIAN_RELAXED_CANDIDATE_V1,
            BotPolicyVersion.GUARDIAN_V1, BotPolicyVersion.GUARDIAN_FINISH_CANDIDATE_V1)
        val keys=listOf("steady","push","low","medium","high","supplies","cover","gamble","brace","target","target_pass")
        val rows=mutableListOf<String>()
        for(board in boards) for(focalPolicy in policies) for(opponent in policies)
            if(focalPolicy != opponent) for(seats in listOf(2,4))
            if(seats==4 || policies.indexOf(focalPolicy)<policies.indexOf(opponent))
            for(seed in 1..24) for(focal in 0 until seats) {
                val lineup=(0 until seats).associateWith {
                    if(it==focal) focalPolicy else opponent
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
                    game.checkpoint("guardian-tier-$seed",1L)))
                assertEquals(lineup,saved.botSeatPolicies)
                assertEquals(game.snapshot(),GameSession.restore(board,events,saved).snapshot())
                for(seat in 0 until seats) {
                    rows+=(listOf(board.id,focalPolicy,opponent,seats,seed,focal+1,seat+1,lineup.getValue(seat),
                        if(winner==seat) 1 else 0,actions)+keys.map{counts[seat][it]?:0}).joinToString(",")
                }
            }
        // 5 courses x 24 seeds x (3 unordered pairs x 2 seats + 6 ordered pairs x 4 seats).
        assertEquals(12960,rows.size)
        val out=File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"guardian_difficulty_competition.csv").writeText(
            "course,focal_policy,opponent,seats,seed,guardian_seat,actor_seat,policy,won,match_actions,"+keys.joinToString(",")+"\n"+
            rows.joinToString("\n")+"\n")
    }
}
