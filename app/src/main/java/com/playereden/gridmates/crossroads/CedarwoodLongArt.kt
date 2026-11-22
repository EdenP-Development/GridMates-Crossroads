package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min

/**
 * A small printed-atlas backdrop for the Long course's two sweeping loops.
 * Drawn exclusively below real route edges, forks and tokens, with no RNG or
 * dependencies on save data, encounter odds or touch coordinates.
 */
internal fun DrawScope.drawCedarwoodLongArt(
    left: Float, top: Float, width: Float, height: Float
) {
    if (width <= 0f || height <= 0f) return
    fun p(x: Float, y: Float) = Offset(left + x * width, top + y * height)
    val u = min(width, height) / 50f
    val pine = Color(0xFF4D755B)
    val moss = Color(0xFF97B19A)
    val gold = Color(0xFFD7B877)
    val oak = Color(0xFF816C50)
    val water = Color(0xFF80B0BB)

    drawOval(
        moss.copy(alpha = .19f), topLeft = p(-.08f, .39f),
        size = Size(width * .56f, height * .64f)
    )
    drawOval(
        pine.copy(alpha = .14f), topLeft = p(.27f, .08f),
        size = Size(width * .52f, height * .78f)
    )
    drawOval(
        gold.copy(alpha = .15f), topLeft = p(.68f, .38f),
        size = Size(width * .38f, height * .48f)
    )

    // One winding lake/stream connects the lowland and upper forest.
    // Crossing-looking details are intentionally low-contrast compared
    // with actual graph edges drawn over them later.
    val stream = Path().apply {
        moveTo(p(.17f, .95f).x, p(.17f, .95f).y)
        cubicTo(
            p(.33f, .84f).x, p(.33f, .84f).y,
            p(.51f, .63f).x, p(.51f, .63f).y,
            p(.47f, .42f).x, p(.47f, .42f).y
        )
        cubicTo(
            p(.46f, .26f).x, p(.46f, .26f).y,
            p(.58f, .15f).x, p(.58f, .15f).y,
            p(.76f, .06f).x, p(.76f, .06f).y
        )
    }
    drawPath(stream, water.copy(alpha = .17f),
        style = Stroke(width = u * 1.6f, cap = StrokeCap.Round))
    drawPath(stream, Color(0xFFE6F2E8).copy(alpha = .38f),
        style = Stroke(width = u * .48f, cap = StrokeCap.Round))

    fun tree(x: Float, y: Float, size: Float) {
        val c = p(x, y)
        val r = u * size
        drawLine(oak.copy(alpha = .31f), c + Offset(0f, r*.2f),
            c + Offset(0f, r*.85f), strokeWidth = r*.16f)
        for (layer in 0..2) {
            val centerY = c.y - r*(.47f - layer*.19f)
            val tri = Path().apply {
                moveTo(c.x, centerY-r*.60f)
                lineTo(c.x-r*(.63f-layer*.09f), centerY+r*.38f)
                lineTo(c.x+r*(.63f-layer*.09f), centerY+r*.38f)
                close()
            }
            drawPath(tri, (if (layer%2==0) pine else moss)
                .copy(alpha = .25f+layer*.035f))
        }
    }
    listOf(
        Triple(.37f,.09f,.72f), Triple(.44f,.06f,.61f),
        Triple(.62f,.05f,.83f), Triple(.72f,.20f,.72f),
        Triple(.39f,.91f,.74f), Triple(.46f,.93f,.89f),
        Triple(.56f,.88f,.65f), Triple(.70f,.85f,.70f),
        Triple(.79f,.94f,.75f), Triple(.29f,.08f,.69f)
    ).forEach { (x,y,z) -> tree(x,y,z) }

    fun cottage(x: Float, y: Float) {
        val c=p(x,y)
        drawRect(Color(0xFFD8C8A7).copy(alpha=.55f),
            topLeft=c+Offset(-u*.77f,-u*.11f),
            size=Size(u*1.54f,u*1.03f))
        val roof=Path().apply {
            moveTo(c.x-u, c.y-u*.10f)
            lineTo(c.x, c.y-u*.91f)
            lineTo(c.x+u,c.y-u*.10f)
            close()
        }
        drawPath(roof, oak.copy(alpha=.40f))
        drawRect(oak.copy(alpha=.30f),
            topLeft=c+Offset(-u*.16f,u*.31f),
            size=Size(u*.32f,u*.60f))
    }
    listOf(.07f to .54f,.10f to .17f,.88f to .12f,.87f to .87f)
        .forEach { (x,y) -> cottage(x,y) }

    // Gentle paper constellations of leaves imply a wooded expedition
    // without creating false nodes or obscuring event colors.
    listOf(.11f to .09f,.28f to .92f,.76f to .08f,.83f to .33f,
           .67f to .91f,.15f to .97f,.96f to .48f).forEachIndexed { i, (x,y) ->
        val c=p(x,y)
        drawOval(
            gold.copy(alpha=.23f),
            topLeft=c+Offset(-u*.33f,-u*.18f),
            size=Size(u*.66f,u*.35f)
        )
        drawLine(oak.copy(alpha=.17f),
            c+Offset(-u*.45f,u*.05f),c+Offset(u*.35f,-u*.18f),
            strokeWidth=u*.08f,cap=StrokeCap.Round)
    }
}
