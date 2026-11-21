package com.playereden.crossroads.core

import java.io.File
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

/** Reproduce every final-round edge case without altering legacy winner semantics. */
class DecisionLabVersion5FinishTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val originals = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val catalogue = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private val emptyEvents = EventCatalogue(emptyList())
    private val epoch = 1794931200000L

    // Exactly three moves to Finish, no forks or events to obscure the turn rule.
    private val fixture = BoardDefinition(
        "v5_equal_round_fixture", "START", "FINISH",
        listOf(
            BoardNode("START", "meadow", "start", listOf("A"), 0f, 0f),
            BoardNode("A", "meadow", "plain", listOf("B"), .2f, .1f),
            BoardNode("B", "meadow", "plain", listOf("FINISH"), .4f, .1f),
            BoardNode("FINISH", "meadow", "finish", emptyList(), .6f, .1f)
        )
    )

    private fun game(seats: Int = 2, seed: Int = 31, v5: Boolean = true) =
        GameSession(if (v5) DecisionLab.variantV5(fixture)
                    else DecisionLab.variantV4(fixture),
            emptyEvents, seats = seats, seed = seed,
            humanIndices = (0 until seats).toSet())

    private fun seedWithPushSuccess() = (1..200).first {
        Random(it).nextInt(1, 7) >= 3
    }

    private fun seedWithPushStumble() = (1..200).first {
        Random(it).nextInt(1, 7) <= 2
    }

    @Test fun firstFinisherWaitsForLaterSeatsAndReplayStaysExact() {
        val g = game(seed = seedWithPushStumble())
        g.rollSteady() // 4 movement on a three-space board leaves 1 unused.
        assertEquals(Phase.ROLL, g.phase)
        assertEquals(1, g.currentIndex)
        assertNull(g.winnerIndex)
        assertEquals("FINISH", g.players[0].position)
        assertTrue(g.finalRoundPending)
        assertEquals("Player 1", g.firstFinisherName)
        val pending = g.checkpoint("v5-final-pending", epoch)
        val restored = GameSession.restore(g.board, emptyEvents, pending)
        assertEquals(g.snapshot(), restored.snapshot())
        assertTrue(restored.finalRoundPending)
        assertEquals(g.firstFinisherName, restored.firstFinisherName)

        g.rollBold() // Stumble: 1 step, cannot reach finish.
        assertEquals(Phase.FINISHED, g.phase)
        assertEquals(0, g.winnerIndex)
        assertEquals("A", g.players[1].position)
        assertFalse(g.finalRoundPending)
        assertEquals(g.snapshot(), GameSession.restore(
            g.board, emptyEvents, g.checkpoint("v5-final", epoch)).snapshot())
    }

    @Test fun laterFinisherCanWinWithGreaterUnusedMovement() {
        val g = game(seed = seedWithPushSuccess())
        g.rollSteady()
        assertNull(g.winnerIndex)
        assertTrue(g.finalRoundPending)
        g.rollBold() // 5-8 steps: exceeds seat one's unused 1 step.
        assertEquals(Phase.FINISHED, g.phase)
        assertEquals(1, g.winnerIndex)
        assertEquals("FINISH", g.players[0].position)
        assertEquals("FINISH", g.players[1].position)
        assertEquals(g.snapshot(), GameSession.restore(
            g.board, emptyEvents, g.checkpoint("last-seat-overtook", epoch)).snapshot())
    }

    @Test fun tiedUnusedMovementKeepsEarliestArrivalAndAllFourGetTheirTurn() {
        val g = game(seats = 4)
        repeat(3) { seat ->
            assertEquals(seat, g.currentIndex)
            g.rollSteady()
            assertEquals(Phase.ROLL, g.phase)
            assertEquals(seat + 1, g.currentIndex)
            assertNull(g.winnerIndex)
            assertTrue(g.finalRoundPending)
        }
        g.rollSteady()
        assertEquals(Phase.FINISHED, g.phase)
        assertEquals(0, g.winnerIndex)
        assertEquals(4, g.players.count { it.position == "FINISH" })
        assertEquals(4, g.turn)
        assertEquals(g.snapshot(), GameSession.restore(
            g.board, emptyEvents, g.checkpoint("v5-tied-finish", epoch)).snapshot())
    }

    @Test fun lastSeatFinishingFirstEndsTheMatchWithoutFakeExtraRound() {
        val seed = (1..10000).first { candidate ->
            val random = Random(candidate)
            (1..3).all { random.nextInt(1, 7) <= 2 }
        }
        val g = game(seats = 4, seed = seed)
        repeat(3) { g.rollBold() }
        assertFalse(g.finalRoundPending)
        assertEquals(3, g.currentIndex)
        g.rollSteady()
        assertEquals(Phase.FINISHED, g.phase)
        assertEquals(3, g.winnerIndex)
        assertEquals(4, g.turn)
    }

    @Test fun oldV4StillStopsImmediatelyOnFirstFinish() {
        val g = game(v5 = false)
        g.rollSteady()
        assertEquals(Phase.FINISHED, g.phase)
        assertEquals(0, g.winnerIndex)
        assertFalse(g.finalRoundPending)
    }

    @Test fun allPriorBoardIdentitiesAndCheckpointsRemainSeparate() {
        for (base in originals) {
            val versions = listOf(
                base, DecisionLab.variant(base), DecisionLab.variantV2(base),
                DecisionLab.variantV3(base), DecisionLab.variantV4(base),
                DecisionLab.variantV5(base)
            )
            assertEquals(6, versions.map {
                MatchCheckpointCodec.fingerprint(it, catalogue)
            }.distinct().size)
            assertEquals(base.id, DecisionLab.baseId(versions.last().id))
            assertTrue(DecisionLab.finishRoundEqually(versions.last()))
            assertFalse(DecisionLab.finishRoundEqually(versions[4]))
            assertEquals(2, DecisionLab.pushHazardSuccessBonus(versions.last(), true))
            for (old in versions.dropLast(1)) {
                val g = GameSession(old, catalogue, seats = 2, seed = 21,
                    humanIndices = setOf(0))
                if (DecisionLab.enabled(old)) g.rollSteady() else g.roll()
                val cp = g.checkpoint("old-save", epoch)
                assertEquals(g.snapshot(), GameSession.restore(old, catalogue, cp).snapshot())
                try {
                    GameSession.restore(versions.last(), catalogue, cp)
                    fail("Prior rules were incorrectly migrated into v5")
                } catch (_: IllegalArgumentException) { }
            }
        }
    }
}
