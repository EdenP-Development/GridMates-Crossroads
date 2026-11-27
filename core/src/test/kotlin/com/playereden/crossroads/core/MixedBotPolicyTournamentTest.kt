package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Real competing policies, each seat independently assigned; all pairs play both orders. */
class MixedBotPolicyTournamentTest {
    @Test fun pairwiseTournamentAndReplay() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val boards = listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json","data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val policies = listOf(BotPolicyVersion.LEGACY_V1, BotPolicyVersion.ADAPTIVE_V2_R1, BotPolicyVersion.ADAPTIVE_V2_R2, BotPolicyVersion.ADAPTIVE_V2_R3, BotPolicyVersion.ADAPTIVE_V2_R4)
        val rows = mutableListOf<String>()
        for (board in boards) for (a in policies.indices) for (b in a+1 until policies.size)
            for (seed in 1..12) for (swap in listOf(false,true)) {
                val lineup = if (swap) listOf(policies[b],policies[a])
                    else listOf(policies[a],policies[b])
                val overrides = lineup.mapIndexed { i, policy -> i to policy }.toMap()
                val game = GameSession(board,events,2,seed,emptySet(),
                    botSeatPolicies = overrides)
                var actions = 0
                var targets = 0
                while (game.phase != Phase.FINISHED && actions < 1500) {
                    if (game.phase == Phase.TARGET) targets++
                    assertEquals(lineup[game.currentIndex],game.policyForBotSeat(game.currentIndex))
                    game.botStep()
                    actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                val winner = requireNotNull(game.winnerIndex)
                val wire = MatchCheckpointCodec.encode(game.checkpoint("mixed-$seed",1L))
                val saved = MatchCheckpointCodec.decode(wire)
                assertEquals(overrides,saved.botSeatPolicies)
                val restored = GameSession.restore(board,events,saved)
                assertEquals(game.snapshot(),restored.snapshot())
                val rematch = restored.freshRematch(seed+100)
                assertEquals(lineup, (0..1).map { rematch.policyForBotSeat(it) })
                rows += listOf(board.id,seed,lineup[0],lineup[1],winner+1,lineup[winner],
                    actions,targets).joinToString(",")
            }
        assertEquals(960,rows.size)
        val out = File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"mixed_tournament.csv").writeText(
            "course,seed,seat1_policy,seat2_policy,winner_seat,winner_policy,actions,targets\n"+
            rows.joinToString("\n")+"\n")
    }

    @Test fun overridesCannotTargetHumanOrInvalidSeat() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val board = GameData.board(File(root,"data/boards/woodland_woven_trail.json").readText())
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        for (seat in listOf(-1,0,2)) {
            try {
                GameSession(board,events,2,17,setOf(0),
                    botSeatPolicies=mapOf(seat to BotPolicyVersion.ADAPTIVE_V2_R4))
                fail("Invalid override accepted: $seat")
            } catch (_: IllegalArgumentException) {}
        }
        val legacy = GameSession(board,events)
        assertFalse(MatchCheckpointCodec.encode(legacy.checkpoint("legacy",1L)).contains("botSeatPolicies"))
    }
}
