package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
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
        CourseOverheadStyle.forBoard(boardId) != null

// First review only Woven Trail. Reusing one scenic composition for Brookside
// and Cedarwood would incorrectly erase what makes each course distinct.
internal enum class OverheadDecoration {
    TREE, FLOWERS, ROCK, COTTAGE, MEADOW_BLOOM, FIR_GROVE, ORCHARD
}
internal data class OverheadFeature(val x: Float, val y: Float, val kind: OverheadDecoration)
internal data class OverheadBridge(
    val x: Float, val y: Float, val directionX: Float, val directionY: Float,
    val before: Float, val after: Float, val crossingSine: Float
)

internal data class OverheadBridgeLayout(
    val before: Float, val after: Float, val widthUnits: Float, val planks: Int
)

/** Conservative visual span: use stream-bank width and crossing angle, bounded
 * by the owning edge. Very short or nearly parallel crossings stay plain trails. */
internal fun overheadBridgeLayout(bridge: OverheadBridge): OverheadBridgeLayout? {
    if (!bridge.crossingSine.isFinite() || bridge.crossingSine < .35f ||
        !bridge.before.isFinite() || !bridge.after.isFinite() ||
        bridge.before < .008f || bridge.after < .008f) return null
    val half = (.032f / bridge.crossingSine).coerceAtMost(.05f)
    val before = min(half, bridge.before - .004f)
    val after = min(half, bridge.after - .004f)
    if (before + after < .025f) return null
    return OverheadBridgeLayout(before, after, 2.8f,
        ((before + after) / .009f).toInt().coerceIn(3, 10))
}

internal object OverheadScenery {
    private fun cubic(
        a: Offset, b: Offset, c: Offset, d: Offset, t: Float
    ): Offset {
        val q = 1f - t
        return a * (q * q * q) + b * (3f * q * q * t) +
            c * (3f * q * t * t) + d * (t * t * t)
    }

    private fun segmentCrossing(
        a: Offset, b: Offset, c: Offset, d: Offset
    ): Offset? {
        val ab = b - a
        val cd = d - c
        val det = ab.x * cd.y - ab.y * cd.x
        if (!det.isFinite() || kotlin.math.abs(det) < 0.0000001f) return null
        val ac = c - a
        val t = (ac.x * cd.y - ac.y * cd.x) / det
        val u = (ac.x * ab.y - ac.y * ab.x) / det
        if (t !in 0f..1f || u !in 0f..1f) return null
        return a + ab * t
    }

    /**
     * Cosmetic bridges only where the decorative stream crosses an ACTUAL
     * board graph edge. Sample the same two cubic sections used to paint the
     * water. Never add routes or bridge nodes to the rules model.
     */
    fun bridges(nodes: List<BoardNode>): List<OverheadBridge> {
        if (nodes.isEmpty()) return emptyList()
        val a = Offset(.70f, -.05f)
        val b = Offset(.64f, .16f)
        val c = Offset(.78f, .25f)
        val d = Offset(.75f, .48f)
        val e = Offset(.62f, .64f)
        val f = Offset(.90f, .72f)
        val g = Offset(.80f, 1.05f)
        val stream = buildList {
            for (i in 0..32) add(cubic(a, b, c, d, i / 32f))
            for (i in 1..32) add(cubic(d, e, f, g, i / 32f))
        }
        val byId = nodes.associateBy { it.id }
        return buildList {
            for (node in nodes) {
                val from = Offset(node.x, node.y)
                for (id in node.next) {
                    val next = byId[id] ?: continue
                    val to = Offset(next.x, next.y)
                    val delta = to - from
                    val length = delta.getDistance()
                    if (!length.isFinite() || length <= 0.0001f) continue
                    for (i in 1 until stream.size) {
                        val point = segmentCrossing(from, to, stream[i - 1], stream[i])
                            ?: continue
                        if (point.x !in 0f..1f || point.y !in 0f..1f) continue
                        if (none { existing ->
                            kotlin.math.hypot(
                                existing.x - point.x, existing.y - point.y
                            ) < .018f
                        }) {
                            add(OverheadBridge(
                                point.x, point.y, delta.x / length, delta.y / length,
                                (point - from).getDistance(), (to - point).getDistance(),
                                run {
                                    val tangent = stream[i] - stream[i - 1]
                                    val tangentLength = tangent.getDistance().coerceAtLeast(.0001f)
                                    kotlin.math.abs(delta.x * tangent.y - delta.y * tangent.x) /
                                        (length * tangentLength)
                                }
                            ))
                        }
                    }
                }
            }
        }
    }
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
            val village = OverheadFeature(.80f, .18f, OverheadDecoration.COTTAGE)
            if (clearOfPlay(village.x, village.y, nodes, .084f) &&
                none {
                    distanceToEdge(village.x, village.y, it.x, it.y, it.x, it.y) < .09f
                }) {
                add(village)
            }
        }
        return candidates + regionalLandmarks(nodes, candidates)
    }

    /** Hand-placed Woven landmarks occupy genuine empty terrain, never routes. */
    fun regionalLandmarks(
        nodes: List<BoardNode>, existing: List<OverheadFeature>
    ): List<OverheadFeature> {
        if (nodes.isEmpty()) return emptyList()
        val authored = listOf(
            OverheadFeature(.28f, .49f, OverheadDecoration.MEADOW_BLOOM),
            OverheadFeature(.60f, .68f, OverheadDecoration.FIR_GROVE),
            OverheadFeature(.94f, .83f, OverheadDecoration.ORCHARD)
        )
        return authored.filter { p ->
            clearOfPlay(p.x, p.y, nodes, .09f) && existing.none { q ->
                kotlin.math.hypot(p.x - q.x, p.y - q.y) < .085f
            }
        }
    }
}

internal fun DrawScope.drawOverheadWoodlandPrint(
    nodes: List<BoardNode>,
    features: List<OverheadFeature>,
    bridges: List<OverheadBridge>,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    environmentAtlas: ImageBitmap? = null,
    environmentAccents: List<EnvironmentAccent> = emptyList(),
    groundcover: List<GroundcoverPatch> = emptyList(),
    footprints: List<LandscapeFootprint> = emptyList()
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
    drawWovenTerrain(left, top, width, height)
    drawLandscapeFootprints(footprints, left, top, width, height)
    drawWoodlandGroundcover(groundcover, left, top, width, height)

    // Winding teal WATER, explicitly not another beige or wood-colored
    // navigable path. The real wooden graph is rendered over this layer.
    val stream = Path().apply {
        val a = at(.70f, -.05f)
        moveTo(a.x, a.y)
        var p = at(.64f, .16f)
        var q = at(.78f, .25f)
        var end = at(.75f, .48f)
        cubicTo(p.x, p.y, q.x, q.y, end.x, end.y)
        p = at(.62f, .64f); q = at(.90f, .72f); end = at(.80f, 1.05f)
        cubicTo(p.x, p.y, q.x, q.y, end.x, end.y)
    }
    clipRect(left, top, left + width, top + height) {
    drawPath(stream, color = Color(0xFF5D8A85).copy(alpha = .49f),
        style = Stroke(width = u * 5.9f, cap = StrokeCap.Round))
    drawPath(stream, color = Color(0xFF93BAB0).copy(alpha = .91f),
        style = Stroke(width = u * 4.4f, cap = StrokeCap.Round))
    drawPath(stream, color = Color(0xFFC4D9BC).copy(alpha = .65f),
        style = Stroke(width = u * 1.0f, cap = StrokeCap.Round))

    }

    // Narrow rectangular timber crossings sit below the authoritative route.
    // Clip their length to the actual edge: never extend a deck across a fork.
    bridges.forEach { bridge ->
        val layout = overheadBridgeLayout(bridge) ?: return@forEach
        val middle = at(bridge.x, bridge.y)
        val vector = Offset(bridge.directionX * width, bridge.directionY * height)
        val scale = vector.getDistance()
        if (scale <= 0f) return@forEach
        val along = vector / scale
        val normal = Offset(-along.y, along.x)
        val start = middle - along * (layout.before * scale)
        val end = middle + along * (layout.after * scale)
        val deckWidth = u * layout.widthUnits
        drawLine(Color(0xFF453C2D).copy(alpha = .28f),
            start + Offset(0f, u * .28f), end + Offset(0f, u * .28f),
            strokeWidth = deckWidth + u * .3f, cap = StrokeCap.Butt)
        drawLine(Color(0xFF816044), start, end,
            strokeWidth = deckWidth, cap = StrokeCap.Butt)
        drawLine(Color(0xFFD6B481), start, end,
            strokeWidth = deckWidth - u * .48f, cap = StrokeCap.Butt)
        for (i in 1 until layout.planks) {
            val p = start + (end - start) * (i.toFloat() / layout.planks)
            drawLine(Color(0xFF8E6C48).copy(alpha = .55f),
                p - normal * (deckWidth * .43f), p + normal * (deckWidth * .43f),
                strokeWidth = u * .15f, cap = StrokeCap.Butt)
        }
        for (side in listOf(-1f, 1f)) {
            val rail = normal * (side * deckWidth * .46f)
            drawLine(Color(0xFF62472F), start + rail, end + rail,
                strokeWidth = u * .32f, cap = StrokeCap.Butt)
        }
    }

    // Smaller atlas-only ornaments use the same camera and normalized frame.
    // Missing art leaves the original procedural scene unchanged.
    environmentAccents.forEach { accent ->
        tryDrawEnvironmentSprite(environmentAtlas, accent.sprite,
            at(accent.x, accent.y), min(width, height))
    }

    features.forEach { feature ->
        val c = at(feature.x, feature.y)
        if (tryDrawEnvironmentSprite(environmentAtlas,
                EnvironmentAtlas.spriteFor(feature.kind), c, min(width, height))) {
            return@forEach
        }
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
            OverheadDecoration.MEADOW_BLOOM -> {
                // A loose golden wildflower clearing, not a new landing disc.
                drawOval(Color(0xFFB9C68A).copy(alpha = .62f),
                    topLeft = c - Offset(u * 4.3f, u * 3.0f),
                    size = Size(u * 8.6f, u * 6.0f))
                for (i in 0 until 17) {
                    val dx = ((i * 7 % 17) - 8) * u * .43f
                    val dy = ((i * 11 % 19) - 9) * u * .26f
                    val p = c + Offset(dx, dy)
                    drawLine(Color(0xFF71884D).copy(alpha = .78f),
                        p + Offset(-u * .16f, u * .52f), p,
                        strokeWidth = u * .17f, cap = StrokeCap.Round)
                    val petal = if (i % 3 == 0) Color(0xFFE9C579) else Color(0xFFF4E6B4)
                    for ((x, y) in listOf(-.26f to 0f, .26f to 0f,
                        0f to -.26f, 0f to .26f)) {
                        drawCircle(petal, u * .21f, p + Offset(x * u, y * u))
                    }
                    drawCircle(Color(0xFFB88A4D), u * .13f, p)
                }
            }
            OverheadDecoration.FIR_GROVE -> {
                // Layered radial fir crowns seen from above, distinct from meadow oaks.
                for ((dx, dy, scale) in listOf(
                    Triple(-1.7f, 1.2f, .84f), Triple(1.65f, 1.35f, .72f),
                    Triple(.05f, -1.4f, 1.0f)
                )) {
                    val p = c + Offset(dx * u, dy * u)
                    val r = u * 1.8f * scale
                    drawCircle(Color(0xFF344F39).copy(alpha = .25f),
                        r * 1.12f, p + Offset(u * .25f, u * .35f))
                    for (layer in 0..2) {
                        val radius = r * (1f - layer * .24f)
                        val crown = Path().apply {
                            for (i in 0 until 16) {
                                val angle = i * kotlin.math.PI / 8.0
                                val d = radius * if (i % 2 == 0) 1f else .58f
                                val x = p.x + kotlin.math.cos(angle).toFloat() * d
                                val y = p.y + kotlin.math.sin(angle).toFloat() * d
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                            close()
                        }
                        drawPath(crown, listOf(Color(0xFF3D6349),
                            Color(0xFF64835A), Color(0xFF93A76C))[layer])
                    }
                    drawCircle(Color(0xFFD2D49B).copy(alpha = .8f), r * .12f, p)
                }
            }
            OverheadDecoration.ORCHARD -> {
                // Four compact cultivated crowns make the village outskirts recognizable.
                drawOval(Color(0xFFD8C28D).copy(alpha = .52f),
                    topLeft = c - Offset(u * 3.8f, u * 3.5f),
                    size = Size(u * 7.6f, u * 7f))
                for (i in 0..3) {
                    val p = c + Offset((i % 2 - .5f) * u * 3.0f,
                        (i / 2 - .5f) * u * 2.9f)
                    drawCircle(Color(0xFF536844).copy(alpha = .30f),
                        u * 1.43f, p + Offset(u * .22f, u * .27f))
                    drawCircle(Color(0xFF648653), u * 1.27f, p)
                    drawCircle(Color(0xFF98AD6A), u * .90f,
                        p - Offset(u * .22f, u * .20f))
                    for ((x, y) in listOf(-.45f to -.16f, .40f to -.38f, .18f to .52f)) {
                        drawCircle(Color(0xFFB77D50), u * .19f, p + Offset(x * u, y * u))
                    }
                }
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
