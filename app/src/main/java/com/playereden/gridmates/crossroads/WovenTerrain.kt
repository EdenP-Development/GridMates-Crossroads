package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlin.math.min

internal data class TerrainFleck(val x: Float, val y: Float, val size: Float, val tone: Int)

/** Fixed board-relative texture; never randomize during animation or a saved race. */
internal object WovenTerrainTexture {
    val flecks: List<TerrainFleck> = List(240) { i ->
        val col = i % 16
        val row = i / 16
        TerrainFleck(
            (col + .18f + ((i * 37) % 61) / 100f) / 16f,
            (row + .18f + ((i * 53) % 61) / 100f) / 15f,
            .0015f + (i % 5) * .00065f,
            i % 3
        )
    }
}

/** Muted printed terrain, under water, ornaments and the authoritative graph. */
internal fun DrawScope.drawWovenTerrain(left: Float, top: Float, width: Float, height: Float) {
    if (width <= 0f || height <= 0f) return
    val unit = min(width, height)
    fun at(x: Float, y: Float) = Offset(left + x * width, top + y * height)
    clipRect(left, top, left + width, top + height) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFD8D9AA), Color(0xFFC0CC9A),
                    Color(0xFFB5C397), Color(0xFFDED1A4)),
                start = at(0f, .45f), end = at(1f, .55f)
            ),
            topLeft = at(0f, 0f), size = Size(width, height)
        )
        // Broad, irregular patches with no outlines that could resemble roads.
        val meadow = Path().apply {
            moveTo(at(-.05f, .28f).x, at(-.05f, .28f).y)
            fun curve(a: Offset, b: Offset, c: Offset) = cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
            curve(at(.19f, .16f), at(.28f, .32f), at(.31f, .51f))
            curve(at(.37f, .70f), at(.25f, .88f), at(.40f, 1.04f))
            lineTo(at(-.05f, 1.04f).x, at(-.05f, 1.04f).y)
            close()
        }
        drawPath(meadow, Color(0xFFE4D6A0).copy(alpha = .38f))
        val forest = Path().apply {
            moveTo(at(.31f, -.04f).x, at(.31f, -.04f).y)
            fun curve(a: Offset, b: Offset, c: Offset) = cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
            curve(at(.60f, .10f), at(.74f, -.04f), at(.77f, .30f))
            curve(at(.64f, .51f), at(.76f, .63f), at(.65f, 1.04f))
            lineTo(at(.39f, 1.04f).x, at(.39f, 1.04f).y)
            curve(at(.52f, .69f), at(.24f, .48f), at(.31f, -.04f))
            close()
        }
        drawPath(forest, Color(0xFF749266).copy(alpha = .17f))
        // Soft pools of daylight and shade; never hard-edged route-like marks.
        listOf(
            Triple(.12f, .83f, .16f), Triple(.24f, .49f, .12f),
            Triple(.49f, .29f, .13f), Triple(.58f, .68f, .11f),
            Triple(.91f, .23f, .13f), Triple(.88f, .82f, .12f)
        ).forEachIndexed { index, (x, y, radius) ->
            val center = at(x, y)
            val tint = if (index == 2 || index == 3) Color(0xFF69875C)
                else Color(0xFFF4E4B6)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tint.copy(alpha = .24f), tint.copy(alpha = 0f)),
                    center = center, radius = unit * radius
                ),
                radius = unit * radius, center = center
            )
        }
        // Overlapping soft pigment washes break up the broad region bands.
        // Fixed board-space positions: no animation shimmer or new game geometry.
        for (i in 0 until 36) {
            val x = (i % 6 + .24f + (i * 17 % 49) / 100f) / 6f
            val y = (i / 6 + .18f + (i * 23 % 53) / 100f) / 6f
            val radius = unit * (.065f + (i % 5) * .014f)
            val center = at(x, y)
            val tint = if (i % 3 == 0) Color(0xFFF0DFA8) else Color(0xFF577A4C)
            drawCircle(Brush.radialGradient(
                listOf(tint.copy(alpha = .16f), tint.copy(alpha = 0f)),
                center = center, radius = radius), radius = radius, center = center)
        }
        // Tiny broken fibers and vegetation strokes remain subtle under the paths.
        WovenTerrainTexture.flecks.forEach { fleck ->
            val p = at(fleck.x, fleck.y)
            val r = fleck.size * unit
            val tint = when (fleck.tone) {
                0 -> Color(0xFF738B57).copy(alpha = .19f)
                1 -> Color(0xFFF8EBC3).copy(alpha = .30f)
                else -> Color(0xFF8B8056).copy(alpha = .11f)
            }
            drawLine(tint, p - Offset(r * .40f, r * .55f),
                p + Offset(r * .50f, r * .55f), strokeWidth = r * .55f,
                cap = StrokeCap.Round)
        }
    }
}
