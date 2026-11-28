package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Compare actual homogeneous R1/R2 engines: pacing and stability, not competitive strength. */
class AdaptiveRouteComparisonTest {
    @Test fun pairedPolicyRacesFinishAndReplay() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val boards = listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json","data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val rows = mutableListOf<String>()
        for (board in boards) for (seats in 2..4) for (seed in 1..24)
            for (policy in listOf(BotPolicyVersion.ADAPTIVE_V2_R1,BotPolicyVersion.ADAPTIVE_V2_R2)) {
                val game = GameSession(board,events,seats,seed,emptySet(),policy)
                var actions = 0
                var rolls = 0
                var routes = 0
                var encounters = 0
                var targets = 0
                while (game.phase != Phase.FINISHED && actions < 1500) {
                    when (game.phase) {
                        Phase.ROLL -> rolls++
                        Phase.ROUTE -> routes++
                        Phase.ENCOUNTER -> encounters++
                        Phase.TARGET -> targets++
                        Phase.FINISHED -> error("Unexpected finish")
                    }
                    game.botStep()
                    actions++
                }
                assertEquals("Stall: ${board.id}/$seats/$seed/$policy",Phase.FINISHED,game.phase)
                assertEquals(actions,rolls+routes+encounters+targets)
                val saved = game.checkpoint("routes-$seed",1L)
                assertEquals(game.snapshot(),GameSession.restore(board,events,saved).snapshot())
                rows += listOf(board.id,seats,seed,policy,requireNotNull(game.winnerIndex)+1,
                    actions,rolls,routes,encounters,targets).joinToString(",")
            }
        assertEquals(576,rows.size)
        val out = File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"route_samples.csv").writeText(
            "course,seats,seed,policy,winner_seat,actions,rolls,routes,encounters,targets\n"+
                rows.joinToString("\n")+"\n")
    }
}
