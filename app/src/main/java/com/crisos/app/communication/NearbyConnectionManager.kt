package com.crisos.app.communication

import android.content.Context
import android.util.Log
import com.crisos.app.domain.model.SosMessage
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages device-to-device communication via Google Nearby Connections API.
 *
 * Uses P2P_CLUSTER strategy for many-to-many connections.
 * Both advertises and discovers simultaneously to find peers.
 */
class NearbyConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "NearbyConn"
        private const val SERVICE_ID = "com.crisos.app.nearby"
        private val STRATEGY = Strategy.P2P_CLUSTER
    }

    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val gson = Gson()

    // Connected endpoint IDs
    private val _connectedEndpoints = MutableStateFlow<Set<String>>(emptySet())
    val connectedEndpoints: StateFlow<Set<String>> = _connectedEndpoints.asStateFlow()

    // Discovered endpoint IDs
    private val _discoveredEndpoints = MutableStateFlow<Map<String, String>>(emptyMap()) // id -> name
    val discoveredEndpoints: StateFlow<Map<String, String>> = _discoveredEndpoints.asStateFlow()

    // Connection state
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    // Last error
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Callback for received messages (message, senderEndpointId)
    var onMessageReceived: ((SosMessage, String) -> Unit)? = null

    // Callback for send confirmations
    var onMessageSent: ((String, Boolean) -> Unit)? = null // messageId, success

    // Callback when an endpoint connects successfully
    var onEndpointConnected: ((String) -> Unit)? = null

    private val localDeviceName: String
        get() = android.os.Build.MODEL

    /**
     * Start advertising and discovering simultaneously.
     */
    fun start() {
        startAdvertising()
        startDiscovery()
        _isActive.value = true
    }

    /**
     * Stop all connections.
     */
    fun stop() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        _connectedEndpoints.value = emptySet()
        _discoveredEndpoints.value = emptyMap()
        _isActive.value = false
    }

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        connectionsClient.startAdvertising(
            localDeviceName,
            SERVICE_ID,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            Log.d(TAG, "Advertising started")
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
            Log.d(TAG, "Discovery started")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Discovery failed", e)
            _lastError.value = "Discovery failed: ${e.message}"
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Found endpoint: $endpointId (${info.endpointName})")
            _discoveredEndpoints.update { it + (endpointId to info.endpointName) }

            // Auto-connect
            connectionsClient.requestConnection(
                localDeviceName,
                endpointId,
                connectionLifecycleCallback
            ).addOnFailureListener { e ->
                Log.e(TAG, "Connection request failed to $endpointId", e)
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Lost endpoint: $endpointId")
            _discoveredEndpoints.update { it - endpointId }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "Connection initiated with: $endpointId (${info.endpointName})")
            // Auto-accept all CriSOS connections
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "Connected to: $endpointId")
                    _connectedEndpoints.update { it + endpointId }
                    onEndpointConnected?.invoke(endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(TAG, "Connection rejected by: $endpointId")
                }
                else -> {
                    Log.d(TAG, "Connection failed with: $endpointId, status: ${result.status}")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from: $endpointId")
            _connectedEndpoints.update { it - endpointId }
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
            // For BYTES payloads, transfer is immediate — no progress tracking needed
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
