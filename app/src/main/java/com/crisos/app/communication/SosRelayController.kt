package com.crisos.app.communication

import android.util.Log
import com.crisos.app.data.repository.SosRepository
import com.crisos.app.domain.model.DeliveryStatus
import com.crisos.app.domain.model.MessageDirection
import com.crisos.app.domain.model.SosMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Result of attempting to process and forward an incoming SOS message.
 */
enum class RelayResult {
    FORWARDED,
    STORED_NO_FORWARD_HOP_LIMIT,
    STORED_NO_FORWARD_EXPIRED,
    STORED_NO_ELIGIBLE_PEERS,
    DUPLICATE_OR_LOOP,
    INVALID_PAYLOAD
}

/**
 * Multi-hop SOS message forwarding controller.
 *
 * Implements store-and-forward relaying over Google Nearby Connections:
 * - Topology: Phone A -> Phone B -> Phone C
 * - Preserves original SOS ID, sender ID, timestamps, and geolocation.
 * - Increments hop count by 1 per relay up to MAX_HOPS (5).
 * - Excludes immediate sender from forwarding targets to prevent 2-node ping-pong.
 * - Thread-safe atomic duplicate and origin loop rejection.
 * - Validates message expiration before forwarding.
 * - Dispatches to each peer independently without blocking.
 */
class SosRelayController(
    private val repository: SosRepository,
    private val localDeviceId: String,
    private val sendToEndpoint: (endpointId: String, message: SosMessage, onResult: (Boolean) -> Unit) -> Unit,
    private val getConnectedEndpoints: () -> Set<String>,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val currentTimeProvider: () -> Long = { System.currentTimeMillis() }
) {
    companion object {
        const val MAX_HOPS = 5
        private const val TAG = "SosRelay"
    }

    // Thread-safe in-memory cache of claimed/processed message IDs to avoid race conditions
    private val processedMessageIds = ConcurrentHashMap.newKeySet<String>()
    private val claimMutex = Mutex()

    /**
     * Mark a locally created message ID as processed so it is never re-processed if received back.
     */
    fun registerLocallyOriginatedMessage(messageId: String) {
        if (messageId.isNotBlank()) {
            processedMessageIds.add(messageId)
        }
    }

    /**
     * Process an incoming SOS message from [senderEndpointId].
     */
    suspend fun handleIncomingMessage(message: SosMessage, senderEndpointId: String): RelayResult {
        // 1. Validate payload
        if (message.id.isBlank()) {
            Log.w(TAG, "[Relay] Dropped invalid SOS message with blank ID from endpoint $senderEndpointId")
            return RelayResult.INVALID_PAYLOAD
        }

        val now = currentTimeProvider()

        // 2. Atomic duplicate detection & loop prevention
        val isDuplicateOrLoop = claimMutex.withLock {
            if (processedMessageIds.contains(message.id)) {
                true
            } else if (repository.messageExists(message.id)) {
                processedMessageIds.add(message.id)
                true
            } else if (message.senderDeviceId.isNotBlank() && message.senderDeviceId == localDeviceId) {
                // Loop back to originating device
                processedMessageIds.add(message.id)
                true
            } else {
                processedMessageIds.add(message.id)
                false
            }
        }

        if (isDuplicateOrLoop) {
            Log.d(TAG, "[Relay] Duplicate or looping SOS ${message.id} from sender $senderEndpointId rejected")
            return RelayResult.DUPLICATE_OR_LOOP
        }

        // 3. Expiry validation
        val isExpired = now > message.expiresAt
        if (isExpired) {
            Log.w(
                TAG,
                "[Relay] SOS ${message.id} from $senderEndpointId is expired " +
                        "(expiresAt=${message.expiresAt}, now=$now). Storing locally without forwarding."
            )
            val expiredMsg = message.copy(
                deliveryStatus = DeliveryStatus.EXPIRED,
                direction = MessageDirection.INCOMING
            )
            repository.saveMessage(expiredMsg)
            return RelayResult.STORED_NO_FORWARD_EXPIRED
        }

        // 4. Persist newly received valid SOS message in local database
        val receivedMsg = message.copy(
            deliveryStatus = DeliveryStatus.RECEIVED,
            direction = MessageDirection.INCOMING
        )
        repository.saveMessage(receivedMsg)
        Log.i(
            TAG,
            "[Relay] Saved received SOS ${message.id} (origin: ${message.senderDeviceId}, hop: ${message.hopCount}) from $senderEndpointId"
        )

        // 5. Hop count limit check
        val effectiveMaxHops = if (message.maxHopCount in 1..MAX_HOPS) message.maxHopCount else MAX_HOPS
        if (message.hopCount >= effectiveMaxHops) {
            Log.i(
                TAG,
                "[Relay] SOS ${message.id} reached maximum hop limit (${message.hopCount}/$effectiveMaxHops). Forwarding halted."
            )
            return RelayResult.STORED_NO_FORWARD_HOP_LIMIT
        }

        // 6. Identify eligible connected peers (strictly excluding immediate sender)
        val connectedPeers = getConnectedEndpoints()
        val eligiblePeers = connectedPeers.filter { it != senderEndpointId }
        if (eligiblePeers.isEmpty()) {
            Log.d(
                TAG,
                "[Relay] SOS ${message.id}: No other connected peers to forward to " +
                        "(connected: $connectedPeers, sender: $senderEndpointId)"
            )
            return RelayResult.STORED_NO_ELIGIBLE_PEERS
        }

        // 7. Prepare forwarded copy with incremented hop count (hopCount + 1)
        val nextHop = message.hopCount + 1
        val forwardedMessage = message.copy(
            hopCount = nextHop,
            maxHopCount = effectiveMaxHops
        )

        Log.i(
            TAG,
            "[Relay] Forwarding SOS ${forwardedMessage.id} (hop $nextHop/$effectiveMaxHops) " +
                    "from $senderEndpointId to ${eligiblePeers.size} peer(s): $eligiblePeers"
        )

        // 8. Forward to each eligible peer independently
        var forwardedAny = false
        for (targetEndpoint in eligiblePeers) {
            try {
                sendToEndpoint(targetEndpoint, forwardedMessage) { success ->
                    if (success) {
                        Log.d(TAG, "[Relay] Successfully sent SOS ${forwardedMessage.id} to peer $targetEndpoint")
                        coroutineScope.launch {
                            repository.updateDeliveryStatus(forwardedMessage.id, DeliveryStatus.RELAYED)
                        }
                    } else {
                        Log.w(TAG, "[Relay] Failed to send SOS ${forwardedMessage.id} to peer $targetEndpoint")
                    }
                }
                forwardedAny = true
            } catch (e: Exception) {
                Log.e(TAG, "[Relay] Error sending SOS ${forwardedMessage.id} to peer $targetEndpoint", e)
            }
        }

        return if (forwardedAny) RelayResult.FORWARDED else RelayResult.STORED_NO_ELIGIBLE_PEERS
    }
}
