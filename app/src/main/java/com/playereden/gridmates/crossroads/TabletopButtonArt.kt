package com.playereden.gridmates.crossroads

import androidx.compose.foundation.Image
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
    // Fit preserves painted corners. Leave existing surface visible outside artwork.
    // Stretchable production surfaces require separate nine-slice review.
    if (image != null) Image(bitmap = image, contentDescription = null,
        modifier = Modifier.matchParentSize(), contentScale = ContentScale.Fit)
}
