package com.playereden.gridmates.crossroads

import androidx.compose.ui.unit.IntOffset
import org.junit.Assert.*
import org.junit.Test

class CrossroadsEnvironmentArtTest {
    @Test fun allSpriteCellsAreUniqueAndInsideTheAtlas() {
        val sprites = EnvironmentSprite.entries
        assertEquals(12, sprites.size)
        assertEquals(12, sprites.map { it.cell }.distinct().size)
        sprites.forEach {
            val point = EnvironmentAtlas.source(it)
            assertTrue(point.x >= 0 && point.y >= 0)
            assertTrue(point.x + EnvironmentAtlas.CELL <= EnvironmentAtlas.SIZE)
            assertTrue(point.y + EnvironmentAtlas.CELL <= EnvironmentAtlas.SIZE)
            assertTrue(it.diameter in .04f.. .09f)
        }
        assertEquals(IntOffset(0, 0), EnvironmentAtlas.source(EnvironmentSprite.OAK))
        assertEquals(IntOffset(768, 512), EnvironmentAtlas.source(EnvironmentSprite.REEDS))
        assertTrue(EnvironmentAtlas.validDimensions(1024, 1024))
        assertFalse(EnvironmentAtlas.validDimensions(1024, 512))
        assertFalse(EnvironmentAtlas.validDimensions(512, 512))
    }

    @Test fun everyExistingDecorationHasADistinctSafeSprite() {
        val sprites = OverheadDecoration.entries.map(EnvironmentAtlas::spriteFor)
        assertEquals(7, sprites.distinct().size)
        assertEquals(EnvironmentSprite.MEADOW_CLEARING,
            EnvironmentAtlas.spriteFor(OverheadDecoration.MEADOW_BLOOM))
        assertEquals(EnvironmentSprite.FIR_GROVE,
            EnvironmentAtlas.spriteFor(OverheadDecoration.FIR_GROVE))
        assertEquals(EnvironmentSprite.ORCHARD,
            EnvironmentAtlas.spriteFor(OverheadDecoration.ORCHARD))
        // Square corners must remain inside even the smaller graph clearance.
        sprites.forEach {
            assertTrue(it.diameter * kotlin.math.sqrt(2f) / 2f < .067f)
        }
    }
}
