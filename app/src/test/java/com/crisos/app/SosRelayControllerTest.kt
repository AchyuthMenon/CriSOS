package com.crisos.app

import com.crisos.app.communication.RelayResult
import com.crisos.app.communication.SosRelayController
import com.crisos.app.data.local.SosMessageDao
import com.crisos.app.data.local.SosMessageEntity
import com.crisos.app.data.repository.SosRepository
import com.crisos.app.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory test DAO implementation for testing repository without Android instrumentation.
 */
class FakeSosMessageDao : SosMessageDao {
    val messages = ConcurrentHashMap<String, SosMessageEntity>()

    override suspend fun insert(message: SosMessageEntity) {
        messages[message.id] = message
    }

    override suspend fun update(message: SosMessageEntity) {
        messages[message.id] = message
    }

    override suspend fun delete(message: SosMessageEntity) {
        messages.remove(message.id)
    }

    override suspend fun messageExists(messageId: String): Boolean =
        messages.containsKey(messageId)

    override suspend fun getMessageByIdOnce(messageId: String): SosMessageEntity? =
        messages[messageId]

    override suspend fun updateDeliveryStatus(messageId: String, status: String) {
        messages[messageId]?.let {
            messages[messageId] = it.copy(deliveryStatus = status)
        }
    }

    override suspend fun getPendingOutgoingMessagesOnce(): List<SosMessageEntity> =
        messages.values.filter {
            it.direction == "OUTGOING" && (it.deliveryStatus == "QUEUED" || it.deliveryStatus == "FAILED")
        }

    override suspend fun getPendingCloudSyncMessages(): List<SosMessageEntity> =
        messages.values.filter { it.cloudSyncStatus == "PENDING" || it.cloudSyncStatus == "FAILED" }

    override suspend fun updateCloudSyncStatus(messageId: String, status: String, syncedAt: Long?) {
        messages[messageId]?.let {
            messages[messageId] = it.copy(cloudSyncStatus = status, cloudSyncedAt = syncedAt)
        }
    }

    override fun getPendingCloudSyncCount(): Flow<Int> =
        flowOf(messages.values.count { it.cloudSyncStatus == "PENDING" || it.cloudSyncStatus == "FAILED" })

    override fun getSyncedCloudCount(): Flow<Int> =
        flowOf(messages.values.count { it.cloudSyncStatus == "SYNCED" })

    override fun getAllMessages(): Flow<List<SosMessageEntity>> = flowOf(messages.values.toList())
    override fun getSentMessages(): Flow<List<SosMessageEntity>> =
        flowOf(messages.values.filter { it.direction == "OUTGOING" })
    override fun getReceivedMessages(): Flow<List<SosMessageEntity>> =
        flowOf(messages.values.filter { it.direction == "INCOMING" })
    override fun getMessageById(messageId: String): Flow<SosMessageEntity?> =
        flowOf(messages[messageId])
    override fun getPendingMessages(): Flow<List<SosMessageEntity>> = flowOf(emptyList())
    override fun getMessageCount(): Flow<Int> = flowOf(messages.size)
    override fun getReceivedCount(): Flow<Int> =
        flowOf(messages.values.count { it.direction == "INCOMING" })
}

@OptIn(ExperimentalCoroutinesApi::class)
class SosRelayControllerTest {

    private lateinit var fakeDao: FakeSosMessageDao
    private lateinit var repository: SosRepository
    private var simulatedTime: Long = 1000000L

    @Before
    fun setUp() {
        fakeDao = FakeSosMessageDao()
        repository = SosRepository(fakeDao)
        simulatedTime = 1000000L
    }

    private fun createSampleSos(
        id: String = "msg-1",
        senderDeviceId: String = "device-A",
        hopCount: Int = 0,
        maxHopCount: Int = 5,
        expiresAt: Long = simulatedTime + 3600000L
    ): SosMessage = SosMessage(
        id = id,
        senderDeviceId = senderDeviceId,
        emergencyCategory = EmergencyCategory.FLOOD,
        description = "Need evacuation",
        assistanceTypes = listOf(AssistanceType.RESCUE),
        latitude = 12.97,
        longitude = 77.59,
        locationAccuracyMeters = 5f,
        locationTimestamp = simulatedTime,
        createdAt = simulatedTime,
        expiresAt = expiresAt,
        hopCount = hopCount,
        maxHopCount = maxHopCount,
        deliveryStatus = DeliveryStatus.QUEUED,
        direction = MessageDirection.OUTGOING
    )

    // 1. Multi-hop: A -> B -> C, with no direct A-C connection. C must receive the same SOS ID.
    @Test
    fun testMultiHopRelayPreservesSosIdAndIncrementsHop() = runTest {
        val forwardedMessagesToC = mutableListOf<SosMessage>()

        // Phone B
        val relayB = SosRelayController(
            repository = repository,
            localDeviceId = "device-B",
            sendToEndpoint = { endpointId, message, onResult ->
                if (endpointId == "endpoint-C") {
                    forwardedMessagesToC.add(message)
                }
                onResult(true)
            },
            getConnectedEndpoints = { setOf("endpoint-A", "endpoint-C") },
            currentTimeProvider = { simulatedTime }
        )

        val originSos = createSampleSos(id = "sos-alpha", senderDeviceId = "device-A", hopCount = 0)
        val result = relayB.handleIncomingMessage(originSos, senderEndpointId = "endpoint-A")

        assertEquals(RelayResult.FORWARDED, result)
        assertEquals(1, forwardedMessagesToC.size)

        val forwardedToC = forwardedMessagesToC[0]
        assertEquals("sos-alpha", forwardedToC.id)
        assertEquals("device-A", forwardedToC.senderDeviceId)
        assertEquals(1, forwardedToC.hopCount) // Hop incremented 0 -> 1
        assertEquals(5, forwardedToC.maxHopCount)
        assertEquals(originSos.latitude, forwardedToC.latitude)
        assertEquals(originSos.description, forwardedToC.description)

        // Verify Phone B saved it locally
        val storedInB = repository.getMessageByIdOnce("sos-alpha")
        assertNotNull(storedInB)
        assertEquals("sos-alpha", storedInB?.id)
        assertEquals(0, storedInB?.hopCount) // Stored at hop 0
    }

    // 2. Multiple peers: B forwards an incoming SOS to all eligible peers except the sender.
    @Test
    fun testForwardsToAllEligiblePeersExcludingSender() = runTest {
        val dispatchedTargets = mutableListOf<String>()

        val relay = SosRelayController(
            repository = repository,
            localDeviceId = "device-B",
            sendToEndpoint = { endpointId, _, onResult ->
                dispatchedTargets.add(endpointId)
                onResult(true)
            },
            getConnectedEndpoints = { setOf("peer-sender", "peer-D", "peer-E") },
            currentTimeProvider = { simulatedTime }
        )

        val msg = createSampleSos()
        val result = relay.handleIncomingMessage(msg, senderEndpointId = "peer-sender")

        assertEquals(RelayResult.FORWARDED, result)
        assertEquals(2, dispatchedTargets.size)
        assertTrue(dispatchedTargets.contains("peer-D"))
        assertTrue(dispatchedTargets.contains("peer-E"))
        assertFalse(dispatchedTargets.contains("peer-sender"))
    }

    // 3. Duplicates: Repeated receipt of the same ID causes only one processing/relay operation.
    @Test
    fun testDuplicateReceiptIgnored() = runTest {
        var forwardCallCount = 0

        val relay = SosRelayController(
            repository = repository,
            localDeviceId = "device-B",
            sendToEndpoint = { _, _, onResult ->
                forwardCallCount++
                onResult(true)
            },
            getConnectedEndpoints = { setOf("peer-A", "peer-C") },
            currentTimeProvider = { simulatedTime }
        )

        val msg = createSampleSos(id = "sos-dup")

        // First receipt from peer-A
        val res1 = relay.handleIncomingMessage(msg, senderEndpointId = "peer-A")
        assertEquals(RelayResult.FORWARDED, res1)
        assertEquals(1, forwardCallCount)

        // Second receipt (same ID) from peer-A
        val res2 = relay.handleIncomingMessage(msg, senderEndpointId = "peer-A")
        assertEquals(RelayResult.DUPLICATE_OR_LOOP, res2)

        // Third receipt (same ID) from peer-C
        val res3 = relay.handleIncomingMessage(msg, senderEndpointId = "peer-C")
        assertEquals(RelayResult.DUPLICATE_OR_LOOP, res3)

        // Forwarding count should STILL be 1
        assertEquals(1, forwardCallCount)
    }

    // 4. Loops: A returning SOS is not forwarded repeatedly.
    @Test
    fun testReturningSosToOriginNotRelayed() = runTest {
        var forwardCallCount = 0

        val relayA = SosRelayController(
            repository = repository,
            localDeviceId = "device-A", // Origin device
            sendToEndpoint = { _, _, onResult ->
                forwardCallCount++
                onResult(true)
            },
            getConnectedEndpoints = { setOf("peer-B") },
            currentTimeProvider = { simulatedTime }
        )

        // Phone A originated this message
        val originMsg = createSampleSos(id = "sos-origin", senderDeviceId = "device-A")
        relayA.registerLocallyOriginatedMessage(originMsg.id)

        // The message loops back to A through peer-B
        val loopedMsg = originMsg.copy(hopCount = 2)
        val result = relayA.handleIncomingMessage(loopedMsg, senderEndpointId = "peer-B")

        assertEquals(RelayResult.DUPLICATE_OR_LOOP, result)
        assertEquals(0, forwardCallCount)
    }

    // 5. Hop limit: Forwarding stops at the configured maximum of 5.
    @Test
    fun testHopLimitStopsForwardingAtMaxHops() = runTest {
        var forwardCallCount = 0

        val relay = SosRelayController(
            repository = repository,
            localDeviceId = "device-X",
            sendToEndpoint = { _, _, onResult ->
                forwardCallCount++
                onResult(true)
            },
            getConnectedEndpoints = { setOf("peer-in", "peer-out") },
            currentTimeProvider = { simulatedTime }
        )

        // Hop count 4 -> forwarded with hop count 5
        val msgHop4 = createSampleSos(id = "sos-hop4", hopCount = 4, maxHopCount = 5)
        val res4 = relay.handleIncomingMessage(msgHop4, senderEndpointId = "peer-in")
        assertEquals(RelayResult.FORWARDED, res4)
        assertEquals(1, forwardCallCount)

        // Hop count 5 -> reached max hops (5) -> must NOT forward
        val msgHop5 = createSampleSos(id = "sos-hop5", hopCount = 5, maxHopCount = 5)
        val res5 = relay.handleIncomingMessage(msgHop5, senderEndpointId = "peer-in")
        assertEquals(RelayResult.STORED_NO_FORWARD_HOP_LIMIT, res5)
        assertEquals(1, forwardCallCount) // forward count unchanged

        // Ensure hop 5 message was still stored locally
        assertNotNull(repository.getMessageByIdOnce("sos-hop5"))
    }

    // 6. Expiry: Expired messages are not forwarded.
    @Test
    fun testExpiredMessagesStoredLocallyAndNotForwarded() = runTest {
        var forwardCallCount = 0

        val relay = SosRelayController(
            repository = repository,
            localDeviceId = "device-B",
            sendToEndpoint = { _, _, onResult ->
                forwardCallCount++
                onResult(true)
            },
            getConnectedEndpoints = { setOf("peer-in", "peer-out") },
            currentTimeProvider = { simulatedTime }
        )

        // Message expired 10 seconds ago
        val expiredMsg = createSampleSos(
            id = "sos-expired",
            expiresAt = simulatedTime - 10000L
        )

        val result = relay.handleIncomingMessage(expiredMsg, senderEndpointId = "peer-in")

        assertEquals(RelayResult.STORED_NO_FORWARD_EXPIRED, result)
        assertEquals(0, forwardCallCount)

        // Verified stored with EXPIRED status
        val stored = repository.getMessageByIdOnce("sos-expired")
        assertNotNull(stored)
        assertEquals(DeliveryStatus.EXPIRED, stored?.deliveryStatus)
    }

    // 7. Partial failure: One peer's send failure does not prevent other peer send attempts.
    @Test
    fun testPartialFailureDoesNotBlockOtherPeers() = runTest {
        val attempts = mutableListOf<String>()

        val relay = SosRelayController(
            repository = repository,
            localDeviceId = "device-B",
            sendToEndpoint = { endpointId, _, onResult ->
                attempts.add(endpointId)
                if (endpointId == "failing-peer") {
                    onResult(false)
                } else {
                    onResult(true)
                }
            },
            getConnectedEndpoints = { setOf("peer-in", "failing-peer", "healthy-peer") },
            currentTimeProvider = { simulatedTime }
        )

        val msg = createSampleSos(id = "sos-partial")
        val result = relay.handleIncomingMessage(msg, senderEndpointId = "peer-in")

        assertEquals(RelayResult.FORWARDED, result)
        assertEquals(2, attempts.size)
        assertTrue(attempts.contains("failing-peer"))
        assertTrue(attempts.contains("healthy-peer"))
    }

    // 8. Connection changes: Disconnecting peers handled safely without crashing.
    @Test
    fun testConnectionChangesHandledSafely() = runTest {
        var connectedPeers = setOf("peer-in") // No other peers connected

        val relay = SosRelayController(
            repository = repository,
            localDeviceId = "device-B",
            sendToEndpoint = { _, _, onResult -> onResult(true) },
            getConnectedEndpoints = { connectedPeers },
            currentTimeProvider = { simulatedTime }
        )

        val msg = createSampleSos(id = "sos-isolated")
        val result = relay.handleIncomingMessage(msg, senderEndpointId = "peer-in")

        // No other peer besides the sender
        assertEquals(RelayResult.STORED_NO_ELIGIBLE_PEERS, result)
        // Message is still safely persisted locally
        assertNotNull(repository.getMessageByIdOnce("sos-isolated"))
    }
}
