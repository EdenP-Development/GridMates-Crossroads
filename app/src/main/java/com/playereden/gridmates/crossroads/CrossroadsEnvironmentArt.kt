package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/** Stable atlas coordinates. Never use enum ordinal as a persistent asset key. */
internal enum class EnvironmentSprite(val cell: Int, val diameter: Float) {
    OAK(0, .062f), FLOWERS(1, .052f), ROCK(2, .050f), COTTAGE(3, .068f),
    MEADOW_CLEARING(4, .090f), FIR_GROVE(5, .090f), ORCHARD(6, .090f),
    BUSH(7, .050f), FIR(8, .062f), MUSHROOMS(9, .050f),
    FENCE(10, .060f), REEDS(11, .050f)
}

internal object EnvironmentAtlas {
    const val SIZE = 1024
    const val CELL = 256

    fun validDimensions(width: Int, height: Int): Boolean =
        width == SIZE && height == SIZE

    fun source(sprite: EnvironmentSprite): IntOffset =
        IntOffset(sprite.cell % 4 * CELL, sprite.cell / 4 * CELL)

    fun spriteFor(kind: OverheadDecoration): EnvironmentSprite = when (kind) {
        OverheadDecoration.TREE -> EnvironmentSprite.OAK
        OverheadDecoration.FLOWERS -> EnvironmentSprite.FLOWERS
        OverheadDecoration.ROCK -> EnvironmentSprite.ROCK
        OverheadDecoration.COTTAGE -> EnvironmentSprite.COTTAGE
        OverheadDecoration.MEADOW_BLOOM -> EnvironmentSprite.MEADOW_CLEARING
        OverheadDecoration.FIR_GROVE -> EnvironmentSprite.FIR_GROVE
        OverheadDecoration.ORCHARD -> EnvironmentSprite.ORCHARD
    }
}

/** Extra atlas-only details; the original procedural scene stays intact if art is absent. */
internal data class EnvironmentAccent(
    val x: Float, val y: Float, val sprite: EnvironmentSprite
)

internal object WovenEnvironmentAccents {
    fun plan(
        nodes: List<com.playereden.crossroads.core.BoardNode>,
        features: List<OverheadFeature>
    ): List<EnvironmentAccent> {
        if (nodes.isEmpty()) return emptyList()
        val candidates = listOf(
            EnvironmentAccent(.25f, .62f, EnvironmentSprite.BUSH),
            EnvironmentAccent(.56f, .31f, EnvironmentSprite.FIR),
            EnvironmentAccent(.69f, .19f, EnvironmentSprite.REEDS),
            EnvironmentAccent(.61f, .94f, EnvironmentSprite.FIR),
            EnvironmentAccent(.29f, .95f, EnvironmentSprite.MUSHROOMS),
            EnvironmentAccent(.70f, .90f, EnvironmentSprite.REEDS),
            EnvironmentAccent(.83f, .06f, EnvironmentSprite.FENCE)
        )
        return buildList {
            for (p in candidates) {
                val radius = p.sprite.diameter * kotlin.math.sqrt(2f) / 2f
                if (p.x !in radius..(1f - radius) ||
                    p.y !in radius..(1f - radius)) continue
                if (!OverheadScenery.clearOfPlay(p.x, p.y, nodes, .067f)) continue
                if (features.any { q ->
                    kotlin.math.hypot(p.x - q.x, p.y - q.y) < .077f
                }) continue
                if (any { q: EnvironmentAccent ->
                    kotlin.math.hypot(p.x - q.x, p.y - q.y) < .077f
                }) continue
                add(p)
            }
        }
    }
}

/** Small irregular ground washes join existing landmarks to the landscape.
 * No new props, routes or event markers; deterministic and bounded per board. */
internal data class LandscapeFootprint(val x: Float, val y: Float,
    val radius: Float, val tone: Int)

internal object LandscapeComposition {
    fun plan(accents: List<EnvironmentAccent>): List<LandscapeFootprint> =
        accents.take(40).flatMap { a ->
            val tone = when (a.sprite) {
                EnvironmentSprite.COTTAGE, EnvironmentSprite.FENCE, EnvironmentSprite.ORCHARD -> 2
                EnvironmentSprite.FIR, EnvironmentSprite.FIR_GROVE, EnvironmentSprite.OAK,
                EnvironmentSprite.BUSH -> 0
                else -> 1
            }
            val radius = a.sprite.diameter * .85f
            listOf(LandscapeFootprint(a.x, a.y, radius, tone),
                LandscapeFootprint((a.x + radius * .33f).coerceIn(0f, 1f),
                    (a.y + radius * .24f).coerceIn(0f, 1f), radius * .63f, tone))
        }
}

internal fun DrawScope.drawLandscapeFootprints(
    footprints: List<LandscapeFootprint>, left: Float, top: Float, width: Float, height: Float
) {
    val unit = minOf(width, height)
    if (unit <= 0f) return
    clipRect(left, top, left + width, top + height) {
        footprints.forEach { p ->
            val center = Offset(left + p.x * width, top + p.y * height)
            val radius = unit * p.radius
            val tint = when (p.tone) {
                0 -> Color(0xFF56764B)
                2 -> Color(0xFFB49460)
                else -> Color(0xFFEAD89E)
            }
            drawCircle(androidx.compose.ui.graphics.Brush.radialGradient(
                listOf(tint.copy(alpha = .32f), tint.copy(alpha = .13f), tint.copy(alpha = 0f)),
                center = center, radius = radius), radius = radius, center = center)
        }
    }
}

/** Draw only decorative art below the playable graph; false means draw fallback. */
internal fun DrawScope.tryDrawEnvironmentSprite(
    atlas: ImageBitmap?,
    sprite: EnvironmentSprite,
    center: Offset,
    boardSide: Float
): Boolean {
    if (atlas == null || !EnvironmentAtlas.validDimensions(atlas.width, atlas.height) ||
        !boardSide.isFinite() || boardSide <= 0f ||
        !center.x.isFinite() || !center.y.isFinite()) return false
    val diameter = (boardSide * sprite.diameter).roundToInt().coerceAtLeast(1)
    // A shared contact shadow anchors the cutout to the printed ground.
    val d = diameter.toFloat()
    drawOval(Color(0xFF34492D).copy(alpha = .20f),
        topLeft = center + Offset(-d * .31f, d * .27f),
        size = Size(d * .68f, d * .15f))
    drawImage(
        image = atlas,
        srcOffset = EnvironmentAtlas.source(sprite),
        srcSize = IntSize(EnvironmentAtlas.CELL, EnvironmentAtlas.CELL),
        dstOffset = IntOffset((center.x - diameter / 2f).roundToInt(),
            (center.y - diameter / 2f).roundToInt()),
        dstSize = IntSize(diameter, diameter),
        filterQuality = FilterQuality.Medium
    )
    return true
}
