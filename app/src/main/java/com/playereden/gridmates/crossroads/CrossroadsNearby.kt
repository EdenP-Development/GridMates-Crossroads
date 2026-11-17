package com.playereden.gridmates.crossroads

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import com.playereden.crossroads.core.CROSSROADS_LAN_VERSION
import java.io.IOException
import java.net.Inet4Address
import java.net.Socket

internal data class NearbyCrossroads(
    val name: String,
    val host: String,
    val port: Int,
    val requiresCode: Boolean
)

/** NSD is only a convenient directory. Manual IPv4 joining remains available. */
internal class CrossroadsNearby(
    context: Context,
    private val onError: (String) -> Unit
) : AutoCloseable {
    private val app = context.applicationContext
    private val manager = app.getSystemService(NsdManager::class.java)
    private val connectivity = app.getSystemService(ConnectivityManager::class.java)
    private val wifi = app.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val multicast = wifi.createMulticastLock("crossroads-nearby").apply {
        setReferenceCounted(false)
    }
    private val handler = Handler(Looper.getMainLooper())
    private val serviceType = "_gridcross._tcp."
    private var advertiser: NsdManager.RegistrationListener? = null
    private var discoverer: NsdManager.DiscoveryListener? = null
    private var generation = 0
    private var pending = ArrayDeque<NsdServiceInfo>()
    private val found = linkedMapOf<String, NearbyCrossroads>()
    private var resolving = false
    private var onRooms: ((List<NearbyCrossroads>) -> Unit)? = null

    fun hasWifi(): Boolean = wifiNetworks().isNotEmpty()
    private fun wifiNetworks(): List<Network> =
        connectivity.allNetworks.filter { network ->
            val caps = connectivity.getNetworkCapabilities(network)
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true &&
                !caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        }

    fun wifiSocket(): Socket =
        wifiNetworks().firstOrNull()?.socketFactory?.createSocket()
            ?: throw IOException("Connect both phones to the same Wi-Fi.")

    fun address(): String =
        wifiNetworks().flatMap { connectivity.getLinkProperties(it)?.linkAddresses.orEmpty() }
            .map { it.address }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress }?.hostAddress ?: "Wi-Fi address unavailable"

    private fun takeMulticast() {
        if (!multicast.isHeld) runCatching { multicast.acquire() }
    }

    fun advertise(port: Int, requiresCode: Boolean) = handler.post {
        stopInternal()
        if (!hasWifi()) { onError("Connect to Wi-Fi before hosting."); return@post }
        try {
            takeMulticast()
            val callback = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(info: NsdServiceInfo) = Unit
                override fun onServiceUnregistered(info: NsdServiceInfo) = Unit
                override fun onRegistrationFailed(info: NsdServiceInfo, code: Int) {
                    onError("Nearby listing is unavailable. Share the address instead.")
                }
                override fun onUnregistrationFailed(info: NsdServiceInfo, code: Int) = Unit
            }
            advertiser = callback
            manager.registerService(NsdServiceInfo().apply {
                serviceName = "Crossroads Woodland"
                serviceType = this@CrossroadsNearby.serviceType
                setPort(port)
                setAttribute("v", CROSSROADS_LAN_VERSION.toString())
                setAttribute("locked", if (requiresCode) "1" else "0")
            }, NsdManager.PROTOCOL_DNS_SD, callback)
        } catch (_: Exception) {
            onError("Nearby listing is unavailable. Share the address instead.")
        }
    }

    fun discover(callback: (List<NearbyCrossroads>) -> Unit) = handler.post {
        stopInternal()
        onRooms = callback
        if (!hasWifi()) { onError("Connect to the same Wi-Fi as the host."); return@post }
        val current = generation
        try {
            takeMulticast()
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) = Unit
                override fun onDiscoveryStopped(serviceType: String) = Unit
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    onError("Nearby search failed. You can join with a host address.")
                }
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
                override fun onServiceFound(info: NsdServiceInfo) { handler.post {
                    if (current == generation && info.serviceType.startsWith("_gridcross._tcp")) {
                        pending.addLast(info); resolveNext(current)
                    }
                } }
                override fun onServiceLost(info: NsdServiceInfo) { handler.post {
                    if (current == generation) {
                        found.remove(info.serviceName)
                        onRooms?.invoke(found.values.toList())
                    }
                } }
            }
            discoverer = listener
            manager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (_: Exception) {
            onError("Nearby search failed. You can join with a host address.")
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveNext(current: Int) {
        if (current != generation || resolving || pending.isEmpty()) return
        resolving = true
        val candidate = pending.removeFirst()
        fun next() {
            if (current == generation) { resolving = false; resolveNext(current) }
        }
        try {
            manager.resolveService(candidate, object : NsdManager.ResolveListener {
                override fun onResolveFailed(info: NsdServiceInfo, code: Int) {
                    handler.post { next() }
                }
                override fun onServiceResolved(info: NsdServiceInfo) { handler.post {
                    if (current == generation) {
                        val address = info.host?.hostAddress
                        val version = info.attributes["v"]?.toString(Charsets.UTF_8)
                        if (!address.isNullOrBlank() && info.port in 1..65535 &&
                            version == CROSSROADS_LAN_VERSION.toString()
                        ) {
                            found[info.serviceName] = NearbyCrossroads(
                                info.serviceName, address, info.port,
                                info.attributes["locked"]?.toString(Charsets.UTF_8) == "1"
                            )
                            onRooms?.invoke(found.values.toList())
                        }
                        next()
                    }
                } }
            })
        } catch (_: Exception) { next() }
    }

    fun stop() = handler.post { stopInternal() }

    private fun stopInternal() {
        generation++
        onRooms = null
        found.clear()
        pending.clear()
        resolving = false
        discoverer?.let { runCatching { manager.stopServiceDiscovery(it) } }
        discoverer = null
        advertiser?.let { runCatching { manager.unregisterService(it) } }
        advertiser = null
        if (multicast.isHeld) runCatching { multicast.release() }
    }

    override fun close() {
        stop()
    }
}
