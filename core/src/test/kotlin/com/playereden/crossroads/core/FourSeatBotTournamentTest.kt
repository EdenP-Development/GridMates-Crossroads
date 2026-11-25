package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Balanced cyclic rotations, not all permutations. Actual mixed four-seat engine. */
class FourSeatBotTournamentTest {
    @Test fun rotateEveryPolicyThroughEverySeatAndReplay() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val boards = listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json","data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val policies = BotPolicyVersion.entries
        assertEquals(5,policies.size)
        val rows = mutableListOf<String>()
        for (board in boards) for (omitted in policies) for (seed in 1..12) {
            val included = policies.filter { it != omitted }
            for (rotation in 0..3) {
                val lineup = (0..3).map { included[(it+rotation)%4] }
                val overrides = lineup.mapIndexed { seat,policy -> seat to policy }.toMap()
                val game = GameSession(board,events,4,seed,emptySet(),botSeatPolicies=overrides)
                var actions = 0
                var targets = 0
                while (game.phase != Phase.FINISHED && actions < 1500) {
                    if (game.phase == Phase.TARGET) targets++
                    game.botStep()
                    actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                val winner = requireNotNull(game.winnerIndex)
                val saved = MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                    game.checkpoint("four-$seed",1L)))
                assertEquals(overrides,saved.botSeatPolicies)
                assertEquals(game.snapshot(),GameSession.restore(board,events,saved).snapshot())
                rows += (listOf(board.id,seed,omitted,rotation) + lineup +
                    listOf(winner+1,lineup[winner],actions,targets)).joinToString(",")
            }
        }
        assertEquals(960,rows.size)
        val out = File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"four_seat_tournament.csv").writeText(
            "course,seed,omitted,rotation,seat1,seat2,seat3,seat4,winner_seat,winner_policy,actions,targets\n"+
            rows.joinToString("\n")+"\n")
    }
}
