package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
