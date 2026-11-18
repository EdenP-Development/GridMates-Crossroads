package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Lab v3 adds a real shelter/hazard response, but never rewrites old saves. */
class DecisionLabVersion3Test {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val originals = listOf(
        "woodland_brookside_quick.json", "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private val epoch = 1794931200000L
    private val fixture = BoardDefinition(
        "encounter_choice_fixture", "START", "FINISH",
        listOf(
            BoardNode("START", "meadow", "start", listOf("F"), .0f, .0f),
            BoardNode("F", "meadow", "fork", listOf("SAFE", "HAZ"), .1f, .1f),
            BoardNode("SAFE", "meadow", "plain", listOf("S2"), .2f, .1f),
            BoardNode("S2", "meadow", "plain", listOf("S3"), .3f, .1f),
            BoardNode("S3", "meadow", "plain", listOf("FINISH"), .4f, .1f),
            BoardNode("HAZ", "meadow", "plain", listOf("H2"), .2f, .3f),
            BoardNode("H2", "meadow", "plain", listOf("FINISH"), .4f, .3f),
            BoardNode("FINISH", "meadow", "finish", emptyList(), .5f, .2f)
        ),
        listOf(ForkDefinition("F", "Choice fork", listOf(
            ForkChoice("SAFE", "Camp path", "low"),
            ForkChoice("HAZ", "Ridge path", "high")
        )))
    )
    private val fixedEvents = EventCatalogue(listOf(
        EventDefinition("shelter", listOf("meadow"), 1,
            EventEffect("shield", 0), listOf("shelter"), "Shelter"),
        EventDefinition("rockfall", listOf("meadow"), 1,
            EventEffect("retreat", 1), listOf("hazard"), "Rockfall")
    ))

    private fun atChoice(risk: String, seed: Int = 14): GameSession {
        val g = GameSession(DecisionLab.variantV3(fixture), fixedEvents,
            seats = 2, seed = seed, humanIndices = setOf(0, 1))
        g.rollSteady()
        assertEquals(Phase.ROUTE, g.phase)
        g.chooseRoute(if (risk == "low") "SAFE" else "HAZ")
        assertEquals(Phase.ENCOUNTER, g.phase)
        assertEquals(g.snapshot(), GameSession.restore(
            g.board, fixedEvents, g.checkpoint("pending", epoch)
        ).snapshot())
        return g
    }

    @Test fun v3IdentitiesAndOldCheckpointsAreNeverReinterpreted() {
        for (base in originals) {
            val versions = listOf(base, DecisionLab.variant(base),
                DecisionLab.variantV2(base), DecisionLab.variantV3(base))
            assertEquals(4, versions.map { MatchCheckpointCodec.fingerprint(it, events) }
                .distinct().size)
            assertEquals(base.id, DecisionLab.baseId(versions.last().id))
            assertFalse(DecisionLab.isV3(versions[2]))
            assertTrue(DecisionLab.isV3(versions[3]))
            assertTrue(DecisionLab.routePreview(versions[3],
                base.forks.first().choices.first().to, "high", true)
                .contains("Make an encounter choice"))
            for (prior in versions.take(3)) {
                val game = GameSession(prior, events, 2, 75, setOf(0))
                if (DecisionLab.enabled(prior)) game.rollSteady() else game.roll()
                val cp = game.checkpoint("previous", epoch)
                val bytes = MatchCheckpointCodec.encode(cp)
                assertEquals(bytes, MatchCheckpointCodec.encode(
                    GameSession.restore(prior, events, cp).checkpoint("previous", epoch)
                ))
                try {
                    GameSession.restore(versions.last(), events, cp)
                    fail("An older checkpoint was silently loaded as Lab v3")
                } catch (_: IllegalArgumentException) { }
            }
        }
    }

    @Test fun shelterChoicesProvideDifferentPredictableBenefitsAndReplay() {
        val supplies = atChoice("low")
        assertEquals(listOf("supplies", "cover"), supplies.availableEncounterChoices())
        try {
            supplies.chooseEncounter("random")
            fail("Unexpected encounter option was accepted")
        } catch (_: IllegalArgumentException) { }
        supplies.chooseEncounter("supplies")
        assertEquals("lab_v3_shelter_supplies", supplies.lastEventId)
        assertEquals(Phase.FINISHED, supplies.phase)
        assertFalse(supplies.players[0].shield)
        assertEquals(supplies.snapshot(), GameSession.restore(
            supplies.board, fixedEvents, supplies.checkpoint("supplies", epoch)
        ).snapshot())

        val protection = atChoice("low")
        protection.chooseEncounter("cover")
        assertEquals("lab_v3_shelter_cover", protection.lastEventId)
        assertTrue(protection.players[0].shield)
        assertEquals(Phase.FINISHED, protection.phase)
        assertEquals(protection.snapshot(), GameSession.restore(
            protection.board, fixedEvents, protection.checkpoint("cover", epoch)
        ).snapshot())
        assertNotEquals(
            MatchCheckpointCodec.encode(supplies.checkpoint("same", epoch)),
            MatchCheckpointCodec.encode(protection.checkpoint("same", epoch))
        )
        assertTrue(protection.checkpoint("cover", epoch).actions.any {
            it.kind == MatchActionKind.ENCOUNTER && it.routeId == "cover"
        })
    }

    @Test fun hazardOffersBraceOrKnownHazardGambleWithoutDuplicateDraw() {
        val safe = atChoice("high")
        assertEquals(listOf("gamble", "brace"), safe.availableEncounterChoices())
        safe.chooseEncounter("brace")
        assertEquals(Phase.ROLL, safe.phase)
        assertEquals(1, safe.currentIndex)
        assertEquals("HAZ", safe.players[0].position)
        assertEquals("lab_v3_hazard_brace", safe.lastEventId)
        assertEquals(safe.snapshot(), GameSession.restore(
            safe.board, fixedEvents, safe.checkpoint("brace", epoch)
        ).snapshot())

        val gamble = atChoice("high")
        gamble.chooseEncounter("gamble")
        assertEquals("rockfall", gamble.lastEventId)
        assertFalse(gamble.snapshot().encounters.isEmpty())
        assertEquals(gamble.snapshot(), GameSession.restore(
            gamble.board, fixedEvents, gamble.checkpoint("hazard", epoch)
        ).snapshot())

        val v2 = GameSession(DecisionLab.variantV2(fixture), fixedEvents,
            2, 14, setOf(0, 1))
        v2.rollSteady()
        v2.chooseRoute("HAZ")
        assertTrue(v2.availableEncounterChoices().isEmpty())
        try {
            v2.chooseEncounter("brace")
            fail("Lab v2 accepted a Lab v3 action")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun v3RacesFinishAndRecoverAcrossCourseSizesAndRacers() {
        for (base in originals) for (seats in 2..4) for (seed in 1..28) {
            val board = DecisionLab.variantV3(base)
            val g = GameSession(board, events, seats, seed, setOf(0))
            var actions = 0
            while (g.phase != Phase.FINISHED && actions++ < 2800) {
                if (g.players[g.currentIndex].isBot) g.botStep()
                else when (g.phase) {
                    Phase.ROLL -> if ((seed + actions) % 3 == 0)
                        g.rollBold() else g.rollSteady()
                    Phase.ROUTE -> g.chooseRoute(g.availableRoutes()[
                        (seed + actions) % g.availableRoutes().size
                    ].id)
                    Phase.ENCOUNTER -> g.chooseEncounter(
                        g.availableEncounterChoices()[
                            (seed + actions) % g.availableEncounterChoices().size
                        ]
                    )
                    Phase.TARGET -> g.chooseTarget(g.availableTargets().first())
                    Phase.FINISHED -> Unit
                }
                if (seed <= 3) {
                    val cp = g.checkpoint("v3-replay", epoch)
                    assertEquals(g.snapshot(), GameSession.restore(board, events, cp).snapshot())
                }
            }
            assertEquals("V3 did not finish ${base.id} $seats seed $seed",
                Phase.FINISHED, g.phase)
            assertEquals(board.finish, g.players[requireNotNull(g.winnerIndex)].position)
        }
    }
}
