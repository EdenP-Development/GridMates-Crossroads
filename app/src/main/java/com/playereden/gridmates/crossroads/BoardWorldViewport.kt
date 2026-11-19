package com.playereden.gridmates.crossroads

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.playereden.crossroads.core.BoardNode
import kotlin.math.max

/**
 * The board is a square illustrated world, even when Android is portrait.
 * A full-screen viewport crops that world instead of distorting the image.
 * Route geometry and tap geometry share this same frame.
 */
internal data class BoardSceneFrame(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
) {
    fun position(x: Float, y: Float): Offset =
        Offset(left + x * width, top + y * height)

    fun position(node: BoardNode): Offset = position(node.x, node.y)

    val right: Float get() = left + width
    val bottom: Float get() = top + height
}

internal object BoardWorldViewport {
    fun frame(size: IntSize, immersive: Boolean): BoardSceneFrame {
        if (size.width <= 0 || size.height <= 0) {
            return BoardSceneFrame(0f, 0f, 0f, 0f)
        }
        if (!immersive) {
            // Original board screen and full-map rendering, unchanged.
            return BoardSceneFrame(
                18f, 18f,
                (size.width - 36f).coerceAtLeast(0f),
                (size.height - 36f).coerceAtLeast(0f)
            )
        }
        // Square illustrated 1536x1536 boards crop to the device viewport,
        // like a camera looking into a larger playfield.
        val side = max(size.width, size.height).toFloat()
        return BoardSceneFrame(
            left = (size.width - side) * 0.5f,
            top = (size.height - side) * 0.5f,
            width = side,
            height = side
        )
    }
}
