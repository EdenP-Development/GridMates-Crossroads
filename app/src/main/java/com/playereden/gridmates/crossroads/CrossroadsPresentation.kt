package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import com.playereden.crossroads.core.Phase
import com.playereden.crossroads.core.DecisionLab

/** Shared, presentation-only labels. Never rename board IDs or saved-game data. */
internal object CrossroadsCourse {
    const val CLASSIC_ID = "woodland_woven_trail_v02"
    const val QUICK_ID = "woodland_brookside_quick_v01"
    const val LONG_ID = "woodland_cedarwood_long_v01"

    fun title(boardId: String): String = when (DecisionLab.baseId(boardId)) {
        QUICK_ID -> "Brookside Dash"
        LONG_ID -> "Cedarwood Expedition"
        CLASSIC_ID -> "Woven Trail"
        else -> "Woodland Crossing"
    }

    fun subtitle(boardId: String): String = if (boardId.endsWith(DecisionLab.V3_SUFFIX))
        "Gameplay 2.0 · Lab v3"
    else if (boardId.endsWith(DecisionLab.V2_SUFFIX))
        "Gameplay 2.0 · Lab v2"
    else if (boardId.endsWith(DecisionLab.SUFFIX))
        "Gameplay 2.0 · Experimental"
    else when (boardId) {
        QUICK_ID -> "Quick race"
        LONG_ID -> "Long race"
        CLASSIC_ID -> "Classic race"
        else -> "Woodland race"
    }

    fun resultLine(boardId: String): String =
        "First across ${title(boardId)}" +
            if (boardId.endsWith(DecisionLab.SUFFIX) ||
                boardId.endsWith(DecisionLab.V2_SUFFIX) ||
                boardId.endsWith(DecisionLab.V3_SUFFIX)) " · Playtest" else ""
}

/**
 * Cinematic camera presets for the active, portrait game world.
 * The old 1.15–1.28x framing made pieces and trails illegible on real phones.
 * Full-map inspection is a separate unanimated 1x canvas, not this camera.
 */
internal object CrossroadsCameraPreset {
    fun followZoom(phase: Phase, turn: Int, animated: Boolean): Float {
        if (!animated || phase == Phase.FINISHED) return 1.0f
        return when (phase) {
            Phase.ROLL -> if (turn <= 1) 2.15f else 2.36f
            Phase.ROUTE -> 2.12f // readable fork and outgoing routes without giant crossings
            Phase.ENCOUNTER -> 2.48f
            Phase.TARGET -> 2.32f
            Phase.FINISHED -> 1.0f
        }
    }

    /** Keep the piece above the context dock, not at the viewport's covered midpoint. */
    fun focusHeightFraction(phase: Phase, immersive: Boolean): Float =
        if (!immersive) 0.50f else when (phase) {
            Phase.ENCOUNTER -> 0.38f
            Phase.TARGET -> 0.40f
            else -> 0.43f
        }

    /** Focus the actual fork first; leave just enough room to preview exits. */
    fun forkAnchor(
        source: Offset,
        exits: List<Offset>,
        immersive: Boolean
    ): Offset {
        if (exits.isEmpty()) return source
        val mean = Offset(exits.map { it.x }.average().toFloat(),
            exits.map { it.y }.average().toFloat())
        val originWeight = if (immersive) 0.79f else 1f / (exits.size + 1f)
        return source * originWeight + mean * (1f - originWeight)
    }

    /**
     * The bottom dock covers part of the world. When a player reaches Start
     * or Finish, let the world extend UNDER that dock instead of clamping the
     * player behind it. No board coordinate or movement is changed.
     */
    fun boundFollowVertical(
        desired: Float,
        viewportHeight: Float,
        worldTop: Float,
        worldBottom: Float,
        zoom: Float,
        phase: Phase,
        immersive: Boolean
    ): Float {
        if (!immersive || viewportHeight <= 0f || zoom <= 1f) {
            return com.playereden.crossroads.core.CameraGeometry.boundTranslation(
                desired, viewportHeight, worldTop, worldBottom, zoom
            )
        }
        val center = viewportHeight * 0.5f
        val scaledTop = center + (worldTop - center) * zoom
        val scaledBottom = center + (worldBottom - center) * zoom
        val safeTop = viewportHeight * 0.18f
        val safeBottom = viewportHeight * when (phase) {
            Phase.ENCOUNTER -> 0.55f
            Phase.TARGET -> 0.57f
            else -> 0.68f
        }
        val min = safeBottom - scaledBottom
        val max = safeTop - scaledTop
        return if (min <= max) desired.coerceIn(min, max)
            else (min + max) * 0.5f
    }

    /** Only the live playfield gains the stronger visual hierarchy. */
    fun trailScale(immersive: Boolean) = if (immersive) 1.58f else 1f
    fun tokenScale(immersive: Boolean) = if (immersive) 1.91f else 1f
    fun markerScale(immersive: Boolean) = if (immersive) 1.55f else 1f
}

/**
 * Original tabletop seating arrangement. Use the *same* offsets in hit-testing
 * and rendering so clustered pieces stay individually identifiable/tappable.
 * Coordinates are pixels in the untransformed board canvas.
 */
internal object CrossroadsPiecePlacement {
    fun offset(rank: Int, count: Int, scale: Float): Offset {
        require(count in 1..4 && rank in 0 until count)
        val spread = 20f * scale
        val lift = -17f * scale
        return when (count) {
            1 -> Offset(0f, -14f * scale)
            2 -> Offset(if (rank == 0) -spread else spread, lift)
            3 -> when (rank) {
                0 -> Offset(-spread, lift)
                1 -> Offset(spread, lift)
                else -> Offset(0f, lift - 34f * scale)
            }
            else -> Offset(
                if (rank % 2 == 0) -spread else spread,
                if (rank < 2) lift else lift - 37f * scale
            )
        }
    }
}
