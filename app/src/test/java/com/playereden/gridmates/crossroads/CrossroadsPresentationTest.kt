package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.Phase
import org.junit.Assert.*
import org.junit.Test

class CrossroadsPresentationTest {
    @Test fun courseTitlesAreSpecificAndNeverAlterStoredIds() {
        assertEquals("Woven Trail", CrossroadsCourse.title(CrossroadsCourse.CLASSIC_ID))
        assertEquals("Brookside Dash", CrossroadsCourse.title(CrossroadsCourse.QUICK_ID))
        assertEquals("Cedarwood Expedition", CrossroadsCourse.title(CrossroadsCourse.LONG_ID))
        assertEquals("Long race", CrossroadsCourse.subtitle(CrossroadsCourse.LONG_ID))
        assertEquals("Classic race", CrossroadsCourse.subtitle(CrossroadsCourse.CLASSIC_ID))
        assertEquals("Quick race", CrossroadsCourse.subtitle(CrossroadsCourse.QUICK_ID))
        assertEquals("First across Brookside Dash",
            CrossroadsCourse.resultLine(CrossroadsCourse.QUICK_ID))
        assertEquals("Woodland Crossing", CrossroadsCourse.title("unknown-future-board"))
        val lab = CrossroadsCourse.QUICK_ID + "__decision_lab_v1"
        assertEquals("Brookside Dash", CrossroadsCourse.title(lab))
        assertEquals("Gameplay 2.0 · Experimental", CrossroadsCourse.subtitle(lab))
        assertEquals("First across Brookside Dash · Playtest", CrossroadsCourse.resultLine(lab))
        val secondLab = CrossroadsCourse.LONG_ID + "__decision_lab_v2"
        assertEquals("Cedarwood Expedition", CrossroadsCourse.title(secondLab))
        assertEquals("Gameplay 2.0 · Lab v2", CrossroadsCourse.subtitle(secondLab))
        assertEquals("First across Cedarwood Expedition · Playtest",
            CrossroadsCourse.resultLine(secondLab))
        val thirdLab = CrossroadsCourse.QUICK_ID + "__decision_lab_v3"
        assertEquals("Brookside Dash", CrossroadsCourse.title(thirdLab))
        assertEquals("Gameplay 2.0 · Lab v3", CrossroadsCourse.subtitle(thirdLab))
        assertEquals("First across Brookside Dash · Playtest",
            CrossroadsCourse.resultLine(thirdLab))
    }

    @Test fun liveWorldZoomsIntoActionAndFullMapRemainsWhole() {
        assertEquals(2.42f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 1, true), 0.0001f)
        assertEquals(2.16f, CrossroadsCameraPreset.followZoom(Phase.ROUTE, 1, true), 0.0001f)
        assertEquals(2.16f, CrossroadsCameraPreset.followZoom(Phase.ROUTE, 12, true), 0.0001f)
        assertEquals(2.52f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 12, true), 0.0001f)
        assertEquals(2.62f, CrossroadsCameraPreset.followZoom(Phase.ENCOUNTER, 12, true), 0.0001f)
        assertEquals(2.48f, CrossroadsCameraPreset.followZoom(Phase.TARGET, 12, true), 0.0001f)
        assertEquals(1.0f, CrossroadsCameraPreset.followZoom(Phase.FINISHED, 21, true), 0.0001f)
        assertEquals(1.0f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 9, false), 0.0001f)
    }

    @Test fun ownerPhoneCloseupIsDefaultYetNeverHidesDecisionOverview() {
        val firstRoll = CrossroadsCameraPreset.followZoom(Phase.ROLL, 1, true)
        val laterRoll = CrossroadsCameraPreset.followZoom(Phase.ROLL, 8, true)
        val atFork = CrossroadsCameraPreset.followZoom(Phase.ROUTE, 8, true)
        val event = CrossroadsCameraPreset.followZoom(Phase.ENCOUNTER, 8, true)
        val target = CrossroadsCameraPreset.followZoom(Phase.TARGET, 8, true)

        assertTrue("First roll should showcase illustrated scenery", firstRoll >= 2.4f)
        assertTrue("Later turns should remain close to the action", laterRoll > firstRoll)
        assertTrue("A fork needs more route context than the traveling camera", atFork < firstRoll)
        assertTrue("At least some scenery stays visible beyond the chosen path",
            atFork >= 2.0f)
        assertTrue("Event art should be readable without manual pinching", event > laterRoll)
        assertTrue("Targeting is a closer view than choosing a fork", target > atFork)
        for (zoom in listOf(firstRoll, laterRoll, atFork, event, target)) {
            assertTrue("Auto zoom stays below manual-pinch maximum",
                zoom < com.playereden.crossroads.core.CameraGeometry.MAX_ZOOM)
        }
        assertEquals("Read-only map must always remain 1x",
            1f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 8, false), .0001f)
    }

    @Test fun adaptiveForkPreviewKeepsCompactForksCloseAndWidensSpreadOutForks() {
        val source = androidx.compose.ui.geometry.Offset(.5f, .5f)
        val compact = listOf(
            androidx.compose.ui.geometry.Offset(.52f, .48f),
            androidx.compose.ui.geometry.Offset(.48f, .52f)
        )
        val broad = listOf(
            androidx.compose.ui.geometry.Offset(.25f, .5f),
            androidx.compose.ui.geometry.Offset(.75f, .5f),
            androidx.compose.ui.geometry.Offset(.5f, .7f)
        )
        val frame = BoardSceneFrame(-280f, -80f, 960f, 960f)
        val tight = CrossroadsCameraPreset.forkPreview(source, compact, frame, 400f, 800f)
        val wide = CrossroadsCameraPreset.forkPreview(source, broad, frame, 400f, 800f)
        assertEquals(2.16f, tight.zoom, .0001f)
        assertTrue(wide.zoom < tight.zoom)
        assertTrue(wide.zoom >= 1.65f)
        assertEquals(.5f, wide.focus.x, .0001f)
        assertEquals(.56f, wide.focus.y, .0001f)
        assertTrue(CrossroadsCameraPreset.FORK_PREVIEW_HEIGHT < .40f)
    }

    @Test fun adaptiveForkPreviewRespondsToAspectRatioAndMissingViewport() {
        val source = androidx.compose.ui.geometry.Offset(.5f, .5f)
        val exits = listOf(
            androidx.compose.ui.geometry.Offset(.38f, .5f),
            androidx.compose.ui.geometry.Offset(.62f, .5f)
        )
        val frame = BoardSceneFrame(0f, 0f, 960f, 960f)
        val narrow = CrossroadsCameraPreset.forkPreview(source, exits, frame, 300f, 800f)
        val wide = CrossroadsCameraPreset.forkPreview(source, exits, frame, 600f, 800f)
        assertTrue(narrow.zoom < wide.zoom)
        assertEquals(2.16f, wide.zoom, .0001f)
        val fallback = CrossroadsCameraPreset.forkPreview(source, exits, frame, 0f, 800f)
        assertEquals(2.16f, fallback.zoom, .0001f)
        assertEquals(CrossroadsCameraPreset.forkAnchor(source, exits, true), fallback.focus)
        assertEquals(source, CrossroadsCameraPreset.forkPreview(
            source, emptyList(), frame, 400f, 800f
        ).focus)
    }

    @Test fun routeDecisionKeepsJunctionVisibleAboveChoiceSheet() {
        val focus = CrossroadsCameraPreset.focusHeightFraction(Phase.ROUTE, true)
        val regular = CrossroadsCameraPreset.focusHeightFraction(Phase.ROLL, true)
        assertTrue("Route focus should sit higher than ordinary rolling", focus < regular)
        assertTrue("Route fork should be above an approximately 40% height sheet", focus < .40f)
        assertTrue("Route camera stays detailed enough for the illustrated board",
            CrossroadsCameraPreset.followZoom(Phase.ROUTE, 3, true) > 1.5f)
    }

    @Test fun cameraLooksAboveTheBottomDockAndStaysAtFork() {
        assertEquals(0.43f, CrossroadsCameraPreset.focusHeightFraction(Phase.ROLL, true), .0001f)
        assertEquals(0.30f, CrossroadsCameraPreset.focusHeightFraction(Phase.ROUTE, true), .0001f)
        assertEquals(0.50f, CrossroadsCameraPreset.focusHeightFraction(Phase.ROUTE, false), .0001f)
        assertEquals(0.38f, CrossroadsCameraPreset.focusHeightFraction(Phase.ENCOUNTER, true), .0001f)
        assertEquals(0.50f, CrossroadsCameraPreset.focusHeightFraction(Phase.ENCOUNTER, false), .0001f)
        val source = androidx.compose.ui.geometry.Offset(.50f, .50f)
        val exits = listOf(
            androidx.compose.ui.geometry.Offset(.15f, .24f),
            androidx.compose.ui.geometry.Offset(.85f, .24f)
        )
        val focused = CrossroadsCameraPreset.forkAnchor(source, exits, true)
        assertEquals(.50f, focused.x, .0001f)
        assertTrue("Fork must remain the anchor, not empty scenery", focused.y > .40f)
        assertEquals(source, CrossroadsCameraPreset.forkAnchor(source, emptyList(), true))
    }

    @Test fun edgeOfWorldTravelerRemainsAboveActionDock() {
        val height = 1536f
        val zoom = 2.4f
        val lo = CrossroadsCameraPreset.boundFollowVertical(
            -10000f, height, 0f, height, zoom, Phase.ROLL, true
        )
        val screenBottom = height / 2f + (height - height / 2f) * zoom + lo
        assertEquals("World bottom should live under the roll dock",
            height * .68f, screenBottom, .01f)

        val hi = CrossroadsCameraPreset.boundFollowVertical(
            10000f, height, 0f, height, zoom, Phase.ROLL, true
        )
        val screenTop = height / 2f + (0f - height / 2f) * zoom + hi
        assertEquals("World top should live under the status HUD",
            height * .18f, screenTop, .01f)

        val encounter = CrossroadsCameraPreset.boundFollowVertical(
            -10000f, height, 0f, height, zoom, Phase.ENCOUNTER, true
        )
        assertTrue("Tall encounter dock needs more reserved space", encounter < lo)
    }

    @Test fun fullMapNeverReceivesImmersiveHudClamping() {
        val expected = com.playereden.crossroads.core.CameraGeometry.boundTranslation(
            -10000f, 800f, 18f, 782f, 2f
        )
        assertEquals(expected, CrossroadsCameraPreset.boundFollowVertical(
            -10000f, 800f, 18f, 782f, 2f, Phase.ENCOUNTER, false
        ), .0001f)
    }

    @Test fun onlyLiveMatchGetsLargerRoadsAndTravelers() {
        assertEquals(1.0f, CrossroadsCameraPreset.trailScale(false), .0001f)
        assertEquals(1.0f, CrossroadsCameraPreset.markerScale(false), .0001f)
        assertEquals(1.0f, CrossroadsCameraPreset.tokenScale(false), .0001f)
        assertEquals(1.896f, CrossroadsCameraPreset.trailScale(true), .0001f)
        assertEquals(1.86f, CrossroadsCameraPreset.markerScale(true), .0001f)
        assertEquals(2.292f, CrossroadsCameraPreset.tokenScale(true), .0001f)
    }

    @Test fun physicalArtAndGameplaySymbolsGrowTogether() {
        val scale = BoardWorldViewport.LIVE_ART_SCALE
        assertTrue("Enlarged art must actually occupy more screen pixels", scale > 1f)
        assertEquals(1.58f * scale, CrossroadsCameraPreset.trailScale(true), .0001f)
        assertEquals(1.91f * scale, CrossroadsCameraPreset.tokenScale(true), .0001f)
        assertEquals(1.55f * scale, CrossroadsCameraPreset.markerScale(true), .0001f)
        // Never let a close-up in normal racing change the Full Map's scale.
        assertEquals(1f, CrossroadsCameraPreset.trailScale(false), .0001f)
        assertEquals(1f, CrossroadsCameraPreset.tokenScale(false), .0001f)
        assertEquals(1f, CrossroadsCameraPreset.markerScale(false), .0001f)
    }

    @Test fun paintedPiecesAndRoadsArePrioritizedButFullMapIsUntouched() {
        val trail = CrossroadsCameraPreset.trailScale(true)
        val token = CrossroadsCameraPreset.tokenScale(true)
        val marker = CrossroadsCameraPreset.markerScale(true)
        assertTrue("Painted paths must be dominant, not hairlines", trail >= 1.5f)
        assertTrue("Traveler portraits should be plainly visible", token >= 1.8f)
        assertTrue("Branch junctions should be legible", marker >= 1.5f)
        assertTrue("Pieces must remain larger than their junctions", token > marker)
        assertEquals(1f, CrossroadsCameraPreset.tokenScale(false), .0001f)
        assertEquals(1f, CrossroadsCameraPreset.trailScale(false), .0001f)
        assertEquals(1f, CrossroadsCameraPreset.markerScale(false), .0001f)
    }

    @Test fun fourPiecesStaySeparatedAtSharedStartAndFocusOffsetMatchesRendering() {
        for (count in 1..4) {
            val offsets = (0 until count).map {
                CrossroadsPiecePlacement.offset(it, count, 1f)
            }
            assertEquals(count, offsets.distinct().size)
            for (i in offsets.indices) {
                for (j in i + 1 until offsets.size) {
                    val distance = (offsets[i] - offsets[j]).getDistance()
                    assertTrue("Seats $i/$j in $count-person cluster overlap: $distance",
                        distance >= 35f)
                }
                assertEquals(offsets[i],
                    CrossroadsPiecePlacement.offset(i, count, 1f))
            }
        }
        assertEquals(2f * CrossroadsPiecePlacement.offset(0, 4, 1f).x,
            CrossroadsPiecePlacement.offset(0, 4, 2f).x, 0.0001f)
    }
}
