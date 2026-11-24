package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class BetaPacingAuditTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
    @Test fun baselineRacePacingAndDecisionExposure() {
        val report = mutableListOf("course,rules,seats,seed,rounds,rolls,forks,encounter_choices,target_choices,actions")
        for (name in listOf("woodland_brookside_quick","woodland_woven_trail","woodland_cedarwood_long")) {
            val base = GameData.board(File(root,"data/boards/$name.json").readText())
            for (board in listOf(base,DecisionLab.variantV4(base),DecisionLab.variantV5(base))) {
                for (seats in listOf(2,4)) for (seed in 1..60) {
                    val game = GameSession(board,events,seats,seed,humanIndices=emptySet())
                    var rolls=0
                    var forks=0
                    var encounters=0
                    var targets=0
                    var actions=0
                    while (game.phase != Phase.FINISHED && actions < 2400) {
                        when (game.phase) {
                            Phase.ROLL -> rolls++
                            Phase.ROUTE -> forks++
                            Phase.ENCOUNTER -> encounters++
                            Phase.TARGET -> targets++
                            Phase.FINISHED -> Unit
                        }
                        game.botStep()
                        actions++
                    }
                    assertEquals(Phase.FINISHED,game.phase)
                    assertEquals(board.finish,game.players[requireNotNull(game.winnerIndex)].position)
                    assertEquals(actions,rolls+forks+encounters+targets)
                    report.add(listOf(base.id,board.id,seats,seed,(game.turn-1)/seats+1,
                        rolls,forks,encounters,targets,actions).joinToString(","))
                }
            }
        }
        assertEquals(1081,report.size)
        val dir=File(root,"core/build/reports/beta-pacing")
        dir.mkdirs()
        File(dir,"baseline.csv").writeText(report.joinToString("\n")+"\n")
        val summary=report.drop(1).map { it.split(",") }.groupBy { it.take(3) }.map { (key,rows) ->
            fun median(column:Int):Double {
                val values=rows.map { it[column].toInt() }.sorted()
                return (values[(values.size-1)/2]+values[values.size/2])/2.0
            }
            "${key.joinToString(" | ")}: median rounds=${median(4)}, rolls=${median(5)}, forks=${median(6)}, encounter choices=${median(7)}, targets=${median(8)}"
        }
        File(dir,"summary.txt").writeText(summary.joinToString("\n")+"\n")
        println("BETA PACING BASELINE (synthetic actions, not wall-clock minutes)")
        summary.forEach { println(it) }
    }
}
