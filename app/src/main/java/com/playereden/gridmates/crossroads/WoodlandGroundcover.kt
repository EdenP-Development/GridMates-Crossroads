package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeCap
import com.playereden.crossroads.core.BoardNode
import kotlin.math.hypot
import kotlin.math.min

internal data class GroundcoverPatch(val x: Float, val y: Float, val radius: Float, val kind: Int)

internal object WoodlandGroundcover {
    /** Deterministic small vegetation, with room for the actual graph and existing landmarks. */
    fun plan(nodes: List<BoardNode>, style: CourseOverheadStyle,
             occupied: List<Offset>): List<GroundcoverPatch> {
        if (nodes.isEmpty()) return emptyList()
        return buildList {
            // Prefer small companion plants near existing landmarks, rather
            // than evenly scattering identical tufts throughout the board.
            // Keep the same clearance, finite placement budget and stable order.
            for ((index, anchor) in occupied.withIndex()) {
                for (j in 0 until 5) {
                    val angle = (index * 137.5 + j * 72.0) * kotlin.math.PI / 180.0
                    val distance = .075f + (j % 3) * .009f
                    val x = anchor.x + kotlin.math.cos(angle).toFloat() * distance
                    val y = anchor.y + kotlin.math.sin(angle).toFloat() * distance
                    val radius = .007f + (j % 3) * .001f
                    if (x !in radius..(1f-radius) || y !in radius..(1f-radius)) continue
                    if (!OverheadScenery.clearOfPlay(x,y,nodes,.034f+radius)) continue
                    if (occupied.any { hypot(x-it.x,y-it.y) < .055f+radius }) continue
                    if (any { p: GroundcoverPatch -> hypot(x-p.x,y-p.y) < p.radius+radius+.006f }) continue
                    add(GroundcoverPatch(x,y,radius,if (j % 3 == 0) 3 else j % 2))
                    if (size >= 60) break
                }
                if (size >= 60) break
            }
            for (i in 0 until 400) {
                val x = (i % 20 + .22f + (i * 17 % 53) / 100f) / 20f
                val y = (i / 20 + .22f + (i * 29 % 53) / 100f) / 20f
                val forest = when (style) {
                    CourseOverheadStyle.CEDARWOOD -> true
                    CourseOverheadStyle.BROOKSIDE -> x > .66f
                    CourseOverheadStyle.WOVEN -> x in .34f.. .69f
                    CourseOverheadStyle.REGIONAL -> y in .38f.. .69f
                }
                // Quiet gaps give groves and clearings greater visual contrast.
                if (!forest && i % 2 == 0) continue
                if (forest && i % 7 == 0) continue
                val radius = .006f + (i % 4) * .002f
                if (x !in radius..(1f-radius) || y !in radius..(1f-radius)) continue
                if (!OverheadScenery.clearOfPlay(x,y,nodes,.034f+radius)) continue
                if (occupied.any { hypot(x-it.x,y-it.y) < .055f+radius }) continue
                if (any { p: GroundcoverPatch -> hypot(x-p.x,y-p.y) < p.radius+radius+.006f }) continue
                add(GroundcoverPatch(x,y,radius,if (forest) i % 2 else 2 + i % 2))
            }
        }
    }
}

/** Small irregular leaves and flowers, not circular waypoint-like symbols. */
internal fun DrawScope.drawWoodlandGroundcover(
    patches: List<GroundcoverPatch>, left: Float, top: Float, width: Float, height: Float
) {
    val unit = min(width,height)
    if (unit <= 0f) return
    patches.forEach { p ->
        val c = Offset(left+p.x*width,top+p.y*height)
        val r = p.radius*unit
        val ink = if (p.kind < 2) Color(0xFF526B43) else Color(0xFF7A8C4D)
        for (j in 0..2) {
            val d = (j-1)*r*.46f
            val foot = c+Offset(d,r*.44f)
            val tip = c+Offset(d*1.65f,-r*(.50f+j%2*.22f))
            drawLine(ink.copy(alpha=.58f),foot,tip,r*.20f,StrokeCap.Round)
            drawLine(Color(0xFFA4B27A).copy(alpha=.65f),tip,
                tip+Offset(r*.24f,r*.22f),r*.25f,StrokeCap.Round)
            if (p.kind == 3) {
                drawCircle(Color(0xFFF2E0AD),r*.19f,tip)
            }
        }
    }
}
