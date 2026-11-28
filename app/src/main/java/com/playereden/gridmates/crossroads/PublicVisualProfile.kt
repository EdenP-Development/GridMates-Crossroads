package com.playereden.gridmates.crossroads

/** Release-candidate visual defaults only; not owner acceptance or a rules gate.
 * QA retains A/B controls. Overhead terrain remains a QA experiment.
 */
internal data class PublicVisualProfile(
    val artworkTrails: Boolean,
    val woodlandPieces: Boolean,
    val illustratedMarkers: Boolean
) {
    companion object {
        fun resolve(debug: Boolean, artworkTrails: Boolean, woodlandPieces: Boolean,
                    illustratedMarkers: Boolean): PublicVisualProfile =
            if (debug) PublicVisualProfile(artworkTrails, woodlandPieces, illustratedMarkers)
            else PublicVisualProfile(true, true, true)
    }
}
