package com.playereden.gridmates.crossroads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * VISUAL-82: A quiet physical tabletop identity. These are decorations and
 * composables only, never an additional board, path or tap target.
 *
 * One shared material vocabulary: dark walnut rail, brass seam, parchment
 * action tray, and numbered enamel player pawns. Kept deliberately thin so
 * screens with 2-4 seats and large fonts are still playable.
 */
internal object TabletopMaterials {
    const val RAIL_WIDTH_DP = 8f
    const val SCORE_PAWN_DIAMETER_DP = 26f
    val walnut = Color(0xFF463727)
    val walnutLight = Color(0xFF785A3E)
    val brass = Color(0xFFD4B77A)
    val paper = Color(0xFFF6E9D0)

    fun label(seatIndex: Int, playerName: String, status: String, shielded: Boolean): String =
        "Player " + (seatIndex + 1) + ", " + playerName + ", " + status +
            if (shielded) ", shielded" else ""
}

/**
 * Noninteractive rim of the actual board, independent of the scrolling art
 * and camera transform. The artwork remains a draggable/zoomable surface.
 * The slim edge makes the view read as a framed game board, not wallpaper.
 */
@Composable
internal fun TabletopBoardRim(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize().clearAndSetSemantics { }) {
        if (size.width < 60f || size.height < 60f) return@Canvas
        val rail = 8.dp.toPx()
        val w = size.width
        val h = size.height
        val shadow = Color(0xB5231D16)
        val face = TabletopMaterials.walnut
        val lit = TabletopMaterials.walnutLight

        // Narrow, shaded wooden rails: no solid panel obscures the paths.
        drawRect(shadow, topLeft = Offset(0f, 0f), size = Size(w, rail + 2.dp.toPx()))
        drawRect(Brush.horizontalGradient(listOf(lit, face), 0f, rail),
            topLeft = Offset(0f, 0f), size = Size(rail, h))
        drawRect(Brush.horizontalGradient(listOf(face, lit), w - rail, w),
            topLeft = Offset(w - rail, 0f), size = Size(rail, h))
        drawRect(Brush.verticalGradient(listOf(lit, face), 0f, rail),
            topLeft = Offset(0f, 0f), size = Size(w, rail))
        drawRect(Brush.verticalGradient(listOf(face, lit), h - rail, h),
            topLeft = Offset(0f, h - rail), size = Size(w, rail))

        // The engraved inner line joins all four edges into one object.
        val inset = rail * 0.78f
        drawRoundRect(
            color = TabletopMaterials.brass.copy(alpha = 0.79f),
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2f, h - inset * 2f),
            cornerRadius = CornerRadius(rail * 1.1f),
            style = Stroke(width = 1.dp.toPx())
        )
        for (cx in listOf(rail * 1.65f, w - rail * 1.65f)) {
            for (cy in listOf(rail * 1.65f, h - rail * 1.65f)) {
                val at = Offset(cx, cy)
                drawCircle(Color(0xAA241B13), radius = 3.3.dp.toPx(),
                    center = at + Offset(0f, 0.7.dp.toPx()))
                drawCircle(TabletopMaterials.brass, radius = 2.7.dp.toPx(), center = at)
                drawCircle(Color(0xFFEEDDAD), radius = 0.9.dp.toPx(),
                    center = at + Offset(-0.6.dp.toPx(), -0.7.dp.toPx()))
            }
        }
    }
}

/** Raised enamel/wood score pieces instead of generic status chips. */
@Composable
internal fun TabletopSeatPlate(
    seatIndex: Int,
    name: String,
    seatColor: Color,
    active: Boolean,
    shielded: Boolean,
    status: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = TabletopMaterials.label(seatIndex, name, status, shielded)
        },
        color = if (active) Color(0xFF4B6046) else Color(0xFF3D3529),
        contentColor = Color(0xFFFFF1D5),
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(if (active) 1.8.dp else 1.dp,
            if (active) Color(0xFFF0D99F) else Color(0xFF937954)),
        shadowElevation = if (active) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.heightIn(min = 38.dp)
                .padding(horizontal = 5.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(26.dp).clearAndSetSemantics { }) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension * 0.42f
                    drawCircle(Color(0xA01F1D15), radius * 1.17f,
                        center = c + Offset(0f, 2.dp.toPx()))
                    drawCircle(TabletopMaterials.brass, radius * 1.13f, center = c)
                    drawCircle(Color(0xFFFFF3D7), radius * 0.99f, center = c)
                    drawCircle(seatColor, radius * 0.84f, center = c)
                    drawCircle(Color.White.copy(alpha = 0.24f),
                        radius * 0.29f, center = c - Offset(radius * 0.26f, radius * 0.28f))
                }
                Text((seatIndex + 1).toString(), color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(Modifier.width(4.dp))
            Text(name, modifier = Modifier.weight(1f, fill = false),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = Color(0xFFFFF0D4))
            if (shielded) {
                Spacer(Modifier.width(2.dp))
                Text("◆", color = Color(0xFFF6D48D), fontSize = 10.sp)
            }
        }
    }
}

/**
 * Tactile action tiles used by the die/risk choices. The whole tile is a
 * readable target, not a two-line Android form button. One shared style keeps
 * movement, route and opponent choices in the same tabletop language.
 */
@Composable
internal fun TabletopActionTile(
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    warm: Boolean = false,
    enabled: Boolean = true
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = CrossroadsTabletopFeedback.scale(pressed && enabled),
        animationSpec = tween(95),
        label = "tabletop-tile-contact"
    )
    val bg = if (warm) Color(0xFFD4AE68) else Color(0xFF3D624A)
    val ink = if (warm) Color(0xFF30291F) else Color(0xFFFFF3D6)
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactions,
        modifier = modifier.heightIn(min = 56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(13.dp),
        color = if (enabled) bg else Color(0xFFD5CEBC),
        border = BorderStroke(2.dp,
            if (enabled) TabletopMaterials.brass else Color(0xFFB2A58B)),
        shadowElevation = if (enabled) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(title, color = if (enabled) ink else Color(0xFF786C59),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(detail, color = if (enabled) ink else Color(0xFF786C59),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center, maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** An inset paper tray held by a walnut lip, reused for every turn phase. */
@Composable
internal fun TabletopActionTray(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = TabletopMaterials.walnut,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.5.dp, TabletopMaterials.brass),
        shadowElevation = 6.dp
    ) {
        Surface(
            modifier = Modifier.padding(2.dp),
            color = TabletopMaterials.paper,
            shape = RoundedCornerShape(14.dp),
        ) {
            content()
        }
    }
}
