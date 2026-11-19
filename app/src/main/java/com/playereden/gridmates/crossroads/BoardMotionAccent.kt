package com.playereden.gridmates.crossroads

import kotlin.math.PI
import kotlin.math.sin

/**
 * Pure visual travel accents; step paths and timing still come from the engine.
 * Each animated hop begins/ends at the exact authoritative graph coordinate.
 */
internal object BoardMotionAccent {
    fun hopOffsetPx(progress: Float, gamePieceScale: Float): Float {
        if (!progress.isFinite() || !gamePieceScale.isFinite() || gamePieceScale <= 0f)
            return 0f
        val t = progress.coerceIn(0f, 1f)
        return (sin(t * PI) * 5.2f * gamePieceScale).toFloat()
    }

    /** Slight scale overshoot at the destination, fading into the normal piece. */
    fun arrivalScale(glow: Float): Float =
        if (!glow.isFinite()) 1f else 1f + 0.16f * glow.coerceIn(0f, 1f)

    /** The ground ripple should never hide the numbered character badge. */
    fun arrivalRippleAlpha(glow: Float): Float =
        if (!glow.isFinite()) 0f else 0.76f * glow.coerceIn(0f, 1f)
}
