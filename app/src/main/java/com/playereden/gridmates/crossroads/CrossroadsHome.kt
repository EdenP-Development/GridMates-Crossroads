package com.playereden.gridmates.crossroads

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.crossroads.core.BotOpponentConfigurationV1
import com.playereden.crossroads.core.BotPersonalityV1
import com.playereden.crossroads.core.BotDifficultyPreviewV1
import com.playereden.crossroads.core.BotDifficultyV1

private val Ink = Color(0xFF48684D)
private val Muted = Color(0xFF80664B)
private val Paper = Color(0xFFF7F0DE)

/** Keep important experimental-rule state visible even with options collapsed. */
internal fun homeRaceSummary(
    seats: Int,
    passAndPlay: Boolean,
    tactical: Boolean,
    fairFinish: Boolean = false,
    courseName: String? = null
): String {
    val summary = if (passAndPlay) "$seats people · Pass & Play · offline"
        else "Solo vs AI · $seats racers · offline"
    // Only tactical + Fair Finish activates Lab v5; a remembered v5 preference
    // by itself does not change an original-rules race.
    return (if (courseName.isNullOrBlank()) summary else "$courseName · $summary") + when {
        !tactical -> ""
        fairFinish -> " · Tactical + Fair Finish ON"
        else -> " · Tactical rules ON"
    }
}

/** A short single-line disclosure on compact portrait phones. */
internal fun homeOptionsToggleLabel(expanded: Boolean): String =
    if (expanded) "Hide more options ↑" else "More options ↓"

/** An original miniature travel motif, not an interactive or alternate board. */
@Composable
private fun HomeTrailIllustration() {
    val commissionedArt = rememberOptionalCrossroadsArt(CrossroadsArtSlot.HOME_HERO)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Color(0xFFECE5D4),
        tonalElevation = 2.dp
    ) {
        if (commissionedArt != null) {
            Image(
                bitmap = commissionedArt,
                contentDescription = null, // Nearby game text is the accessible label.
                modifier = Modifier.fillMaxWidth().height(168.dp),
                contentScale = ContentScale.Crop
            )
        } else {
        Canvas(Modifier.fillMaxWidth().height(168.dp)) {
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
}

/** A small brass-and-ink heading, visually separating setup groups. */
@Composable
private fun HomeSectionHeading(label: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold, color = Ink)
        Canvas(Modifier.weight(1f).height(10.dp)) {
            val y = size.height / 2f
            drawLine(Color(0xFFBBA371), Offset(0f,y), Offset(size.width,y), 1.dp.toPx())
            val r = 3.dp.toPx()
            drawCircle(Color(0xFFBBA371),r,Offset(size.width-r,y))
        }
    }
}

/** Course artwork is a decorative preview, not a second navigable board. */
@Composable
private fun HomeCourseCard(id: String, label: String, selected: Boolean,
    onClick: () -> Unit, modifier: Modifier = Modifier) {
    val art = rememberOptionalCrossroadsArt(CrossroadsArtCatalog.backplate(id))
    Surface(
        onClick = onClick,
        modifier = modifier.semantics {
            this.selected = selected
            contentDescription = CrossroadsCourse.title(id) + ", " + label
        },
        shape = MaterialTheme.shapes.medium,
        color = if (selected) Color(0xFF3D624A) else Color(0xFFECE2CA),
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) Color(0xFFE8CE94) else Color(0xFFAA9672)),
        shadowElevation = if (selected) 3.dp else 1.dp
    ) {
        Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(48.dp)
                .clip(MaterialTheme.shapes.small)) {
                if (art != null) {
                    Image(art, contentDescription = null, modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop)
                } else {
                    Canvas(Modifier.fillMaxSize()) {
                        drawWoodlandBoardArt(id, 0f, 0f, size.width, size.height)
                    }
                }
            }
            Text(label, modifier = Modifier.padding(vertical = 5.dp),
                color = if (selected) Color(0xFFFFF5DA) else Ink,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun CrossroadsHomeScreen(
    wins: Int,
    completed: Int,
    onStart: (Int, Boolean, Map<Int, BotOpponentConfigurationV1>, Int) -> Unit,
    selectedCourseId: String,
    onCourseChange: (String) -> Unit,
    boardLook: CrossroadsBoardLook,
    onBoardLookChange: (CrossroadsBoardLook) -> Unit,
    playtestRules: Boolean,
    onPlaytestRulesChange: (Boolean) -> Unit,
    fairFinishExperiment: Boolean,
    onFairFinishExperimentChange: (Boolean) -> Unit,
    onNearbyHost: () -> Unit,
    onNearbyJoin: () -> Unit,
    onExportProgress: () -> Unit,
    onImportProgress: () -> Unit,
    recoveryWarning: String? = null,
    regionalCourseId: String? = null,
    convergenceCourseId: String? = null
) {
    // The first run is a single tap: 1 human + 1 bot. No room codes or setup screens.
    var seats by remember { mutableIntStateOf(2) }
    var passAndPlay by remember { mutableStateOf(false) }
    val opponentChoices = remember { mutableStateMapOf<Int, BotOpponentConfigurationV1>() }
    var starterChoice by remember { mutableIntStateOf(0) }
    var opponentSeat by remember { mutableStateOf<Int?>(null) }
    val effectiveBoardId = CrossroadsExperimentalMode.boardId(selectedCourseId, playtestRules, fairFinishExperiment)
    val opponentPreview = OfflineOpponentPreview.enabled(BuildConfig.DEBUG, passAndPlay, effectiveBoardId)
    var showGuide by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showAppearancePicker by remember { mutableStateOf(false) }
    // Keep the first viewport focused on starting a race. Secondary tools
    // remain available in an explicit, accessible options section.
    var showExtraOptions by remember { mutableStateOf(false) }
    var showQaCourses by remember { mutableStateOf(false) }
    // Materials' default inactive switch is lavender; make BOTH experimental
    // controls use the same woodland palette at every on/off state.
    val woodlandSwitchColors = SwitchDefaults.colors(
        checkedThumbColor = Color(0xFFF7F0DE),
        checkedTrackColor = Color(0xFF48684D),
        checkedBorderColor = Color(0xFF48684D),
        uncheckedThumbColor = Color(0xFF897958),
        uncheckedTrackColor = Color(0xFFF2ECDD),
        uncheckedBorderColor = Color(0xFFA39170)
    )
    // GAMEFEEL-67: all race selectors now share original tactile wooden
    // controls, rather than Material's generic FilterChips.

    // A game-first home screen: art appears immediately and Start stays
    // reachable on short phones, regardless of setup scroll position.
    val homeScroll = rememberScrollState()
    Column(Modifier.fillMaxSize().background(Paper)
        .windowInsetsPadding(WindowInsets.safeDrawing)) {
        Box(Modifier.weight(1f).fillMaxWidth().clip(androidx.compose.ui.graphics.RectangleShape)) {
        Canvas(Modifier.matchParentSize()) {
            val spacing = 13.dp.toPx()
            val columns = (size.width / spacing).toInt()
            val rows = (size.height / spacing).toInt()
            for (y in 0..rows) for (x in 0..columns) {
                val i = y * (columns+1)+x
                val p = Offset((x+.2f+(i*13%7)*.08f)*spacing,
                    (y+.2f+(i*17%5)*.1f)*spacing)
                drawLine(Color(0xFF9F8860).copy(alpha=.075f),p,
                    p+Offset(2.dp.toPx(),1.dp.toPx()),.65.dp.toPx(),StrokeCap.Round)
            }
        }
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(homeScroll)
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        Text("GRIDMATES · BOARD GAMES",
            style = MaterialTheme.typography.labelMedium, color = Muted)
        // The illustrated course is the game's cover, not a separate text
        // page header followed by a thumbnail.
        Box(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)) {
            HomeTrailIllustration()
            Column(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    .background(Color(0xD9233B2B))
                    .padding(horizontal = 15.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text("Crossroads", fontSize = 31.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFFF8EED7))
                Text("Every path is a choice.",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE6D5AA))
            }
        }
        if (recoveryWarning != null) {
            Surface(color = Color(0xFFF1E0CF), shape = MaterialTheme.shapes.medium) {
                Text(
                    recoveryWarning,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall, color = Muted
                )
            }
        }
        HomeSectionHeading("PLAY")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabletopChoicePill(
                text = "Solo vs AI",
                selected = !passAndPlay,
                onClick = { passAndPlay = false },
                modifier = Modifier.weight(1f)
            )
            TabletopChoicePill(
                text = "Pass & Play",
                selected = passAndPlay,
                onClick = { passAndPlay = true },
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            if (passAndPlay) "$seats people · take turns on this phone · offline"
            else "You and ${seats - 1} ${if (seats == 2) "AI opponent" else "AI opponents"} · offline",
            style = MaterialTheme.typography.bodySmall, color = Muted
        )
        Text("Players", style = MaterialTheme.typography.labelMedium, color = Ink,
            fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (2..4).forEach { count ->
                TabletopChoicePill(
                    text = "$count",
                    selected = count == seats,
                    onClick = { seats = count },
                    modifier = Modifier.weight(1f).semantics { contentDescription = "$count players" }
                )
            }
        }
        HomeSectionHeading("Choose your course")
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
                HomeCourseCard(
                    id = id, label = label,
                    selected = selectedCourseId == id,
                    onClick = { onCourseChange(id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (BuildConfig.DEBUG && (regionalCourseId != null || convergenceCourseId != null)) {
            val experimentalSelected = selectedCourseId == regionalCourseId ||
                selectedCourseId == convergenceCourseId
            OutlinedButton(
                onClick = { showQaCourses = !showQaCourses },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (showQaCourses) "Hide test courses" else if (experimentalSelected)
                    "Test course selected · Show courses" else "Test courses · QA")
            }
            if (showQaCourses) {
        if (regionalCourseId != null) {
            Text("Experimental course · QA", style = MaterialTheme.typography.labelSmall, color = Muted)
            TabletopChoicePill(
                text = "Regional Journey",
                selected = selectedCourseId == regionalCourseId,
                onClick = { onCourseChange(regionalCourseId) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (convergenceCourseId != null) {
            TabletopChoicePill(
                text = "Woven Trail · Balance Preview",
                selected = selectedCourseId == convergenceCourseId,
                onClick = { onCourseChange(convergenceCourseId) },
                modifier = Modifier.fillMaxWidth()
            )
        }
            }
        }
        Text(
            when (selectedCourseId) {
                convergenceCourseId -> "QA only · balanced junction layout · offline Lab v5"
                regionalCourseId -> "Regional Journey · 8 crossroads · experimental offline course"
                CrossroadsCourse.QUICK_ID ->
                    "Brookside Dash · 2 crossroads · a short woodland sprint"
                CrossroadsCourse.LONG_ID ->
                    "Cedarwood Expedition · 6 crossroads · a much longer winding journey"
                else ->
                    "Woven Trail · 5 crossroads · the original woodland race"
            },
            style = MaterialTheme.typography.bodySmall, color = Muted
        )
        // The menu's next decision is Start. All secondary controls are
        // reachable but no longer make its first screen a long settings form.
        HomeSectionHeading("More ways to play")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showGuide = true },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)) {
                Text("How to play", textAlign = TextAlign.Center)
            }
            OutlinedButton(onClick = { showExtraOptions = !showExtraOptions },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)) {
                Text(if (showExtraOptions) "Hide options" else "More options", textAlign = TextAlign.Center)
            }
        }
        Text("Nearby · Two phones on the same Wi-Fi", style = MaterialTheme.typography.bodySmall,
            color = Muted)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onNearbyHost, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                Text("Host")
            }
            OutlinedButton(onClick = onNearbyJoin, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                Text("Join")
            }
        }
        if (showExtraOptions) {
        HomeSectionHeading("Race options")
        if (opponentPreview) {
            HomeSectionHeading("STARTING PLAYER · QA")
            TabletopUtilityButton(
                text = OfflineStartingSeatPreview.label(starterChoice, seats),
                onClick = { starterChoice = OfflineStartingSeatPreview.nextChoice(starterChoice, seats) },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Tap to cycle: You, other players, or Random", color = Muted,
                style = MaterialTheme.typography.bodySmall)
            HomeSectionHeading("AI OPPONENTS · QA")
            Text("Optional · Lab v5 only · Different playing styles, not difficulty levels",
                color = Muted, style = MaterialTheme.typography.bodySmall)
            for (seat in 1 until seats) {
                val choice = opponentChoices[seat]
                TabletopUtilityButton(
                    text = "Player ${seat + 1}: " + (choice?.let { OfflineOpponentPreview.label(it) } ?: "Classic"),
                    onClick = { opponentSeat = seat },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("Board appearance", color = Ink,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold)
                Text("Visual only · rules unchanged",
                    style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            TextButton(onClick = { showAppearancePicker = true }) {
                Text(boardLook.label + " ▾")
            }
        }
        TabletopPanel(
            color = if (playtestRules) Color(0xFFE4EBD9) else Color(0xFFECE5D4),
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tactical choices · Experimental", color = Ink,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold)
                    Text("Optional offline tactical rules",
                        style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Switch(
                    checked = playtestRules,
                    onCheckedChange = onPlaytestRulesChange,
                    colors = woodlandSwitchColors,
                    modifier = Modifier.semantics {
                        contentDescription = "Tactical choices, experimental rules"
                    }
                )
            }
        }
        if (playtestRules) {
            TabletopPanel(
                color = if (fairFinishExperiment) Color(0xFFE4EBD9) else Color(0xFFF2ECDD),
                elevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fair finish · Lab v5",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold, color = Ink)
                        Text("Let later racers finish the round",
                            style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    Switch(
                        checked = fairFinishExperiment,
                        onCheckedChange = onFairFinishExperimentChange,
                        colors = woodlandSwitchColors,
                        modifier = Modifier.semantics {
                            contentDescription = "Fair finish, experimental Lab v5"
                        }
                    )
                }
            }
            Text(
                if (fairFinishExperiment)
                    "Lab v5: after the first finish, later seats get one last move. " +
                        "Most unused steps wins if multiple players finish."
                else "Lab v4: Steady and Push, interactive encounters and the " +
                    "successful Push hazard reward; first to Finish wins.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }
        HomeSectionHeading("Local progress")
        OutlinedButton(onClick = { showAbout = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("About & data") }
        TabletopPanel(
            color = Color(0xFFEAE1CA),
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp
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
        } // End optional setup, nearby and record information
        }
        // A narrow decorative scrollbar signals additional content without extra words.
        if (homeScroll.maxValue > 0 && homeScroll.maxValue != Int.MAX_VALUE) {
            Canvas(Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                .width(7.dp).padding(vertical = 10.dp)) {
                val content = size.height + homeScroll.maxValue
                val thumb = (size.height * size.height / content).coerceAtLeast(18.dp.toPx())
                    .coerceAtMost(size.height)
                val travel = (size.height-thumb).coerceAtLeast(0f)
                val y = travel * homeScroll.value / homeScroll.maxValue
                val x = size.width / 2f
                drawLine(Color(0xFFBBA371).copy(alpha=.28f),Offset(x,0f),Offset(x,size.height),
                    2.dp.toPx(),StrokeCap.Round)
                drawLine(Ink.copy(alpha=.58f),Offset(x,y),Offset(x,y+thumb),
                    3.dp.toPx(),StrokeCap.Round)
            }
        }
        }
        // The primary action remains visible as players browse course and
        // multiplayer settings; decorative assets never push it below the fold.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Paper,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    homeRaceSummary(seats, passAndPlay,
                        playtestRules || effectiveBoardId.endsWith(com.playereden.crossroads.core.DecisionLab.V5_SUFFIX),
                        effectiveBoardId.endsWith(com.playereden.crossroads.core.DecisionLab.V5_SUFFIX),
                        courseName = CrossroadsCourse.title(selectedCourseId)),
                    style = MaterialTheme.typography.labelMedium, color = Muted
                )
                OfflineOpponentPreview.summary(BuildConfig.DEBUG, passAndPlay,
                    effectiveBoardId, seats, opponentChoices)?.let { summary ->
                    Text(summary, style = MaterialTheme.typography.labelMedium, color = Ink)
                }
                if (opponentPreview && OfflineStartingSeatPreview.normalized(starterChoice, seats) != 0) {
                    Text(OfflineStartingSeatPreview.label(starterChoice, seats),
                        style = MaterialTheme.typography.labelMedium, color = Ink)
                }
                TabletopStartRaceButton(
                    onClick = { onStart(seats, passAndPlay,
                        if (opponentPreview) opponentChoices.toMap() else emptyMap(),
                        if (opponentPreview) starterChoice else 0) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    val editingSeat = opponentSeat
    if (editingSeat != null && opponentPreview && editingSeat in 1 until seats) {
        AlertDialog(
            onDismissRequest = { opponentSeat = null },
            containerColor = Paper,
            title = { Text("Player ${editingSeat + 1} opponent", color = Ink) },
            text = {
                Column(Modifier.fillMaxWidth().heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()).selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Choose a playing style. Advanced is experimental, not a proven harder level.",
                        color = Muted, style = MaterialTheme.typography.bodySmall)
                    for (personality in BotPersonalityV1.entries) {
                        for (difficulty in BotDifficultyPreviewV1.available(personality)) {
                            val config = BotOpponentConfigurationV1(personality, difficulty)
                            val selected = (opponentChoices[editingSeat] ?:
                                BotOpponentConfigurationV1(BotPersonalityV1.CLASSIC)) == config
                            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .selectable(selected = selected, role = Role.RadioButton, onClick = {
                                    opponentChoices[editingSeat] = config
                                    opponentSeat = null
                                }).padding(horizontal = 4.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RadioButton(selected = selected, onClick = null)
                                Column(Modifier.weight(1f)) {
                                    Text(OfflineOpponentPreview.label(config), color = Ink,
                                        fontWeight = FontWeight.SemiBold)
                                    Text(OfflineOpponentPreview.description(config), color = Muted,
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { opponentSeat = null }) { Text("Close") } }
        )
    }
    if (showAppearancePicker) {
        AlertDialog(
            onDismissRequest = { showAppearancePicker = false },
            containerColor = Paper,
            title = { Text("Board appearance", color = Ink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Choose the visual style for every course. This does not change routes, rewards or saved matches.",
                        style = MaterialTheme.typography.bodySmall, color = Muted)
                    CrossroadsBoardLook.entries.forEach { look ->
                        OutlinedButton(
                            onClick = {
                                onBoardLookChange(look)
                                showAppearancePicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    (if (look == boardLook) "✓ " else "") + look.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(look.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppearancePicker = false }) { Text("Done") }
            }
        )
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
