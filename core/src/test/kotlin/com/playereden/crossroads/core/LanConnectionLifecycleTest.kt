package com.playereden.crossroads.core

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Test

/**
 * LAN-77: actual JVM loopback sockets, not a fake Packet serializer or Android Wi-Fi test.
 *
 * Tests the occupied/reserved guest seat, optional room code, reconnect token,
 * rematch rollover and stale action rejection. Only the host's GameSession
 * may mutate the authoritative board. All sockets/threads are closed in finally.
 */
class LanConnectionLifecycleTest {
    private val root = java.io.File(System.getProperty("crossroads.repoDir"))
    private val board = GameData.board(
        java.io.File(root, "data/boards/woodland_woven_trail.json").readText()
    )
    private val events = GameData.events(
        java.io.File(root, "data/events/woven_events_v02.json").readText()
    )
    private val started = 1794931200000L

    private fun race(seed: Int = 393) =
        GameSession(board, events, seats = 2, seed = seed, humanIndices = setOf(0, 1))

    private fun dial(port: Int): Socket = Socket().apply {
        connect(InetSocketAddress("127.0.0.1", port), 2500)
        soTimeout = 5000
        tcpNoDelay = true
    }

    private fun hello(socket: Socket, code: String = "", token: String = ""): LanPacket {
        LanWire.write(socket.getOutputStream(), LanPacket("hello", code = code, token = token))
        return LanWire.read(socket.getInputStream())
    }

    private fun readState(socket: Socket, matchId: String, revisionAtLeast: Int): MatchCheckpoint {
        repeat(25) {
            val packet = LanWire.read(socket.getInputStream())
            when (packet.type) {
                "ping" -> LanWire.write(socket.getOutputStream(),
                    LanPacket("pong", roomId = packet.roomId))
                "state", "welcome" -> {
                    val cp = requireNotNull(packet.checkpoint)
                    if (cp.matchId == matchId &&
                        cp.expected.revision >= revisionAtLeast) return cp
                }
                "notice" -> Unit
                else -> error("Unexpected host packet: ${packet.type}")
            }
        }
        error("Expected host state was never received")
    }

    private fun advanceHostToGuest(game: GameSession) {
        var ticks = 0
        while (game.currentIndex == 0 && game.phase != Phase.FINISHED && ticks++ < 30) {
            when (game.phase) {
                Phase.ROLL -> LanTurnAuthority.apply(game, 0, LanAction(LanActionType.ROLL))
                Phase.ROUTE -> LanTurnAuthority.apply(game, 0,
                    LanAction(LanActionType.ROUTE, route = game.availableRoutes().first().id))
                Phase.TARGET -> LanTurnAuthority.apply(game, 0,
                    LanAction(LanActionType.TARGET, target = game.availableTargets().first()))
                Phase.ENCOUNTER -> error("LAN must not run experimental rules")
                Phase.FINISHED -> Unit
            }
        }
        assertEquals("Guest should receive seat 1 turn", 1, game.currentIndex)
        assertEquals(Phase.ROLL, game.phase)
    }

    @Test fun privateRoomRejectsWrongCodeAndSecondGuestYetAllowsReservedTokenRejoin() {
        val connected = CountDownLatch(1)
        val disconnected = CountDownLatch(1)
        val game = race()
        val host = CrossroadsLanHost(
            game.checkpoint("private-room", started),
            requiresCode = true,
            onPresence = { present, _ ->
                if (present) connected.countDown() else disconnected.countDown()
            }
        )
        val open = mutableListOf<Socket>()
        fun client(): Socket = dial(host.port).also(open::add)
        try {
            assertEquals(6, host.code.length)
            assertTrue(host.code.all(Char::isDigit))

            val wrong = client()
            val deniedCode = hello(wrong, code = "wrong")
            assertEquals("error", deniedCode.type)
            assertTrue(deniedCode.message.contains("room code", ignoreCase = true))
            wrong.close()

            val owner = client()
            val welcomed = hello(owner, code = host.code)
            assertEquals("welcome", welcomed.type)
            assertEquals(host.roomId, welcomed.roomId)
            val seatToken = welcomed.token
            assertTrue(seatToken.length >= 32)
            assertEquals(game.snapshot(),
                GameSession.restore(board, events, requireNotNull(welcomed.checkpoint)).snapshot())
            assertTrue("Host never marked the seat occupied", connected.await(4, TimeUnit.SECONDS))
            assertTrue(host.connected)

            val competing = client()
            val full = hello(competing, code = host.code)
            assertEquals("error", full.type)
            assertTrue(full.message.contains("full", ignoreCase = true))
            competing.close()

            owner.close()
            assertTrue("Guest disconnect did not release live socket",
                disconnected.await(4, TimeUnit.SECONDS))

            val stolen = client()
            val reserved = hello(stolen, code = host.code, token = "not-the-owner")
            assertEquals("error", reserved.type)
            assertTrue(reserved.message.contains("reserved", ignoreCase = true))
            stolen.close()

            val returning = client()
            val rejoined = hello(returning, token = seatToken)
            assertEquals("welcome", rejoined.type)
            assertEquals(seatToken, rejoined.token)
            assertEquals(host.roomId, rejoined.roomId)
            assertEquals(game.snapshot(), GameSession.restore(board, events,
                requireNotNull(rejoined.checkpoint)).snapshot())

            // A rejoined guest must receive a newly published authoritative state.
            LanTurnAuthority.apply(game, 0, LanAction(LanActionType.ROLL))
            host.publish(game.checkpoint("private-room", started))
            val next = readState(returning, "private-room", game.revision)
            assertEquals(game.snapshot(), GameSession.restore(board, events, next).snapshot())
        } finally {
            open.forEach { runCatching { it.close() } }
            host.close()
        }
    }

    @Test fun sameSocketRejectsStaleAndDuplicateActionsAcrossHostControlledRematch() {
        val current = AtomicReference(race(393))
        advanceHostToGuest(current.get())
        val matchId = AtomicReference("round-a")
        val rejected = AtomicInteger(0)
        lateinit var host: CrossroadsLanHost
        host = CrossroadsLanHost(
            current.get().checkpoint(matchId.get(), started),
            onAction = { action ->
                val game = current.get()
                val id = matchId.get()
                try {
                    LanTurnAuthority.applyRemote(game, id, 1, action)
                    host.publish(game.checkpoint(id, started))
                } catch (_: IllegalArgumentException) {
                    rejected.incrementAndGet()
                    host.message("Old or duplicate move rejected")
                    host.publish(game.checkpoint(id, started))
                }
            }
        )
        val socket = dial(host.port)
        try {
            val welcome = hello(socket)
            assertEquals("welcome", welcome.type)
            assertEquals("round-a", requireNotNull(welcome.checkpoint).matchId)
            val initialRevision = current.get().revision
            val old = LanAction(LanActionType.ROLL,
                expectedMatchId = "round-a", expectedRevision = initialRevision)
            LanWire.write(socket.getOutputStream(),
                LanPacket("action", roomId = host.roomId, action = old))
            val accepted = readState(socket, "round-a", initialRevision + 1)
            assertEquals(current.get().snapshot(),
                GameSession.restore(board, events, accepted).snapshot())

            // A duplicate packet cannot roll the authoritative die twice.
            val frozen = current.get().snapshot()
            LanWire.write(socket.getOutputStream(),
                LanPacket("action", roomId = host.roomId, action = old))
            readState(socket, "round-a", frozen.revision)
            assertEquals(frozen, current.get().snapshot())
            assertEquals(1, rejected.get())

            // Host rematch keeps the admitted guest on the same socket but
            // gives the new race a new match identity and seed.
            val rematch = race(394)
            advanceHostToGuest(rematch)
            current.set(rematch)
            matchId.set("round-b")
            host.publish(rematch.checkpoint("round-b", started + 1000))
            val newWelcome = readState(socket, "round-b", rematch.revision)
            assertEquals(rematch.snapshot(),
                GameSession.restore(board, events, newWelcome).snapshot())
            assertNotEquals("round-a", newWelcome.matchId)

            val nextTurn = rematch.revision
            LanWire.write(socket.getOutputStream(),
                LanPacket("action", roomId = host.roomId, action = old))
            readState(socket, "round-b", nextTurn)
            assertEquals(nextTurn, current.get().revision)
            assertEquals(2, rejected.get())

            val fresh = LanAction(LanActionType.ROLL,
                expectedMatchId = "round-b", expectedRevision = nextTurn)
            LanWire.write(socket.getOutputStream(),
                LanPacket("action", roomId = host.roomId, action = fresh))
            val updated = readState(socket, "round-b", nextTurn + 1)
            assertEquals(rematch.snapshot(),
                GameSession.restore(board, events, updated).snapshot())
            assertTrue("Rematch should still be on the same guest socket", host.connected)
        } finally {
            runCatching { socket.close() }
            host.close()
        }
    }

    @Test fun deniedGuestSeesSpecificRoomCodeErrorExactlyOnce() {
        val game = race()
        val host = CrossroadsLanHost(
            game.checkpoint("code-check", started),
            requiresCode = true
        )
        val reported = java.util.concurrent.CopyOnWriteArrayList<String>()
        val firstError = CountDownLatch(1)
        val secondError = CountDownLatch(2)
        val receivedState = AtomicInteger(0)
        val guest = CrossroadsLanGuest(
            onState = { receivedState.incrementAndGet() },
            onConnection = { connected, note ->
                if (!connected) {
                    reported.add(note.orEmpty())
                    firstError.countDown()
                    secondError.countDown()
                }
            }
        )
        try {
            // A three-letter code cannot accidentally equal the six-digit code.
            guest.connect("127.0.0.1", host.port, code = "bad")
            assertTrue("Guest did not surface a join rejection",
                firstError.await(5, TimeUnit.SECONDS))
            assertFalse("A generic disconnect overwrote the real rejection",
                secondError.await(400, TimeUnit.MILLISECONDS))
            assertEquals(1, reported.size)
            assertTrue("Lost the reason for refusal: ${reported.single()}",
                reported.single().contains("room code", ignoreCase = true))
            assertEquals(0, receivedState.get())
            assertFalse(guest.connected)
            assertTrue(guest.rejoinToken.isEmpty())
        } finally {
            guest.close()
            host.close()
        }
    }

    @Test fun cancellingGuestDuringHandshakeClosesPendingSocket() {
        val listener = java.net.ServerSocket(0)
        val receivedHello = CountDownLatch(1)
        val peerClosed = CountDownLatch(1)
        val callbacks = AtomicInteger(0)
        val failure = AtomicReference<Throwable?>(null)
        val reader = Thread {
            try {
                listener.accept().use { link ->
                    link.soTimeout = 4000
                    assertEquals("hello", LanWire.read(link.getInputStream()).type)
                    receivedHello.countDown()
                    // Deliberately withhold welcome while the user cancels.
                    assertEquals(-1, link.getInputStream().read())
                    peerClosed.countDown()
                }
            } catch (e: Throwable) {
                failure.set(e)
            }
        }.apply { isDaemon = true; start() }
        val guest = CrossroadsLanGuest(
            onState = { callbacks.incrementAndGet() },
            onConnection = { _, _ -> callbacks.incrementAndGet() }
        )
        try {
            guest.connect("127.0.0.1", listener.localPort)
            assertTrue("Guest did not send hello", receivedHello.await(4, TimeUnit.SECONDS))
            guest.close()
            assertTrue("Cancel did not close handshake socket", peerClosed.await(3, TimeUnit.SECONDS))
            reader.join(1000)
            assertNull(failure.get())
            assertFalse(guest.connected)
            assertEquals(0, callbacks.get())
            assertTrue(guest.rejoinToken.isEmpty())
        } finally {
            guest.close()
            listener.close()
            reader.join(5000)
        }
    }

    @Test fun invalidRoomIdentityDisconnectsWithoutExecutingGuestTurn() {
        val game = race(393)
        advanceHostToGuest(game)
        val callbackCalls = AtomicInteger(0)
        val host = CrossroadsLanHost(
            game.checkpoint("no-cross-room", started),
            onAction = { callbackCalls.incrementAndGet() }
        )
        val link = dial(host.port)
        try {
            val welcome = hello(link)
            assertEquals("welcome", welcome.type)
            val before = game.snapshot()
            LanWire.write(link.getOutputStream(),
                LanPacket("action", roomId = "different-room",
                    action = LanAction(LanActionType.ROLL,
                        expectedMatchId = "no-cross-room",
                        expectedRevision = before.revision)))
            // The host should close the connection, never execute the packet.
            try {
                val packet = LanWire.read(link.getInputStream())
                fail("Unexpected reply after wrong-room action: ${packet.type}")
            } catch (_: IOException) {
                // EOF/reset is expected.
            }
            assertEquals(0, callbackCalls.get())
            assertEquals(before, game.snapshot())
        } finally {
            runCatching { link.close() }
            host.close()
        }
    }
}
