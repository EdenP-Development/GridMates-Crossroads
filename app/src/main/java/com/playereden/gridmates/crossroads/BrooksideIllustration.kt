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
 * Original, deliberately subtle illustrated scenery for Brookside Dash.
 * Called beneath the actual board graph. Nothing here is an interactive space,
 * route, die modifier, network state, or checkpoint input.
 *
 * Positions are fixed for the Brookside course only, so each install renders
 * the same miniature woodland atlas (no runtime randomness or image licensing).
 */
internal fun DrawScope.drawBrooksideDetails(
    left: Float, top: Float, width: Float, height: Float
) {
    if (width <= 0f || height <= 0f) return
    val u = min(width, height) / 48f
    fun at(x: Float, y: Float) = Offset(left + x * width, top + y * height)

    val forest = Color(0xFF52765B)
    val mutedPine = Color(0xFF74987B)
    val earth = Color(0xFF806C50)
    val roof = Color(0xFFB78E62)
    val petals = Color(0xFFE0CF9C)
    val stream = Color(0xFF729FA9)

    fun conifer(x: Float, y: Float, scale: Float) {
        val c = at(x, y)
        val r = u * scale
        drawOval(
            earth.copy(alpha = 0.10f),
            topLeft = c + Offset(-r * 0.78f, r * 0.7f),
            size = Size(r * 1.55f, r * 0.5f)
        )
        drawLine(
            earth.copy(alpha = 0.38f),
            c + Offset(0f, -r * 0.24f),
            c + Offset(0f, r * 1.13f),
            strokeWidth = r * 0.16f,
            cap = StrokeCap.Round
        )
        for (layer in 0..2) {
            val w = r * (0.82f - layer * 0.12f)
            val dy = -r * 0.88f + layer * r * 0.43f
            val triangle = Path().apply {
                moveTo(c.x, c.y + dy - r * 0.72f)
                lineTo(c.x - w, c.y + dy + r * 0.65f)
                lineTo(c.x + w, c.y + dy + r * 0.65f)
                close()
            }
            drawPath(triangle, (if (layer % 2 == 0) forest else mutedPine)
                .copy(alpha = 0.25f + layer * 0.045f))
        }
    }

    fun cottage(x: Float, y: Float, scale: Float) {
        val c = at(x, y)
        val r = u * scale
        drawOval(
            earth.copy(alpha = 0.11f),
            topLeft = c + Offset(-r * 1.22f, r * 0.72f),
            size = Size(r * 2.6f, r * 0.45f)
        )
        drawRoundRect(
            Color(0xFFDBCAAA).copy(alpha = 0.70f),
            topLeft = c + Offset(-r, -r * 0.32f),
            size = Size(r * 2f, r * 1.30f),
            cornerRadius = CornerRadius(r * 0.12f)
        )
        val rooftop = Path().apply {
            moveTo(c.x - r * 1.25f, c.y - r * 0.32f)
            lineTo(c.x, c.y - r * 1.38f)
            lineTo(c.x + r * 1.25f, c.y - r * 0.32f)
            close()
        }
        drawPath(rooftop, roof.copy(alpha = 0.60f))
        drawRoundRect(
            earth.copy(alpha = 0.42f),
            topLeft = c + Offset(-r * 0.22f, r * 0.18f),
            size = Size(r * 0.43f, r * 0.80f),
            cornerRadius = CornerRadius(r * 0.05f)
        )
        drawCircle(petals.copy(alpha = 0.55f), r * 0.16f,
            c + Offset(r * 0.58f, r * 0.10f))
    }

    fun meadowTuft(x: Float, y: Float, scale: Float) {
        val c = at(x, y)
        val r = u * scale
        val shade = forest.copy(alpha = 0.20f)
        drawLine(shade, c + Offset(-r, 0f), c + Offset(0f, -r * 0.68f),
            strokeWidth = r * 0.13f, cap = StrokeCap.Round)
        drawLine(shade, c + Offset(r, 0f), c + Offset(0f, -r * 0.68f),
            strokeWidth = r * 0.13f, cap = StrokeCap.Round)
        drawLine(shade, c, c + Offset(0f, -r),
            strokeWidth = r * 0.10f, cap = StrokeCap.Round)
    }

    fun flower(x: Float, y: Float, scale: Float) {
        val c = at(x, y)
        val r = u * scale
        drawCircle(petals.copy(alpha = 0.28f), r * 1.2f, c)
        listOf(
            Offset(-r * 0.38f, 0f), Offset(r * 0.38f, 0f),
            Offset(0f, -r * 0.38f), Offset(0f, r * 0.38f)
        ).forEach { shift ->
            drawCircle(Color(0xFFF4E3C8).copy(alpha = 0.47f), r * 0.36f, c + shift)
        }
        drawCircle(roof.copy(alpha = 0.48f), r * 0.30f, c)
    }

    // Meadow, forest, and village need distinct landmarks. Keep the middle
    // of the actual route lighter so numbered pawns and decision circles win.
    listOf(
        Triple(0.44f, 0.10f, 0.82f), Triple(0.52f, 0.10f, 1.00f),
        Triple(0.62f, 0.15f, 0.80f), Triple(0.68f, 0.09f, 0.66f),
        Triple(0.55f, 0.91f, 0.90f), Triple(0.63f, 0.91f, 0.70f),
        Triple(0.46f, 0.91f, 0.76f), Triple(0.37f, 0.96f, 0.60f)
    ).forEach { (x, y, scale) -> conifer(x, y, scale) }

    listOf(
        Triple(0.86f, 0.13f, 0.75f),
        Triple(0.96f, 0.75f, 0.68f),
        Triple(0.84f, 0.90f, 0.59f)
    ).forEach { (x, y, scale) -> cottage(x, y, scale) }

    listOf(
        Triple(0.10f, 0.14f, 0.66f), Triple(0.16f, 0.20f, 0.72f),
        Triple(0.25f, 0.13f, 0.52f), Triple(0.06f, 0.42f, 0.75f),
        Triple(0.28f, 0.89f, 0.61f), Triple(0.12f, 0.96f, 0.69f),
        Triple(0.79f, 0.15f, 0.52f), Triple(0.83f, 0.96f, 0.47f)
    ).forEach { (x, y, scale) -> meadowTuft(x, y, scale) }
    listOf(
        Triple(0.12f, 0.26f, 0.58f), Triple(0.22f, 0.13f, 0.53f),
        Triple(0.06f, 0.64f, 0.50f), Triple(0.20f, 0.94f, 0.65f),
        Triple(0.77f, 0.88f, 0.49f)
    ).forEach { (x, y, scale) -> flower(x, y, scale) }

    // Pebbles and small ripples make the creek read as water rather than a
    // translucent route. They are intentionally less contrasty than real nodes.
    listOf(
        0.31f to 0.24f, 0.36f to 0.38f, 0.41f to 0.54f,
        0.54f to 0.79f, 0.62f to 0.93f
    ).forEachIndexed { index, (x, y) ->
        val c = at(x, y)
        drawOval(
            earth.copy(alpha = 0.20f),
            topLeft = c + Offset(if (index % 2 == 0) -u * 1.15f else u * 0.46f, 0f),
            size = Size(u * 0.85f, u * 0.37f)
        )
        drawLine(
            stream.copy(alpha = 0.24f),
            c + Offset(-u * 0.65f, -u * 0.75f),
            c + Offset(u * 0.65f, -u * 0.75f),
            strokeWidth = u * 0.10f,
            cap = StrokeCap.Round
        )
    }

    // A restrained wood footbridge is an identifiable landmark, not an
    // alternate traversal path. Actual valid routes are rendered afterwards.
    val bridge = at(0.49f, 0.64f)
    drawLine(
        earth.copy(alpha = 0.22f),
        bridge + Offset(-u * 1.7f, -u * 0.4f),
        bridge + Offset(u * 1.7f, u * 0.35f),
        strokeWidth = u * 0.56f, cap = StrokeCap.Round
    )
    for (step in -1..1) {
        drawLine(
            Color(0xFFF1E2BF).copy(alpha = 0.28f),
            bridge + Offset(step * u * 0.65f, -u * 0.54f),
            bridge + Offset(step * u * 0.65f, u * 0.52f),
            strokeWidth = u * 0.15f, cap = StrokeCap.Round
        )
    }

    // Fine edge contours feel like a printed atlas without creating fake paths.
    val contour = Path().apply {
        moveTo(at(0.02f, 0.19f).x, at(0.02f, 0.19f).y)
        cubicTo(
            at(0.12f, 0.11f).x, at(0.12f, 0.11f).y,
            at(0.21f, 0.11f).x, at(0.21f, 0.11f).y,
            at(0.31f, 0.17f).x, at(0.31f, 0.17f).y
        )
    }
    drawPath(contour, earth.copy(alpha = 0.085f),
        style = Stroke(width = u * 0.10f, cap = StrokeCap.Round))
}
