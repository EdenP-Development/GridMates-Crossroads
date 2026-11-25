package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Actual homogeneous R2/R3 games; sample encounter decisions before/after each action. */
class AdaptiveEncounterComparisonTest {
    @Test fun compareEncounterResponsesAndMischief() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val boards = listOf("data/boards/woodland_brookside_quick.json",
            "data/boards/woodland_woven_trail.json","data/boards/woodland_cedarwood_long.json",
            "design/prototypes/woodland_woven_regional_v05.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val rows = mutableListOf<String>()
        for (board in boards) for (seats in 2..4) for (seed in 1..24)
            for (policy in listOf(BotPolicyVersion.ADAPTIVE_V2_R2,BotPolicyVersion.ADAPTIVE_V2_R3)) {
                val game = GameSession(board,events,seats,seed,emptySet(),policy)
                var actions = 0
                var rolls = 0
                var supplies = 0
                var cover = 0
                var brace = 0
                var gamble = 0
                var targets = 0
                var blocked = 0
                var distinctHazards = mutableSetOf<String>()
                val distance = BotRouteDistances(board)
                while (game.phase != Phase.FINISHED && actions < 1500) {
                    val phase = game.phase
                    if (phase == Phase.ROLL) rolls++
                    if (phase == Phase.TARGET) {
                        targets++
                        val target = game.availableTargets().minByOrNull {
                            distance.distance(game.players[it].position)
                        }
                        if (target != null && game.players[target].shield) blocked++
                    }
                    val encounter = phase == Phase.ENCOUNTER
                    game.botStep()
                    if (encounter) {
                        when (val id = game.lastEventId) {
                            "lab_v3_shelter_supplies" -> supplies++
                            "lab_v3_shelter_cover" -> cover++
                            "lab_v3_hazard_brace" -> brace++
                            else -> {
                                assertNotNull("Expected hazard result",id)
                                gamble++
                                distinctHazards.add(requireNotNull(id))
                            }
                        }
                    }
                    actions++
                }
                assertEquals("Stall: ${board.id}/$seats/$seed/$policy",Phase.FINISHED,game.phase)
                val saved = game.checkpoint("encounter-$seed",1L)
                assertEquals(game.snapshot(),GameSession.restore(board,events,saved).snapshot())
                assertTrue(blocked <= targets)
                rows += listOf(board.id,seats,seed,policy,actions,rolls,supplies,cover,
                    brace,gamble,targets,blocked,distinctHazards.size).joinToString(",")
            }
        assertEquals(576,rows.size)
        val out = File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"encounter_samples.csv").writeText(
            "course,seats,seed,policy,actions,rolls,supplies,cover,brace,gamble,targets,shielded_targets,distinct_hazard_ids\n"+
                rows.joinToString("\n")+"\n")
    }
}
