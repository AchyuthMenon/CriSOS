package com.crisos.app

import com.crisos.app.data.local.SosMessageEntity
import com.crisos.app.domain.model.*
import com.crisos.app.location.LocationResult
import com.crisos.app.ui.components.formatCoordinate
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class SosMessageTest {

    private val gson = Gson()

    @Test
    fun testSosMessageSerializationAndDeserialization() {
        val original = SosMessage(
            id = "test-uuid-1234",
            senderDeviceId = "device_alpha",
            emergencyCategory = EmergencyCategory.FLOOD,
            description = "Water rising fast on Ground floor",
            assistanceTypes = listOf(AssistanceType.RESCUE, AssistanceType.FOOD),
            latitude = 12.9716,
            longitude = 77.5946,
            locationAccuracyMeters = 5.2f,
            locationTimestamp = 1700000000000L,
            createdAt = 1700000000000L,
            expiresAt = 1700086400000L,
            hopCount = 0,
            maxHopCount = 5,
            deliveryStatus = DeliveryStatus.QUEUED,
            direction = MessageDirection.OUTGOING
        )

        val json = gson.toJson(original)
        assertNotNull(json)
        assertTrue(json.contains("test-uuid-1234"))
        assertTrue(json.contains("FLOOD"))

        val deserialized = gson.fromJson(json, SosMessage::class.java)
        assertEquals(original.id, deserialized.id)
        assertEquals(original.senderDeviceId, deserialized.senderDeviceId)
        assertEquals(original.emergencyCategory, deserialized.emergencyCategory)
        assertEquals(original.description, deserialized.description)
        assertEquals(original.assistanceTypes, deserialized.assistanceTypes)
        assertEquals(original.latitude!!, deserialized.latitude!!, 0.0001)
        assertEquals(original.longitude!!, deserialized.longitude!!, 0.0001)
        assertEquals(original.locationAccuracyMeters, deserialized.locationAccuracyMeters)
        assertEquals(original.deliveryStatus, deserialized.deliveryStatus)
    }

    @Test
    fun testSosMessageWithoutLocationSerialization() {
        val noLocationMessage = SosMessage(
            id = "test-no-loc",
            emergencyCategory = EmergencyCategory.MEDICAL,
            description = "Need insulin",
            assistanceTypes = listOf(AssistanceType.MEDICAL_AID),
            latitude = null,
            longitude = null,
            locationAccuracyMeters = null,
            locationTimestamp = null
        )

        val json = gson.toJson(noLocationMessage)
        val deserialized = gson.fromJson(json, SosMessage::class.java)

        assertEquals("test-no-loc", deserialized.id)
        assertNull(deserialized.latitude)
        assertNull(deserialized.longitude)
        assertNull(deserialized.locationAccuracyMeters)
    }

    @Test
    fun testEntityDomainModelMapping() {
        val domain = SosMessage(
            id = "entity-test-id",
            senderDeviceId = "dev-123",
            emergencyCategory = EmergencyCategory.TRAPPED,
            description = "Stuck in elevator",
            assistanceTypes = listOf(AssistanceType.RESCUE, AssistanceType.WATER),
            latitude = 37.7749,
            longitude = -122.4194,
            locationAccuracyMeters = 10f,
            locationTimestamp = 1600000000000L,
            createdAt = 1600000000000L,
            expiresAt = 1600086400000L,
            hopCount = 1,
            maxHopCount = 5,
            deliveryStatus = DeliveryStatus.SENT,
            direction = MessageDirection.OUTGOING,
            incidentId = "incident-999"
        )

        val entity = SosMessageEntity.fromDomainModel(domain)
        assertEquals("entity-test-id", entity.id)
        assertEquals("TRAPPED", entity.emergencyCategory)
        assertEquals("RESCUE,WATER", entity.assistanceTypes)
        assertEquals("SENT", entity.deliveryStatus)

        val convertedBack = entity.toDomainModel()
        assertEquals(domain, convertedBack)
    }

    @Test
    fun testEntityUnknownEnumFallback() {
        val corruptedEntity = SosMessageEntity(
            id = "corrupt-id",
            messageType = "SOS",
            senderDeviceId = "dev-x",
            emergencyCategory = "UNKNOWN_FUTURE_CATEGORY",
            description = "Test",
            assistanceTypes = "UNKNOWN_ASSISTANCE,RESCUE",
            latitude = null,
            longitude = null,
            locationAccuracyMeters = null,
            locationTimestamp = null,
            createdAt = 1000L,
            expiresAt = 2000L,
            hopCount = 0,
            maxHopCount = 5,
            deliveryStatus = "INVALID_STATUS",
            direction = "INVALID_DIR",
            incidentId = null
        )

        val domain = corruptedEntity.toDomainModel()
        // Should fallback gracefully without crashing
        assertEquals(EmergencyCategory.OTHER, domain.emergencyCategory)
        assertEquals(DeliveryStatus.DRAFT, domain.deliveryStatus)
        assertEquals(MessageDirection.OUTGOING, domain.direction)
        assertEquals(listOf(AssistanceType.RESCUE), domain.assistanceTypes)
    }

    @Test
    fun testLocationStaleness() {
        val freshTime = System.currentTimeMillis() - 60_000 // 1 min ago
        val freshLocation = LocationResult(10.0, 20.0, 5f, freshTime, "gps")
        assertFalse(freshLocation.isStale(thresholdMs = 5 * 60 * 1000))

        val staleTime = System.currentTimeMillis() - (10 * 60 * 1000) // 10 mins ago
        val staleLocation = LocationResult(10.0, 20.0, 5f, staleTime, "gps")
        assertTrue(staleLocation.isStale(thresholdMs = 5 * 60 * 1000))
    }

    @Test
    fun testFormatCoordinateDirections() {
        assertEquals("12.971600°N", formatCoordinate(12.9716, isLatitude = true))
        assertEquals("33.868800°S", formatCoordinate(-33.8688, isLatitude = true))
        assertEquals("77.594600°E", formatCoordinate(77.5946, isLatitude = false))
        assertEquals("122.419400°W", formatCoordinate(-122.4194, isLatitude = false))
    }
}
