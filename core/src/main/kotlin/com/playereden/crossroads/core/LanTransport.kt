package com.playereden.crossroads.core

import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private fun lanThread(name: String) = java.util.concurrent.ThreadFactory { job ->
    Thread(job, name).apply { isDaemon = true }
}

/**
 * At most one guest (seat 1). Network threads only carry validated requests;
 * the Android UI applies them to the host's GameSession on its main thread.
 */
class CrossroadsLanHost(
    initial: MatchCheckpoint,
    private val requiresCode: Boolean = false,
    private val onAction: (LanAction) -> Unit = {},
    private val onPresence: (Boolean, String?) -> Unit = { _, _ -> }
) : AutoCloseable {
    val roomId: String = UUID.randomUUID().toString()
    val code: String = if (requiresCode)
        SecureRandom().nextInt(1_000_000).toString().padStart(6, '0') else ""
    @Volatile private var checkpoint = initial
    @Volatile private var running = true
    @Volatile private var peer: Socket? = null
    @Volatile private var reservedToken: String? = null
    private val socket = ServerSocket(0).apply { reuseAddress = true }
    private val acceptor = Executors.newSingleThreadExecutor(lanThread("crossroads-host-accept"))
    private val readers = Executors.newCachedThreadPool(lanThread("crossroads-host-read"))
    private val writer = Executors.newSingleThreadExecutor(lanThread("crossroads-host-write"))
    private val heartbeat: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor(lanThread("crossroads-host-heartbeat"))
    val port: Int get() = socket.localPort
    val connected: Boolean get() = peer?.isConnected == true && peer?.isClosed == false

    init {
        acceptor.execute {
            while (running) {
                val incoming = try { socket.accept() } catch (_: IOException) { break }
                readers.execute { handle(incoming) }
            }
        }
        heartbeat.scheduleAtFixedRate({
            if (running && connected) send(LanPacket("ping", roomId = roomId))
        }, 3, 3, TimeUnit.SECONDS)
    }

    fun publish(next: MatchCheckpoint) {
        checkpoint = next
        if (connected) send(LanPacket("state", roomId = roomId, checkpoint = next))
    }

    fun message(text: String) = send(LanPacket(
        "notice", roomId = roomId, message = text.take(250)
    ))

    private fun send(packet: LanPacket) {
        writer.execute {
            val current = peer ?: return@execute
            try { LanWire.write(current.getOutputStream(), packet) }
            catch (_: Exception) { drop(current, "Connection interrupted. Rejoin to continue.") }
        }
    }

    private fun handle(incoming: Socket) {
        try {
            incoming.tcpNoDelay = true
            incoming.soTimeout = 7000
            val hello = LanWire.read(incoming.getInputStream())
            val accepted: String
            synchronized(this) {
                val existing = peer
                val reserved = reservedToken
                val reason = when {
                    !running -> "Room closed"
                    hello.type != "hello" -> "Incompatible room request"
                    existing != null && !existing.isClosed -> "Room is full"
                    reserved != null && hello.token != reserved -> "This seat is reserved. Ask the host to restart the room."
                    reserved == null && requiresCode && hello.code != code -> "Incorrect room code"
                    else -> null
                }
                if (reason != null) {
                    LanWire.write(incoming.getOutputStream(),
                        LanPacket("error", roomId = roomId, message = reason))
                    return
                }
                accepted = reserved ?: UUID.randomUUID().toString()
                reservedToken = accepted
                peer = incoming
                incoming.soTimeout = 20000
                LanWire.write(incoming.getOutputStream(),
                    LanPacket("welcome", roomId = roomId, token = accepted, checkpoint = checkpoint))
            }
            onPresence(true, null)
            while (running && !incoming.isClosed) {
                val frame = LanWire.read(incoming.getInputStream())
                if (frame.roomId != roomId) throw IOException("Unexpected room")
                when (frame.type) {
                    "action" -> {
                        val action = frame.action ?: throw IOException("Missing move")
                        if ((action.route?.length ?: 0) > 100)
                            throw IOException("Invalid route")
                        onAction(action)
                    }
                    "pong" -> Unit
                    else -> throw IOException("Unsupported room command")
                }
            }
        } catch (_: Exception) {
            // A dropped socket leaves the seat reserved for the same guest.
        } finally {
            drop(incoming, "Connection interrupted. Rejoin to continue.")
        }
    }

    private fun drop(incoming: Socket, reason: String) {
        runCatching { incoming.close() }
        synchronized(this) {
            if (peer !== incoming) return
            peer = null
        }
        onPresence(false, reason)
    }

    override fun close() {
        running = false
        runCatching { socket.close() }
        val old = peer
        peer = null
        runCatching { old?.close() }
        heartbeat.shutdownNow()
        acceptor.shutdownNow()
        readers.shutdownNow()
        writer.shutdownNow()
    }
}

/** A guest never modifies the rules engine: it sends only a proposed action. */
class CrossroadsLanGuest(
    private val socketFactory: () -> Socket = { Socket() },
    private val onState: (MatchCheckpoint) -> Unit,
    private val onConnection: (Boolean, String?) -> Unit
) : AutoCloseable {
    private val worker = Executors.newSingleThreadExecutor(lanThread("crossroads-guest"))
    private val writer = Executors.newSingleThreadExecutor(lanThread("crossroads-guest-write"))
    @Volatile private var socket: Socket? = null
    @Volatile private var running = true
    @Volatile var roomId: String = ""
        private set
    @Volatile var rejoinToken: String = ""
        private set
    @Volatile var connected: Boolean = false
        private set

    fun connect(host: String, port: Int, code: String = "", resumeToken: String = "") {
        require(host.isNotBlank() && port in 1..65535) { "Invalid host address" }
        worker.execute {
            val link = try { socketFactory() } catch (_: Exception) {
                onConnection(false, "Connect both devices to the same Wi-Fi."); return@execute
            }
            try {
                link.connect(InetSocketAddress(host, port), 5000)
                link.soTimeout = 20000
                link.tcpNoDelay = true
                LanWire.write(link.getOutputStream(),
                    LanPacket("hello", code = code, token = resumeToken))
                val welcome = LanWire.read(link.getInputStream())
                if (welcome.type == "error") throw IOException(welcome.message)
                if (welcome.type != "welcome" || welcome.checkpoint == null ||
                    welcome.token.isBlank() || welcome.roomId.isBlank()
                ) throw IOException("The host is using an incompatible room.")
                socket = link
                roomId = welcome.roomId
                rejoinToken = welcome.token
                connected = true
                onState(welcome.checkpoint)
                onConnection(true, null)
                var lastRevision = welcome.checkpoint.expected.revision
                var lastMatchId = welcome.checkpoint.matchId
                while (running && !link.isClosed) {
                    val frame = LanWire.read(link.getInputStream())
                    if (frame.roomId != roomId) throw IOException("Wrong room")
                    when (frame.type) {
                        "state" -> {
                            val state = frame.checkpoint ?: throw IOException("Missing game state")
                            if (state.matchId == lastMatchId &&
                                state.expected.revision < lastRevision
                            ) throw IOException("Out-of-order game state")
                            lastMatchId = state.matchId
                            lastRevision = state.expected.revision
                            onState(state)
                        }
                        "ping" -> send(LanPacket("pong", roomId = roomId))
                        "notice" -> onConnection(true, frame.message)
                        "error" -> onConnection(true, frame.message)
                        else -> throw IOException("Unsupported game message")
                    }
                }
            } catch (e: Exception) {
                if (running) onConnection(false,
                    e.message?.take(200) ?: "Connection interrupted. Rejoin to continue.")
            } finally {
                connected = false
                if (socket === link) socket = null
                runCatching { link.close() }
                if (running) onConnection(false, "Disconnected. Rejoin the host to continue.")
            }
        }
    }

    fun submit(action: LanAction) {
        check(connected) { "Rejoin before making a move" }
        send(LanPacket("action", roomId = roomId, action = action))
    }

    private fun send(frame: LanPacket) {
        writer.execute {
            val link = socket ?: return@execute
            try { LanWire.write(link.getOutputStream(), frame) }
            catch (_: Exception) { runCatching { link.close() } }
        }
    }

    override fun close() {
        running = false
        connected = false
        runCatching { socket?.close() }
        worker.shutdownNow()
        writer.shutdownNow()
    }
}
