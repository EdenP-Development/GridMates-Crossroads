package com.playereden.gridmates.crossroads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.playereden.crossroads.core.Phase

/**
 * Compact tabletop game HUD, separate from engine and networking logic.
 * The phase caption and color are not exclusive cues: the complete turn title
 * remains accessible as text, regardless of player color or visual treatment.
 */
@Composable
internal fun GameHudTurnStrip(
    title: String,
    phase: Phase,
    moving: Boolean,
    seatColor: Color,
    botTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val moment = turnMomentName(phase, moving, botTurn)
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$moment. $title"
        },
        color = Color(0xFF385945),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFBDA677)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp)
                .padding(horizontal = 9.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(
                modifier = Modifier.width(5.dp).height(26.dp)
                    .background(seatColor, RoundedCornerShape(4.dp))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(moment, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFFF0D7A5))
                Text(title, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = Color(0xFFFFF7E8),
                    maxLines = 2)
            }
        }
    }
}

/** Keep the disclosure short while preserving its purpose. */
internal fun turnDetailsLabel(expanded: Boolean): String =
    if (expanded) "Hide details" else "Turn details"

/** Used by the prototype and its tests; no gameplay phase is changed by HUD. */
internal fun gameHudActionHeading(phase: Phase): String = when (phase) {
    Phase.ROLL -> "CHOOSE YOUR PACE"
    Phase.ROUTE -> "PICK YOUR PATH"
    Phase.ENCOUNTER -> "MAKE YOUR CHOICE"
    Phase.TARGET -> "CHOOSE A TARGET"
    Phase.FINISHED -> "JOURNEY COMPLETE"
}

/** Compact on-board decisions, with the actual consequences still readable. */
internal data class EncounterActionCopy(val title: String, val impact: String)

internal fun encounterActionCopy(option: String, pushHazardSuccessBonus: Int = 0): EncounterActionCopy = when (option) {
    "supplies" -> EncounterActionCopy("SUPPLIES", "+2 spaces")
    "cover" -> EncounterActionCopy("COVER", "+1 & shield")
    "gamble" -> EncounterActionCopy("GAMBLE",
        if (pushHazardSuccessBonus > 0) "Success: +$pushHazardSuccessBonus extra" else "Face hazard")
    "brace" -> EncounterActionCopy("BRACE", "Stop & shield")
    else -> EncounterActionCopy(option.uppercase(), "Choose")
}

internal fun gameActionDockMaxFraction(phase: Phase): Float = when (phase) {
    Phase.ROLL -> 0.32f
    Phase.ROUTE -> 0.32f
    Phase.ENCOUNTER -> 0.37f
    Phase.TARGET -> 0.39f
    Phase.FINISHED -> 0.30f
}

/**
 * In a fork, the die/remaining-steps and named junction already communicate
 * the immediate choice. Repeating the last-action sentence above them took
 * up valuable painted-board space on the owner's QA1672 phone.
 *
 * The original narration is still available through "Rules & last action".
 * Encounter actions need their illustration/choices; other phases keep
 * their normal context line.
 */
internal fun showCollapsedActionNote(
    phase: Phase,
    humanEncounterChoiceReady: Boolean
): Boolean = when (phase) {
    Phase.ROUTE -> false
    Phase.ENCOUNTER -> !humanEncounterChoiceReady
    else -> true
}

/** Protect the primary button group from being clipped on short handsets. */
internal fun gameActionDockMinimumHeight(phase: Phase): Dp = when (phase) {
    Phase.ROLL -> 204.dp
    Phase.ROUTE -> 246.dp
    Phase.ENCOUNTER -> 272.dp
    Phase.TARGET -> 250.dp
    Phase.FINISHED -> 175.dp
}
