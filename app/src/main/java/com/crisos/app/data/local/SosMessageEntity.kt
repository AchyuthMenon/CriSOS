package com.crisos.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crisos.app.domain.model.*

/**
 * Room entity for SOS messages.
 * Mirrors [SosMessage] but uses simple types for Room compatibility.
 */
@Entity(tableName = "sos_messages")
data class SosMessageEntity(
    @PrimaryKey val id: String,
    val messageType: String,
    val senderDeviceId: String,
    val emergencyCategory: String,
    val description: String,
    val assistanceTypes: String, // comma-separated

    val latitude: Double?,
    val longitude: Double?,
    val locationAccuracyMeters: Float?,
    val locationTimestamp: Long?,

    val createdAt: Long,
    val expiresAt: Long,

    val hopCount: Int,
    val maxHopCount: Int,

    val deliveryStatus: String,
    val direction: String,

    val incidentId: String?
) {
    fun toDomainModel(): SosMessage = SosMessage(
        id = id,
        messageType = messageType,
        senderDeviceId = senderDeviceId,
        emergencyCategory = try {
            EmergencyCategory.valueOf(emergencyCategory)
        } catch (_: Exception) {
            EmergencyCategory.OTHER
        },
        description = description,
        assistanceTypes = if (assistanceTypes.isBlank()) emptyList()
        else assistanceTypes.split(",").mapNotNull { name ->
            try { AssistanceType.valueOf(name.trim()) } catch (_: Exception) { null }
        },
        latitude = latitude,
        longitude = longitude,
        locationAccuracyMeters = locationAccuracyMeters,
        locationTimestamp = locationTimestamp,
        createdAt = createdAt,
        expiresAt = expiresAt,
        hopCount = hopCount,
        maxHopCount = maxHopCount,
        deliveryStatus = try {
            DeliveryStatus.valueOf(deliveryStatus)
        } catch (_: Exception) {
            DeliveryStatus.DRAFT
        },
        direction = try {
            MessageDirection.valueOf(direction)
        } catch (_: Exception) {
            MessageDirection.OUTGOING
        },
        incidentId = incidentId
    )

    companion object {
        fun fromDomainModel(msg: SosMessage): SosMessageEntity = SosMessageEntity(
            id = msg.id,
            messageType = msg.messageType,
            senderDeviceId = msg.senderDeviceId,
            emergencyCategory = msg.emergencyCategory.name,
            description = msg.description,
            assistanceTypes = msg.assistanceTypes.joinToString(",") { it.name },
            latitude = msg.latitude,
            longitude = msg.longitude,
            locationAccuracyMeters = msg.locationAccuracyMeters,
            locationTimestamp = msg.locationTimestamp,
            createdAt = msg.createdAt,
            expiresAt = msg.expiresAt,
            hopCount = msg.hopCount,
            maxHopCount = msg.maxHopCount,
            deliveryStatus = msg.deliveryStatus.name,
            direction = msg.direction.name,
            incidentId = msg.incidentId
        )
    }
}
