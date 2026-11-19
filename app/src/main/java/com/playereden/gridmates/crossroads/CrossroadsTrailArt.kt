package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Experimental printed-board trail treatment.
 *
 * Trail geometry is ALWAYS sourced from the authoritative BoardDefinition
 * and drawn dynamically on the same Canvas/camera as the tokens.
 * These strokes are not baked into any generated image.
 *
 * The QA menu exposes both variants for a same-board comparison:
 * - printed = layered ink, parchment ribbon and colored stitching
 * - simple  = established light-line renderer from GAMEFEEL-41
 *
 * This is deliberately not a new collectible/theme pack or game rule.
 */
internal fun DrawScope.drawCrossroadsTrail(
    from: Offset,
    to: Offset,
    route: String,
    dense: Boolean,
    printed: Boolean
) {
    val routeInk = when (route) {
        "risky" -> Color(0xFFC29450)
        "safe" -> Color(0xFF83A181)
        "mischief" -> Color(0xFFA18BB1)
        else -> Color(0xFFD4C3A3)
    }
    if (!printed) {
        // Keep the last QA-approved legibility treatment intact for A/B review.
        drawLine(
            Color(0xFF334C3C).copy(alpha = 0.80f), from, to,
            strokeWidth = if (dense) 12f else 15f, cap = StrokeCap.Round
        )
        drawLine(
            routeInk, from, to,
            strokeWidth = if (dense) 8.5f else 11f, cap = StrokeCap.Round
        )
        drawLine(
            Color(0xFFFCF8ED), from, to,
            strokeWidth = if (dense) 2.1f else 3f, cap = StrokeCap.Round
        )
        return
    }

    // A narrow inked shadow and warm paper deck mimic a printed board-game
    // path. Distinct route types show through as colored sidebands and dashes.
    // No random grain: deterministic strokes avoid shimmer while panning.
    val weight = if (dense) 0.88f else 1f
    drawLine(
        Color(0xFF31453A).copy(alpha = 0.66f), from, to,
        strokeWidth = 15f * weight, cap = StrokeCap.Round
    )
    drawLine(
        routeInk.copy(alpha = 0.92f), from, to,
        strokeWidth = 12f * weight, cap = StrokeCap.Round
    )
    drawLine(
        Color(0xFFF6EBD0), from, to,
        strokeWidth = 8.3f * weight, cap = StrokeCap.Round
    )
    drawLine(
        routeInk.copy(alpha = 0.80f), from, to,
        strokeWidth = 1.7f * weight, cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.5f, 8.5f), 0f)
    )
}


/** QA-only visual treatment; never a game mode or saved-match setting. */
internal enum class CrossroadsTrailStyle(val label: String) {
    ARTWORK("Artwork"),
    PRINTED("Printed"),
    SIMPLE("Simple");

    fun next(): CrossroadsTrailStyle = when (this) {
        ARTWORK -> PRINTED
        PRINTED -> SIMPLE
        SIMPLE -> ARTWORK
    }
}

/** Missing sprites or Classic map always use the existing deterministic fallback. */
internal fun effectiveTrailStyle(
    wanted: CrossroadsTrailStyle,
    illustratedBoard: Boolean,
    atlasPresent: Boolean
): CrossroadsTrailStyle = when {
    !illustratedBoard -> CrossroadsTrailStyle.SIMPLE
    wanted == CrossroadsTrailStyle.ARTWORK && !atlasPresent -> CrossroadsTrailStyle.PRINTED
    else -> wanted
}

/** One piece of the horizontally sliced road atlas, in unrotated Canvas pixels. */
internal data class TrailSpriteSlice(
    val sourceX: Int,
    val sourceWidth: Int,
    val offsetPx: Float,
    val widthPx: Float
)

/**
 * Preserve the painted proportions of stones and moss on longer graph edges.
 *
 * The 128×32 atlas has 16-pixel soft ends, and a reusable 96-pixel center.
 * Rather than stretching the entire strip to fit an arbitrarily long route,
 * repeat its center at approximately its native scale and retain both endcaps.
 * 24 center stamps is a deliberate upper bound for older Android devices.
 *
 * Geometry and colors remain determined by the game, not this artwork.
 */
internal fun planTrailSpriteSlices(
    edgeLength: Float,
    renderedHeight: Float
): List<TrailSpriteSlice> {
    if (!edgeLength.isFinite() || !renderedHeight.isFinite() ||
        edgeLength < 1f || renderedHeight <= 0f) return emptyList()

    val capWidth = renderedHeight * 16f / 32f
    if (edgeLength <= capWidth * 2f + 2f) {
        return listOf(TrailSpriteSlice(0, 128, 0f, edgeLength))
    }
    val middleLength = edgeLength - 2f * capWidth
    val idealCenterWidth = renderedHeight * 96f / 32f
    val repeats = kotlin.math.ceil(middleLength / idealCenterWidth)
        .toInt().coerceIn(1, 24)
    val eachWidth = middleLength / repeats
    val slices = ArrayList<TrailSpriteSlice>(repeats + 2)
    slices.add(TrailSpriteSlice(0, 16, 0f, capWidth))
    repeat(repeats) { i ->
        slices.add(TrailSpriteSlice(16, 96, capWidth + i * eachWidth, eachWidth))
    }
    slices.add(TrailSpriteSlice(112, 16, edgeLength - capWidth, capWidth))
    return slices
}

/** A real painted RGBA sprite aligned to the original graph edge.
 * PNG atlas upper rectangle: [0, 0, 128, 32]. Center is tiled to keep pebble\n * proportions consistent at different route lengths. Geometry never comes from art.
 */
internal fun DrawScope.drawCrossroadsIllustratedTrail(
    from: Offset,
    to: Offset,
    route: String,
    dense: Boolean,
    atlas: androidx.compose.ui.graphics.ImageBitmap
) {
    val delta = to - from
    val length = delta.getDistance()
    if (length < 1f) return
    val weight = if (dense) 0.87f else 1f
    val routeTint = when (route) {
        "safe" -> Color(0xFF759474)
        "risky" -> Color(0xFFB58A4B)
        "mischief" -> Color(0xFF9980B0)
        else -> Color(0xFFAA946F)
    }
    // Small, route-specific painted edge visible underneath irregular road sprite.
    drawLine(routeTint.copy(alpha = 0.75f), from, to,
        strokeWidth = 15f * weight, cap = StrokeCap.Round)
    val angle = Math.toDegrees(kotlin.math.atan2(delta.y.toDouble(), delta.x.toDouble())).toFloat()
    rotate(angle, pivot = from) {
        val heightPx = 13.2f * weight
        val destinationY = (from.y - heightPx / 2f).toInt()
        val destinationHeight = heightPx.toInt().coerceAtLeast(1)
        planTrailSpriteSlices(length, heightPx).forEach { slice ->
            // Calculate both endpoints before rounding to avoid hairline gaps.
            val leftX = (from.x + slice.offsetPx).toInt()
            val rightX = (from.x + slice.offsetPx + slice.widthPx).toInt()
            drawImage(
                image = atlas,
                srcOffset = androidx.compose.ui.unit.IntOffset(slice.sourceX, 0),
                srcSize = androidx.compose.ui.unit.IntSize(slice.sourceWidth, 32),
                dstOffset = androidx.compose.ui.unit.IntOffset(leftX, destinationY),
                dstSize = androidx.compose.ui.unit.IntSize(
                    (rightX - leftX).coerceAtLeast(1), destinationHeight
                ),
                filterQuality = androidx.compose.ui.graphics.FilterQuality.Medium
            )
        }
    }
}

/** Printed junction sprite beneath existing colored node and accessibility labels.
 * Atlas rectangle: [32, 42, 64, 64].
 */
internal fun DrawScope.drawCrossroadsIllustratedJunction(
    center: Offset,
    radius: Float,
    atlas: androidx.compose.ui.graphics.ImageBitmap
) {
    val outer = radius + 3.5f
    val d = (outer * 2f).toInt().coerceAtLeast(1)
    drawImage(
        image = atlas,
        srcOffset = androidx.compose.ui.unit.IntOffset(32, 42),
        srcSize = androidx.compose.ui.unit.IntSize(64, 64),
        dstOffset = androidx.compose.ui.unit.IntOffset(
            (center.x - outer).toInt(), (center.y - outer).toInt()
        ),
        dstSize = androidx.compose.ui.unit.IntSize(d, d),
        filterQuality = androidx.compose.ui.graphics.FilterQuality.Medium
    )
}
