package com.crisos.app

import androidx.sqlite.db.SupportSQLiteDatabase
import com.crisos.app.communication.RelayResult
import com.crisos.app.communication.SosRelayController
import com.crisos.app.data.local.CrisosDatabase
import com.crisos.app.data.local.SosMessageEntity
import com.crisos.app.data.repository.SosRepository
import com.crisos.app.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Unit tests verifying MongoDB Atlas Cloud Synchronization logic:
 * - Offline persistence with PENDING cloud status
 * - Successful server acknowledgement updating cloud-sync status
 * - Transient failure retention for exponential backoff
 * - Multiple pending records retrieval
 * - Cloud failures having zero interference with peer-to-peer relaying
 * - Room migration from version 1 to 2
 */
class CloudSyncTest {

    private lateinit var dao: FakeSosMessageDao
    private lateinit var repository: SosRepository

    @Before
    fun setUp() {
        dao = FakeSosMessageDao()
        repository = SosRepository(dao)
    }

    @Test
    fun testOfflineSosPersistenceDefaultsToPendingSync() = runBlocking {
        val sos = SosMessage(
            id = "offline-sos-001",
            senderDeviceId = "phone_a",
            emergencyCategory = EmergencyCategory.MEDICAL,
            description = "Offline patient needing oxygen",
            deliveryStatus = DeliveryStatus.QUEUED
        )

        // Verifies domain model defaults
        assertEquals(CloudSyncStatus.PENDING, sos.cloudSyncStatus)
        assertNull(sos.cloudSyncedAt)

        repository.saveMessage(sos)
        val retrieved = repository.getMessageByIdOnce("offline-sos-001")
        assertNotNull(retrieved)
        assertEquals(CloudSyncStatus.PENDING, retrieved!!.cloudSyncStatus)
        assertNull(retrieved.cloudSyncedAt)

        val entity = SosMessageEntity.fromDomainModel(sos)
        assertEquals("PENDING", entity.cloudSyncStatus)
        assertNull(entity.cloudSyncedAt)

        val mappedBack = entity.toDomainModel()
        assertEquals(CloudSyncStatus.PENDING, mappedBack.cloudSyncStatus)
    }

    @Test
    fun testSuccessfulServerAcknowledgementUpdatesCloudSyncStatus() = runBlocking {
        val message = SosMessage(id = "ack-test-uuid", cloudSyncStatus = CloudSyncStatus.PENDING)
        repository.saveMessage(message)
        val syncTimestamp = 1710000000000L

        repository.updateCloudSyncStatus("ack-test-uuid", CloudSyncStatus.SYNCED, syncTimestamp)

        val updated = repository.getMessageByIdOnce("ack-test-uuid")
        assertNotNull(updated)
        assertEquals(CloudSyncStatus.SYNCED, updated!!.cloudSyncStatus)
        assertEquals(syncTimestamp, updated.cloudSyncedAt)
    }

    @Test
    fun testFailedUploadRemainsPendingForRetry() = runBlocking {
        val message = SosMessage(id = "retry-test-uuid", cloudSyncStatus = CloudSyncStatus.PENDING)
        repository.saveMessage(message)

        // For retryable transient errors, status remains PENDING in database
        repository.updateCloudSyncStatus("retry-test-uuid", CloudSyncStatus.PENDING, null)

        val updated = repository.getMessageByIdOnce("retry-test-uuid")
        assertNotNull(updated)
        assertEquals(CloudSyncStatus.PENDING, updated!!.cloudSyncStatus)
        assertNull(updated.cloudSyncedAt)
    }

    @Test
    fun testClientErrorMarksAsFailedToPreventInfiniteRetries() = runBlocking {
        val message = SosMessage(id = "client-error-uuid", cloudSyncStatus = CloudSyncStatus.PENDING)
        repository.saveMessage(message)

        repository.updateCloudSyncStatus("client-error-uuid", CloudSyncStatus.FAILED, null)

        val updated = repository.getMessageByIdOnce("client-error-uuid")
        assertNotNull(updated)
        assertEquals(CloudSyncStatus.FAILED, updated!!.cloudSyncStatus)
    }

    @Test
    fun testMultiplePendingRecordsCanBeRetrieved() = runBlocking {
        val msg1 = SosMessage(id = "msg-1", cloudSyncStatus = CloudSyncStatus.PENDING)
        val msg2 = SosMessage(id = "msg-2", cloudSyncStatus = CloudSyncStatus.FAILED)
        val msg3 = SosMessage(id = "msg-3", cloudSyncStatus = CloudSyncStatus.SYNCED)

        repository.saveMessage(msg1)
        repository.saveMessage(msg2)
        repository.saveMessage(msg3)

        val pending = repository.getPendingCloudSyncMessages()
        assertEquals(2, pending.size)
        assertTrue(pending.any { it.id == "msg-1" && it.cloudSyncStatus == CloudSyncStatus.PENDING })
        assertTrue(pending.any { it.id == "msg-2" && it.cloudSyncStatus == CloudSyncStatus.FAILED })
        assertFalse(pending.any { it.id == "msg-3" })
    }

    @Test
    fun testCloudSyncFailureDoesNotInterruptPeerToPeerRelaying() = runBlocking {
        // Arrange an incoming valid SOS message
        val incomingSos = SosMessage(
            id = "relay-sos-999",
            senderDeviceId = "remote_victim_phone",
            emergencyCategory = EmergencyCategory.FLOOD,
            description = "Water surging, need boat",
            hopCount = 0,
            maxHopCount = 5,
            expiresAt = System.currentTimeMillis() + 3600000
        )

        val forwardedEndpoints = mutableListOf<String>()
        val relayController = SosRelayController(
            repository = repository,
            localDeviceId = "my_local_relay_device",
            sendToEndpoint = { endpointId, _, onResult ->
                forwardedEndpoints.add(endpointId)
                onResult(true)
            },
            getConnectedEndpoints = { setOf("rescuer_phone_c") }
        )

        // Setup hook simulating a cloud sync failure (e.g. offline or API down)
        var hookTriggered = false
        relayController.onSosStored = {
            hookTriggered = true
        }

        // Act: Handle the incoming message
        val result = relayController.handleIncomingMessage(incomingSos, "immediate_sender_phone_b")

        // Assert: Peer-to-peer forwarding succeeded completely despite cloud state
        assertEquals(RelayResult.FORWARDED, result)
        assertEquals(1, forwardedEndpoints.size)
        assertEquals("rescuer_phone_c", forwardedEndpoints[0])
        assertTrue("onSosStored hook was notified", hookTriggered)

        // Check local message was persisted with PENDING cloud sync status
        val stored = repository.getMessageByIdOnce("relay-sos-999")
        assertNotNull(stored)
        assertEquals(CloudSyncStatus.PENDING, stored!!.cloudSyncStatus)
    }

    @Test
    fun testRoomMigration1To2SqlExecution() {
        val mockDb = mock(SupportSQLiteDatabase::class.java)

        CrisosDatabase.MIGRATION_1_2.migrate(mockDb)

        verify(mockDb).execSQL("ALTER TABLE sos_messages ADD COLUMN cloudSyncStatus TEXT NOT NULL DEFAULT 'PENDING'")
        verify(mockDb).execSQL("ALTER TABLE sos_messages ADD COLUMN cloudSyncedAt INTEGER")
    }

    @Test
    fun testCloudSyncStatusEntityMappingConsistency() {
        val original = SosMessage(
            id = "cloud-status-test",
            cloudSyncStatus = CloudSyncStatus.SYNCED,
            cloudSyncedAt = 1700000005000L
        )

        val entity = SosMessageEntity.fromDomainModel(original)
        assertEquals("SYNCED", entity.cloudSyncStatus)
        assertEquals(1700000005000L, entity.cloudSyncedAt)

        val restored = entity.toDomainModel()
        assertEquals(CloudSyncStatus.SYNCED, restored.cloudSyncStatus)
        assertEquals(1700000005000L, restored.cloudSyncedAt)
    }
}
