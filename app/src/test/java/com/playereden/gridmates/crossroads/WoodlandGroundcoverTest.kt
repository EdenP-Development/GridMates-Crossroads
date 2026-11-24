package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import com.playereden.crossroads.core.GameData
import java.io.File
import kotlin.math.hypot
import org.junit.Assert.*
import org.junit.Test

class WoodlandGroundcoverTest {
    @Test fun everyCourseHasDeterministicGraphSafeSmallVegetation() {
        for ((name, style) in listOf(
            "woodland_brookside_quick" to CourseOverheadStyle.BROOKSIDE,
            "woodland_woven_trail" to CourseOverheadStyle.WOVEN,
            "woodland_cedarwood_long" to CourseOverheadStyle.CEDARWOOD
        )) {
            val file = listOf(File("data/boards/$name.json"),File("../data/boards/$name.json")).first { it.isFile }
            val nodes = GameData.board(file.readText()).nodes
            val features = if (style == CourseOverheadStyle.WOVEN) OverheadScenery.plan(nodes) else emptyList()
            val accents = if (style == CourseOverheadStyle.WOVEN)
                WovenEnvironmentAccents.plan(nodes,features) else CourseOverheadScenery.plan(style,nodes)
            val occupied = features.map { Offset(it.x,it.y) } + accents.map { Offset(it.x,it.y) }
            val patches = WoodlandGroundcover.plan(nodes,style,occupied)
            assertTrue(patches.isNotEmpty())
            assertTrue(patches.size <= 400)
            assertEquals(patches,WoodlandGroundcover.plan(nodes,style,occupied))
            patches.forEach { p ->
                assertTrue(p.radius in .006f.. .0121f)
                assertTrue(OverheadScenery.clearOfPlay(p.x,p.y,nodes,.034f+p.radius))
                assertTrue(occupied.all { hypot(p.x-it.x,p.y-it.y) >= .055f+p.radius })
                assertTrue(patches.filter { it != p }.all { q ->
                    hypot(p.x-q.x,p.y-q.y) >= p.radius+q.radius+.006f
                })
            }
        }
    }
}
