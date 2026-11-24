package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

class WovenTerrainTest {
    @Test fun textureIsBoundedAndDistributedAcrossTheBoard() {
        val flecks = WovenTerrainTexture.flecks
        assertEquals(240, flecks.size)
        assertEquals(240, flecks.map { it.x to it.y }.distinct().size)
        assertEquals(setOf(0, 1, 2), flecks.map { it.tone }.toSet())
        flecks.forEach {
            assertTrue(it.x in .005f.. .995f)
            assertTrue(it.y in .005f.. .995f)
            assertTrue(it.size in .001f.. .005f)
        }
        for (col in 0 until 16) {
            assertEquals(15, flecks.count { (it.x * 16).toInt() == col })
        }
        for (row in 0 until 15) {
            assertEquals(16, flecks.count { (it.y * 15).toInt() == row })
        }
    }
}
