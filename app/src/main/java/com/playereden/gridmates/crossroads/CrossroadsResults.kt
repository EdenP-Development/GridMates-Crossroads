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

@Composable
private fun ResultStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Parchment,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(value, color = Evergreen, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            Text(label, color = Earth, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Compact, scrollable match receipt. The outer dialog owns its header and
 * permanently visible buttons, so the current phone can display every key
 * outcome without the "Last recorded encounter" details disappearing behind
 * a fixed footer. Short or large-font devices can scroll the full receipt.
 *
 * Never assign second/third/fourth positions: only the winner completed the
 * route when the engine ended the race.
 */
@Composable
internal fun CrossroadsResultsContent(
    state: Snapshot,
    elapsedSeconds: Long,
    modifier: Modifier = Modifier
) {
    val winnerIndex = state.winnerIndex
    val winnerName = winnerIndex?.let { state.players.getOrNull(it)?.name } ?: "A player"
    val roundCount = (state.turn - 1) / state.players.size + 1
    val lastEncounter = state.encounters.lastOrNull()

    Column(
        modifier = modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = if (winnerIndex == 0) Color(0xFFE4EAD5) else Color(0xFFF0E5CF),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✦", fontSize = 27.sp, color = Evergreen)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (winnerName == "You") "You reached the finish!" else "$winnerName wins!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Evergreen
                    )
                    Text("First across Woodland Crossing",
                        style = MaterialTheme.typography.bodySmall, color = Earth)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResultStat("Rounds", "$roundCount", Modifier.weight(1f))
            ResultStat("Time", "${elapsedSeconds / 60}m ${elapsedSeconds % 60}s",
                Modifier.weight(1f))
            ResultStat("Players", "${state.players.size}", Modifier.weight(1f))
        }

        // Show the *complete* last outcome before secondary traveler details,
        // rather than a heading with its explanation hidden below the fold.
        if (lastEncounter != null) {
            Surface(
                color = Color(0xFFECE7D9),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Last encounter", style = MaterialTheme.typography.labelMedium,
                        color = Evergreen, fontWeight = FontWeight.Bold)
                    Text("${lastEncounter.actor} · ${lastEncounter.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold, color = Evergreen)
                    Text(lastEncounter.outcome,
                        style = MaterialTheme.typography.bodySmall, color = Earth)
                }
            }
        }

        Text("Travelers", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, color = Evergreen)
        state.players.forEachIndexed { index, player ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    Modifier.size(13.dp)
                        .background(SeatColors[index % SeatColors.size],
                            androidx.compose.foundation.shape.CircleShape)
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
        Text("Match time includes pauses and map inspection.",
            style = MaterialTheme.typography.labelSmall, color = Earth)
    }
}
