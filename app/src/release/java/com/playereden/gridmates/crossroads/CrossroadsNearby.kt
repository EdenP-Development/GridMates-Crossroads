package com.playereden.gridmates.crossroads

import android.content.Context
import java.io.IOException
import java.net.Socket

internal data class NearbyCrossroads(
    val name: String,
    val host: String,
    val port: Int,
    val requiresCode: Boolean
)

/** Public offline build: no Android networking service is accessed. */
internal class CrossroadsNearby(
    @Suppress("UNUSED_PARAMETER") context: Context,
    private val onError: (String) -> Unit
) : AutoCloseable {
    fun hasWifi(): Boolean = false
    fun wifiSocket(): Socket = throw IOException("Nearby is unavailable in this edition.")
    fun address(): String = "Nearby unavailable"
    fun advertise(port: Int, requiresCode: Boolean) { onError("Nearby is unavailable in this edition.") }
    fun discover(callback: (List<NearbyCrossroads>) -> Unit) { callback(emptyList()) }
    fun stop() = Unit
    override fun close() = Unit
}
