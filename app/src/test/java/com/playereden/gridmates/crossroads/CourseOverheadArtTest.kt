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
}
