package com.playereden.gridmates.crossroads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
    TabletopPanel(modifier = modifier.fillMaxWidth()) {
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
                        TabletopActionTile(
                            title = copy.title,
                            detail = copy.impact,
                            onClick = { onChoose(option) },
                            modifier = Modifier.weight(1f),
                            warm = option == "supplies" || option == "gamble",
                            enabled = enabled
                        )
                    }
                }
            }
        }
    }
}
