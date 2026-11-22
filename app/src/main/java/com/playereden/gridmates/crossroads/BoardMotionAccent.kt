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

    /**
     * A restrained contact glint at the END of each individual graph step.
     *
     * The normal travel animation already animates 0 -> 1 for EACH authored
     * waypoint. This independent visual pulse uses that existing progress;
     * it adds no delays, timers, route nodes, or changes to the actual move.
     * Zero at the endpoints prevents a stale halo when the piece stops.
     */
    fun stepContactPulse(progress: Float, inMotion: Boolean): Float {
        if (!inMotion || !progress.isFinite()) return 0f
        val t = progress.coerceIn(0f, 1f)
        if (t <= 0.72f || t >= 1f) return 0f
        val contactPhase = (t - 0.72f) / 0.28f
        return (sin(contactPhase * PI) * 0.48f).toFloat().coerceAtLeast(0f)
    }

    /** Slight scale overshoot at the destination, fading into the normal piece. */
    fun arrivalScale(glow: Float): Float =
        if (!glow.isFinite()) 1f else 1f + 0.16f * glow.coerceIn(0f, 1f)

    /** The ground ripple should never hide the numbered character badge. */
    fun arrivalRippleAlpha(glow: Float): Float =
        if (!glow.isFinite()) 0f else 0.76f * glow.coerceIn(0f, 1f)
}
