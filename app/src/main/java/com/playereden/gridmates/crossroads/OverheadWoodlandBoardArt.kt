package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.playereden.crossroads.core.BoardNode
import kotlin.math.min
import kotlin.math.sqrt

/**
 * VISUAL-84: an honest overhead printed tabletop, instead of putting overhead
 * roads on perspective landscape paintings with skies and distant horizons.
 *
 * QA A/B backdrop only. All roads, nodes, traveler positions, game turns,
 * camera math, hit detection and saves come from the EXISTING engine.
 * The scenery planner deliberately avoids playable links and spaces.
 */
internal enum class OverheadDecoration { TREE, FLOWERS, ROCK, COTTAGE }
internal data class OverheadFeature(val x: Float, val y: Float, val kind: OverheadDecoration)

internal object OverheadScenery {
    fun distanceToEdge(
        x: Float, y: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val denom = dx * dx + dy * dy
        val t = if (denom <= 0.0000001f) 0f else
            (((x - x1) * dx + (y - y1) * dy) / denom).coerceIn(0f, 1f)
        val px = x1 + t * dx
        val py = y1 + t * dy
        return sqrt((x - px) * (x - px) + (y - py) * (y - py))
    }

    fun clearOfPlay(x: Float, y: Float, nodes: List<BoardNode>, margin: Float): Boolean {
        if (!x.isFinite() || !y.isFinite() || x !in 0f..1f || y !in 0f..1f ||
            !margin.isFinite() || margin <= 0f) return false
        val lookup = nodes.associateBy { it.id }
        for (node in nodes) {
            if (distanceToEdge(x, y, node.x, node.y, node.x, node.y) < margin) {
                return false
            }
            for (id in node.next) {
                val other = lookup[id] ?: continue
                if (distanceToEdge(x, y, node.x, node.y, other.x, other.y) < margin) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Spatially fixed plants and cottages—not randomized event spawns. An
     * approximately 6×7 lattice keeps the same quiet landmarks between games.
     * Geometry clearance is generous because roads are magnified on phones.
     */
    fun plan(nodes: List<BoardNode>): List<OverheadFeature> {
        if (nodes.isEmpty()) return emptyList()
        val candidates = buildList {
            for (row in 0..6) for (col in 0..6) {
                val j = row * 7 + col
                val x = (0.075f + col * 0.139f +
                    ((j * 7) % 5 - 2) * 0.007f).coerceIn(.04f, .96f)
                val y = (0.075f + row * 0.139f +
                    ((j * 11) % 7 - 3) * 0.006f).coerceIn(.04f, .96f)
                val kind = when {
                    x > .68f && y < .52f && (j % 3 == 0) ->
                        OverheadDecoration.COTTAGE
                    j % 5 == 0 -> OverheadDecoration.ROCK
                    j % 3 == 0 -> OverheadDecoration.FLOWERS
                    else -> OverheadDecoration.TREE
                }
                if (clearOfPlay(x, y, nodes, if (kind == OverheadDecoration.COTTAGE)
                        .084f else .067f)) {
                    add(OverheadFeature(x, y, kind))
                }
            }
        }
        return candidates
    }
}

internal fun DrawScope.drawOverheadWoodlandPrint(
    nodes: List<BoardNode>,
    features: List<OverheadFeature>,
    left: Float,
    top: Float,
    width: Float,
    height: Float
) {
    if (width <= 0f || height <= 0f) return
    fun at(x: Float, y: Float) = Offset(left + width * x, top + height * y)
    val u = min(width, height) / 100f

    // Artwork is a TOP-DOWN sheet of printed paper: no sky/horizon and no
    // houses depicted from a viewpoint incompatible with the game graph.
    drawRect(
        Color(0xFFE6DEBD),
        topLeft = Offset(left, top),
        size = Size(width, height)
    )
    drawOval(
        Color(0xFFA8BF80).copy(alpha = .43f),
        topLeft = at(-.18f, -.10f), size = Size(width * .81f, height * 1.15f)
    )
    drawOval(
        Color(0xFF91AA7B).copy(alpha = .29f),
        topLeft = at(.38f, -.14f), size = Size(width * .77f, height * .87f)
    )
    drawOval(
        Color(0xFFE8C791).copy(alpha = .24f),
        topLeft = at(.64f, .22f), size = Size(width * .56f, height * .83f)
    )

    // Winding teal WATER, explicitly not another beige or wood-colored
    // navigable path. The real wooden graph is rendered over this layer.
    val stream = Path().apply {
        val a = at(.70f, -.05f)
        moveTo(a.x, a.y)
        var p = at(.64f, .16f)
        var q = at(.87f, .25f)
        var end = at(.75f, .48f)
        cubicTo(p.x, p.y, q.x, q.y, end.x, end.y)
        p = at(.62f, .64f); q = at(.90f, .72f); end = at(.80f, 1.05f)
        cubicTo(p.x, p.y, q.x, q.y, end.x, end.y)
    }
    drawPath(stream, color = Color(0xFF5D8A85).copy(alpha = .49f),
        style = Stroke(width = u * 5.9f, cap = StrokeCap.Round))
    drawPath(stream, color = Color(0xFF93BAB0).copy(alpha = .91f),
        style = Stroke(width = u * 4.4f, cap = StrokeCap.Round))
    drawPath(stream, color = Color(0xFFC4D9BC).copy(alpha = .65f),
        style = Stroke(width = u * 1.0f, cap = StrokeCap.Round))

    // Paper speckle is fixed by row/column instead of runtime randomness;
    // keep it faint and distant from the existing graph symbols.
    for (i in 0 until 54) {
        val fx = ((i * 37) % 53 + .5f) / 53f
        val fy = ((i * 23 + 11) % 47 + .5f) / 47f
        drawCircle(Color(0xFF5E784F).copy(alpha = .08f),
            radius = (.28f + (i % 3) * .21f) * u, center = at(fx, fy))
    }

    features.forEach { feature ->
        val c = at(feature.x, feature.y)
        when (feature.kind) {
            OverheadDecoration.TREE -> {
                val r = u * (1.35f + (feature.x * 10f % 3f) * .19f)
                drawCircle(Color(0xFF324B37).copy(alpha = .23f),
                    r * 1.23f, c + Offset(u * .19f, u * .31f))
                // A canopy viewed from ABOVE: scalloped leaves and a single
                // engraved stem, not a side-on tree silhouette.
                drawCircle(Color(0xFF4F7152), r * 1.10f, c)
                drawCircle(Color(0xFF78935D), r * .70f, c - Offset(r * .29f, r * .20f))
                drawCircle(Color(0xFFA5B77D), r * .37f, c - Offset(r * .39f, r * .39f))
                drawCircle(Color(0xFF415F45), r * .46f, c + Offset(r * .40f, r * .15f))
                drawCircle(Color(0xFF9D774B), r * .13f, c)
            }
            OverheadDecoration.FLOWERS -> {
                val colors = listOf(Color(0xFFF4E9C8), Color(0xFFEDD2A3),
                    Color(0xFFF6F1DA))
                for (i in 0 until 4) {
                    val p = c + Offset((i % 2 - .5f) * u * 1.7f,
                        (i / 2 - .5f) * u * 1.6f)
                    drawCircle(Color(0xFF648054).copy(alpha = .35f), u * .47f, p)
                    drawCircle(colors[i % colors.size], u * .36f, p)
                    drawCircle(Color(0xFFD5B064), u * .15f, p)
                }
            }
            OverheadDecoration.ROCK -> {
                drawOval(Color(0xFF52604F).copy(alpha = .24f),
                    topLeft = c - Offset(u * 1.15f, u * .35f),
                    size = Size(u * 2.8f, u * 1.65f))
                drawOval(Color(0xFFADB198),
                    topLeft = c - Offset(u * 1.17f, u * .88f),
                    size = Size(u * 2.25f, u * 1.62f))
                drawOval(Color(0xFFD9D5B5).copy(alpha = .75f),
                    topLeft = c - Offset(u * .78f, u * .61f),
                    size = Size(u * .95f, u * .40f))
            }
            OverheadDecoration.COTTAGE -> {
                val rx = u * 1.82f
                val ry = u * 1.55f
                drawRoundRect(Color(0xFF4D513D).copy(alpha = .30f),
                    topLeft = c - Offset(rx, ry) + Offset(u * .24f, u * .29f),
                    size = Size(rx * 2.1f, ry * 2.1f))
                drawRoundRect(Color(0xFF75563A),
                    topLeft = c - Offset(rx, ry), size = Size(rx * 2f, ry * 2f))
                drawRoundRect(Color(0xFFC18E58),
                    topLeft = c - Offset(rx * .81f, ry * .78f),
                    size = Size(rx * 1.62f, ry * 1.56f))
                drawLine(Color(0xFFF2D89C),
                    c - Offset(rx * .68f, 0f), c + Offset(rx * .68f, 0f),
                    strokeWidth = u * .27f, cap = StrokeCap.Round)
                drawRoundRect(Color(0xFFEFDFB7),
                    topLeft = c + Offset(rx * .39f, -ry * .64f),
                    size = Size(u * .45f, u * .50f))
            }
        }
    }
}
