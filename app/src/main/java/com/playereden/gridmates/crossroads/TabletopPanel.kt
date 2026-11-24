package com.playereden.gridmates.crossroads

import androidx.compose.foundation.BorderStroke
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
        content = content
    )
}
