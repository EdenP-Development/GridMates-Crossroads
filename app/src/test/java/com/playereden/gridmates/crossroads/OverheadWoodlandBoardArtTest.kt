package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.BoardNode
import com.playereden.crossroads.core.GameData
import com.playereden.crossroads.core.DecisionLab
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class OverheadWoodlandBoardArtTest {
    private val graph = listOf(
        BoardNode("start", "meadow", "start", listOf("fork"), .12f, .78f),
        BoardNode("fork", "meadow", "fork", listOf("safe", "risky"), .48f, .50f),
        BoardNode("safe", "forest", "event", listOf("finish"), .74f, .31f),
        BoardNode("risky", "forest", "event", listOf("finish"), .67f, .66f),
        BoardNode("finish", "village", "finish", emptyList(), .92f, .14f)
    )

    private val wovenBoard by lazy {
        val root = listOf(File("data"), File("../data"))
            .first { File(it, "boards/woodland_woven_trail.json").isFile }
        GameData.board(File(root, "boards/woodland_woven_trail.json").readText())
    }

    @Test fun fullAuthoredWovenGraphKeepsOrnamentsClearOfEveryRealPath() {
        // Real course review, not just the five-node synthetic test fixture.
        val board = wovenBoard
        assertEquals(38, board.nodes.size)
        val ornaments = OverheadScenery.plan(board.nodes)
        assertEquals("Changing route topology requires new scenic placement QA",
            23, ornaments.size)
        assertEquals("Village should read as a destination, not a solitary icon",
            2, ornaments.count { it.kind == OverheadDecoration.COTTAGE })
        ornaments.forEach { p ->
            val margin = if (p.kind == OverheadDecoration.COTTAGE) .084f else .067f
            assertTrue("Scenery at ${p.x},${p.y} overlaps Woven graph",
                OverheadScenery.clearOfPlay(p.x, p.y, board.nodes, margin))
        }
    }

    @Test fun WovenHasThreeDistinctRegionalLandmarksInSafeClearings() {
        val ornaments = OverheadScenery.plan(wovenBoard.nodes)
        val kinds = setOf(OverheadDecoration.MEADOW_BLOOM,
            OverheadDecoration.FIR_GROVE, OverheadDecoration.ORCHARD)
        val landmarks = ornaments.filter { it.kind in kinds }
        assertEquals(3, landmarks.size)
        assertEquals(kinds, landmarks.map { it.kind }.toSet())
        landmarks.forEach { p ->
            assertTrue(OverheadScenery.clearOfPlay(p.x, p.y, wovenBoard.nodes, .09f))
            assertTrue(ornaments.filter { it != p }.all { q ->
                kotlin.math.hypot(p.x - q.x, p.y - q.y) >= .085f
            })
        }
        assertEquals(landmarks, OverheadScenery.regionalLandmarks(
            wovenBoard.nodes, ornaments.filter { it.kind !in kinds }
        ))
        // A moved playable node must suppress the decoration, never vice versa.
        val bloom = landmarks.first { it.kind == OverheadDecoration.MEADOW_BLOOM }
        val blocked = wovenBoard.nodes + BoardNode(
            "qa_blocker", "meadow", "plain", emptyList(), bloom.x, bloom.y
        )
        assertFalse(OverheadScenery.regionalLandmarks(blocked, emptyList())
            .any { it.kind == OverheadDecoration.MEADOW_BLOOM })
        assertTrue(OverheadScenery.regionalLandmarks(emptyList(), emptyList()).isEmpty())
    }

    @Test fun WovenStreamBridgesOnlyActualLegalRoutes() {
        val bridges = OverheadScenery.bridges(wovenBoard.nodes)
        assertEquals("Two streams must cross real graph edges", 2, bridges.size)
        assertEquals(bridges, OverheadScenery.bridges(wovenBoard.nodes))
        val crossings = listOf(
            "F4" to "V1",
            "F4" to "O1"
        )
        val byId = wovenBoard.nodes.associateBy { it.id }
        bridges.forEach { bridge ->
            assertTrue(bridge.x in 0f..1f && bridge.y in 0f..1f)
            assertEquals(1f, kotlin.math.hypot(
                bridge.directionX, bridge.directionY
            ), .001f)
            assertTrue("A bridge cannot create a new path or node",
                crossings.any { (a, b) ->
                    val from = requireNotNull(byId[a])
                    val to = requireNotNull(byId[b])
                    val onSegment = OverheadScenery.distanceToEdge(
                        bridge.x, bridge.y, from.x, from.y, to.x, to.y
                    ) < .008f
                    val endpointBuffer = .050f
                    val awayFromStops = kotlin.math.hypot(
                        bridge.x - from.x, bridge.y - from.y
                    ) > endpointBuffer && kotlin.math.hypot(
                        bridge.x - to.x, bridge.y - to.y
                    ) > endpointBuffer
                    onSegment && awayFromStops
                })
        }
        assertTrue(OverheadScenery.bridges(emptyList()).isEmpty())
    }

    @Test fun optionalOverheadPerspectiveAppliesToSavedWovenLabVersionsOnly() {
        for (suffix in listOf(
            DecisionLab.SUFFIX, DecisionLab.V2_SUFFIX, DecisionLab.V3_SUFFIX,
            DecisionLab.V4_SUFFIX, DecisionLab.V5_SUFFIX
        )) {
            assertTrue(useOverheadBoardPreview(
                "woodland_woven_trail_v02" + suffix, true, true, true
            ))
            assertFalse(useOverheadBoardPreview(
                "woodland_brookside_quick_v01" + suffix, true, true, true
            ))
        }
    }

    @Test fun overheadPrintIsOnlyAnOptInWovenQaPreview() {
        assertTrue(useOverheadBoardPreview(
            "woodland_woven_trail_v02", true, true, true
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_woven_trail_v02", false, true, true
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_woven_trail_v02", true, false, true
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_woven_trail_v02", true, true, false
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_brookside_quick_v01", true, true, true
        ))
        assertFalse(useOverheadBoardPreview(
            "woodland_cedarwood_long_v01", true, true, true
        ))
    }

    @Test fun overheadFeaturesStayAwayFromRealRoadsAndMarkers() {
        assertFalse(OverheadScenery.clearOfPlay(.48f, .50f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(.30f, .64f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(.92f, .14f, graph, .067f))
        assertTrue(OverheadScenery.clearOfPlay(.10f, .12f, graph, .067f))
        val ornaments = OverheadScenery.plan(graph)
        assertTrue(ornaments.isNotEmpty())
        ornaments.forEach { place ->
            val margin = if (place.kind == OverheadDecoration.COTTAGE) .084f else .067f
            assertTrue("Scenery cannot masquerade as a legal route or covered node",
                OverheadScenery.clearOfPlay(place.x, place.y, graph, margin))
        }
    }

    @Test fun sceneryIsDeterministicAndBoundedAcrossRecompositions() {
        val a = OverheadScenery.plan(graph)
        val b = OverheadScenery.plan(graph)
        assertEquals(a, b)
        assertTrue(a.size <= 53)
        assertEquals(a.size, a.distinct().size)
        assertTrue(a.all { it.x in 0f..1f && it.y in 0f..1f })
        assertTrue(OverheadScenery.plan(emptyList()).isEmpty())
    }

    @Test fun graphSegmentDistanceHandlesEndpointsAndShortSegments() {
        assertEquals(0f, OverheadScenery.distanceToEdge(.5f, .5f,
            .5f, .5f, .5f, .5f), .001f)
        assertEquals(.2f, OverheadScenery.distanceToEdge(.5f, .7f,
            .2f, .5f, .8f, .5f), .001f)
        assertEquals(.3f, OverheadScenery.distanceToEdge(.9f, .5f,
            .3f, .5f, .6f, .5f), .001f)
    }

    @Test fun invalidOutOfBoundsDecorationPointsNeverPassClearance() {
        assertFalse(OverheadScenery.clearOfPlay(Float.NaN, .5f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(-.1f, .5f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(.5f, 1.1f, graph, .067f))
        assertFalse(OverheadScenery.clearOfPlay(.5f, .5f, graph, 0f))
    }
}
