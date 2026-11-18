package com.playereden.crossroads.core

/**
 * Opt-in sandbox with immutable rules identities:
 * v1 matches from QA1312 must replay with exactly v1 mechanics, while a new
 * v2 board ID introduces the experimental shelter/Steady synergy.
 *
 * Neither version changes original board JSON, LAN packets or match records.
 */
object DecisionLab {
    const val SUFFIX = "__decision_lab_v1" // Compatibility: never rename v1.
    const val V2_SUFFIX = "__decision_lab_v2"
    const val V3_SUFFIX = "__decision_lab_v3"

    fun enabled(board: BoardDefinition): Boolean =
        board.id.endsWith(SUFFIX) || board.id.endsWith(V2_SUFFIX) ||
            board.id.endsWith(V3_SUFFIX)

    fun isV2(board: BoardDefinition): Boolean = board.id.endsWith(V2_SUFFIX)

    fun isV3(board: BoardDefinition): Boolean = board.id.endsWith(V3_SUFFIX)

    fun supportsSteadyShelterBonus(board: BoardDefinition): Boolean =
        isV2(board) || isV3(board)

    fun baseId(id: String): String =
        id.removeSuffix(V3_SUFFIX).removeSuffix(V2_SUFFIX).removeSuffix(SUFFIX)

    /** Legacy experimental rules, retained for existing QA1312 checkpoints. */
    fun variant(board: BoardDefinition): BoardDefinition {
        require(!enabled(board)) { "Already a Decision Lab board" }
        return board.copy(id = board.id + SUFFIX)
    }

    /** New experimental rules. Different fingerprint prevents silent replay drift. */
    fun variantV2(board: BoardDefinition): BoardDefinition {
        require(!enabled(board)) { "Already a Decision Lab board" }
        return board.copy(id = board.id + V2_SUFFIX)
    }

    /** Lab v3 adds a response to hazards and shelter visits. */
    fun variantV3(board: BoardDefinition): BoardDefinition {
        require(!enabled(board)) { "Already a Decision Lab board" }
        return board.copy(id = board.id + V3_SUFFIX)
    }

    /** Visible shortest graph distance, not a forecast of dice, bonuses or events. */
    fun minimumSpacesToFinish(board: BoardDefinition, from: String): Int {
        val memo = mutableMapOf<String, Int>()
        fun visit(id: String, seen: Set<String>): Int {
            if (id == board.finish) return 0
            memo[id]?.let { return it }
            require(id !in seen) { "Cyclic course route" }
            val next = board.node(id).next
            require(next.isNotEmpty()) { "Unreachable finish from $id" }
            return (1 + next.minOf { visit(it, seen + id) }).also { memo[id] = it }
        }
        return visit(from, emptySet())
    }

    /** Honesty-first route text; disclose only the known risk and public path graph. */
    fun routePreview(board: BoardDefinition, to: String, risk: String?,
                     steadyChosen: Boolean,
                     encounterAvailable: Boolean = true): String {
        val shortest = minimumSpacesToFinish(board, to)
        // A powerful move can cross more than one fork. The first on-entry
        // encounter uses the turn's single event, so later junctions cannot
        // honestly promise another draw or the Steady bonus.
        if (!encounterAvailable)
            return "Min $shortest spaces left · Encounter already used this turn"
        val encounter = when (risk) {
            "high" -> "Hazard on entry"
            "low" -> "Shelter on entry"
            "medium" -> "Mischief on entry"
            else -> "Encounter unknown"
        }
        val synergy = if (supportsSteadyShelterBonus(board) && risk == "low" && steadyChosen)
            " · Steady trail bonus +1"
        else ""
        val interactive = if (isV3(board) && risk in listOf("low", "high"))
            " · Make an encounter choice" else ""
        return "Min $shortest spaces left · $encounter$synergy$interactive"
    }
}
