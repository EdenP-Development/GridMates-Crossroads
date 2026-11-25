package com.playereden.gridmates.crossroads

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Only explicitly promoted runtime art is loaded; source library is not bundled. */
internal enum class TabletopArtFamily(val stem: String) {
    PRIMARY("primary"), SECONDARY("secondary")
}

internal object TabletopButtonArt {
    const val SOURCE_WIDTH = 384
    const val SOURCE_HEIGHT = 144
    const val CAP_WIDTH = 72

    // Only expand artwork horizontally; narrow controls retain proportional Fit.
    fun expandedCap(width: Int, height: Int): Int? {
        if (width <= 0 || height <= 0 || width.toLong() * SOURCE_HEIGHT < height.toLong() * SOURCE_WIDTH) return null
        val cap = (height.toDouble() * CAP_WIDTH / SOURCE_HEIGHT).roundToInt()
        return cap.takeIf { it > 0 && 2L * it < width }
    }

    fun filename(enabled: Boolean, pressed: Boolean): String =
        filename(TabletopArtFamily.PRIMARY, enabled, pressed, false)

    fun filename(family: TabletopArtFamily, enabled: Boolean, pressed: Boolean, selected: Boolean): String {
        val state = when {
            !enabled -> "disabled"
            pressed -> "pressed"
            selected -> "highlight"
            else -> "normal"
        }
        return "${family.stem}_$state.png"
    }
}

@Composable
internal fun BoxScope.OptionalPrimaryButtonArt(enabled: Boolean, pressed: Boolean) {
    OptionalTabletopButtonArt(TabletopArtFamily.PRIMARY, enabled, pressed)
}

@Composable
internal fun BoxScope.OptionalTabletopButtonArt(
    family: TabletopArtFamily,
    enabled: Boolean = true,
    pressed: Boolean = false,
    selected: Boolean = false
) {
    val context = LocalContext.current.applicationContext
    val filename = TabletopButtonArt.filename(family, enabled, pressed, selected)
    // Load a family's complete state set once, rather than decoding during a press.
    val images = androidx.compose.runtime.key(context, family) {
        val bitmaps by produceState<Map<String, ImageBitmap>>(emptyMap(), context, family) {
            value = withContext(Dispatchers.IO) {
                listOf("normal", "highlight", "pressed", "disabled").mapNotNull { state ->
                    val name = "${family.stem}_$state.png"
                    val bitmap = runCatching {
                        context.assets.open("art/ui/v1/$name").use {
                            BitmapFactory.decodeStream(it)
                                ?.takeIf { b -> b.width == 384 && b.height == 144 }
                                ?.asImageBitmap()
                        }
                    }.getOrNull()
                    bitmap?.let { name to it }
                }.toMap()
            }
        }
        bitmaps
    }
    val image = images[filename]
    // QA preview: preserve the end caps at the same scale as the image height.
    // Only the middle band expands; narrow controls retain proportional Fit.
    if (image != null) BoxWithConstraints(Modifier.matchParentSize()) {
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val cap = TabletopButtonArt.expandedCap(w, h)
        if (cap == null) {
            Image(bitmap = image, contentDescription = null,
                modifier = Modifier.matchParentSize(), contentScale = ContentScale.Fit)
        } else {
            Canvas(Modifier.matchParentSize()) {
                val sw = TabletopButtonArt.SOURCE_WIDTH
                val sh = TabletopButtonArt.SOURCE_HEIGHT
                val c = TabletopButtonArt.CAP_WIDTH
                val source = intArrayOf(0, c, sw - c, sw)
                val dest = intArrayOf(0, cap, w - cap, w)
                for (i in 0..2) {
                    drawImage(image,
                        srcOffset = IntOffset(source[i], 0),
                        srcSize = IntSize(source[i + 1] - source[i], sh),
                        dstOffset = IntOffset(dest[i], 0),
                        dstSize = IntSize(dest[i + 1] - dest[i], h),
                        filterQuality = FilterQuality.Medium)
                }
            }
        }
    }
}
