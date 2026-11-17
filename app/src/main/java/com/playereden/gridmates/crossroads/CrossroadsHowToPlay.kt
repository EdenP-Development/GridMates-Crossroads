package com.playereden.gridmates.crossroads

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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val HelpForest = Color(0xFF48684D)
private val HelpBark = Color(0xFF80664B)
private val HelpPaper = Color(0xFFF7F0DE)

@Composable
private fun GuideStep(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = Color(0xFFE2EAD9),
            shape = MaterialTheme.shapes.small
        ) {
            Text(number,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = HelpForest)
        }
        Column(Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold, color = HelpForest)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = HelpBark)
        }
    }
}

/**
 * A reference guide, not onboarding that blocks a first match.
 * Wording follows the currently implemented GameSession rules and Woven Trail
 * fixture. Future boards should get board-specific explanations if rules change.
 */
@Composable
internal fun CrossroadsHowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.92f)
                    .heightIn(max = (maxHeight - 24.dp).coerceAtLeast(1.dp)),
                shape = MaterialTheme.shapes.extraLarge,
                color = HelpPaper,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 17.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("How to Play",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = HelpForest)
                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        Text("One start. One finish. Different journeys.",
                            style = MaterialTheme.typography.bodyMedium, color = HelpForest,
                            fontWeight = FontWeight.SemiBold)
                        GuideStep(
                            "1", "Reach the finish first",
                            "Everyone begins at Start. The first player to reach Finish wins the race."
                        )
                        GuideStep(
                            "2", "Roll and travel",
                            "On your turn, roll the die. Your piece travels along the trail automatically."
                        )
                        GuideStep(
                            "3", "Choose at a crossroads",
                            "When a trail branches, choose where to go. Any steps left from your roll continue along your chosen path."
                        )
                        GuideStep(
                            "4", "Watch for encounters",
                            "Landing on a marked encounter space can help, delay, or cause mischief. A shield protects against one prank."
                        )
                        GuideStep(
                            "5", "Use the risk clues",
                            "High, medium, and low risk describe each trail, not guaranteed outcomes. In Solo, AI chooses its own paths; in Pass & Play, each person decides."
                        )
                        HorizontalDivider(color = HelpBark.copy(alpha = 0.22f))
                        Text("Explore the board",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold, color = HelpForest)
                        Text(
                            "Pinch to zoom, drag to explore, and double-tap to follow the action again. Cozy and Brisk change only the animation speed, not the odds. Board overview and the journey journal are in Match options (⋯).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HelpBark
                        )
                        Text(
                            "Offline choices: Solo against 1–3 AI opponents, or Pass & Play for 2–4 people taking turns on one phone. Same-Wi-Fi play is not available yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelpBark
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text("Got it")
                    }
                }
            }
        }
    }
}
