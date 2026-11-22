package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.playereden.crossroads.core.BoardNode
import org.junit.Assert.*
import org.junit.Test

/** Same square frame must be used for painting, camera focus and legal path taps. */
class BoardWorldViewportTest {
    private val origin = BoardNode("fork", "Meadow", "fork",
        listOf("east", "west"), .5f, .5f)
    private val west = BoardNode("west", "West", "event", emptyList(), .3f, .35f)
    private val east = BoardNode("east", "East", "event", emptyList(), .7f, .35f)

    @Test fun portraitWorldFillsViewportWithoutDistortingSquareArtwork() {
        val scene = BoardWorldViewport.frame(IntSize(400, 800), true)
        assertEquals(1.20f, BoardWorldViewport.LIVE_ART_SCALE, .001f)
        assertEquals(-280f, scene.left, .001f)
        assertEquals(-80f, scene.top, .001f)
        assertEquals(960f, scene.width, .001f)
        assertEquals(scene.width, scene.height, .001f)
        assertEquals(200f, scene.position(.5f, .5f).x, .001f)
        assertEquals(400f, scene.position(.5f, .5f).y, .001f)
    }

    @Test fun landscapeWorldAlsoFillsViewportAndPreservesProportions() {
        val scene = BoardWorldViewport.frame(IntSize(800, 400), true)
        assertEquals(-80f, scene.left, .001f)
        assertEquals(-280f, scene.top, .001f)
        assertEquals(960f, scene.width, .001f)
        assertEquals(960f, scene.height, .001f)
    }

    @Test fun illustratedWorldIsLargerButCenteredOnTheSameGraphPoint() {
        val viewport = IntSize(400, 800)
        val frame = BoardWorldViewport.frame(viewport, true)
        val regular = 800f
        assertTrue("Artwork must occupy more actual screen pixels than before",
            frame.width > regular)
        assertEquals("Graph center remains screen center",
            viewport.width / 2f, frame.position(.5f, .5f).x, .001f)
        assertEquals("Graph center remains screen center",
            viewport.height / 2f, frame.position(.5f, .5f).y, .001f)
        // This is an independent miniature view, NEVER a scaled live closeup.
        assertTrue(BoardWorldViewport.frame(viewport, false).width < frame.width)
    }

    @Test fun legacyFullMapFrameKeepsOriginalCoordinateSystem() {
        val frame = BoardWorldViewport.frame(IntSize(400, 400), false)
        assertEquals(18f, frame.left, .001f)
        assertEquals(18f, frame.top, .001f)
        assertEquals(364f, frame.width, .001f)
        assertEquals(364f, frame.height, .001f)
    }

    @Test fun paintedDestinationsAndScreenHitTargetsAgree() {
        val canvas = IntSize(400, 800)
        val westScreen = BoardRouteHitTest.screenPosition(
            west, canvas, 0f, 1f, Offset.Zero, immersiveWorld = true
        )
        val frame = BoardWorldViewport.frame(canvas, true)
        assertEquals(frame.position(west).x, westScreen.x, .001f)
        assertEquals(frame.position(west).y, westScreen.y, .001f)
        val selected = BoardRouteHitTest.select(
            tap = westScreen,
            origin = origin,
            destinations = listOf(west, east),
            canvas = canvas,
            insetPx = 0f,
            zoom = 1f,
            translation = Offset.Zero,
            touchRadiusPx = 28f,
            immersiveWorld = true
        )
        assertEquals("west", selected)
    }

    @Test fun enlargedArtStillMatchesLegalRouteTapsAtFullManualZoom() {
        val canvas = IntSize(400, 800)
        val zoom = 4f
        val pan = Offset(-175f, 88f)
        val scene = BoardWorldViewport.frame(canvas, true)
        assertEquals(960f, scene.width, .001f)
        val westTap = BoardRouteHitTest.screenPosition(
            west, canvas, 0f, zoom, pan, immersiveWorld = true
        )
        assertEquals("west", BoardRouteHitTest.select(
            tap = westTap, origin = origin,
            destinations = listOf(west, east),
            canvas = canvas, insetPx = 0f,
            zoom = zoom, translation = pan,
            touchRadiusPx = 28f, immersiveWorld = true
        ))
        assertNull("The old unscaled artwork coordinate must not count as a tap",
            BoardRouteHitTest.select(
                tap = Offset(20f, 15f), origin = origin,
                destinations = listOf(west, east),
                canvas = canvas, insetPx = 0f, zoom = zoom,
                translation = pan, touchRadiusPx = 28f, immersiveWorld = true
            ))
    }

    @Test fun cropAndPanStillKeepRouteHitsAccurate() {
        val canvas = IntSize(400, 800)
        val pan = Offset(-145f, 22f)
        val target = BoardRouteHitTest.screenPosition(
            east, canvas, 0f, 1.5f, pan, immersiveWorld = true
        )
        assertEquals("east", BoardRouteHitTest.select(
            tap = target,
            origin = origin,
            destinations = listOf(west, east),
            canvas = canvas,
            insetPx = 0f,
            zoom = 1.5f,
            translation = pan,
            touchRadiusPx = 28f,
            immersiveWorld = true
        ))
    }
}
