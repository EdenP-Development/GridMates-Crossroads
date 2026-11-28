package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import com.playereden.crossroads.core.BoardNode
import com.playereden.crossroads.core.DecisionLab
import kotlin.math.min

internal enum class CourseOverheadStyle {
    BROOKSIDE, WOVEN, CEDARWOOD, REGIONAL;

    companion object {
        fun forBoard(id: String): CourseOverheadStyle? = when (DecisionLab.baseId(id)) {
            "woodland_brookside_quick_v01" -> BROOKSIDE
            "woodland_woven_trail_v02" -> WOVEN
            "woodland_cedarwood_long_v01" -> CEDARWOOD
            CrossroadsCourse.REGIONAL_ID -> REGIONAL
            else -> null
        }
    }
}

/** Independently authored positions; never transfer Woven's stream or landmarks. */
internal object CourseOverheadScenery {
    private fun p(x: Float, y: Float, sprite: EnvironmentSprite) =
        EnvironmentAccent(x, y, sprite)

    fun plan(style: CourseOverheadStyle, nodes: List<BoardNode>): List<EnvironmentAccent> {
        if (nodes.isEmpty()) return emptyList()
        val proposed = when (style) {
            CourseOverheadStyle.WOVEN -> return emptyList()
            CourseOverheadStyle.BROOKSIDE -> listOf(
                p(.17f,.064f,EnvironmentSprite.MEADOW_CLEARING),
                p(.398f,.182f,EnvironmentSprite.OAK),
                p(.602f,.072f,EnvironmentSprite.FIR_GROVE),
                p(.83f,.162f,EnvironmentSprite.COTTAGE),
                p(.064f,.162f,EnvironmentSprite.FLOWERS),
                p(.174f,.292f,EnvironmentSprite.FLOWERS),
                p(.068f,.39f,EnvironmentSprite.BUSH),
                p(.06f,.504f,EnvironmentSprite.OAK),
                p(.394f,.496f,EnvironmentSprite.MEADOW_CLEARING),
                p(.386f,.61f,EnvironmentSprite.ROCK),
                p(.72f,.602f,EnvironmentSprite.FLOWERS),
                p(.948f,.72f,EnvironmentSprite.COTTAGE),
                p(.174f,.834f,EnvironmentSprite.FLOWERS),
                p(.508f,.826f,EnvironmentSprite.REEDS),
                p(.606f,.842f,EnvironmentSprite.OAK),
                p(.94f,.834f,EnvironmentSprite.ORCHARD),
                p(.284f,.936f,EnvironmentSprite.BUSH),
                p(.716f,.944f,EnvironmentSprite.FENCE)
            )
            CourseOverheadStyle.REGIONAL -> listOf(
                p(.18f,.10f,EnvironmentSprite.COTTAGE),
                p(.42f,.11f,EnvironmentSprite.ORCHARD),
                p(.75f,.10f,EnvironmentSprite.COTTAGE),
                p(.30f,.43f,EnvironmentSprite.FIR_GROVE),
                p(.53f,.43f,EnvironmentSprite.OAK),
                p(.72f,.43f,EnvironmentSprite.FIR_GROVE),
                p(.28f,.74f,EnvironmentSprite.FLOWERS),
                p(.52f,.74f,EnvironmentSprite.MEADOW_CLEARING),
                p(.72f,.74f,EnvironmentSprite.FLOWERS),
                // Village outskirts and grouped forest companions, screened by the same graph clearance.
                p(.08f,.08f,EnvironmentSprite.BUSH),
                p(.30f,.08f,EnvironmentSprite.FENCE),
                p(.55f,.07f,EnvironmentSprite.BUSH),
                p(.88f,.09f,EnvironmentSprite.ORCHARD),
                p(.63f,.44f,EnvironmentSprite.FIR),
                p(.85f,.43f,EnvironmentSprite.FIR),
                p(.40f,.75f,EnvironmentSprite.BUSH),
                // Trailhead boundary and destination cottage: real empty terrain,
                // never a new route or an obstruction on the S/F socket.
                p(.15f,.725f,EnvironmentSprite.FENCE),
                p(.925f,.175f,EnvironmentSprite.COTTAGE)
            )
            CourseOverheadStyle.CEDARWOOD -> listOf(
                p(.052f,.048f,EnvironmentSprite.FIR),
                p(.17f,.064f,EnvironmentSprite.FIR_GROVE),
                p(.838f,.048f,EnvironmentSprite.FIR),
                p(.94f,.292f,EnvironmentSprite.ROCK),
                p(.052f,.618f,EnvironmentSprite.FIR),
                p(.948f,.72f,EnvironmentSprite.FIR),
                p(.94f,.834f,EnvironmentSprite.FIR_GROVE),
                p(.166f,.948f,EnvironmentSprite.ROCK),
                p(.284f,.936f,EnvironmentSprite.FIR_GROVE),
                p(.382f,.952f,EnvironmentSprite.MUSHROOMS),
                p(.5f,.94f,EnvironmentSprite.FIR),
                p(.834f,.932f,EnvironmentSprite.COTTAGE),
                p(.932f,.948f,EnvironmentSprite.FIR)
            )
        }
        return buildList {
            for (a in proposed) {
                val radius = a.sprite.diameter * kotlin.math.sqrt(2f) / 2f
                if (a.x !in radius..(1f-radius) || a.y !in radius..(1f-radius)) continue
                if (!OverheadScenery.clearOfPlay(a.x, a.y, nodes, .078f)) continue
                if (any { b: EnvironmentAccent ->
                    kotlin.math.hypot(a.x-b.x,a.y-b.y) < .09f
                }) continue
                add(a)
            }
        }
    }
}

/** Fixed overlapping pigment pools soften the regional boundaries without suggesting new paths. */
internal data class RegionalTerrainWash(val x: Float, val y: Float, val radius: Float, val tone: Int)

internal object RegionalTerrainComposition {
    val washes = listOf(
        RegionalTerrainWash(.14f,.15f,.23f,0),
        RegionalTerrainWash(.48f,.19f,.20f,0),
        RegionalTerrainWash(.86f,.21f,.22f,0),
        RegionalTerrainWash(.16f,.43f,.23f,1),
        RegionalTerrainWash(.43f,.48f,.24f,1),
        RegionalTerrainWash(.72f,.40f,.22f,1),
        RegionalTerrainWash(.91f,.56f,.17f,1),
        RegionalTerrainWash(.20f,.71f,.20f,2),
        RegionalTerrainWash(.52f,.79f,.24f,2),
        RegionalTerrainWash(.84f,.88f,.23f,2)
    )
}

/** Course-specific printed terrain with shared atlas. No artificial paths or bridge claims. */
internal fun DrawScope.drawSiblingCourseOverhead(
    style: CourseOverheadStyle, accents: List<EnvironmentAccent>,
    atlas: ImageBitmap, left: Float, top: Float, width: Float, height: Float,
    groundcover: List<GroundcoverPatch> = emptyList(),
    footprints: List<LandscapeFootprint> = emptyList()
) {
    if (width <= 0f || height <= 0f) return
    val unit = min(width,height)
    fun at(x: Float,y: Float) = Offset(left+x*width,top+y*height)
    val palette = if (style == CourseOverheadStyle.BROOKSIDE)
        listOf(Color(0xFFE5DDB2),Color(0xFFC7D3A4),Color(0xFFDDD3A6))
    else listOf(Color(0xFFC8C6A0),Color(0xFF9DAF87),Color(0xFFC9BC95))
    clipRect(left,top,left+width,top+height) {
        val terrainBrush = if (style == CourseOverheadStyle.REGIONAL)
            Brush.verticalGradient(0f to Color(0xFFE4D1AC), .30f to Color(0xFFD4D4AA),
                .49f to Color(0xFF9DAF87), .65f to Color(0xFFA9BA8D),
                .82f to Color(0xFFD6DAA7), 1f to Color(0xFFE8DDB2),
                startY=top, endY=top+height)
        else Brush.linearGradient(palette,start=at(0f,.2f),end=at(1f,.8f))
        drawRect(terrainBrush,
            topLeft=at(0f,0f),size=Size(width,height))
        val pools = if (style == CourseOverheadStyle.BROOKSIDE)
            listOf(Triple(.18f,.3f,.25f),Triple(.48f,.5f,.19f),Triple(.88f,.81f,.22f))
        else listOf(Triple(.19f,.86f,.19f),Triple(.47f,.43f,.29f),Triple(.82f,.24f,.23f))
        pools.forEachIndexed { i,(x,y,r) ->
            val tint = if (i==1) Color(0xFF688A64) else Color(0xFFF4E2B7)
            drawCircle(Brush.radialGradient(listOf(tint.copy(alpha=.27f),tint.copy(alpha=0f)),
                center=at(x,y),radius=unit*r),radius=unit*r,center=at(x,y))
        }
        if (style == CourseOverheadStyle.REGIONAL) {
            RegionalTerrainComposition.washes.forEach { wash ->
                val tint = when (wash.tone) {
                    0 -> Color(0xFFB88F59)
                    1 -> Color(0xFF486E46)
                    else -> Color(0xFFF1D58B)
                }
                val center = at(wash.x,wash.y)
                val radius = unit * wash.radius
                drawCircle(Brush.radialGradient(
                    listOf(tint.copy(alpha = .24f), tint.copy(alpha = 0f)),
                    center = center, radius = radius), radius = radius, center = center)
            }
        }
        WovenTerrainTexture.flecks.forEach { f ->
            val tint = if (style == CourseOverheadStyle.REGIONAL && f.tone != 1) {
                when {
                    f.y < .30f -> Color(0xFF92734C).copy(alpha=.20f)
                    f.y < .67f -> Color(0xFF405F40).copy(alpha=.22f)
                    else -> Color(0xFF8A9154).copy(alpha=.18f)
                }
            } else if (f.tone==1) Color(0xFFF5E9C5).copy(alpha=.24f)
                else Color(0xFF536B49).copy(alpha=.15f)
            val center=at(f.x,f.y)
            drawCircle(tint,radius=f.size*unit*.42f,center=center)
        }
        drawLandscapeFootprints(footprints, left, top, width, height)
        drawWoodlandGroundcover(groundcover, left, top, width, height)
        accents.forEach { a -> tryDrawEnvironmentSprite(atlas,a.sprite,at(a.x,a.y),unit) }
    }
}
