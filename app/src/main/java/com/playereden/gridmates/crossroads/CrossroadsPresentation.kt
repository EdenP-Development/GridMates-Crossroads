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

/** The whole board is visible at the start and finish, with gentler midgame tracking. */
internal object CrossroadsCameraPreset {
    fun followZoom(phase: Phase, turn: Int, animated: Boolean): Float {
        if (!animated || phase == Phase.FINISHED || turn <= 1) return 1.0f
        return if (phase == Phase.ROUTE) 1.15f else 1.28f
    }
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
