package com.playereden.crossroads.core

/** Only legal, visible opponent state and the already-drawn prank magnitude. */
internal data class BotTargetView(val seat: Int, val distance: Int,
    val shield: Boolean, val afterDistance: Int)

/** Frozen R4: prefer effective setbacks, with a stronger emphasis on imminent finishers. */
internal object AdaptiveBotTargetR4 {
    fun choose(candidates: List<BotTargetView>): Int? {
        val nearest = candidates.minOfOrNull { it.distance } ?: return null
        fun score(target: BotTargetView): Double {
            val setback = (target.afterDistance - target.distance).coerceAtLeast(0)
            val effect = if (target.shield) 0.75 else setback.toDouble()
            val gap = target.distance - nearest
            val urgency = if (target.distance <= 6) 1.5 else 1.0
            return effect * urgency - gap * 0.65
        }
        return candidates.sortedWith(compareByDescending<BotTargetView> { score(it) }
            .thenBy { it.distance }.thenBy { it.seat }).first().seat
    }
}
