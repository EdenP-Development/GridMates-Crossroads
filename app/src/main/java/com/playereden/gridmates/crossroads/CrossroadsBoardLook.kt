package com.playereden.gridmates.crossroads

/**
 * Presentation-only board looks. A course chooses the graph and rules;
 * the board look changes only the drawing beneath that authoritative graph.
 *
 * Future seasonal sets should extend this renderer with audited local artwork,
 * NOT fork board IDs, dice, saves, matchmaking or gameplay probabilities.
 */
internal enum class CrossroadsBoardLook(
    val label: String,
    val description: String
) {
    ILLUSTRATED(
        "Illustrated",
        "Use each course's storybook scenery when installed."
    ),
    CLASSIC_MAP(
        "Classic map",
        "Use the original drawn woodland map, even when illustrations are available."
    );

    companion object {
        fun fromStored(value: String?): CrossroadsBoardLook =
            entries.firstOrNull { it.name == value } ?: ILLUSTRATED
    }
}
