package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** V4 is a separately versioned offline experiment. Old actions and saves remain valid. */
class DecisionLabVersion4Test {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val originals = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText()
    )
    private val epoch = 1794931200000L

    private val fixture = BoardDefinition(
        "v4_hazard_fixture", "START", "FINISH",
        listOf(
            BoardNode("START", "meadow", "start", listOf("F"), .0f, .0f),
            BoardNode("F", "meadow", "fork", listOf("HAZ", "SAFE"), .1f, .0f),
            BoardNode("SAFE", "meadow", "plain", listOf("FINISH"), .2f, .4f),
            BoardNode("HAZ", "meadow", "plain", listOf("H1"), .2f, .0f)
        ) + (1..14).map { i ->
            BoardNode("H$i", "meadow", "plain",
                listOf(if (i == 14) "FINISH" else "H${i + 1}"), .2f + i * .01f, .0f)
        } + BoardNode("FINISH", "meadow", "finish", emptyList(), .5f, .2f),
        listOf(ForkDefinition("F", "Test fork", listOf(
            ForkChoice("HAZ", "Risky", "high"),
            ForkChoice("SAFE", "Shelter", "low")
        )))
    )

    private fun fixed(type: String): EventCatalogue = EventCatalogue(listOf(
        EventDefinition("hazard_$type", listOf("meadow"), 1,
            EventEffect(type, if (type == "advance") 1 else 2),
            listOf("hazard"), "Known hazard"),
        EventDefinition("shelter", listOf("meadow"), 1,
            EventEffect("shield", 0), listOf("shelter"), "Known shelter")
    ))

    private fun pending(version: Int, pushed: Boolean, event: EventCatalogue, seed: Int): GameSession {
        val board = if (version == 4) DecisionLab.variantV4(fixture)
                    else DecisionLab.variantV3(fixture)
        val game = GameSession(board, event, 2, seed, setOf(0, 1))
        if (pushed) game.rollBold() else game.rollSteady()
        require(game.phase == Phase.ROUTE) { "Seed $seed did not reach the fork" }
        game.chooseRoute("HAZ")
        require(game.phase == Phase.ENCOUNTER)
        return game
    }

    @Test fun fourthVariantNeverReinterpretsEarlierCheckpoints() {
        for (base in originals) {
            val versions = listOf(base, DecisionLab.variant(base),
                DecisionLab.variantV2(base), DecisionLab.variantV3(base),
                DecisionLab.variantV4(base))
            assertEquals(5, versions.map { MatchCheckpointCodec.fingerprint(it, events) }
                .distinct().size)
            assertEquals(base.id, DecisionLab.baseId(versions.last().id))
            assertTrue(DecisionLab.enabled(versions.last()))
            assertTrue(DecisionLab.hasInteractiveEncounter(versions[3]))
            assertTrue(DecisionLab.hasInteractiveEncounter(versions[4]))
            assertTrue(DecisionLab.supportsSteadyShelterBonus(versions[4]))
            assertTrue(DecisionLab.routePreview(versions[4],
                base.forks.first().choices.first().to, "high", false)
                .contains("successful Gamble: +2 extra"))
            assertFalse(DecisionLab.routePreview(versions[3],
                base.forks.first().choices.first().to, "high", false)
                .contains("successful Gamble: +2 extra"))

            for (prior in versions.take(4)) {
                val game = GameSession(prior, events, 2, 41, setOf(0, 1))
                if (DecisionLab.enabled(prior)) game.rollSteady() else game.roll()
                val checkpoint = game.checkpoint("prior", epoch)
                assertEquals(game.snapshot(),
                    GameSession.restore(prior, events, checkpoint).snapshot())
                try {
                    GameSession.restore(versions.last(), events, checkpoint)
                    fail("Old checkpoint was silently migrated into v4")
                } catch (_: IllegalArgumentException) { }
            }
        }
    }

    @Test fun successfulPushGambleAddsTwoStepsOnlyInV4AndReplays() {
        val catalogue = fixed("advance")
        val seed = (1..100).first { candidate ->
            val game = GameSession(DecisionLab.variantV3(fixture),
                catalogue, 2, candidate, setOf(0, 1))
            game.rollBold()
            game.phase == Phase.ROUTE
        }
        val v3 = pending(3, true, catalogue, seed)
        val v4 = pending(4, true, catalogue, seed)
        assertEquals(0, v3.pendingPushHazardBonus)
        assertEquals(2, v4.pendingPushHazardBonus)
        val waiting = v4.checkpoint("v4-pending", epoch)
        assertEquals(v4.snapshot(),
            GameSession.restore(v4.board, catalogue, waiting).snapshot())

        v3.chooseEncounter("gamble")
        v4.chooseEncounter("gamble")
        assertEquals("V4 adds exactly two spaces on the same success event",
            v3.players[0].traversed.size + 2, v4.players[0].traversed.size)
        assertTrue(v4.lastMessage.contains("Push risk bonus +2"))
        assertFalse(v3.lastMessage.contains("Push risk bonus"))
        assertEquals(v4.snapshot(),
            GameSession.restore(v4.board, catalogue,
                v4.checkpoint("v4-after", epoch)).snapshot())

        val steadyV3 = pending(3, false, catalogue, seed)
        val steadyV4 = pending(4, false, catalogue, seed)
        assertEquals(0, steadyV4.pendingPushHazardBonus)
        steadyV3.chooseEncounter("gamble")
        steadyV4.chooseEncounter("gamble")
        assertEquals(steadyV3.players[0].traversed,
            steadyV4.players[0].traversed)
    }

    @Test fun retreatAndBraceNeverGetTheRewardOrASecondHazardDraw() {
        val catalogue = fixed("retreat")
        val seed = (1..100).first { candidate ->
            val game = GameSession(DecisionLab.variantV3(fixture),
                catalogue, 2, candidate, setOf(0, 1))
            game.rollBold()
            game.phase == Phase.ROUTE
        }
        val v3 = pending(3, true, catalogue, seed)
        val v4 = pending(4, true, catalogue, seed)
        v3.chooseEncounter("gamble")
        v4.chooseEncounter("gamble")
        assertEquals(v3.players[0].position, v4.players[0].position)
        assertEquals(v3.players[0].traversed, v4.players[0].traversed)
        assertFalse(v4.lastMessage.contains("Push risk bonus"))
        val brace = pending(4, true, catalogue, seed)
        brace.chooseEncounter("brace")
        assertEquals("lab_v3_hazard_brace", brace.lastEventId)
        assertTrue(brace.players[0].shield)
        assertEquals(Phase.ROLL, brace.phase)
        assertEquals(brace.snapshot(), GameSession.restore(
            brace.board, catalogue, brace.checkpoint("brace", epoch)).snapshot())
    }
}
