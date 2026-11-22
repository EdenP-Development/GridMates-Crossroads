package com.playereden.crossroads.core

/**
 * Presentation-only camera mathematics. Pixel coordinates are local to the
 * untransformed board Canvas; no route, roll or match state is modified here.
 */
object CameraGeometry {
    const val MIN_ZOOM = 1f
    const val MAX_ZOOM = 4.0f // owner phone: inspect the illustrated world beyond the old 3x ceiling

    fun zoom(value: Float): Float =
        if (value.isFinite()) value.coerceIn(MIN_ZOOM, MAX_ZOOM) else MIN_ZOOM

    /** graphicsLayer uses its centre as the default scale pivot. */
    fun centerTranslation(viewCenter: Float, focusPoint: Float, zoom: Float): Float =
        (viewCenter - focusPoint) * zoom

    /**
     * Keeps the content under the gesture centroid stationary while zooming,
     * then applies the user's drag. A camera operates independently per axis.
     */
    fun zoomAt(
        translation: Float,
        centroid: Float,
        viewCenter: Float,
        fromZoom: Float,
        toZoom: Float,
        pan: Float
    ): Float {
        val factor = zoom(toZoom) / zoom(fromZoom)
        return translation + pan +
            (centroid - viewCenter - translation) * (1f - factor)
    }

    /**
     * At higher zoom, ensure the scaled drawable board always covers the
     * viewport. At 1x, park a board that is narrower than the viewport centrally.
     */
    fun boundTranslation(
        translation: Float,
        viewport: Float,
        boardMin: Float,
        boardMax: Float,
        zoom: Float
    ): Float {
        if (viewport <= 0 || boardMax <= boardMin) return 0f
        val center = viewport / 2f
        val scaledMin = center + (boardMin - center) * zoom
        val scaledMax = center + (boardMax - center) * zoom
        val slack = (viewport - (boardMax - boardMin)).coerceAtLeast(0f) / 2f
        val minimum = viewport - slack - scaledMax
        val maximum = slack - scaledMin
        if (minimum > maximum) return (minimum + maximum) / 2f
        return translation.coerceIn(minimum, maximum)
    }

    fun forkFocus(
        sourceX: Float, sourceY: Float,
        choices: List<Pair<Float, Float>>
    ): Pair<Float, Float> {
        val locations = choices + (sourceX to sourceY)
        return locations.map { it.first }.average().toFloat() to
            locations.map { it.second }.average().toFloat()
    }
}
