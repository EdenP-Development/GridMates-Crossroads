package com.playereden.crossroads.core

/** Read-only, deterministic race assessment. No RNG or event outcome access. */
internal enum class BotRaceStage { OPENING, MIDGAME, ENDGAME }
internal enum class BotRaceStanding { LEADING, TIED, TRAILING, UNCONTESTED }

internal data class BotRacerView(val seat: Int, val distance: Int, val shield: Boolean)
internal data class BotRaceAwareness(
    val own: BotRacerView,
    val opponents: List<BotRacerView>,
    val nearestOpponent: BotRacerView?,
    val gapToLeader: Int?,
    val standing: BotRaceStanding,
    val stage: BotRaceStage,
    val finalRoundPending: Boolean
)

internal object BotRaceAssessment {
    fun assess(
        ownSeat: Int,
        racers: List<BotRacerView>,
        courseDistance: Int,
        finalRoundPending: Boolean
    ): BotRaceAwareness {
        require(racers.map { it.seat }.distinct().size == racers.size)
        require(racers.all { it.distance >= 0 } && courseDistance > 0)
        val own = racers.single { it.seat == ownSeat }
        val opponents = racers.filter { it.seat != ownSeat }
            .sortedWith(compareBy<BotRacerView> { it.distance }.thenBy { it.seat })
        val nearest = opponents.firstOrNull()
        val gap = nearest?.let { own.distance - it.distance }
        val standing = when {
            gap == null -> BotRaceStanding.UNCONTESTED
            gap < 0 -> BotRaceStanding.LEADING
            gap == 0 -> BotRaceStanding.TIED
            else -> BotRaceStanding.TRAILING
        }
        val frontDistance = minOf(own.distance, nearest?.distance ?: own.distance)
        // Endgame is a race-wide condition, even for a distant trailing player.
        // Thresholds are preliminary policy inputs, not changes to game rules.
        val stage = when {
            finalRoundPending || frontDistance <= maxOf(6, courseDistance / 5) ->
                BotRaceStage.ENDGAME
            frontDistance.toLong() * 4 >= courseDistance.toLong() * 3 ->
                BotRaceStage.OPENING
            else -> BotRaceStage.MIDGAME
        }
        return BotRaceAwareness(own, opponents, nearest, gap, standing, stage, finalRoundPending)
    }
}
