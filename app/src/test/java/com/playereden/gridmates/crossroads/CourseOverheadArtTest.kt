package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.GameData
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class CourseOverheadArtTest {
    @Test fun siblingCoursesHaveDistinctSafeScenery() {
        val results = listOf(
            "woodland_brookside_quick" to CourseOverheadStyle.BROOKSIDE,
            "woodland_cedarwood_long" to CourseOverheadStyle.CEDARWOOD
        ).map { (name, style) ->
            val file = listOf(File("data/boards/$name.json"), File("../data/boards/$name.json"))
                .first { it.isFile }
            val board = GameData.board(file.readText())
            val scenery = CourseOverheadScenery.plan(style, board.nodes)
            assertTrue(scenery.size >= 10)
            assertEquals(scenery, CourseOverheadScenery.plan(style, board.nodes))
            scenery.forEach { a ->
                assertTrue(OverheadScenery.clearOfPlay(a.x,a.y,board.nodes,.078f))
                assertTrue(scenery.filter { it != a }.all { b ->
                    kotlin.math.hypot(a.x-b.x,a.y-b.y) >= .09f
                })
            }
            scenery
        }
        assertNotEquals(results[0],results[1])
        assertTrue(results[0].count { it.sprite == EnvironmentSprite.FLOWERS } >
            results[1].count { it.sprite == EnvironmentSprite.FLOWERS })
        assertTrue(results[1].count { it.sprite == EnvironmentSprite.FIR } >
            results[0].count { it.sprite == EnvironmentSprite.FIR })
        assertNull(CourseOverheadStyle.forBoard("unknown"))
        assertFalse(useOverheadBoardPreview("unknown",true,true,true))
    }

    @Test fun regionalSceneryRespectsItsOwnGeometryAndQaGate() {
        val file = listOf(File("design/prototypes/woodland_woven_regional_v05.json"),
            File("../design/prototypes/woodland_woven_regional_v05.json")).first { it.isFile }
        val board = GameData.board(file.readText())
        val style = CourseOverheadStyle.REGIONAL
        assertEquals(style, CourseOverheadStyle.forBoard(board.id))
        assertEquals(style, CourseOverheadStyle.forBoard(board.id + com.playereden.crossroads.core.DecisionLab.V5_SUFFIX))
        assertTrue(useOverheadBoardPreview(board.id,true,true,true))
        assertFalse(useOverheadBoardPreview(board.id,false,true,true))
        val scenery = CourseOverheadScenery.plan(style,board.nodes)
        assertTrue("Regional scenery should exceed the original nine landmarks", scenery.size >= 15)
        assertEquals("Regional Journey", CrossroadsCourse.compactTitle(board.id))
        assertEquals("Woven Trail: Regional Journey", CrossroadsCourse.title(board.id))
        assertEquals(scenery,CourseOverheadScenery.plan(style,board.nodes))
        scenery.forEach { a ->
            val radius = a.sprite.diameter * kotlin.math.sqrt(2f) / 2f
            assertTrue(a.x in radius..(1f-radius))
            assertTrue(a.y in radius..(1f-radius))
            assertTrue(OverheadScenery.clearOfPlay(a.x,a.y,board.nodes,.078f))
            assertTrue(scenery.filter { it != a }.all { b ->
                kotlin.math.hypot(a.x-b.x,a.y-b.y) >= .09f
            })
        }
        val occupied = scenery.map { androidx.compose.ui.geometry.Offset(it.x,it.y) }
        val patches = WoodlandGroundcover.plan(board.nodes,style,occupied)
        assertTrue(patches.isNotEmpty())
        assertEquals(patches,WoodlandGroundcover.plan(board.nodes,style,occupied))
        patches.forEach { p ->
            assertTrue(OverheadScenery.clearOfPlay(p.x,p.y,board.nodes,.034f+p.radius))
            assertTrue(occupied.all { kotlin.math.hypot(p.x-it.x,p.y-it.y) >= .055f+p.radius })
        }
    }
}
