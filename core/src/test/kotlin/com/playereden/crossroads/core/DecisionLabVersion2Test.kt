package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Lab v2 is a new rules identity: existing QA1312 Lab v1 saves must not drift. */
class DecisionLabVersion2Test {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val originals = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private val started = 1794931200000L

    private val fixture = BoardDefinition(
        id = "shelter_fixture", start = "START", finish = "FINISH",
        nodes = listOf(
            BoardNode("START", "meadow", "start", listOf("J"), 0f, 0f),
            BoardNode("J", "meadow", "fork", listOf("SAFE1", "RISK1"), .15f, .1f),
            BoardNode("SAFE1", "meadow", "path", listOf("SAFE2"), .2f, .2f),
            BoardNode("SAFE2", "meadow", "path", listOf("SAFE3"), .3f, .2f),
            BoardNode("SAFE3", "meadow", "path", listOf("FINISH"), .4f, .2f),
            BoardNode("RISK1", "meadow", "path", listOf("SAFE2"), .2f, .4f),
            BoardNode("FINISH", "meadow", "finish", emptyList(), .6f, .2f)
        ),
        forks = listOf(ForkDefinition("J", "Shelter fork", listOf(
            ForkChoice("SAFE1", "Safe road", "low"),
            ForkChoice("RISK1", "Risky road", "high")
        )))
    )
    private val shieldOnly = EventCatalogue(listOf(
        EventDefinition("guaranteed_shelter", listOf("meadow"), 1,
            EventEffect("shield", 0), listOf("shelter"), "Trail shelter"),
        EventDefinition("guaranteed_hazard", listOf("meadow"), 1,
            EventEffect("retreat", 1), listOf("hazard"), "Rockfall")
    ))

    @Test fun versionsHaveDistinctFingerprintsAndHonestRoutePreviews() {
        for (original in originals) {
            val oldLab = DecisionLab.variant(original)
            val newLab = DecisionLab.variantV2(original)
            assertTrue(DecisionLab.enabled(oldLab))
            assertTrue(DecisionLab.enabled(newLab))
            assertFalse(DecisionLab.isV2(oldLab))
            assertTrue(DecisionLab.isV2(newLab))
            assertEquals(original.id, DecisionLab.baseId(oldLab.id))
            assertEquals(original.id, DecisionLab.baseId(newLab.id))
            assertEquals(original.nodes, newLab.nodes)
            assertEquals(original.forks, newLab.forks)
            val hashes = listOf(original, oldLab, newLab).map {
                MatchCheckpointCodec.fingerprint(it, events)
            }
            assertEquals(3, hashes.distinct().size)
            for (choice in original.forks.first().choices) {
                val remaining = DecisionLab.minimumSpacesToFinish(original, choice.to)
                assertTrue(remaining > 0)
                val info = DecisionLab.routePreview(newLab, choice.to,
                    choice.risk, steadyChosen = true)
                assertTrue(info.contains("Min $remaining spaces left"))
                assertTrue(info.contains("on entry"))
                assertEquals(choice.risk == "low", info.contains("Steady trail bonus +1"))
                assertFalse(DecisionLab.routePreview(oldLab, choice.to, choice.risk, true)
                    .contains("Steady trail bonus +1"))
                assertFalse(DecisionLab.routePreview(newLab, choice.to, choice.risk, false)
                    .contains("Steady trail bonus +1"))
            }
        }
    }

    @Test fun safeSteadyShieldAddsExactlyOneStepInV2NotV1() {
        fun play(board: BoardDefinition): GameSession {
            val game = GameSession(board, shieldOnly, 2, 42, setOf(0, 1))
            game.rollSteady()
            assertEquals(Phase.ROUTE, game.phase)
            assertEquals("J", game.players[0].position)
            assertTrue(game.steadyTravelChosen)
            game.chooseRoute("SAFE1")
            assertEquals("guaranteed_shelter", game.lastEventId)
            assertTrue(game.players[0].shield)
            assertEquals(game.snapshot(), GameSession.restore(board, shieldOnly,
                game.checkpoint("safe-fork", started)).snapshot())
            return game
        }
        val v1 = DecisionLab.variant(fixture)
        val v2 = DecisionLab.variantV2(fixture)
        val old = play(v1)
        val newer = play(v2)
        assertEquals("SAFE3", old.players[0].position)
        assertEquals(Phase.ROLL, old.phase)
        assertEquals("FINISH", newer.players[0].position)
        assertEquals(Phase.FINISHED, newer.phase)
        assertTrue(newer.snapshot().encounters.last().outcome.contains("Steady trail +1"))
        assertFalse(old.snapshot().encounters.last().outcome.contains("Steady trail +1"))
        try {
            GameSession.restore(v1, shieldOnly, newer.checkpoint("v2-save", started))
            fail("v2 checkpoint was silently accepted by v1 rules")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun pushingIntoRiskOrShelterPreservesV1Outcome() {
        val v1 = DecisionLab.variant(fixture)
        val v2 = DecisionLab.variantV2(fixture)
        var checked = 0
        for (seed in 1..28) {
            val old = GameSession(v1, shieldOnly, 2, seed, setOf(0, 1))
            val newer = GameSession(v2, shieldOnly, 2, seed, setOf(0, 1))
            old.rollBold()
            newer.rollBold()
            assertEquals(old.snapshot(), newer.snapshot())
            if (old.phase != Phase.ROUTE) continue
            val route = if (checked++ % 2 == 0) "SAFE1" else "RISK1"
            old.chooseRoute(route)
            newer.chooseRoute(route)
            assertEquals(old.snapshot(), newer.snapshot())
        }
        assertTrue("Never tested an eligible Push fork", checked > 4)
    }

    @Test fun oldLabAndLegacyCheckpointsKeepExactReplay() {
        for (original in originals) {
            val v1 = DecisionLab.variant(original)
            val v2 = DecisionLab.variantV2(original)
            val previous = GameSession(v1, events, 2, 134, setOf(0))
            previous.rollSteady()
            val saved = previous.checkpoint("qa1312-save", started)
            val payload = MatchCheckpointCodec.encode(saved)
            assertEquals(payload, MatchCheckpointCodec.encode(
                GameSession.restore(v1, events, saved)
                    .checkpoint("qa1312-save", started)))
            try {
                GameSession.restore(v2, events, saved)
                fail("v1 replay erroneously used v2")
            } catch (_: IllegalArgumentException) { }
            val legacy = GameSession(original, events, 2, 119, setOf(0))
            legacy.roll()
            assertEquals(legacy.snapshot(), GameSession.restore(original,
                events, legacy.checkpoint("original", started)).snapshot())
        }
    }

    @Test fun allThreeV2CoursesFinishAndReplayAcrossSeats() {
        for (original in originals) for (seats in 2..4) for (seed in 1..30) {
            val board = DecisionLab.variantV2(original)
            val game = GameSession(board, events, seats, seed, setOf(0))
            var actions = 0
            while (game.phase != Phase.FINISHED && actions++ < 2700) {
                if (game.players[game.currentIndex].isBot) game.botStep()
                else when (game.phase) {
                    Phase.ROLL -> if ((seed + actions) % 2 == 0)
                        game.rollSteady() else game.rollBold()
                    Phase.ROUTE -> game.chooseRoute(game.availableRoutes()[
                        (seed + actions) % game.availableRoutes().size
                    ].id)
                    Phase.TARGET -> game.chooseTarget(game.availableTargets().first())
                    Phase.FINISHED -> Unit
                }
                if (seed <= 3) {
                    val cp = game.checkpoint("v2-test", started)
                    assertEquals(game.snapshot(),
                        GameSession.restore(board, events, cp).snapshot())
                }
            }
            assertEquals("v2 stalled on $seats / $seed / $board",
                Phase.FINISHED, game.phase)
            assertEquals(board.finish,
                game.players[requireNotNull(game.winnerIndex)].position)
        }
    }
}
