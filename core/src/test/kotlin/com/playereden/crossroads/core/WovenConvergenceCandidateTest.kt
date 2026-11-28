package com.playereden.crossroads.core

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Candidate identity and content stay distinct from every shipped course. */
class WovenConvergenceCandidateTest {
    @Test fun candidateMatchesMeasuredZeroSpacingVariant() {
        val root = File(System.getProperty("crossroads.repoDir"))
        val original = GameData.board(File(root,"data/boards/woodland_woven_trail.json").readText())
        val candidate = GameData.board(File(root,"design/prototypes/woodland_woven_convergence_v01.json").readText())
        val events = GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        assertNotEquals(original.id,candidate.id)
        assertEquals(original.copy(id=candidate.id,nodes=original.nodes.map {
            if(it.id=="L4") it.copy(next=listOf("F2")) else it
        }),candidate)
        assertEquals(listOf("F3"),original.node("L4").next)
        assertNotEquals(MatchCheckpointCodec.fingerprint(original,events),
            MatchCheckpointCodec.fingerprint(candidate,events))
        val board = DecisionLab.variantV5(candidate)
        var targets = 0
        for(policy in listOf(BotPolicyVersion.LEGACY_V1,BotPolicyVersion.ADAPTIVE_V2_R2,
            BotPolicyVersion.ADAPTIVE_V2_R4)) for(seats in listOf(2,4)) for(seed in 1..24) {
            val game = GameSession(board,events,seats,seed,emptySet(),policy)
            var actions=0
            while(game.phase!=Phase.FINISHED && actions++<1500) {
                if(game.phase==Phase.TARGET && game.players[game.currentIndex].position=="M1") targets++
                game.botStep()
            }
            assertEquals(Phase.FINISHED,game.phase)
            val saved=game.checkpoint("candidate-$seed",1L)
            assertEquals(game.snapshot(),GameSession.restore(board,events,saved).snapshot())
        }
        assertEquals("Measured zero-spacing F2 targeting count",56,targets)
    }
}
