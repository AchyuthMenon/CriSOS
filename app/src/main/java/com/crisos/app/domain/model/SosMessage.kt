package com.crisos.app.domain.model

import java.util.UUID

/**
 * Core SOS message model used throughout the app.
 *
 * Location fields are nullable — an SOS can exist without coordinates.
 * Hop count fields are reserved for future relay support.
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

    // Relay support (reserved for future)
    val hopCount: Int = 0,
    val maxHopCount: Int = 5,

    // Status
    val deliveryStatus: DeliveryStatus = DeliveryStatus.DRAFT,
    val direction: MessageDirection = MessageDirection.OUTGOING,

    // Grouping (reserved for future incident grouping)
    val incidentId: String? = null
)
