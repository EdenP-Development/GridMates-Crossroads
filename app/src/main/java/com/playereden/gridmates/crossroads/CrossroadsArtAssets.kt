package com.playereden.gridmates.crossroads

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.playereden.crossroads.core.DecisionLab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ARTREADY-38: optional packaged artwork, never gameplay data.
 *
 * Put approved PNGs in app/src/main/assets/art/v1/ with EXACT filenames
 * listed here and in the provenance manifest. Missing art is intentional:
 * all existing Compose-drawn scenery, tokens and text remain the fallback.
 *
 * Bitmap decoding happens off the main thread; each visible surface keeps
 * a Compose-owned image and does not decode once per animation frame.
 */
internal enum class CrossroadsArtSlot(val filename: String) {
    HOME_HERO("home_hero.png"),
    BOARD_BROOKSIDE("board_brookside.png"),
    BOARD_WOVEN("board_woven.png"),
    BOARD_CEDARWOOD("board_cedarwood.png"),
    EVENT_SHELTER("event_shelter.png"),
    EVENT_HAZARD("event_hazard.png"),
    EVENT_MISCHIEF("event_mischief.png"),
    RESULT_VICTORY("result_victory.png"),
    TOKEN_SEAT_1("token_seat_1.png"),
    TOKEN_SEAT_2("token_seat_2.png"),
    TOKEN_SEAT_3("token_seat_3.png"),
    TOKEN_SEAT_4("token_seat_4.png");

    val packagedPath: String get() = "art/v1/$filename"
}

internal object CrossroadsArtCatalog {
    /** All experimental versions must use the exact same course artwork. */
    fun backplate(boardId: String): CrossroadsArtSlot? =
        when (DecisionLab.baseId(boardId)) {
            "woodland_brookside_quick_v01" -> CrossroadsArtSlot.BOARD_BROOKSIDE
            "woodland_woven_trail_v02" -> CrossroadsArtSlot.BOARD_WOVEN
            "woodland_cedarwood_long_v01" -> CrossroadsArtSlot.BOARD_CEDARWOOD
            else -> null // Unrecognized courses get existing neutral paper.
        }

    fun seat(index: Int): CrossroadsArtSlot? = when (index) {
        0 -> CrossroadsArtSlot.TOKEN_SEAT_1
        1 -> CrossroadsArtSlot.TOKEN_SEAT_2
        2 -> CrossroadsArtSlot.TOKEN_SEAT_3
        3 -> CrossroadsArtSlot.TOKEN_SEAT_4
        else -> null
    }
}

@Composable
internal fun rememberOptionalCrossroadsArt(slot: CrossroadsArtSlot?): ImageBitmap? {
    val context = LocalContext.current.applicationContext
    val state by produceState<ImageBitmap?>(null, context, slot) {
        value = if (slot == null) null else withContext(Dispatchers.IO) {
            loadOptionalCrossroadsArt(context, slot)
        }
    }
    return state
}

private fun loadOptionalCrossroadsArt(
    context: Context, slot: CrossroadsArtSlot
): ImageBitmap? =
    runCatching {
        context.assets.open(slot.packagedPath).use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    }.getOrNull()
