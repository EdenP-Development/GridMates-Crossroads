package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Compare actual existing policies, not hypothetical labels or difficulty ranks. */
class BotStrategyCalibrationTest {
    @Test fun profileStrategiesOnOriginalAndConvergingCourses() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val boards=listOf("data/boards/woodland_woven_trail.json",
            "design/prototypes/woodland_woven_convergence_v02.json").map {
            DecisionLab.variantV5(GameData.board(File(root,it).readText()))
        }
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        val rows=mutableListOf<String>()
        for(board in boards) for(preset in BotStrategyPreset.entries)
            for(seats in 2..4) for(seed in 1..24) {
                val game=GameSession(board,events,seats,seed,emptySet(),preset.policy)
                var actions=0
                var rolls=0
                var steady=0
                var unknownRoll=0
                var low=0
                var medium=0
                var high=0
                var unknownRoute=0
                var supplies=0
                var cover=0
                var gamble=0
                var brace=0
                var unknownEncounter=0
                var targets=0
                while(game.phase!=Phase.FINISHED && actions<1500) {
                    val actor=game.players[game.currentIndex]
                    when(game.phase) {
                        Phase.ROLL -> {
                            rolls++
                            game.botStep()
                            // V5 steady is exactly four, push uses 1..6.
                            // Last message and die can be overwritten by movement;
                            // do not infer travel stance from a die value of four.
                            if(game.steadyTravelChosen) steady++
                            else unknownRoll++
                        }
                        Phase.ROUTE -> {
                            val fork=board.forkAt(actor.position)
                            val before=actor.traversed.size
                            game.botStep()
                            when(fork?.choices?.firstOrNull { it.to==actor.traversed.getOrNull(before) }?.risk) {
                                "low" -> low++
                                "medium" -> medium++
                                "high" -> high++
                                else -> unknownRoute++
                            }
                        }
                        Phase.ENCOUNTER -> {
                            val options=game.availableEncounterChoices()
                            game.botStep()
                            when(game.lastEventId) {
                                "lab_v3_shelter_supplies" -> supplies++
                                "lab_v3_shelter_cover" -> cover++
                                "lab_v3_hazard_brace" -> brace++
                                else -> if("gamble" in options && game.lastEventId!=null) gamble++ else unknownEncounter++
                            }
                        }
                        Phase.TARGET -> { targets++;game.botStep() }
                        Phase.FINISHED -> error("Unexpected finished loop")
                    }
                    actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                assertEquals(rolls,steady+unknownRoll)
                assertEquals(game.snapshot(),GameSession.restore(board,events,
                    game.checkpoint("calibration-$seed",1L)).snapshot())
                rows+=listOf(board.id,preset, seats,seed,actions,rolls,steady,unknownRoll,
                    low,medium,high,unknownRoute,supplies,cover,gamble,brace,unknownEncounter,
                    targets,requireNotNull(game.winnerIndex)+1).joinToString(",")
            }
        assertEquals(720,rows.size)
        val out=File(root,"core/build/reports/ai2-comparison")
        check(out.exists() || out.mkdirs())
        File(out,"strategy_calibration.csv").writeText(
            "course,preset,seats,seed,actions,rolls,confirmed_steady,unclassified_rolls,low,medium,high,unknown_route,supplies,cover,gamble,brace,unknown_encounter,targets,winner_seat\n"+
            rows.joinToString("\n")+"\n")
    }
}
