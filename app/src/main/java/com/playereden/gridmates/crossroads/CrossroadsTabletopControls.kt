package com.playereden.gridmates.crossroads

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Shadow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * GAMEFEEL-67: Shared game-like, tactile selection components for the front door.
 * Buttons react immediately to contact, but do not bounce, shake, or delay input.
 * All match choices still invoke the owner's existing callbacks unchanged.
 */
/** Native fallback palette; illustrated labels retain the same ink colors. */
internal object TabletopLabelPalette {
    val darkPaper = Color(0xFF3D624A)
    val lightInk = Color(0xFFFFF5DA)
    val lightPaper = Color(0xFFECE2CA)
    val darkInk = Color(0xFF493925)
    val disabledPaper = Color(0xFFDCD6C2)
    val disabledInk = Color(0xFF625747)
}

private fun tabletopInkShadow(light: Boolean): Shadow =
    Shadow(if (light) Color(0xFF203526) else Color(0x99FFF5DA), Offset(0f, 1f), 2f)

internal object CrossroadsTabletopFeedback {
    const val PRESSED_SCALE = 0.975f
    const val REST_SCALE = 1f
    fun scale(pressed: Boolean): Float = if (pressed) PRESSED_SCALE else REST_SCALE
}

@Composable
internal fun TabletopChoicePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = CrossroadsTabletopFeedback.scale(pressed),
        animationSpec = tween(durationMillis = 95),
        label = "tabletop-choice-contact"
    )
    val ink = if (selected) TabletopLabelPalette.lightInk else TabletopLabelPalette.darkInk
    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .semantics { this.selected = selected },
        shape = RoundedCornerShape(13.dp),
        color = Color.Transparent,
        contentColor = ink,
        shadowElevation = 0.dp
    ) {
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val safeInset = if (maxWidth >= 200.dp) 28.dp else 10.dp
            OptionalTabletopButtonArt(
                family = if (selected) TabletopArtFamily.PRIMARY else TabletopArtFamily.SECONDARY,
                pressed = pressed, selected = selected
            )
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = safeInset, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
            Text(
                text = text,
                color = ink,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.labelLarge.copy(shadow = tabletopInkShadow(selected)),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        }
    }
}

/** One tiny original die mark is enough: no unrelated sparkle or logo clutter. */
@Composable
private fun TabletopDieStamp() {
    Canvas(Modifier.size(24.dp)) {
        val edge = size.minDimension
        drawRoundRect(
            color = Color(0xFFF9EDC9), size = Size(edge, edge),
            cornerRadius = CornerRadius(edge * 0.18f)
        )
        drawRoundRect(
            color = Color(0xFFBCA06B), size = Size(edge, edge),
            cornerRadius = CornerRadius(edge * 0.18f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = edge * 0.055f)
        )
        val radius = edge * 0.083f
        val dot = Color(0xFF375744)
        listOf(
            Offset(0.29f, 0.29f), Offset(0.71f, 0.29f),
            Offset(0.29f, 0.71f), Offset(0.71f, 0.71f)
        ).forEach { p ->
            drawCircle(dot, radius, center = Offset(p.x * edge, p.y * edge))
        }
    }
}

/**
 * The same tactile primary action style is reused in the board's route dock.
 * Keep the action immediately enabled/clickable; feedback never delays input.
 */
@Composable
internal fun TabletopPrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    withDie: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = CrossroadsTabletopFeedback.scale(pressed && enabled),
        animationSpec = tween(durationMillis = 95),
        label = "tabletop-primary-contact"
    )
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.heightIn(min = if (withDie) 58.dp else 54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        contentColor = if (enabled) Color(0xFFFFF5DA) else Color(0xFF786D5A),
        shadowElevation = 0.dp
    ) {
        BoxWithConstraints {
            val safeInset = if (maxWidth >= 200.dp) 32.dp else 12.dp
            OptionalPrimaryButtonArt(enabled, pressed)
            Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = safeInset, vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (withDie) {
                OptionalTabletopIcon(TabletopIconArt.DICE, Modifier.size(24.dp)) {
                    TabletopDieStamp()
                }
                androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
            }
            Text(
                text,
                fontWeight = FontWeight.Bold,
                fontSize = if (withDie) 17.sp else 15.sp,
                style = MaterialTheme.typography.labelLarge.copy(shadow = tabletopInkShadow(enabled)),
                color = if (enabled) TabletopLabelPalette.lightInk else TabletopLabelPalette.disabledInk,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        }
    }
}

/** Compact, tactile secondary action with the same brass and parchment vocabulary. */
@Composable
internal fun TabletopUtilityButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    icon: TabletopIconArt? = null
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = CrossroadsTabletopFeedback.scale(pressed),
        animationSpec = tween(95),
        label = "tabletop-utility-contact"
    )
    Surface(
        onClick = onClick,
        interactionSource = interactions,
        modifier = modifier.heightIn(min = 48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(11.dp),
        color = Color.Transparent,
        contentColor = if (dark) Color(0xFFFFF3D6) else Color(0xFF385945),
        shadowElevation = 0.dp
    ) {
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val safeInset = if (maxWidth >= 200.dp) 28.dp else 10.dp
            OptionalTabletopButtonArt(
                family = if (dark) TabletopArtFamily.PRIMARY else TabletopArtFamily.SECONDARY,
                pressed = pressed
            )
            Box(
                modifier = Modifier.padding(horizontal = safeInset, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        OptionalTabletopIcon(icon, Modifier.size(20.dp)
                            .padding(end = 3.dp)) { }
                    }
                    Text(text, style = MaterialTheme.typography.labelLarge.copy(shadow = tabletopInkShadow(dark)),
                        color = if (dark) TabletopLabelPalette.lightInk else TabletopLabelPalette.darkInk,
                        fontWeight = FontWeight.Medium, textAlign = TextAlign.Center,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
internal fun TabletopStartRaceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TabletopPrimaryActionButton(
        text = "START RACE",
        onClick = onClick,
        modifier = modifier,
        withDie = true
    )
}
