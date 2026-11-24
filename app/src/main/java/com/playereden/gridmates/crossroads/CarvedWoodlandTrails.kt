package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.floor

/**
 * VISUAL-83: High-resolution, carved wooden road sections for the actual
 * PLAYABLE illustrated course. The path still comes from authoritative
 * BoardNode edges; no embedded road geometry exists inside the scenic PNG.
 *
 * Unlike the original 128x32 texture strip this stays sharp at 2-4x zoom
 * and keeps the same hand-painted paper, bark, moss and brass vocabulary as
 * the rest of the physical board.
 */
internal data class CarvedRoadLayout(
    val weight: Float,
    val seamFractions: List<Float>
)

/**
 * QA1672 phone review: cream-wide roads covered too much of the painted
 * wilderness. Reduce visual road *ink*, not the graph, map, pawn size or
 * route tapping radius. The color-coded edge remains readable.
 */
internal const val CARVED_ROAD_INK_RATIO = 0.79f

/**
 * Decorative joinery is sparse on short/dense graph edges. It must never
 * become a false stepping stone, extra selectable node or confusing grid.
 * Entirely deterministic and independent of gameplay seed / die values.
 */
internal fun carvedRoadLayout(
    lengthPx: Float,
    dense: Boolean,
    visibilityScale: Float
): CarvedRoadLayout {
    if (!lengthPx.isFinite() || lengthPx < 1f ||
        !visibilityScale.isFinite() || visibilityScale <= 0f) {
        return CarvedRoadLayout(0f, emptyList())
    }
    val weight = (if (dense) 0.87f else 1f) *
        visibilityScale * CARVED_ROAD_INK_RATIO
    // Make wood joints fewer rather than densely tiled across every scene.
    // Use actual screen-space magnification, not reduced stroke weight.
    val seamEvery = 95f * visibilityScale
    val sections = floor(lengthPx / seamEvery).toInt().coerceIn(0, 5)
    val seams = if (sections == 0) emptyList() else
        (1..sections).map { it.toFloat() / (sections + 1) }
    return CarvedRoadLayout(weight, seams)
}

internal fun carvedRoadRiskColor(route: String): Color = when (route) {
    "safe" -> Color(0xFF638764)
    "risky" -> Color(0xFFB17A37)
    "mischief" -> Color(0xFF8C6B9F)
    else -> Color(0xFFA48C66)
}

/**
 * Draw the wooden trail UNDER stops, illustrated medallions, route-choice
 * highlighting and travelers. Every layer shares the existing camera
 * transform and no touch event is owned by this decoration.
 */
/**
 * A matte, earthy path with a single colored margin rather than parallel
 * polished rails. The centerline is still the exact legal board edge.
 */
internal fun DrawScope.drawCrossroadsEarthenTrail(
    from: Offset, to: Offset, route: String, dense: Boolean,
    visibilityScale: Float = 1f
) {
    val delta = to - from
    val length = delta.getDistance()
    val w = carvedRoadLayout(length, dense, visibilityScale).weight
    if (w <= 0f) return
    val normal = Offset(-delta.y / length, delta.x / length)
    drawLine(Color(0xFF52613B).copy(alpha = .25f), from, to,
        strokeWidth = 25f*w, cap = StrokeCap.Round)
    drawLine(Color(0xFF66543A), from, to,
        strokeWidth = 19f*w, cap = StrokeCap.Round)
    drawLine(carvedRoadRiskColor(route), from, to,
        strokeWidth = 16f*w, cap = StrokeCap.Round)
    drawLine(Color(0xFFC4A575), from, to,
        strokeWidth = 11f*w, cap = StrokeCap.Round)
    // Small broken flecks suggest packed earth, never transverse plank joints.
    val count = (length / (13f * visibilityScale)).toInt().coerceIn(1, 36)
    for (i in 1..count) {
        val t = i.toFloat() / (count + 1)
        val p = from + delta*t + normal*((i*7%5-2)*1.3f*w)
        val tint = if (i%2==0) Color(0xFFEDCF95) else Color(0xFF92734B)
        drawLine(tint.copy(alpha=.40f),p,p+delta/length*(2.2f*w),
            strokeWidth=1.2f*w,cap=StrokeCap.Round)
    }
}

internal fun DrawScope.drawCrossroadsCarvedTrail(
    from: Offset,
    to: Offset,
    route: String,
    dense: Boolean,
    visibilityScale: Float = 1f
) {
    val delta = to - from
    val distance = delta.getDistance()
    val layout = carvedRoadLayout(distance, dense, visibilityScale)
    val w = layout.weight
    if (w <= 0f) return
    val direction = delta / distance
    val normal = Offset(-direction.y, direction.x)
    val risk = carvedRoadRiskColor(route)

    // A real physical object sits just above the painted ground: its soft
    // offset shadow is visible even over light yellow meadow scenery.
    drawLine(
        Color(0xFF1C3327).copy(alpha = 0.24f),
        from + Offset(0f, 2.8f * w), to + Offset(0f, 2.8f * w),
        strokeWidth = 24f * w, cap = StrokeCap.Round
    )
    drawLine(Color(0xFF42372A), from, to,
        strokeWidth = 22f * w, cap = StrokeCap.Round)
    drawLine(Color(0xFF846440), from, to,
        strokeWidth = 19.4f * w, cap = StrokeCap.Round)
    // Distinctive green/amber/purple inlay is structural, not just a label.
    drawLine(risk, from, to,
        strokeWidth = 17.3f * w, cap = StrokeCap.Round)
    // Warmer, narrower wood deck exposes more scenic illustration and
    // lets the green/amber/purple inlay read as a deliberate route marking.
    drawLine(Color(0xFFC5A374), from, to,
        strokeWidth = 12.8f * w, cap = StrokeCap.Round)
    drawLine(Color(0xFFE0C18C), from - normal * (1.1f * w),
        to - normal * (1.1f * w),
        strokeWidth = 7.2f * w, cap = StrokeCap.Round)

    // Fine engraved grain instead of magnified blurry atlas pixels.
    drawLine(Color(0xFFF2DBAB).copy(alpha = 0.56f),
        from - normal * (3.4f * w), to - normal * (3.4f * w),
        strokeWidth = 0.85f * w, cap = StrokeCap.Round)
    drawLine(Color(0xFF795936).copy(alpha = 0.35f),
        from + normal * (4.5f * w), to + normal * (4.5f * w),
        strokeWidth = 1.1f * w, cap = StrokeCap.Round)

    // A couple of restrained wood joints make long road pieces feel
    // constructed instead of being infinitely stretched digital lines.
    layout.seamFractions.forEach { t ->
        val at = from + delta * t
        drawLine(
            Color(0xFF7A5635).copy(alpha = 0.66f),
            at - normal * (5.5f * w),
            at + normal * (5.5f * w),
            strokeWidth = 1.4f * w, cap = StrokeCap.Round
        )
        drawLine(
            Color(0xFFFFF0C4).copy(alpha = 0.61f),
            at - normal * (5.4f * w) + direction * (1.3f * w),
            at + normal * (5.4f * w) + direction * (1.3f * w),
            strokeWidth = 0.55f * w, cap = StrokeCap.Round
        )
    }
}
