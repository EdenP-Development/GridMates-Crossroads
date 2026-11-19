package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Optional QA-only printed waypoint medallions.
 * The source atlas contains 4 square 128px tiles arranged 2 by 2.
 * The board's authoritative node graph determines all positions and choices.
 */
internal enum class WoodlandMarker(val cell: Int) {
    FORK(0), SHELTER(1), HAZARD(2), MISCHIEF(3)
}

internal fun woodlandMarkerFor(kind: String, eventPool: String?): WoodlandMarker? =
    when (kind) {
        "fork" -> WoodlandMarker.FORK
        "event" -> when (eventPool) {
            "shelter" -> WoodlandMarker.SHELTER
            "hazard" -> WoodlandMarker.HAZARD
            "mischief" -> WoodlandMarker.MISCHIEF
            else -> null
        }
        else -> null // plain, start and finish keep their original marks
    }

internal fun woodlandMarkerEnabled(
    isDebug: Boolean,
    requested: Boolean,
    atlasLoaded: Boolean,
    illustratedBoard: Boolean
): Boolean = isDebug && requested && atlasLoaded && illustratedBoard

internal fun markerAtlasOffset(marker: WoodlandMarker): IntOffset =
    IntOffset((marker.cell % 2) * 128, (marker.cell / 2) * 128)

/** Small, readable printed icon, anchored at exactly the old node center. */
internal fun DrawScope.drawCrossroadsPaintedMarker(
    center: Offset,
    radius: Float,
    marker: WoodlandMarker,
    atlas: ImageBitmap
) {
    if (radius <= 0f) return
    // Keep forks only modestly larger than ordinary event stops.
    val diameter = (radius * (if (marker == WoodlandMarker.FORK) 2.1f else 2.35f))
        .toInt().coerceAtLeast(1)
    drawImage(
        image = atlas,
        srcOffset = markerAtlasOffset(marker),
        srcSize = IntSize(128, 128),
        dstOffset = IntOffset((center.x - diameter / 2f).toInt(),
            (center.y - diameter / 2f).toInt()),
        dstSize = IntSize(diameter, diameter),
        filterQuality = FilterQuality.Medium
    )
}
