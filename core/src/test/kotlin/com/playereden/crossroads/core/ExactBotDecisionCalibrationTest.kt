package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Exact accepted nested action receipts; never infer choices from outcomes. */
class ExactBotDecisionCalibrationTest {
    @Test fun profileAndReplayExactDecisions() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val boards=listOf("data/boards/woodland_woven_trail.json",
            "design/prototypes/woodland_woven_convergence_v02.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val rows=mutableListOf<String>()
        for(board in boards) for(preset in listOf(BotStrategyPreset.CLASSIC, BotStrategyPreset.TRAVELER, BotStrategyPreset.SCOUT, BotStrategyPreset.GUARDIAN, BotStrategyPreset.STRATEGIST))
            for(seats in 2..4) for(seed in 1..24) {
                val game=GameSession(board,events,seats,seed,emptySet(),preset.policy)
                val counts=mutableMapOf<String,Int>()
                fun count(key:String) { counts[key]=(counts[key]?:0)+1 }
                var actions=0
                while(game.phase!=Phase.FINISHED && actions<1500) {
                    val phase=game.phase
                    val fork=board.forkAt(game.players[game.currentIndex].position)
                    val routes=game.availableRoutes().map { it.id }
                    val options=game.availableEncounterChoices()
                    val targets=game.availableTargets()
                    game.botStep()
                    val receipt=game.lastBotDecision
                    when(phase) {
                        Phase.ROLL -> {
                            assertNotNull(receipt)
                            when(requireNotNull(receipt).kind) {
                                MatchActionKind.STEADY_ROLL -> count("steady")
                                MatchActionKind.BOLD_ROLL -> count("push")
                                else -> fail("Unexpected travel receipt")
                            }
                        }
                        Phase.ROUTE -> {
                            assertEquals(MatchActionKind.ROUTE,receipt?.kind)
                            assertTrue(receipt?.routeId in routes)
                            val risk=requireNotNull(fork?.choices?.firstOrNull { it.to==receipt?.routeId }).risk
                            count(risk)
                        }
                        Phase.ENCOUNTER -> {
                            assertEquals(MatchActionKind.ENCOUNTER,receipt?.kind)
                            assertTrue(receipt?.routeId in options)
                            count(requireNotNull(receipt?.routeId))
                        }
                        Phase.TARGET -> {
                            if(targets.isEmpty()) { assertNull(receipt);count("target_pass") }
                            else {
                                assertEquals(MatchActionKind.TARGET,receipt?.kind)
                                assertTrue(receipt?.targetIndex in targets)
                                count("target")
                            }
                        }
                        Phase.FINISHED -> error("Unexpected finish")
                    }
                    actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                assertEquals(actions,counts.values.sum())
                val saved=game.checkpoint("exact-$seed",1L)
                assertTrue(saved.actions.all { it.kind==MatchActionKind.BOT })
                val restored=GameSession.restore(board,events,saved)
                assertEquals(game.snapshot(),restored.snapshot())
                assertEquals(game.lastBotDecision,restored.lastBotDecision)
                val keys=listOf("steady","push","low","medium","high","supplies","cover","gamble","brace","target","target_pass")
                rows+=(listOf(board.id,preset,seats,seed,actions)+keys.map{counts[it]?:0}+listOf(requireNotNull(game.winnerIndex)+1)).joinToString(",")
            }
        assertEquals(720,rows.size)
        val out=File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"exact_strategy_calibration.csv").writeText(
            "course,preset,seats,seed,actions,steady,push,low,medium,high,supplies,cover,gamble,brace,target,target_pass,winner_seat\n"+
            rows.joinToString("\n")+"\n")
    }
}
