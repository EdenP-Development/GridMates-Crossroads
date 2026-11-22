package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.playereden.crossroads.core.BoardNode
import com.playereden.crossroads.core.DecisionLab
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
internal fun useOverheadBoardPreview(
    boardId: String,
    debug: Boolean,
    requested: Boolean,
    illustrated: Boolean
): Boolean =
    debug && requested && illustrated &&
        DecisionLab.baseId(boardId) == "woodland_woven_trail_v02"

// First review only Woven Trail. Reusing one scenic composition for Brookside
// and Cedarwood would incorrectly erase what makes each course distinct.
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
            // A second village roof anchors the real north-east Woven region
            // without moving or hiding the final route graph. Keep this
            // additional landmark Woven-specific via the call-site preview gate.
            val village = OverheadFeature(.80f, .16f, OverheadDecoration.COTTAGE)
            if (clearOfPlay(village.x, village.y, nodes, .084f) &&
                candidates.none {
                    distanceToEdge(village.x, village.y, it.x, it.y, it.x, it.y) < .09f
                }) {
                add(village)
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
                // Larger layered canopy shapes carry the visual detail at
                // normal phone zoom rather than appearing as tiny circles.
                val r = u * (2.00f + (feature.x * 10f % 3f) * .24f)
                drawCircle(Color(0xFF233E30).copy(alpha = .24f),
                    r * 1.19f, c + Offset(u * .26f, u * .40f))
                drawCircle(Color(0xFF33543A), r * 1.11f, c)
                for ((dx, dy) in listOf(
                    -.48f to -.29f, .22f to -.49f, .56f to .08f,
                    .15f to .53f, -.49f to .35f
                )) {
                    drawCircle(Color(0xFF57794E), r * .57f,
                        c + Offset(r * dx, r * dy))
                }
                drawCircle(Color(0xFF83A366), r * .53f,
                    c - Offset(r * .23f, r * .23f))
                drawCircle(Color(0xFFB9C686).copy(alpha = .83f), r * .22f,
                    c - Offset(r * .40f, r * .43f))
                drawCircle(Color(0xFF314C38).copy(alpha = .43f), r * .15f,
                    c + Offset(r * .33f, r * .23f))
            }
            OverheadDecoration.FLOWERS -> {
                val colors = listOf(Color(0xFFF4E9C8), Color(0xFFEDD2A3),
                    Color(0xFFF6F1DA))
                for (i in 0 until 4) {
                    val p = c + Offset((i % 2 - .5f) * u * 2.6f,
                        (i / 2 - .5f) * u * 2.5f)
                    drawCircle(Color(0xFF648054).copy(alpha = .34f), u * .78f, p)
                    for ((dx, dy) in listOf(
                        .42f to 0f, -.42f to 0f, 0f to .42f, 0f to -.42f
                    )) {
                        drawCircle(colors[i % colors.size], u * .32f,
                            p + Offset(dx * u, dy * u))
                    }
                    drawCircle(Color(0xFFD5B064), u * .20f, p)
                }
            }
            OverheadDecoration.ROCK -> {
                drawOval(Color(0xFF52604F).copy(alpha = .28f),
                    topLeft = c - Offset(u * 1.68f, u * .33f),
                    size = Size(u * 3.9f, u * 2.20f))
                drawOval(Color(0xFF9CA695),
                    topLeft = c - Offset(u * 1.65f, u * 1.32f),
                    size = Size(u * 3.18f, u * 2.32f))
                drawOval(Color(0xFFD9D5B5).copy(alpha = .79f),
                    topLeft = c - Offset(u * 1.10f, u * .93f),
                    size = Size(u * 1.42f, u * .60f))
                drawOval(Color(0xFF74836E).copy(alpha = .54f),
                    topLeft = c + Offset(u * .52f, -u * .48f),
                    size = Size(u * .72f, u * .91f))
            }
            OverheadDecoration.COTTAGE -> {
                val rx = u * 2.38f
                val ry = u * 2.14f
                drawRoundRect(Color(0xFF4D513D).copy(alpha = .30f),
                    topLeft = c - Offset(rx, ry) + Offset(u * .24f, u * .29f),
                    size = Size(rx * 2.1f, ry * 2.1f))
                drawRoundRect(Color(0xFF75563A),
                    topLeft = c - Offset(rx, ry), size = Size(rx * 2f, ry * 2f))
                drawRoundRect(Color(0xFFC18E58),
                    topLeft = c - Offset(rx * .81f, ry * .78f),
                    size = Size(rx * 1.62f, ry * 1.56f))
                // Top-view roof ridge and tile lines read as a miniature
                // cottage instead of a generic rectangular map icon.
                drawLine(Color(0xFFF4DEAF),
                    c - Offset(rx * .70f, 0f), c + Offset(rx * .70f, 0f),
                    strokeWidth = u * .28f, cap = StrokeCap.Round)
                for (i in -1..1) {
                    val yy = i * ry * .45f
                    drawLine(Color(0xFF916641).copy(alpha = .45f),
                        c + Offset(-rx * .71f, yy),
                        c + Offset(rx * .71f, yy),
                        strokeWidth = u * .13f, cap = StrokeCap.Round)
                }
                drawRoundRect(Color(0xFFEFDFB7),
                    topLeft = c + Offset(rx * .39f, -ry * .64f),
                    size = Size(u * .64f, u * .62f))
                drawRoundRect(Color(0xFF799762).copy(alpha = .76f),
                    topLeft = c + Offset(-rx * .80f, ry * .86f),
                    size = Size(rx * 1.60f, ry * .36f))
            }
        }
    }
}
