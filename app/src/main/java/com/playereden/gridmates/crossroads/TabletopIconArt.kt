package com.playereden.gridmates.crossroads

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal enum class TabletopIconArt(val stem: String) {
    HOME("home"), MULTIPLAYER("multiplayer"), MAP("map"), JOURNAL("journal"),
    SETTINGS("settings"), SOUND("sound"), MUSIC("music"), DICE("dice"),
    ZOOM_IN("zoom_in"), ZOOM_OUT("zoom_out"), EXPAND("expand"), COLLAPSE("collapse");

    val filename: String get() = "icon_$stem.png"
    val packagedPath: String get() = "art/ui/v1/$filename"
    fun validDimensions(width: Int, height: Int): Boolean = width == 128 && height == 128
}

/** Decorative only: parent controls retain their native labels and click semantics. */
@Composable
internal fun OptionalTabletopIcon(
    icon: TabletopIconArt,
    modifier: Modifier,
    fallback: @Composable () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val image = key(context, icon) {
        val bitmap by produceState<ImageBitmap?>(null, context, icon) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    context.assets.open(icon.packagedPath).use { stream ->
                        BitmapFactory.decodeStream(stream)
                            ?.takeIf { icon.validDimensions(it.width, it.height) }
                            ?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
        bitmap
    }
    if (image == null) fallback()
    else Image(bitmap = image, contentDescription = null,
        modifier = modifier, contentScale = ContentScale.Fit)
}
