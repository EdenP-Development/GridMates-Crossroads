package com.playereden.crossroads.core

import java.io.File
import kotlin.math.hypot
import org.junit.Assert.*
import org.junit.Test

class WovenConvergenceGeometryTest {
    @Test fun refinedGeometryPreservesRulesAndReducesLongEdge() {
        val root=File(System.getProperty("crossroads.repoDir"))
        val first=GameData.board(File(root,"design/prototypes/woodland_woven_convergence_v01.json").readText())
        val refined=GameData.board(File(root,"design/prototypes/woodland_woven_convergence_v02.json").readText())
        assertNotEquals(first.id,refined.id)
        assertEquals(first.copy(id=refined.id,nodes=first.nodes.map {
            val other=refined.node(it.id)
            it.copy(x=other.x,y=other.y)
        }),refined)
        assertEquals(setOf("L2","L3","L4"),first.nodes.filter {
            val other=refined.node(it.id)
            it.x!=other.x || it.y!=other.y
        }.map{it.id}.toSet())
        fun length(a:BoardNode,b:BoardNode)=hypot((a.x-b.x).toDouble(),(a.y-b.y).toDouble())
        assertTrue(length(refined.node("L4"),refined.node("F2")) < 0.15)
        val edges=refined.nodes.flatMap { a -> a.next.map { a to refined.node(it) } }
        fun cross(a:BoardNode,b:BoardNode,c:BoardNode)=
            (b.x-a.x)*(c.y-a.y)-(b.y-a.y)*(c.x-a.x)
        for(i in edges.indices) for(j in i+1 until edges.size) {
            val (a,b)=edges[i]
            val (c,d)=edges[j]
            if(setOf(a.id,b.id,c.id,d.id).size<4) continue
            assertFalse("Crossing ${a.id}-${b.id} / ${c.id}-${d.id}",
                cross(a,b,c)*cross(a,b,d)<0 && cross(c,d,a)*cross(c,d,b)<0)
        }
        assertTrue(edges.all { (a,b) -> length(a,b)<0.24 })
        val events=GameData.events(File(root,"data/events/woven_events_v02.json").readText())
        for(seed in 1..24) {
            val original=GameSession(DecisionLab.variantV5(first),events,4,seed,emptySet(),BotPolicyVersion.ADAPTIVE_V2_R2)
            val revised=GameSession(DecisionLab.variantV5(refined),events,4,seed,emptySet(),BotPolicyVersion.ADAPTIVE_V2_R2)
            var actions=0
            while(original.phase!=Phase.FINISHED && actions++<1500) {
                original.botStep(); revised.botStep()
                assertEquals(original.snapshot(),revised.snapshot())
            }
            assertEquals(Phase.FINISHED,original.phase)
        }
    }
}
