package com.playereden.crossroads.core

import org.junit.Assert.*
import org.junit.Test

class CameraGeometryTest {
    @Test fun zoomRemainsWithinSafeRange() {
        assertEquals(1f, CameraGeometry.zoom(0.2f), 0.0001f)
        assertEquals(3f, CameraGeometry.zoom(9f), 0.0001f)
        assertEquals(1f, CameraGeometry.zoom(Float.NaN), 0.0001f)
    }

    @Test fun followCentersTheFocusedPiece() {
        val viewportCenter = 150f
        val focusPoint = 70f
        val zoom = 1.75f
        val translation = CameraGeometry.centerTranslation(viewportCenter, focusPoint, zoom)
        val screenPosition = viewportCenter +
            (focusPoint - viewportCenter) * zoom + translation
        assertEquals(viewportCenter, screenPosition, 0.0001f)
    }

    @Test fun pinchKeepsItsCentroidAnchored() {
        val viewportCenter = 180f
        val centroid = 70f
        val oldTranslation = -40f
        val newTranslation = CameraGeometry.zoomAt(
            oldTranslation, centroid, viewportCenter, 1.25f, 1.75f, 0f
        )
        // An arbitrary point that appeared at centroid remains there afterward.
        val originalPoint = viewportCenter +
            (centroid - viewportCenter - oldTranslation) / 1.25f
        val after = viewportCenter + (originalPoint - viewportCenter) * 1.75f +
            newTranslation
        assertEquals(centroid, after, 0.001f)
    }

    @Test fun dragMovesTheCameraByTheDragDistance() {
        assertEquals(30f, CameraGeometry.zoomAt(15f, 100f, 100f, 2f, 2f, 15f), 0.001f)
    }

    @Test fun panCannotMoveTheBoardCompletelyOffScreen() {
        val min = CameraGeometry.boundTranslation(-10000f, 300f, 20f, 280f, 2f)
        val max = CameraGeometry.boundTranslation(10000f, 300f, 20f, 280f, 2f)
        assertTrue(min < 0f)
        assertTrue(max > 0f)
        assertTrue(max - min < 650f)
        assertEquals(0f, CameraGeometry.boundTranslation(100f, 300f, 20f, 280f, 1f), 0.0001f)
    }

    @Test fun junctionFocusAccountsForEveryExit() {
        val result = CameraGeometry.forkFocus(0.4f, 0.4f,
            listOf(0.2f to 0.1f, 0.6f to 0.2f, 0.4f to 0.9f))
        assertEquals(0.4f, result.first, 0.0001f)
        assertEquals(0.4f, result.second, 0.0001f)
    }
}
