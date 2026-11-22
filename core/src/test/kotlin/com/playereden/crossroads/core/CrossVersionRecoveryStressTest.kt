package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * SAVE-78: intentionally interrupt matches at several valid action boundaries.
 *
 * These are six immutable versions of each real authored course, across Solo
 * and Pass & Play, two/four seats and fixed seeds. No artificial game rules,
 * odds, animation or time assumptions are introduced.
 */
class CrossVersionRecoveryStressTest {
    private val root = File(System.getProperty("crossroads.repoDir"))
    private val originals = listOf(
        "woodland_brookside_quick.json",
        "woodland_woven_trail.json",
        "woodland_cedarwood_long.json"
    ).map { GameData.board(File(root, "data/boards/$it").readText()) }
    private val events = GameData.events(
        File(root, "data/events/woven_events_v02.json").readText())
    private val start = 1794931200000L

    private fun variants(base: BoardDefinition) = listOf(
        "original" to base,
        "lab-v1" to DecisionLab.variant(base),
        "lab-v2" to DecisionLab.variantV2(base),
        "lab-v3" to DecisionLab.variantV3(base),
        "lab-v4" to DecisionLab.variantV4(base),
        "lab-v5" to DecisionLab.variantV5(base)
    )

    private fun performOneLegalAction(game: GameSession, seed: Int, tick: Int) {
        if (game.players[game.currentIndex].isBot) {
            game.botStep()
            return
        }
        when (game.phase) {
            Phase.ROLL -> {
                if (!game.playtestRules) game.roll()
                else if ((seed + tick + game.currentIndex) % 4 == 0)
                    game.rollBold()
                else game.rollSteady()
            }
            Phase.ROUTE -> {
                val exits = game.availableRoutes()
                assertTrue("No legal route at an active crossroads", exits.isNotEmpty())
                // Vary decisions without consuming any game-owned RNG.
                game.chooseRoute(exits[(seed + tick) % exits.size].id)
            }
            Phase.TARGET -> {
                val targets = game.availableTargets()
                assertTrue("Target phase without targets", targets.isNotEmpty())
                game.chooseTarget(targets.first())
            }
            Phase.ENCOUNTER -> {
                val options = game.availableEncounterChoices()
                assertTrue("Interactive encounter without choices", options.isNotEmpty())
                val choice = when {
                    "gamble" in options && tick % 3 != 0 -> "gamble"
                    "brace" in options -> "brace"
                    "supplies" in options && tick % 3 != 0 -> "supplies"
                    "cover" in options -> "cover"
                    else -> options.first()
                }
                game.chooseEncounter(choice)
            }
            Phase.FINISHED -> error("Finished race cannot take another action")
        }
    }

    data class Record(
        val baseCourse: String,
        val version: String,
        val seats: Int,
        val mode: String,
        val seed: Int,
        val actions: Int,
        val recoveries: Int,
        val peakCheckpointChars: Int,
        val sawRoute: Boolean,
        val sawTarget: Boolean,
        val sawEncounter: Boolean,
        val sawFinalChance: Boolean
    ) {
        fun csv() = listOf(baseCourse, version, seats, mode, seed, actions,
            recoveries, peakCheckpointChars, sawRoute, sawTarget,
            sawEncounter, sawFinalChance).joinToString(",")
    }

    @Test fun seededMatchesSurviveRepeatedInterruptionsWithIdenticalFuture() {
        val records = mutableListOf<Record>()
        var totalRecoveries = 0
        for (base in originals) {
            for ((version, board) in variants(base)) {
                for (seats in listOf(2, 4)) for (mode in listOf("solo", "pass")) {
                    for (seed in listOf(17, 83)) {
                        val humans = if (mode == "solo") setOf(0)
                            else (0 until seats).toSet()
                        val matchId = "stress-${base.id}-$version-$seats-$mode-$seed"
                        val active = GameSession(board, events, seats, seed, humans)
                        var mirror = GameSession.restore(board, events,
                            active.checkpoint(matchId, start))
                        var recoveries = 1
                        var peakChars = 0
                        var ticks = 0
                        var sawRoute = false
                        var sawTarget = false
                        var sawEncounter = false
                        var sawFinalChance = false
                        while (active.phase != Phase.FINISHED && ticks < 1200) {
                            sawRoute = sawRoute || active.phase == Phase.ROUTE
                            sawTarget = sawTarget || active.phase == Phase.TARGET
                            sawEncounter = sawEncounter || active.phase == Phase.ENCOUNTER
                            sawFinalChance = sawFinalChance || active.finalRoundPending
                            assertEquals("Before action $ticks: $matchId",
                                active.snapshot(), mirror.snapshot())
                            val pendingBefore = active.finalRoundPending
                            performOneLegalAction(active, seed, ticks)
                            performOneLegalAction(mirror, seed, ticks)
                            ticks++
                            assertEquals("RNG/route drift at action $ticks: $matchId",
                                active.snapshot(), mirror.snapshot())
                            if (ticks <= 2 || ticks % 7 == 0 ||
                                active.phase == Phase.ENCOUNTER ||
                                active.phase == Phase.TARGET ||
                                (active.finalRoundPending && !pendingBefore) ||
                                active.phase == Phase.FINISHED
                            ) {
                                val encoded = MatchCheckpointCodec.encode(
                                    active.checkpoint(matchId, start))
                                peakChars = maxOf(peakChars, encoded.length)
                                assertTrue("Checkpoint exceeds accepted payload",
                                    encoded.length <= MatchCheckpointCodec.MAX_CHECKPOINT_CHARS)
                                val decoded = MatchCheckpointCodec.decode(encoded)
                                mirror = GameSession.restore(board, events, decoded)
                                recoveries++
                                assertEquals("Post restore mismatch: $matchId",
                                    active.snapshot(), mirror.snapshot())
                                assertEquals(active.finalRoundPending, mirror.finalRoundPending)
                                assertEquals(active.leadingFinisherName, mirror.leadingFinisherName)
                                assertEquals(active.fairFinishReceipt, mirror.fairFinishReceipt)
                            }
                        }
                        assertEquals("Unfinished $matchId after $ticks commands",
                            Phase.FINISHED, active.phase)
                        assertTrue("Did not exercise the race: $matchId", ticks > 0)
                        assertEquals(active.snapshot(), mirror.snapshot())
                        assertEquals(active.fairFinishReceipt, mirror.fairFinishReceipt)
                        if (version == "lab-v5") {
                            assertNotNull("v5 finish has no receipt: $matchId",
                                active.fairFinishReceipt)
                        } else {
                            assertNull("Old version acquired a v5 receipt: $matchId",
                                active.fairFinishReceipt)
                        }
                        val finished = MatchCheckpointCodec.decode(MatchCheckpointCodec.encode(
                            active.checkpoint(matchId, start)))
                        assertEquals(active.snapshot(),
                            GameSession.restore(board, events, finished).snapshot())
                        totalRecoveries += recoveries
                        records += Record(base.id, version, seats, mode, seed,
                            ticks, recoveries, peakChars, sawRoute, sawTarget,
                            sawEncounter, sawFinalChance)
                    }
                }
            }
        }
        assertEquals(144, records.size)
        assertTrue("No checkpoints were restored", totalRecoveries >= 144)
        assertTrue("No fork decisions were exercised", records.any { it.sawRoute })
        assertTrue("No interactive encounter was exercised", records.any { it.sawEncounter })
        assertTrue("No pending final chance was captured",
            records.filter { it.version == "lab-v5" }.any { it.sawFinalChance })
        // TARGET is event-dependent. Record occurrences rather than invent
        // guaranteed target coverage from stochastic regional card draws.

        val dir = File(root, "core/build/reports/save-78")
        check(dir.exists() || dir.mkdirs())
        File(dir, "cross_version_recovery_samples.csv").writeText(
            "course,version,seats,mode,seed,actions,restores,peak_checkpoint_chars," +
                "saw_route,saw_target,saw_encounter,saw_pending_fair_finish\n" +
                records.joinToString("\n") { it.csv() } + "\n"
        )
        val summary = listOf(
            "SAVE-78 — Repeated checkpoint and replay stress",
            "144 synthetic full matches: 3 boards x 6 immutable versions " +
                "x 2 seat counts (2/4) x Solo/Pass x 2 seeds.",
            "Total successful in-race restores: $totalRecoveries",
            "Total legal actions: ${records.sumOf { it.actions }}",
            "Highest encoded checkpoint characters: ${records.maxOf { it.peakCheckpointChars }}",
            "Matches with a route decision: ${records.count { it.sawRoute }}",
            "Matches with a mischief target: ${records.count { it.sawTarget }}",
            "Matches with an encounter choice: ${records.count { it.sawEncounter }}",
            "Lab v5 matches with pending final chance: " +
                "${records.count { it.version == "lab-v5" && it.sawFinalChance }}",
            "Every restored game produced the same future snapshot and finish winner.",
            "Lab v5 reconstructed the same final-round receipt after each restore.",
            "These JVM runs are NOT actual Android process kills or install-over QA."
        ).joinToString("\n") + "\n"
        File(dir, "cross_version_recovery_summary.txt").writeText(summary)
        println(summary)
    }
}
