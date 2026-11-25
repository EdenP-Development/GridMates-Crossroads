package com.playereden.gridmates.crossroads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

internal enum class LanScreen { NONE, HOST_SETUP, HOST_WAIT, JOIN }

internal fun parseCrossroadsAddress(raw: String): Pair<String, Int>? {
    val parts = raw.trim().split(":")
    if (parts.size != 2) return null
    val octets = parts[0].split(".")
    if (octets.size != 4 || octets.any {
        it.isEmpty() || it.length > 3 || !it.all(Char::isDigit) ||
            (it.toIntOrNull() ?: -1) !in 0..255
    }) return null
    val port = parts[1].toIntOrNull() ?: return null
    if (port !in 1..65535) return null
    return parts[0] to port
}

@Composable
internal fun CrossroadsLanLobby(
    screen: LanScreen,
    rooms: List<NearbyCrossroads>,
    hostAddress: String,
    hostPort: Int,
    hostSeats: Int,
    inviteCode: String,
    note: String?,
    busy: Boolean,
    onHost: (Boolean, Int) -> Unit,
    onJoin: (String, Int, String) -> Unit,
    onRefresh: () -> Unit,
    onExit: () -> Unit,
    onRejoin: () -> Unit,
    hasRejoin: Boolean
) {
    val forest = Color(0xFF48684D)
    val cream = Color(0xFFF7F0DE)
    var requireCode by remember { mutableStateOf(false) }
    var participants by remember { mutableIntStateOf(2) }
    var address by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var addressCopied by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val choiceColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Color(0xFFE2EAD9),
        selectedLabelColor = forest,
        labelColor = Color(0xFF80664B)
    )
    Column(
        modifier = Modifier.fillMaxSize().background(cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("GRIDMATES · CROSSROADS", color = forest,
            style = MaterialTheme.typography.labelLarge)
        Text(
            when (screen) {
                LanScreen.HOST_SETUP -> "Host a game"
                LanScreen.HOST_WAIT -> "Your nearby game"
                LanScreen.JOIN -> "Join a game"
                else -> "Nearby play"
            },
            color = forest, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text("Connect both phones to the same Wi-Fi. No account or internet service needed.",
            style = MaterialTheme.typography.bodyMedium)
        when (screen) {
            LanScreen.HOST_SETUP -> {
                Text("Two people on separate phones, plus optional AI rivals.",
                    style = MaterialTheme.typography.bodyMedium)
                Text("Racers in this match", color = forest,
                    style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (2..4).forEach { count ->
                        FilterChip(
                            selected = participants == count,
                            onClick = { participants = count },
                            label = { Text("$count racers") },
                            colors = choiceColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Text(
                    if (participants == 2) "2 human players · no AI"
                    else "2 human players · ${participants - 2} AI ${if (participants == 3) "rival" else "rivals"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = requireCode, onCheckedChange = { requireCode = it })
                    Text("Require a six-digit room code")
                }
                Button(
                    onClick = { onHost(requireCode, participants) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) { Text("Create nearby room") }
            }
            LanScreen.HOST_WAIT -> {
                TabletopPanel(
                    color = Color(0xFFE5EBD8),
                    contentColor = Color(0xFF3B5841),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 1.dp
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("Waiting for Player 2", color = forest,
                            style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (hostSeats == 2) "2 people on phones · no AI"
                            else "$hostSeats racers · 2 people on phones and ${hostSeats - 2} AI",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text("Your room should appear on the other phone under Nearby games.")
                        if (inviteCode.isNotEmpty()) {
                            Text("Room code: $inviteCode", fontWeight = FontWeight.Bold)
                        } else Text("No room code needed")
                        Text("Manual address", color = forest,
                            style = MaterialTheme.typography.labelMedium)
                        Text("$hostAddress:$hostPort",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold)
                        OutlinedButton(
                            enabled = hostAddress.isNotBlank() && hostPort in 1..65535,
                            onClick = {
                                clipboard.setText(AnnotatedString("$hostAddress:$hostPort"))
                                addressCopied = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (addressCopied) "Address copied" else "Copy host address") }
                    }
                }
                Text("The match begins when Player 2 joins.")
            }
            LanScreen.JOIN -> {
                Text("Nearby games", color = forest, style = MaterialTheme.typography.titleMedium)
                if (rooms.isEmpty()) {
                    TabletopPanel(
                        color = Color(0xFFE5EBD8),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 1.dp
                    ) {
                        Column(Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("Looking for a room", color = forest,
                                fontWeight = FontWeight.SemiBold)
                            Text("Ask Player 1 to create a room, then refresh. " +
                                "If it doesn't appear, use their address below.")
                        }
                    }
                }
                rooms.forEach { room ->
                    OutlinedButton(
                        onClick = { onJoin(room.host, room.port, code) },
                        enabled = !busy && (!room.requiresCode || code.length == 6),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(room.name, fontWeight = FontWeight.SemiBold)
                            Text(if (room.requiresCode) "Room code required" else "Tap to join",
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                TextButton(onClick = onRefresh, enabled = !busy) { Text("Refresh nearby games") }
                if (hasRejoin) {
                    Button(onClick = onRejoin, enabled = !busy,
                        modifier = Modifier.fillMaxWidth()) { Text("Rejoin previous room") }
                }
                HorizontalDivider()
                Text("Join manually", color = forest, style = MaterialTheme.typography.titleMedium)
                Text("Enter the address shown on the host phone (including the port).")
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text("Host address") },
                    placeholder = { Text("192.168.1.24:45000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code, onValueChange = {
                        code = it.filter(Char::isDigit).take(6)
                    },
                    label = { Text("Room code (if required)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { parseCrossroadsAddress(address)?.let { (h,p) -> onJoin(h,p,code) } },
                    enabled = !busy && parseCrossroadsAddress(address) != null &&
                        (code.isEmpty() || code.length == 6),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) { Text(if (busy) "Connecting…" else "Join by address") }
            }
            LanScreen.NONE -> Unit
        }
        if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (!note.isNullOrEmpty()) {
            TabletopPanel(color = Color(0xFFF1E5D3), elevation = 0.dp) {
                Text(note, modifier = Modifier.padding(12.dp))
            }
        }
        TextButton(
            onClick = onExit,
            modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = 48.dp)
        ) { Text("Back to main menu") }
        if (BuildConfig.DEBUG) {
            Text("Nearby QA · Two Android phones · Same Wi-Fi",
                color = Color(0xFF80664B), style = MaterialTheme.typography.labelSmall)
        }
    }
}
