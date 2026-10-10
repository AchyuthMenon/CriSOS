package com.crisos.app.domain.model

import java.util.UUID

/**
 * Core SOS message model used throughout the app.
 *
 * Location fields are nullable — an SOS can exist without coordinates.
 * Includes both peer-to-peer [deliveryStatus] and remote MongoDB Atlas [cloudSyncStatus].
 */
data class SosMessage(
    val id: String = UUID.randomUUID().toString(),
    val messageType: String = "SOS",
    val senderDeviceId: String = "",
    val emergencyCategory: EmergencyCategory = EmergencyCategory.OTHER,
    val description: String = "",
    val assistanceTypes: List<AssistanceType> = emptyList(),

    // Location — all nullable
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAccuracyMeters: Float? = null,
    val locationTimestamp: Long? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = createdAt + (24 * 60 * 60 * 1000), // default 24h

    // Relay support
    val hopCount: Int = 0,
    val maxHopCount: Int = 5,

    // Peer-to-peer delivery status
    val deliveryStatus: DeliveryStatus = DeliveryStatus.DRAFT,
    val direction: MessageDirection = MessageDirection.OUTGOING,

    // Remote cloud storage (MongoDB Atlas) synchronization status
    val cloudSyncStatus: CloudSyncStatus = CloudSyncStatus.PENDING,
    val cloudSyncedAt: Long? = null,

    // Grouping (reserved for incident grouping)
    val incidentId: String? = null
)
