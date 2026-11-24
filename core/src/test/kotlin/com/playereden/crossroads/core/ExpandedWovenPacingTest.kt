package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class ExpandedWovenPacingTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
    @Test fun comparePrototypeWithOriginal() {
        val original = GameData.board(File(root,"data/boards/woodland_woven_trail.json").readText())
        val expanded = GameData.board(File(root,"design/prototypes/woodland_woven_expanded.json").readText())
        assertNotEquals(original.id,expanded.id)
        assertEquals(81,expanded.nodes.size)
        assertEquals(32,DecisionLab.minimumSpacesToFinish(expanded,expanded.start))
        assertEquals(original.forks.size,expanded.forks.size)
        for (node in original.nodes) {
            val copy = expanded.node(node.id)
            assertEquals(node.kind,copy.kind)
            assertEquals(node.eventPool,copy.eventPool)
            assertEquals(node.next,copy.next.map { expanded.node(it).next.single() })
        }
        val rows=mutableListOf("board,seats,seed,rounds,rolls,forks,encounters,targets")
        for (base in listOf(original,expanded)) for (board in listOf(base,DecisionLab.variantV5(base))) {
            for (seats in listOf(2,4)) for(seed in 1..60) {
                val game=GameSession(board,events,seats,seed,humanIndices=emptySet())
                var rolls=0; var forks=0; var encounters=0; var targets=0; var actions=0
                while(game.phase!=Phase.FINISHED && actions<2400) {
                    when(game.phase) {
                        Phase.ROLL -> rolls++
                        Phase.ROUTE -> forks++
                        Phase.ENCOUNTER -> encounters++
                        Phase.TARGET -> targets++
                        Phase.FINISHED -> Unit
                    }
                    game.botStep(); actions++
                }
                assertEquals(Phase.FINISHED,game.phase)
                rows.add(listOf(board.id,seats,seed,(game.turn-1)/seats+1,rolls,forks,encounters,targets).joinToString(","))
            }
        }
        assertEquals(481,rows.size)
        val dir=File(root,"core/build/reports/beta-pacing")
        dir.mkdirs()
        File(dir,"expanded-woven.csv").writeText(rows.joinToString("\n")+"\n")
        val summary=rows.drop(1).map{it.split(",")}.groupBy{it.take(2)}.map{(key,samples)->
            fun median(col:Int):Double {
                val values=samples.map{it[col].toInt()}.sorted()
                return (values[(values.size-1)/2]+values[values.size/2])/2.0
            }
            "${key.joinToString(" | ")}: median rounds=${median(3)}, rolls=${median(4)}, forks=${median(5)}, encounter choices=${median(6)}, targets=${median(7)}"
        }
        File(dir,"expanded-woven.txt").writeText(summary.joinToString("\n")+"\n")
    }
}
