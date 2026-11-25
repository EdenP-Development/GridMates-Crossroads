package com.playereden.gridmates.crossroads

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class TabletopLabelPaletteTest {
    private fun contrast(a: Color, b: Color): Float {
        val x = a.luminance()
        val y = b.luminance()
        return (maxOf(x, y) + 0.05f) / (minOf(x, y) + 0.05f)
    }

    @Test fun nativeLabelPairsMeetNormalTextContrastTarget() {
        with(TabletopLabelPalette) {
            assertTrue(contrast(lightInk, darkPaper) >= 4.5f)
            assertTrue(contrast(darkInk, lightPaper) >= 4.5f)
            assertTrue(contrast(disabledInk, disabledPaper) >= 4.5f)
        }
    }
}
