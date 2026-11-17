package com.playereden.gridmates.crossroads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.crossroads.core.Snapshot

private val Evergreen = Color(0xFF48684D)
private val Earth = Color(0xFF80664B)
private val Parchment = Color(0xFFF7F0DE)
private val SeatColors = listOf(
    Color(0xFFC75C49), Color(0xFF3D729F),
    Color(0xFF729B60), Color(0xFFB48A36)
)

/**
 * A finished match's actual facts. We only rank the first finisher;
 * other seats were still on the trail when the race ended.
 * No fabricated placement, reward, currency, or future result is displayed.
 */
@Composable
internal fun CrossroadsResultsContent(
    state: Snapshot,
    elapsedSeconds: Long
) {
    val winnerIndex = state.winnerIndex
    val winnerName = winnerIndex?.let { state.players.getOrNull(it)?.name } ?: "A player"
    val roundCount = (state.turn - 1) / state.players.size + 1
    val lastEncounter = state.encounters.lastOrNull()

    Column(
        modifier = Modifier.fillMaxWidth().heightIn(max = 430.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Surface(
            color = if (winnerIndex == 0) Color(0xFFE4EAD5) else Color(0xFFF0E5CF),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("✦", fontSize = 27.sp, color = Evergreen)
                Text(
                    if (winnerIndex == 0) "You reached the finish!" else "$winnerName wins!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Evergreen
                )
                Text(
                    "First to cross Woodland Crossing",
                    color = Earth, style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            @Composable fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
                Surface(
                    modifier = modifier,
                    color = Parchment,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.padding(horizontal = 9.dp, vertical = 11.dp)) {
                        Text(value, color = Evergreen, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium)
                        Text(label, color = Earth,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Stat("Rounds", "$roundCount", Modifier.weight(1f))
            Stat("Elapsed", "${elapsedSeconds / 60}m ${elapsedSeconds % 60}s", Modifier.weight(1f))
            Stat("Players", "${state.players.size}", Modifier.weight(1f))
        }
        Text("Travelers", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, color = Evergreen)
        state.players.forEachIndexed { index, player ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    Modifier.size(13.dp)
                        .background(SeatColors[index], androidx.compose.foundation.shape.CircleShape)
                )
                Text(player.name, modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (index == winnerIndex) "Finished first" else "On the trail",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (index == winnerIndex) FontWeight.Bold
                        else FontWeight.Normal,
                    color = if (index == winnerIndex) Evergreen else Earth
                )
            }
        }
        if (lastEncounter != null) {
            HorizontalDivider(color = Earth.copy(alpha = 0.22f))
            Text("Last recorded encounter", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold, color = Evergreen)
            Text("${lastEncounter.actor} · ${lastEncounter.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
            Text(lastEncounter.outcome, style = MaterialTheme.typography.bodySmall,
                color = Earth)
        }
        Text(
            "Elapsed time includes pauses and time spent viewing the board.",
            style = MaterialTheme.typography.labelSmall, color = Earth
        )
    }
}
