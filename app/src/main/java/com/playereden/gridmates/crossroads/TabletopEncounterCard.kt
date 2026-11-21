package com.playereden.gridmates.crossroads

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * GAMEFEEL-69: Treat a shelter or hazard as a small tabletop event card, not
 * an unrelated 44dp thumbnail and a row of platform buttons. The game still
 * supplies the ONLY legal choices and the existing callback resolves them.
 */
internal object TabletopEncounterPresentation {
    fun isShelter(choices: List<String>): Boolean = "supplies" in choices
    fun title(choices: List<String>): String =
        if (isShelter(choices)) "Trail shelter" else "Trail hazard"
    fun subtitle(choices: List<String>): String =
        if (isShelter(choices)) "A welcome stop on the trail"
        else "A tricky stretch of the trail"
}

@Composable
internal fun TabletopEncounterCard(
    choices: List<String>,
    artwork: ImageBitmap?,
    enabled: Boolean,
    onChoose: (String) -> Unit,
    modifier: Modifier = Modifier,
    pushHazardSuccessBonus: Int = 0
) {
    val shelter = TabletopEncounterPresentation.isShelter(choices)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF7EDD7),
        contentColor = Color(0xFF3B5841),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.5.dp, Color(0xFFB59B6C)),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 9.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFC7AD76)),
                    color = Color(0xFFE1D0A9)
                ) {
                    if (artwork != null) {
                        Image(
                            bitmap = artwork,
                            contentDescription = null,
                            modifier = Modifier.size(62.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(62.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (shelter) "⌂" else "!",
                                fontSize = 28.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF546A48)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        "WOODLAND ENCOUNTER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF937341)
                    )
                    Text(
                        TabletopEncounterPresentation.title(choices),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B5841)
                    )
                    Text(
                        TabletopEncounterPresentation.subtitle(choices),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        color = Color(0xFF7E674A)
                    )
                }
            }
            choices.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    pair.forEach { option ->
                        val copy = encounterActionCopy(option, pushHazardSuccessBonus)
                        val warm = option == "supplies" || option == "gamble"
                        val bg = if (warm) Color(0xFFB58B4A) else Color(0xFF40664D)
                        val ink = if (warm) Color(0xFF26261D) else Color(0xFFFFF5DD)
                        val interactionSource = remember(option) { MutableInteractionSource() }
                        val pressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = CrossroadsTabletopFeedback.scale(pressed && enabled),
                            animationSpec = tween(durationMillis = 95),
                            label = "encounter-contact-$option"
                        )
                        Surface(
                            onClick = { onChoose(option) },
                            enabled = enabled,
                            interactionSource = interactionSource,
                            modifier = Modifier.weight(1f)
                                .heightIn(min = 54.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale },
                            color = bg,
                            contentColor = ink,
                            shape = RoundedCornerShape(11.dp),
                            border = BorderStroke(1.dp,
                                if (warm) Color(0xFFE8CE91) else Color(0xFF9FB397)),
                            shadowElevation = if (enabled) 2.dp else 0.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 5.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    copy.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                    textAlign = TextAlign.Center,
                                    color = ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    copy.impact,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ink,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
