package com.playereden.gridmates.crossroads

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.playereden.crossroads.core.Snapshot
import com.playereden.crossroads.core.DecisionLab
import com.playereden.crossroads.core.FairFinishReceipt

private val Evergreen = Color(0xFF48684D)
private val Earth = Color(0xFF80664B)
private val Parchment = Color(0xFFF7F0DE)
private val SeatColors = listOf(
    Color(0xFFC75C49), Color(0xFF3D729F),
    Color(0xFF729B60), Color(0xFFB48A36)
)

/** Winner wording depends on the actual reported traveler, not seat index. */
internal object CrossroadsFinishCopy {
    fun headline(winnerName: String): String =
        if (winnerName == "You") "You reached the finish!" else "$winnerName wins!"

    fun isLocalWinner(winnerName: String): Boolean = winnerName == "You"

    /** This copy describes the actual finish decision, not a generic rule. */
    fun fairFinishExplanation(receipt: FairFinishReceipt): String {
        if (receipt.finishers == 1) {
            return if (receipt.laterSeatsGotFinalChance)
                "${receipt.firstFinisherName} crossed first. Later racers had their last turn, " +
                    "but nobody else reached Finish."
            else
                "${receipt.firstFinisherName} crossed from the final seat. " +
                    "Everyone already had their turn this round."
        }
        val steps = receipt.winningUnusedSteps
        val plural = if (steps == 1) "step" else "steps"
        val tie = if (receipt.tiedOnWinningSteps)
            " Tied on unused steps; the earlier of the tied finishers won." else ""
        return "${receipt.firstFinisherName} crossed first. ${receipt.finishers} racers " +
            "reached Finish. ${receipt.winnerName} won with $steps unused $plural.$tie"
    }
}

/** Original painted finish seal: a tabletop flag medal, not an imported asset. */
@Composable
private fun CrossroadsFinishSeal(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.43f
        drawCircle(color = Color(0xFFB49257), radius = radius * 1.12f, center = center)
        drawCircle(color = Color(0xFFF8ECCF), radius = radius * 0.98f, center = center)
        drawCircle(
            color = Color(0xFF47694C), radius = radius * 0.86f, center = center,
            style = Stroke(width = size.minDimension * 0.035f)
        )
        val poleX = center.x - radius * 0.31f
        drawLine(
            color = Color(0xFF47694C),
            start = Offset(poleX, center.y - radius * 0.66f),
            end = Offset(poleX, center.y + radius * 0.64f),
            strokeWidth = size.minDimension * 0.065f
        )
        val pennant = Path().apply {
            moveTo(poleX, center.y - radius * 0.65f)
            lineTo(center.x + radius * 0.60f, center.y - radius * 0.36f)
            lineTo(poleX, center.y - radius * 0.08f)
            close()
        }
        drawPath(pennant, Color(0xFFAC7E38))
        drawLine(
            color = Color(0xFF47694C),
            start = Offset(poleX - radius * 0.15f, center.y + radius * 0.66f),
            end = Offset(poleX + radius * 0.18f, center.y + radius * 0.66f),
            strokeWidth = size.minDimension * 0.05f
        )
    }
}

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
 * Never invent second/third/fourth placements. Lab v5 may let multiple
 * travelers reach FINISH during the last round; show those finishes honestly.
 */
@Composable
internal fun CrossroadsResultsContent(
    state: Snapshot,
    courseId: String,
    elapsedSeconds: Long,
    modifier: Modifier = Modifier,
    finishReceipt: FairFinishReceipt? = null
) {
    val winnerIndex = state.winnerIndex
    val winnerName = winnerIndex?.let { state.players.getOrNull(it)?.name } ?: "A player"
    val roundCount = (state.turn - 1) / state.players.size + 1
    val equalRound = courseId.endsWith(DecisionLab.V5_SUFFIX)
    val winnerFinishNode = winnerIndex?.let { state.players.getOrNull(it)?.position }
    val lastEncounter = state.encounters.lastOrNull()

    val victoryArtwork = rememberOptionalCrossroadsArt(CrossroadsArtSlot.RESULT_VICTORY)
    // A short optional visual reveal makes crossing the finish feel conclusive.
    // Compose honors the device's reduced/disabled animator duration scale.
    // The match is already decided, and no result button waits on this effect.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val finishScale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.94f,
        animationSpec = tween(durationMillis = 320),
        label = "finish-seal-reveal"
    )
    val finishAlpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.80f,
        animationSpec = tween(durationMillis = 270),
        label = "finish-seal-fade"
    )

    Column(
        modifier = modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (victoryArtwork != null) {
            Image(
                bitmap = victoryArtwork,
                contentDescription = null, // Winner and time remain readable text.
                modifier = Modifier.fillMaxWidth().height(84.dp),
                contentScale = ContentScale.Crop
            )
        }
        Surface(
            color = if (CrossroadsFinishCopy.isLocalWinner(winnerName))
                Color(0xFFE4EAD5) else Color(0xFFF0E5CF),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().graphicsLayer {
                scaleX = finishScale
                scaleY = finishScale
                alpha = finishAlpha
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CrossroadsFinishSeal(Modifier.size(48.dp))
                Column(modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        CrossroadsFinishCopy.headline(winnerName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Evergreen
                    )
                    Text(CrossroadsCourse.resultLine(courseId),
                        style = MaterialTheme.typography.bodySmall, color = Earth)
                }
            }
        }
        if (equalRound) {
            Text(
                if (finishReceipt != null)
                    CrossroadsFinishCopy.fairFinishExplanation(finishReceipt)
                else "Fair finish · The finishing round is complete.",
                style = MaterialTheme.typography.bodySmall,
                color = Earth
            )
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
                    when {
                        index == winnerIndex && equalRound -> "Final-round winner"
                        index == winnerIndex -> "Finished first"
                        equalRound && player.position == winnerFinishNode -> "Reached Finish"
                        else -> "On the trail"
                    },
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
