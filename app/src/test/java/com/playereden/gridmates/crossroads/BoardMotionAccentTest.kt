package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

class BoardMotionAccentTest {
    @Test fun aStepReturnsExactlyToBoardSurface() {
        assertEquals(0f, BoardMotionAccent.hopOffsetPx(0f, 1.5f), .0001f)
        assertEquals(0f, BoardMotionAccent.hopOffsetPx(1f, 1.5f), .0001f)
        assertTrue(BoardMotionAccent.hopOffsetPx(.5f, 1.5f) > 7f)
        assertEquals(
            BoardMotionAccent.hopOffsetPx(.25f, 2f),
            BoardMotionAccent.hopOffsetPx(.75f, 2f),
            .001f
        )
    }

    @Test fun theVisualHopOnlyScalesWithTokenAppearance() {
        val normal = BoardMotionAccent.hopOffsetPx(.5f, 1f)
        assertEquals(normal * 1.42f, BoardMotionAccent.hopOffsetPx(.5f, 1.42f), .001f)
        assertEquals(0f, BoardMotionAccent.hopOffsetPx(.5f, 0f), .001f)
        assertEquals(0f, BoardMotionAccent.hopOffsetPx(Float.NaN, 2f), .001f)
        assertEquals(0f, BoardMotionAccent.hopOffsetPx(.5f, Float.POSITIVE_INFINITY), .001f)
    }

    @Test fun arrivalPopAndRippleFadeWithoutChangingPositionOrSizeLimits() {
        assertEquals(1f, BoardMotionAccent.arrivalScale(0f), .0001f)
        assertEquals(1.16f, BoardMotionAccent.arrivalScale(1f), .0001f)
        assertEquals(1.16f, BoardMotionAccent.arrivalScale(2f), .0001f)
        assertEquals(1f, BoardMotionAccent.arrivalScale(Float.NaN), .0001f)
        assertEquals(0f, BoardMotionAccent.arrivalRippleAlpha(0f), .0001f)
        assertEquals(.76f, BoardMotionAccent.arrivalRippleAlpha(1f), .0001f)
        assertEquals(0f, BoardMotionAccent.arrivalRippleAlpha(Float.NaN), .0001f)
        assertEquals(0f, BoardMotionAccent.arrivalRippleAlpha(-1f), .0001f)
    }
}
