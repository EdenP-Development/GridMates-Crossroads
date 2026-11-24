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
    // Key the produced value as well as the effect to avoid showing a stale state.
    val image = androidx.compose.runtime.key(context, filename) {
        val bitmap by produceState<ImageBitmap?>(null, context, filename) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    context.assets.open("art/ui/v1/$filename").use {
                        BitmapFactory.decodeStream(it)?.takeIf { b -> b.width == 384 && b.height == 144 }?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
        bitmap
    }
    // Fit preserves painted corners. Leave existing surface visible outside artwork.
    // Stretchable production surfaces require separate nine-slice review.
    if (image != null) Image(bitmap = image, contentDescription = null,
        modifier = Modifier.matchParentSize(), contentScale = ContentScale.Fit)
}
