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
internal object TabletopButtonArt {
    fun filename(enabled: Boolean, pressed: Boolean): String =
        if (!enabled) "primary_disabled.png" else if (pressed) "primary_pressed.png"
        else "primary_normal.png"
}

@Composable
internal fun BoxScope.OptionalPrimaryButtonArt(enabled: Boolean, pressed: Boolean) {
    val context = LocalContext.current.applicationContext
    val filename = TabletopButtonArt.filename(enabled, pressed)
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
