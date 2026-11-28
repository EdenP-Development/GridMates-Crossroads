package com.playereden.gridmates.crossroads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared quiet paper frame. Content owns its sizing, scrolling and semantics.
 * Illustrated panel textures remain reserved until exact art import and inset QA.
 */
@Composable
internal fun TabletopPanel(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFF7EDD7),
    contentColor: Color = Color(0xFF3B5841),
    corner: Dp = 15.dp,
    border: BorderStroke? = BorderStroke(1.5.dp, Color(0xFFB59B6C)),
    elevation: Dp = 3.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = RoundedCornerShape(corner),
        border = border,
        shadowElevation = elevation,
    ) {
        Box(Modifier.drawBehind {
            // Quiet inset engraving, no bitmap substitute or interactive overlay.
            val inset = 4.dp.toPx()
            if (size.width > inset * 2 && size.height > inset * 2) {
                drawRoundRect(
                    color = TabletopMaterials.brass.copy(alpha = 0.22f),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius((corner.toPx() - inset).coerceAtLeast(0f)),
                    style = Stroke(width = 0.7.dp.toPx())
                )
            }
        }) { content() }
    }
}
