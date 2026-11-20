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
        assertEquals(2.15f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 1, true), 0.0001f)
        assertEquals(2.20f, CrossroadsCameraPreset.followZoom(Phase.ROUTE, 1, true), 0.0001f)
        assertEquals(2.20f, CrossroadsCameraPreset.followZoom(Phase.ROUTE, 12, true), 0.0001f)
        assertEquals(2.48f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 12, true), 0.0001f)
        assertEquals(2.60f, CrossroadsCameraPreset.followZoom(Phase.ENCOUNTER, 12, true), 0.0001f)
        assertEquals(2.40f, CrossroadsCameraPreset.followZoom(Phase.TARGET, 12, true), 0.0001f)
        assertEquals(1.0f, CrossroadsCameraPreset.followZoom(Phase.FINISHED, 21, true), 0.0001f)
        assertEquals(1.0f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 9, false), 0.0001f)
    }

    @Test fun cameraLooksAboveTheBottomDockAndStaysAtFork() {
        assertEquals(0.43f, CrossroadsCameraPreset.focusHeightFraction(Phase.ROLL, true), .0001f)
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
        assertEquals(1.67f, CrossroadsCameraPreset.trailScale(true), .0001f)
        assertEquals(1.55f, CrossroadsCameraPreset.markerScale(true), .0001f)
        assertEquals(1.91f, CrossroadsCameraPreset.tokenScale(true), .0001f)
    }

    @Test fun paintedPiecesAndRoadsArePrioritizedButFullMapIsUntouched() {
        val trail = CrossroadsCameraPreset.trailScale(true)
        val token = CrossroadsCameraPreset.tokenScale(true)
        val marker = CrossroadsCameraPreset.markerScale(true)
        assertTrue("Painted paths must be dominant, not hairlines", trail >= 1.6f)
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
