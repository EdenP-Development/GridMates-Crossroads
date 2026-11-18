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
    }

    @Test fun initialForkAndFinishKeepMoreMapInView() {
        assertEquals(1.0f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 1, true), 0.0001f)
        assertEquals(1.0f, CrossroadsCameraPreset.followZoom(Phase.ROUTE, 1, true), 0.0001f)
        assertEquals(1.15f, CrossroadsCameraPreset.followZoom(Phase.ROUTE, 12, true), 0.0001f)
        assertEquals(1.28f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 12, true), 0.0001f)
        assertEquals(1.0f, CrossroadsCameraPreset.followZoom(Phase.FINISHED, 21, true), 0.0001f)
        assertEquals(1.0f, CrossroadsCameraPreset.followZoom(Phase.ROLL, 9, false), 0.0001f)
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
