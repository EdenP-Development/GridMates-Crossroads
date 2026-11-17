package com.playereden.crossroads.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Test

class LanMultiplayerTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val board = GameData.board(File(root, "data/boards/woodland_woven_trail.json").readText())
    private val catalogue = GameData.events(File(root, "data/events/woven_events_v02.json").readText())
    private val stamp = 1794931200000L

    private fun room(seed: Int = 133): GameSession =
        GameSession(board, catalogue, seats = 2, seed = seed, humanIndices = setOf(0, 1))

    @Test fun onlySeatOwnerCanRollChooseRouteOrTarget() {
        val game = room()
        val fresh = game.snapshot()
        try {
            LanTurnAuthority.apply(game, 1, LanAction(LanActionType.ROLL))
            fail("Guest rolled on host's turn")
        } catch (_: IllegalArgumentException) {}
        try {
            LanTurnAuthority.apply(game, 0, LanAction(LanActionType.ROUTE, route = "FAKE"))
            fail("Host chose a path before rolling")
        } catch (_: IllegalArgumentException) {}
        try {
            LanTurnAuthority.apply(game, 0, LanAction(LanActionType.TARGET, target = 1))
            fail("Host targeted without mischief")
        } catch (_: IllegalArgumentException) {}
        assertEquals(fresh, game.snapshot())
        LanTurnAuthority.apply(game, 0, LanAction(LanActionType.ROLL))
        assertTrue(game.revision > fresh.revision)
    }

    @Test fun malformedAndOversizeFramesAreRefused() {
        val buffer = ByteArrayOutputStream()
        val frame = LanPacket("action", roomId = "abc",
            action = LanAction(LanActionType.ROUTE, route = "X"))
        LanWire.write(buffer, frame)
        assertEquals(frame, LanWire.read(ByteArrayInputStream(buffer.toByteArray())))
        val oversized = byteArrayOf(0x00, 0x10, 0x00, 0x00)
        try {
            LanWire.read(ByteArrayInputStream(oversized))
            fail("Accepted oversized frame")
        } catch (_: IOException) {}
        val invalid = ByteArrayOutputStream()
        LanWire.write(invalid, LanPacket("hello", version = 999))
        try {
            LanWire.read(ByteArrayInputStream(invalid.toByteArray()))
            fail("Accepted unsupported version")
        } catch (_: IOException) {}
    }

    @Test fun hostAndGuestExchangeRealVerifiedTurnsOverTcp() {
        val game = room(393)
        // Give the host a legitimate first roll/route before the guest connects,
        // so seat 1 is the next player in the actual authoritative engine.
        var ticks = 0
        while (game.currentIndex == 0 && game.phase != Phase.FINISHED && ticks++ < 20) {
            when (game.phase) {
                Phase.ROLL -> LanTurnAuthority.apply(game, 0, LanAction(LanActionType.ROLL))
                Phase.ROUTE -> LanTurnAuthority.apply(game, 0,
                    LanAction(LanActionType.ROUTE, route = game.availableRoutes().first().id))
                Phase.TARGET -> LanTurnAuthority.apply(game, 0,
                    LanAction(LanActionType.TARGET, target = game.availableTargets().first()))
                Phase.FINISHED -> Unit
            }
        }
        assertEquals(1, game.currentIndex)
        val initialRevision = game.revision
        val welcome = CountDownLatch(1)
        val afterRoll = CountDownLatch(1)
        val latest = AtomicReference<MatchCheckpoint>()
        lateinit var host: CrossroadsLanHost
        host = CrossroadsLanHost(
            game.checkpoint("tcp-test", stamp),
            onAction = { move ->
                synchronized(game) {
                    LanTurnAuthority.apply(game, 1, move)
                    host.publish(game.checkpoint("tcp-test", stamp))
                }
            }
        )
        val client = CrossroadsLanGuest(
            onState = { value ->
                latest.set(value)
                if (value.expected.revision > initialRevision) {
                    afterRoll.countDown()
                }
                welcome.countDown()
            },
            onConnection = { _, _ -> }
        )
        try {
            client.connect("127.0.0.1", host.port)
            assertTrue("Guest never received initial state", welcome.await(8, TimeUnit.SECONDS))
            assertEquals(game.snapshot(), GameSession.restore(board, catalogue,
                requireNotNull(latest.get())).snapshot())
            client.submit(LanAction(LanActionType.ROLL))
            assertTrue("Guest action was not broadcast by host",
                afterRoll.await(8, TimeUnit.SECONDS))
            assertEquals(game.snapshot(), GameSession.restore(board, catalogue,
                requireNotNull(latest.get())).snapshot())
            assertTrue("Guest did not receive rejoin token", client.rejoinToken.length >= 32)
        } finally {
            client.close()
            host.close()
        }
    }
}
