package com.crisos.app.communication

import android.content.Context
import android.util.Log
import com.crisos.app.domain.model.SosMessage
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages device-to-device communication via Google Nearby Connections API.
 *
 * Uses P2P_CLUSTER strategy for many-to-many mesh connections.
 * Both advertises and discovers simultaneously to find peers.
 * Filters out self-discovered BLE loopbacks (e.g., on Vivo/MediaTek hardware).
 */
class NearbyConnectionManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {

    companion object {
        private const val TAG = "NearbyConn"
        private const val SERVICE_ID = "com.crisos.app.nearby"
        private val STRATEGY = Strategy.P2P_CLUSTER
        private const val MONITOR_CHECK_INTERVAL_MS = 6000L
        private const val RESCAN_INTERVAL_MS = 24000L
    }

    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val gson = Gson()

    // Connected endpoint IDs
    private val _connectedEndpoints = MutableStateFlow<Set<String>>(emptySet())
    val connectedEndpoints: StateFlow<Set<String>> = _connectedEndpoints.asStateFlow()

    // Discovered endpoint IDs -> names
    private val _discoveredEndpoints = MutableStateFlow<Map<String, String>>(emptyMap())
    val discoveredEndpoints: StateFlow<Map<String, String>> = _discoveredEndpoints.asStateFlow()

    // Connected endpoint IDs -> names for unambiguous UI representation
    private val _connectedPeerNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val connectedPeerNames: StateFlow<Map<String, String>> = _connectedPeerNames.asStateFlow()

    // In-flight connection requests to prevent simultaneous handshake storms
    private val pendingConnectionEndpoints = ConcurrentHashMap.newKeySet<String>()

    // Connection state
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    // Last error
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Background continuous monitor job
    private var monitorJob: Job? = null
    private var lastRescanTimestamp = 0L

    // Callback for received messages (message, senderEndpointId)
    var onMessageReceived: ((SosMessage, String) -> Unit)? = null

    // Callback for send confirmations
    var onMessageSent: ((String, Boolean) -> Unit)? = null // messageId, success

    // Callback when an endpoint connects successfully
    var onEndpointConnected: ((String) -> Unit)? = null

    // Device identification to filter out self-advertisements on devices with non-filtering BLE controllers
    val localDeviceId: String = DeviceUtils.getDeviceId(context)
    private val localModelName: String = android.os.Build.MODEL.ifBlank { "CriSOS Peer" }

    // Wire format: "ModelName::DeviceId"
    private val localAdvertisingName: String = "$localModelName::$localDeviceId"

    private fun parseEndpointInfo(rawName: String): Pair<String, String> {
        val parts = rawName.split("::")
        return if (parts.size >= 2) {
            val model = parts[0].ifBlank { "Nearby Peer" }
            val id = parts[1]
            Pair(model, id)
        } else {
            Pair(rawName.ifBlank { "Nearby Peer" }, "")
        }
    }

    private fun isSelfEndpoint(rawName: String): Boolean {
        val (_, peerDeviceId) = parseEndpointInfo(rawName)
        return peerDeviceId.isNotBlank() && peerDeviceId == localDeviceId
    }

    private fun getDisplayName(rawName: String): String {
        val (model, _) = parseEndpointInfo(rawName)
        return model
    }

    /**
     * Start advertising, discovering, and continuous connection monitoring.
     */
    fun start() {
        if (_isActive.value) return
        _isActive.value = true
        lastRescanTimestamp = System.currentTimeMillis()
        startAdvertising()
        startDiscovery()
        startContinuousMonitoring()
        Log.i(TAG, "CriSOS Nearby networking started ($localAdvertisingName)")
    }

    /**
     * Stop all connections, advertising, and discovery.
     */
    fun stop() {
        monitorJob?.cancel()
        monitorJob = null
        try {
            connectionsClient.stopAdvertising()
            connectionsClient.stopDiscovery()
            connectionsClient.stopAllEndpoints()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Nearby client", e)
        }
        pendingConnectionEndpoints.clear()
        _connectedEndpoints.value = emptySet()
        _discoveredEndpoints.value = emptyMap()
        _connectedPeerNames.value = emptyMap()
        _isActive.value = false
        Log.i(TAG, "CriSOS Nearby networking stopped")
    }

    /**
     * Trigger immediate re-scan and reconnect attempt for discovered peers.
     */
    fun rescanNow() {
        if (!_isActive.value) return
        Log.d(TAG, "Manual re-scan requested")
        lastRescanTimestamp = System.currentTimeMillis()
        restartDiscovery()
        checkAndConnectUnconnectedPeers()
    }

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        connectionsClient.startAdvertising(
            localAdvertisingName,
            SERVICE_ID,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            Log.d(TAG, "Advertising started successfully as $localAdvertisingName")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Advertising failed", e)
            _lastError.value = "Advertising failed: ${e.message}"
        }
    }

    private fun startDiscovery() {
        val options = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            options
        ).addOnSuccessListener {
            Log.d(TAG, "Discovery started successfully")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Discovery failed", e)
            _lastError.value = "Discovery failed: ${e.message}"
        }
    }

    private fun restartDiscovery() {
        try {
            connectionsClient.stopDiscovery()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop discovery during restart", e)
        }
        coroutineScope.launch {
            delay(500L)
            if (_isActive.value) {
                startDiscovery()
            }
        }
    }

    /**
     * Continuously checks for unconnected discovered devices and triggers reconnection.
     * Also refreshes discovery if no peers are connected to keep BLE/WiFi radios active.
     */
    private fun startContinuousMonitoring() {
        monitorJob?.cancel()
        monitorJob = coroutineScope.launch {
            while (isActive) {
                delay(MONITOR_CHECK_INTERVAL_MS)
                if (!_isActive.value) break

                try {
                    // Check discovered devices and initiate connection if not connected
                    checkAndConnectUnconnectedPeers()

                    // If zero peers are connected, refresh discovery periodically to unstick radio scans
                    val now = System.currentTimeMillis()
                    if (_connectedEndpoints.value.isEmpty() && (now - lastRescanTimestamp) >= RESCAN_INTERVAL_MS) {
                        Log.d(TAG, "No peers connected; refreshing discovery scan to keep radio active")
                        lastRescanTimestamp = now
                        restartDiscovery()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception in continuous monitoring loop", e)
                }
            }
        }
    }

    private fun checkAndConnectUnconnectedPeers() {
        val connected = _connectedEndpoints.value
        val discovered = _discoveredEndpoints.value

        for ((endpointId, rawName) in discovered) {
            if (isSelfEndpoint(rawName)) continue

            if (!connected.contains(endpointId) && !pendingConnectionEndpoints.contains(endpointId)) {
                Log.d(TAG, "Attempting connection to discovered peer: $endpointId ($rawName)")
                connectToEndpoint(endpointId)
            }
        }
    }

    fun connectToEndpoint(endpointId: String) {
        if (_connectedEndpoints.value.contains(endpointId)) {
            return
        }
        val rawName = _discoveredEndpoints.value[endpointId] ?: ""
        if (isSelfEndpoint(rawName)) {
            Log.d(TAG, "Preventing connection to self-discovered endpoint: $endpointId")
            return
        }
        if (!pendingConnectionEndpoints.add(endpointId)) {
            return // Connection already in progress
        }

        Log.d(TAG, "Requesting connection to $endpointId from $localAdvertisingName")
        connectionsClient.requestConnection(
            localAdvertisingName,
            endpointId,
            connectionLifecycleCallback
        ).addOnSuccessListener {
            Log.d(TAG, "Connection request sent to $endpointId")
        }.addOnFailureListener { e ->
            pendingConnectionEndpoints.remove(endpointId)
            Log.w(TAG, "Connection request failed to $endpointId: ${e.message}")

            // If already connected at API level, register as connected
            if (e is ApiException && e.statusCode == ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT) {
                markEndpointConnected(endpointId)
            }
        }
    }

    private fun markEndpointConnected(endpointId: String) {
        val rawName = _discoveredEndpoints.value[endpointId] ?: "Nearby Device"
        if (isSelfEndpoint(rawName)) {
            Log.w(TAG, "Ignoring self-loopback mark connected: $endpointId")
            return
        }
        val friendlyName = getDisplayName(rawName)
        _connectedEndpoints.update { it + endpointId }
        _connectedPeerNames.update { it + (endpointId to friendlyName) }
        onEndpointConnected?.invoke(endpointId)
        Log.i(TAG, "Endpoint successfully connected: $endpointId ($friendlyName)")
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            if (isSelfEndpoint(info.endpointName)) {
                Log.d(TAG, "Ignoring self-discovered BLE beacon from own device: $endpointId")
                return
            }
            val friendlyName = getDisplayName(info.endpointName)
            Log.d(TAG, "Discovered peer endpoint: $endpointId ($friendlyName)")
            _discoveredEndpoints.update { it + (endpointId to info.endpointName) }
            // Immediately initiate connection
            connectToEndpoint(endpointId)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Lost discovery beacon for endpoint: $endpointId")
            // Note: Keep in _discoveredEndpoints if connected to preserve peer name,
            // only prune if not connected.
            if (!_connectedEndpoints.value.contains(endpointId)) {
                _discoveredEndpoints.update { it - endpointId }
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            if (isSelfEndpoint(info.endpointName)) {
                Log.w(TAG, "Rejecting self-connection loopback from own device: $endpointId")
                connectionsClient.rejectConnection(endpointId)
                pendingConnectionEndpoints.remove(endpointId)
                return
            }

            val friendlyName = getDisplayName(info.endpointName)
            Log.d(TAG, "Connection initiated with: $endpointId ($friendlyName)")
            // Store peer name from handshake info
            _discoveredEndpoints.update { current ->
                if (!current.containsKey(endpointId)) current + (endpointId to info.endpointName) else current
            }

            // Auto-accept connection
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    Log.d(TAG, "Accepted connection from $endpointId ($friendlyName)")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to accept connection from $endpointId", e)
                    pendingConnectionEndpoints.remove(endpointId)
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            pendingConnectionEndpoints.remove(endpointId)

            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK,
                ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT -> {
                    markEndpointConnected(endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.w(TAG, "Connection rejected by: $endpointId")
                }
                else -> {
                    Log.w(TAG, "Connection attempt ended with status: ${result.status.statusCode} (${result.status}) for $endpointId")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from: $endpointId")
            pendingConnectionEndpoints.remove(endpointId)
            _connectedEndpoints.update { it - endpointId }
            _connectedPeerNames.update { it - endpointId }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val bytes = payload.asBytes() ?: return
                try {
                    val json = String(bytes, Charsets.UTF_8)
                    val message = gson.fromJson(json, SosMessage::class.java)
                    Log.d(TAG, "Received SOS message: ${message.id} from $endpointId (hop ${message.hopCount})")
                    onMessageReceived?.invoke(message, endpointId)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse received payload from $endpointId", e)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // BYTES payloads transfer immediately
        }
    }

    /**
     * Send an SOS message to all connected endpoints.
     */
    fun sendMessage(message: SosMessage, onResult: ((Boolean) -> Unit)? = null) {
        val json = gson.toJson(message)
        val payload = Payload.fromBytes(json.toByteArray(Charsets.UTF_8))

        val endpoints = _connectedEndpoints.value
        if (endpoints.isEmpty()) {
            Log.w(TAG, "No connected endpoints — message queued locally")
            onMessageSent?.invoke(message.id, false)
            onResult?.invoke(false)
            return
        }

        connectionsClient.sendPayload(endpoints.toList(), payload)
            .addOnSuccessListener {
                Log.d(TAG, "Message ${message.id} sent to ${endpoints.size} endpoint(s)")
                onMessageSent?.invoke(message.id, true)
                onResult?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send message ${message.id}", e)
                onMessageSent?.invoke(message.id, false)
                onResult?.invoke(false)
            }
    }

    /**
     * Send an SOS message to a specific connected endpoint.
     */
    fun sendMessageToEndpoint(endpointId: String, message: SosMessage, onResult: ((Boolean) -> Unit)? = null) {
        val json = gson.toJson(message)
        val payload = Payload.fromBytes(json.toByteArray(Charsets.UTF_8))

        connectionsClient.sendPayload(endpointId, payload)
            .addOnSuccessListener {
                Log.d(TAG, "Message ${message.id} sent to $endpointId")
                onMessageSent?.invoke(message.id, true)
                onResult?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send message ${message.id} to $endpointId", e)
                onMessageSent?.invoke(message.id, false)
                onResult?.invoke(false)
            }
    }
}
