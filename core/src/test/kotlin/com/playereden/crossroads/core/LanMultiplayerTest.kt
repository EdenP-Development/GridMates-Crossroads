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

    private fun room(seed: Int = 133, seats: Int = 2): GameSession =
        GameSession(board, catalogue, seats = seats, seed = seed, humanIndices = setOf(0, 1))

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

    @Test fun replayedAndPreviousMatchRemoteCommandsNeverChangeTheGame() {
        val game = room(393)
        var steps = 0
        while (game.currentIndex == 0 && steps++ < 20) {
            when (game.phase) {
                Phase.ROLL -> LanTurnAuthority.apply(game, 0, LanAction(LanActionType.ROLL))
                Phase.ROUTE -> LanTurnAuthority.apply(game, 0,
                    LanAction(LanActionType.ROUTE, route = game.availableRoutes().first().id))
                Phase.TARGET -> LanTurnAuthority.apply(game, 0,
                    LanAction(LanActionType.TARGET, target = game.availableTargets().first()))
                Phase.ENCOUNTER -> error("LAN never permits experimental encounters")
                Phase.FINISHED -> error("Race ended before Player 2")
            }
        }
        assertEquals(1, game.currentIndex)
        val original = game.snapshot()
        val valid = LanAction(LanActionType.ROLL, expectedMatchId = "match-a",
            expectedRevision = original.revision)
        try {
            LanTurnAuthority.applyRemote(game, "match-b", 1, valid)
            fail("Previous match command was accepted")
        } catch (_: IllegalArgumentException) { }
        assertEquals(original, game.snapshot())
        LanTurnAuthority.applyRemote(game, "match-a", 1, valid)
        val accepted = game.snapshot()
        assertTrue(accepted.revision > original.revision)
        try {
            LanTurnAuthority.applyRemote(game, "match-a", 1, valid)
            fail("Duplicate command was accepted")
        } catch (_: IllegalArgumentException) { }
        assertEquals(accepted, game.snapshot())
    }

    @Test fun twoNearbyHumansCanRaceOneOrTwoHostOwnedBotsWithVerifiedReplay() {
        for (seats in 3..4) {
            for (seed in listOf(101, 202, 303)) {
                val game = room(seed, seats)
                assertEquals(2, game.players.count { !it.isBot })
                assertEquals(seats - 2, game.players.count { it.isBot })
                var actions = 0
                var botActions = 0
                while (game.phase != Phase.FINISHED && actions++ < 400) {
                    val seat = game.currentIndex
                    if (game.players[seat].isBot) {
                        assertTrue("A bot must not take a guest's seat", seat >= 2)
                        val before = game.snapshot()
                        try {
                            LanTurnAuthority.apply(game, 1, LanAction(LanActionType.ROLL))
                            fail("Guest took action on an AI turn")
                        } catch (_: IllegalArgumentException) { }
                        assertEquals(before, game.snapshot())
                        game.botStep() // Only the host does this; guest replays the checkpoint.
                        botActions++
                    } else {
                        val action = when (game.phase) {
                            Phase.ROLL -> LanAction(LanActionType.ROLL)
                            Phase.ROUTE -> LanAction(LanActionType.ROUTE,
                                route = game.availableRoutes().first().id)
                            Phase.TARGET -> LanAction(LanActionType.TARGET,
                                target = game.availableTargets().first())
                            Phase.ENCOUNTER -> error("LAN never permits experimental encounters")
                            Phase.FINISHED -> error("Already finished")
                        }
                        LanTurnAuthority.apply(game, seat, action)
                    }
                    val checkpoint = game.checkpoint("nearby-ai-$seats-$seed", stamp)
                    val guestMirror = GameSession.restore(board, catalogue, checkpoint)
                    assertEquals("Guest diverged from host at action $actions",
                        game.snapshot(), guestMirror.snapshot())
                }
                assertEquals("Race did not finish", Phase.FINISHED, game.phase)
                assertTrue("AI never got to move", botActions > 0)
                assertTrue(game.checkpoint("nearby-ai-$seats-$seed", stamp).actions
                    .any { it.kind == MatchActionKind.BOT })
            }
        }
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
                Phase.ENCOUNTER -> error("LAN never permits experimental encounters")
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
                    LanTurnAuthority.applyRemote(game, "tcp-test", 1, move)
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
            client.submit(LanAction(LanActionType.ROLL,
                expectedMatchId = "tcp-test", expectedRevision = initialRevision))
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
