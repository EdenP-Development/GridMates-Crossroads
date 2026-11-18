package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min

/**
 * ART-08: An original, deterministic watercolor-paper woodland layer.
 *
 * Board paths, tokens, forks, effects and input transforms remain ABOVE this artwork.
 * Everything is derived from normalized, fixed Woven Trail coordinates; there are no
 * runtime randomness, external image rights, app asset or save dependencies.
 *
 * Unlike the game graph, this is intentionally presentation-only. A different board
 * id gets a neutral parchment ground instead of misleading hard-coded landmarks.
 */
internal fun DrawScope.drawWoodlandBoardArt(
    boardId: String,
    left: Float,
    top: Float,
    width: Float,
    height: Float
) {
    if (width <= 0f || height <= 0f) return

    val paper = Color(0xFFECE5D4)
    val meadow = Color(0xFFACC596)
    val pine = Color(0xFF587B62)
    val forest = Color(0xFF83A78A)
    val village = Color(0xFFD5B88B)
    val ink = Color(0xFF76634D)
    val creek = Color(0xFF8FBCC3)
    val gold = Color(0xFFD9B872)

    val u = min(width, height) / 46f
    fun p(x: Float, y: Float) = Offset(left + x * width, top + y * height)

    drawRoundRect(
        color = paper,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(u * 1.4f)
    )
    if (com.playereden.crossroads.core.DecisionLab.baseId(boardId) ==
        "woodland_cedarwood_long_v01") {
        drawCedarwoodLongArt(left, top, width, height)
        return
    }
    // Brookside Dash is a second handcrafted course. Give it its own quiet
    // paper/creek identity instead of reusing Woven Trail landmarks that would
    // misrepresent its map. All art stays beneath traversable paths.
    if (com.playereden.crossroads.core.DecisionLab.baseId(boardId) ==
        "woodland_brookside_quick_v01") {
        drawOval(
            color = meadow.copy(alpha = 0.25f),
            topLeft = p(-0.14f, 0.09f),
            size = Size(width * 0.57f, height * 0.96f)
        )
        drawOval(
            color = forest.copy(alpha = 0.24f),
            topLeft = p(0.36f, -0.03f),
            size = Size(width * 0.42f, height * 1.07f)
        )
        drawOval(
            color = village.copy(alpha = 0.21f),
            topLeft = p(0.74f, 0.09f),
            size = Size(width * 0.33f, height * 0.82f)
        )
        val brook = Path().apply {
            moveTo(p(0.32f, 0.09f).x, p(0.32f, 0.09f).y)
            cubicTo(
                p(0.28f, 0.36f).x, p(0.28f, 0.36f).y,
                p(0.48f, 0.43f).x, p(0.48f, 0.43f).y,
                p(0.49f, 0.61f).x, p(0.49f, 0.61f).y
            )
            cubicTo(
                p(0.50f, 0.80f).x, p(0.50f, 0.80f).y,
                p(0.65f, 0.82f).x, p(0.65f, 0.82f).y,
                p(0.69f, 0.98f).x, p(0.69f, 0.98f).y
            )
        }
        drawPath(brook, creek.copy(alpha = 0.22f),
            style = Stroke(width = u * 1.25f, cap = StrokeCap.Round))
        drawPath(brook, Color(0xFFDDF0EF).copy(alpha = 0.48f),
            style = Stroke(width = u * 0.43f, cap = StrokeCap.Round))
        // Small landmarks, deliberately outside the critical junction text.
        listOf(0.43f to 0.09f, 0.53f to 0.13f, 0.60f to 0.18f,
            0.51f to 0.89f, 0.58f to 0.93f).forEach { (x, y) ->
            val c = p(x, y)
            val crown = Path().apply {
                moveTo(c.x, c.y - u * 0.9f)
                lineTo(c.x - u * 0.55f, c.y + u * 0.45f)
                lineTo(c.x + u * 0.55f, c.y + u * 0.45f)
                close()
            }
            drawPath(crown, pine.copy(alpha = 0.39f))
        }
        listOf(0.13f to 0.30f, 0.23f to 0.17f, 0.15f to 0.95f).forEach { (x, y) ->
            drawCircle(gold.copy(alpha = 0.27f), radius = u * 0.48f, center = p(x, y))
        }
        // Additional illustrated landmarks are purely background art. The
        // route graph, event markers and numbered tokens render after this.
        drawBrooksideDetails(left, top, width, height)
        return
    }
    // Decorative art is unique to each handcrafted course. Never paint a
    // known map's landmarks onto a different route graph.
    if (boardId != "woodland_woven_trail_v02") return

    // Very soft zones, instead of the previous blocky rectangular color panels.
    // The roads are drawn afterwards, so the regions never hide a route.
    drawOval(
        color = meadow.copy(alpha = 0.23f),
        topLeft = p(-0.17f, 0.08f),
        size = Size(width * 0.61f, height * 0.87f)
    )
    drawOval(
        color = forest.copy(alpha = 0.21f),
        topLeft = p(0.27f, -0.07f),
        size = Size(width * 0.53f, height * 1.13f)
    )
    drawOval(
        color = village.copy(alpha = 0.19f),
        topLeft = p(0.70f, 0.06f),
        size = Size(width * 0.40f, height * 0.85f)
    )
    // Faint paper contours reinforce the handmade board look without competing
    // with the safety/hazard/mischief route colors.
    fun contour(vararg coordinates: Pair<Float, Float>) {
        if (coordinates.size < 2) return
        val path = Path().apply {
            moveTo(p(coordinates[0].first, coordinates[0].second).x,
                   p(coordinates[0].first, coordinates[0].second).y)
            coordinates.drop(1).forEach { (x, y) -> lineTo(p(x, y).x, p(x, y).y) }
        }
        drawPath(path, ink.copy(alpha = 0.10f), style = Stroke(width = u * 0.10f, cap = StrokeCap.Round))
    }
    contour(0.01f to 0.15f, 0.10f to 0.11f, 0.23f to 0.14f, 0.33f to 0.11f)
    contour(0.03f to 0.25f, 0.12f to 0.22f, 0.31f to 0.24f)
    contour(0.36f to 0.94f, 0.47f to 0.96f, 0.58f to 0.93f, 0.68f to 0.97f)
    contour(0.74f to 0.91f, 0.81f to 0.88f, 0.95f to 0.90f)

    // A winding stream makes the Cedar Bridge area recognizable. Rendered BELOW
    // paths and fork circles: the actual bridge/route remains visibly traversable.
    val water = Path().apply {
        moveTo(p(0.35f, 0.48f).x, p(0.35f, 0.48f).y)
        cubicTo(
            p(0.43f, 0.52f).x, p(0.43f, 0.52f).y,
            p(0.37f, 0.67f).x, p(0.37f, 0.67f).y,
            p(0.50f, 0.70f).x, p(0.50f, 0.70f).y
        )
        cubicTo(
            p(0.59f, 0.74f).x, p(0.59f, 0.74f).y,
            p(0.57f, 0.83f).x, p(0.57f, 0.83f).y,
            p(0.64f, 0.96f).x, p(0.64f, 0.96f).y
        )
    }
    drawPath(water, creek.copy(alpha = 0.22f),
        style = Stroke(width = u * 1.35f, cap = StrokeCap.Round))
    drawPath(water, Color(0xFFDAEFF0).copy(alpha = 0.39f),
        style = Stroke(width = u * 0.45f, cap = StrokeCap.Round))

    fun flower(x: Float, y: Float, scale: Float, tint: Color) {
        val c = p(x, y)
        val r = u * scale
        listOf(Offset(-r, 0f), Offset(r, 0f), Offset(0f, -r), Offset(0f, r)).forEach { d ->
            drawCircle(tint.copy(alpha = 0.60f), radius = r * 0.54f, center = c + d)
        }
        drawCircle(gold.copy(alpha = 0.74f), radius = r * 0.45f, center = c)
    }

    // Meadow: low grass, dandelion-like flowers and broad open patches.
    listOf(
        0.07f to 0.31f, 0.29f to 0.12f, 0.08f to 0.55f,
        0.30f to 0.64f, 0.07f to 0.96f, 0.34f to 0.93f,
        0.32f to 0.35f, 0.23f to 0.51f
    ).forEachIndexed { index, (x, y) ->
        val o = p(x, y)
        val spread = u * if (index % 2 == 0) 0.78f else 0.55f
        drawLine(pine.copy(alpha = 0.22f), o + Offset(-spread, 0f),
            o + Offset(0f, -spread * 0.65f), strokeWidth = u * 0.12f, cap = StrokeCap.Round)
        drawLine(pine.copy(alpha = 0.22f), o + Offset(spread, 0f),
            o + Offset(0f, -spread * 0.65f), strokeWidth = u * 0.12f, cap = StrokeCap.Round)
    }
    flower(0.06f, 0.22f, 0.27f, Color(0xFFEEE1BB))
    flower(0.30f, 0.53f, 0.32f, Color(0xFFE5C7B6))
    flower(0.06f, 0.72f, 0.28f, Color(0xFFF4E2BD))
    flower(0.34f, 0.14f, 0.26f, Color(0xFFD7C1D7))
    flower(0.33f, 0.95f, 0.27f, Color(0xFFE5C7B6))

    fun pineTree(x: Float, y: Float, scale: Float, lighter: Boolean) {
        val c = p(x, y)
        val radius = u * scale
        drawLine(ink.copy(alpha = 0.34f),
            c + Offset(0f, radius * 0.42f), c + Offset(0f, radius * 1.50f),
            strokeWidth = radius * 0.28f, cap = StrokeCap.Round)
        val triangle = Path().apply {
            moveTo(c.x, c.y - radius * 1.12f)
            lineTo(c.x - radius * 0.92f, c.y + radius * 0.68f)
            lineTo(c.x + radius * 0.92f, c.y + radius * 0.68f)
            close()
        }
        drawPath(triangle,
            (if (lighter) Color(0xFF9BB69A) else pine).copy(alpha = 0.55f))
        drawLine(Color(0xFFD9E5CD).copy(alpha = 0.38f),
            c + Offset(0f, -radius * 0.67f),
            c + Offset(0f, radius * 0.36f),
            strokeWidth = u * 0.075f, cap = StrokeCap.Round)
    }

    // Forest: groups with breathing room around important forks. Trees are pale
    // and behind path strokes, not icons that the player might mistake for events.
    listOf(
        Triple(0.40f, 0.10f, 0.90f), Triple(0.46f, 0.08f, 0.64f),
        Triple(0.59f, 0.075f, 0.86f), Triple(0.65f, 0.11f, 0.64f),
        Triple(0.52f, 0.29f, 0.56f), Triple(0.58f, 0.28f, 0.54f),
        Triple(0.39f, 0.29f, 0.60f), Triple(0.68f, 0.31f, 0.61f),
        Triple(0.52f, 0.60f, 0.54f), Triple(0.66f, 0.60f, 0.67f),
        Triple(0.40f, 0.88f, 0.72f), Triple(0.46f, 0.96f, 0.58f),
        Triple(0.63f, 0.94f, 0.74f), Triple(0.70f, 0.92f, 0.56f)
    ).forEachIndexed { i, (x, y, scale) -> pineTree(x, y, scale, i % 3 == 0) }

    fun cottage(x: Float, y: Float, scale: Float) {
        val c = p(x, y)
        val w = u * scale * 1.65f
        val h = u * scale * 1.15f
        drawRoundRect(
            color = Color(0xFFE5D2AB).copy(alpha = 0.82f),
            topLeft = Offset(c.x - w / 2f, c.y - h * 0.22f),
            size = Size(w, h),
            cornerRadius = CornerRadius(u * 0.13f)
        )
        val roof = Path().apply {
            moveTo(c.x - w * 0.64f, c.y - h * 0.20f)
            lineTo(c.x, c.y - h * 0.93f)
            lineTo(c.x + w * 0.64f, c.y - h * 0.20f)
            close()
        }
        drawPath(roof, Color(0xFFB67F63).copy(alpha = 0.63f))
        drawRoundRect(
            color = ink.copy(alpha = 0.35f),
            topLeft = Offset(c.x - u * 0.15f, c.y + h * 0.23f),
            size = Size(u * 0.30f, h * 0.55f),
            cornerRadius = CornerRadius(u * 0.07f)
        )
    }

    // Village: a few friendly rooftops, fence posts and orchard trees.
    cottage(0.78f, 0.135f, 0.88f)
    cottage(0.855f, 0.115f, 0.74f)
    cottage(0.77f, 0.49f, 0.71f)
    cottage(0.895f, 0.84f, 0.88f)
    cottage(0.955f, 0.77f, 0.64f)
    listOf(0.78f to 0.88f, 0.85f to 0.93f, 0.975f to 0.94f).forEach { (x, y) ->
        val c = p(x, y)
        drawCircle(meadow.copy(alpha = 0.61f), radius = u * 0.72f, center = c)
        drawCircle(pine.copy(alpha = 0.21f), radius = u * 0.36f,
            center = c + Offset(-u * 0.19f, -u * 0.12f))
    }
    // Sparse rustic fencing marks the lower village edge.
    (0..7).forEach { index ->
        val c = p(0.75f + 0.03f * index, 0.98f)
        drawLine(ink.copy(alpha = 0.31f), c + Offset(0f, -u * 0.45f),
            c + Offset(0f, u * 0.20f), strokeWidth = u * 0.13f, cap = StrokeCap.Round)
        if (index != 7) {
            val n = p(0.75f + 0.03f * (index + 1), 0.98f)
            drawLine(ink.copy(alpha = 0.23f), c + Offset(0f, -u * 0.15f),
                n + Offset(0f, -u * 0.15f), strokeWidth = u * 0.095f)
        }
    }
}
