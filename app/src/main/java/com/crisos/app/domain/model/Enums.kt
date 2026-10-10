package com.crisos.app.domain.model

/**
 * Emergency categories for SOS messages.
 */
enum class EmergencyCategory(val label: String, val icon: String) {
    FLOOD("Flood", "🌊"),
    MEDICAL("Medical Emergency", "🏥"),
    TRAPPED("Trapped / Collapsed Structure", "🏚️"),
    FIRE("Fire", "🔥"),
    EARTHQUAKE("Earthquake", "⚠️"),
    VIOLENCE("Violence / Security Threat", "🚨"),
    OTHER("Other Emergency", "🆘")
}

/**
 * Types of assistance that can be requested.
 */
enum class AssistanceType(val label: String) {
    RESCUE("Rescue"),
    WATER("Water"),
    FOOD("Food"),
    SHELTER("Shelter"),
    MEDICAL_AID("Medical Aid"),
    CLOTHING("Clothing"),
    COMMUNICATION("Communication"),
    TRANSPORT("Transport"),
    OTHER("Other")
}

/**
 * Delivery status of an SOS message.
 * Distinguishes between local persistence and actual delivery.
 */
enum class DeliveryStatus(val label: String) {
    DRAFT("Draft"),
    QUEUED("Queued — Waiting for nearby device"),
    SENDING("Sending…"),
    SENT("Sent to nearby device"),
    RELAYED("Relayed through intermediate device"),
    RECEIVED("Received"),
    ACKNOWLEDGED("Acknowledged by responder"),
    FAILED("Delivery failed"),
    EXPIRED("Expired")
}

/**
 * Whether this message was created locally or received from another device.
 */
enum class MessageDirection {
    OUTGOING,
    INCOMING
}
