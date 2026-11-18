package com.playereden.gridmates.crossroads

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF48684D)
private val Muted = Color(0xFF80664B)
private val Paper = Color(0xFFF7F0DE)

/** An original miniature travel motif, not an interactive or alternate board. */
@Composable
private fun HomeTrailIllustration() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Color(0xFFECE5D4),
        tonalElevation = 2.dp
    ) {
        Canvas(Modifier.fillMaxWidth().height(104.dp)) {
            val inset = 12f
            val w = (size.width - 2f * inset).coerceAtLeast(1f)
            val h = (size.height - 2f * inset).coerceAtLeast(1f)
            fun point(x: Float, y: Float) = Offset(inset + x * w, inset + y * h)
            drawWoodlandBoardArt(
                boardId = "woodland_woven_trail_v02",
                left = inset, top = inset, width = w, height = h
            )
            // A recognizable pair of joining paths, but not the actual Woven Trail.
            // Both lines remain decorative; no labels could mislead about rules.
            val upper = Path().apply {
                val start = point(0.10f, 0.73f)
                moveTo(start.x, start.y)
                val end = point(0.89f, 0.19f)
                cubicTo(
                    point(0.17f, 0.13f).x, point(0.17f, 0.13f).y,
                    point(0.66f, 0.35f).x, point(0.66f, 0.35f).y,
                    end.x, end.y
                )
            }
            val lower = Path().apply {
                val start = point(0.10f, 0.73f)
                moveTo(start.x, start.y)
                val end = point(0.89f, 0.19f)
                cubicTo(
                    point(0.31f, 0.95f).x, point(0.31f, 0.95f).y,
                    point(0.72f, 0.85f).x, point(0.72f, 0.85f).y,
                    end.x, end.y
                )
            }
            listOf(upper, lower).forEachIndexed { i, path ->
                drawPath(path, Color(0xFF735E41).copy(alpha = 0.48f),
                    style = Stroke(width = 14f, cap = StrokeCap.Round))
                drawPath(path, if (i == 0) Color(0xFFFFE9BC) else Color(0xFFE1EACD),
                    style = Stroke(width = 9f, cap = StrokeCap.Round))
            }
            drawCircle(Ink, 12f, point(0.10f, 0.73f))
            drawCircle(Color(0xFFFDF9EB), 7f, point(0.10f, 0.73f))
            drawCircle(Color(0xFFBB8B41), 14f, point(0.89f, 0.19f))
            drawCircle(Color(0xFFFFF3D5), 8f, point(0.89f, 0.19f))
        }
    }
}

@Composable
internal fun CrossroadsHomeScreen(
    wins: Int,
    completed: Int,
    onStart: (Int, Boolean) -> Unit,
    selectedCourseId: String,
    onCourseChange: (String) -> Unit,
    playtestRules: Boolean,
    onPlaytestRulesChange: (Boolean) -> Unit,
    onNearbyHost: () -> Unit,
    onNearbyJoin: () -> Unit,
    onExportProgress: () -> Unit,
    onImportProgress: () -> Unit,
    recoveryWarning: String? = null
) {
    // The first run is a single tap: 1 human + 1 bot. No room codes or setup screens.
    var seats by remember { mutableIntStateOf(2) }
    var passAndPlay by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    // Keep both setup selectors in the Woodland palette; Material's default
    // selected chip tint was lavender on actual Android QA1130 screenshots.
    val choiceColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Color(0xFFE2EAD9),
        selectedLabelColor = Ink,
        labelColor = Muted
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("GRIDMATES · BOARD GAMES",
            style = MaterialTheme.typography.labelLarge, color = Muted)
        Text("Crossroads", fontSize = 33.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Every path is a choice.", style = MaterialTheme.typography.titleMedium,
            color = Muted)
        if (recoveryWarning != null) {
            Surface(color = Color(0xFFF1E0CF), shape = MaterialTheme.shapes.medium) {
                Text(
                    recoveryWarning,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall, color = Muted
                )
            }
        }
        Text("Ready to play?", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = Ink)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !passAndPlay,
                onClick = { passAndPlay = false },
                label = { Text("Solo vs AI") },
                colors = choiceColors,
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = passAndPlay,
                onClick = { passAndPlay = true },
                label = { Text("Pass & Play") },
                colors = choiceColors,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            if (passAndPlay) "$seats people · take turns on this phone · offline"
            else "You and ${seats - 1} ${if (seats == 2) "AI opponent" else "AI opponents"} · offline",
            style = MaterialTheme.typography.bodySmall, color = Muted
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (2..4).forEach { count ->
                FilterChip(
                    selected = count == seats,
                    onClick = { seats = count },
                    label = { Text("$count players", textAlign = TextAlign.Center) },
                   colors = choiceColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Text("Choose your course", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = Ink)
        // Short labels keep all three race lengths on one comfortable row
        // at the owner's real-device font size; the description names the map.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                CrossroadsCourse.QUICK_ID to "Quick",
                CrossroadsCourse.CLASSIC_ID to "Classic",
                CrossroadsCourse.LONG_ID to "Long"
            ).forEach { (id, label) ->
                FilterChip(
                    selected = selectedCourseId == id,
                    onClick = { onCourseChange(id) },
                    label = { Text(label, maxLines = 1) },
                    colors = choiceColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Text(
            when (selectedCourseId) {
                CrossroadsCourse.QUICK_ID ->
                    "Brookside Dash · 2 crossroads · a short woodland sprint"
                CrossroadsCourse.LONG_ID ->
                    "Cedarwood Expedition · 6 crossroads · a much longer winding journey"
                else ->
                    "Woven Trail · 5 crossroads · the original woodland race"
            },
            style = MaterialTheme.typography.bodySmall, color = Muted
        )
        Surface(
            color = if (playtestRules) Color(0xFFE4EBD9) else Color(0xFFECE5D4),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gameplay 2.0 · Playtest v2", color = Ink,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold)
                    Text("Optional offline tactical rules",
                        style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Switch(checked = playtestRules, onCheckedChange = onPlaytestRulesChange)
            }
        }
        if (playtestRules) {
            Text("Choose Steady or Push. Entering a route triggers its encounter; Steady + Shelter gains an extra step. Experimental balance.",
                style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Button(
            onClick = { onStart(seats, passAndPlay) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text("Start journey", style = MaterialTheme.typography.titleMedium)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNearbyHost,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) { Text("Host nearby") }
            OutlinedButton(
                onClick = onNearbyJoin,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) { Text("Join nearby") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = { showGuide = true }) { Text("How to play") }
            TextButton(onClick = { showAbout = true }) { Text("About & data") }
        }
        // Decorative hero artwork belongs *after* the action, not between the
        // title and Start journey. On real phones that placed Start below the fold.
        HomeTrailIllustration()
        Text(
            "Meadow, forest, village. Choose your trail and reach the finish first.",
            style = MaterialTheme.typography.bodySmall, color = Muted
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAE1CA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("Your local record", fontWeight = FontWeight.SemiBold, color = Ink)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Column {
                        Text("$wins", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Ink)
                        Text(if (wins == 1) "Solo win" else "Solo wins",
                            style = MaterialTheme.typography.labelMedium, color = Muted)
                    }
                    Column {
                        Text("$completed", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Ink)
                        Text("Finished matches",
                            style = MaterialTheme.typography.labelMedium, color = Muted)
                    }
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Text("QA playtest · Offline Solo / Pass & Play / Nearby · Woodland Crossing",
                style = MaterialTheme.typography.labelSmall, color = Muted)
        }
    }
    if (showGuide) {
        CrossroadsHowToPlayDialog(onDismiss = { showGuide = false })
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About Crossroads", color = Ink) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("A cozy GridMates board race. Play alone against AI, pass one phone around, or connect two phones over nearby Wi-Fi.")
                    Text("SAVING YOUR PROGRESS", color = Ink,
                        fontWeight = FontWeight.SemiBold)
                    Text("Current offline matches and your local win and match counts are stored on this device. No account or cloud sync is required. Clearing app storage or uninstalling can erase this progress.")
                    Text("Make a portable backup before switching from QA to the public app. Choose a safe location for the JSON file; it is readable text, not an encrypted backup.")
                    OutlinedButton(
                        onClick = { showAbout = false; onExportProgress() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Export local progress") }
                    OutlinedButton(
                        onClick = { showAbout = false; onImportProgress() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Import a backup") }
                    Text("Importing requires confirmation and replaces the current device's local record and saved offline race. It does not import multiplayer rooms or synchronize devices.")
                    Text("NEARBY PLAY", color = Ink,
                        fontWeight = FontWeight.SemiBold)
                    Text("Nearby games connect directly on the same Wi-Fi. The host shares live match data, including game actions and the match seed, with the joined device. The current nearby connection is not encrypted, so use a trusted private Wi-Fi network.")
                    if (BuildConfig.DEBUG) {
                        Text("QA PRIVACY NOTE", color = Ink,
                            fontWeight = FontWeight.SemiBold)
                        Text("This information describes the test app's current behavior. A published privacy policy and distribution review are still required before public release.")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Done") }
            },
            containerColor = Paper
        )
    }
}
