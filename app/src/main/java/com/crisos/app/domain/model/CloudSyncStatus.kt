package com.crisos.app.domain.model

/**
 * Status of remote cloud storage synchronization to MongoDB Atlas.
 * Operates independently from peer-to-peer [DeliveryStatus].
 */
enum class CloudSyncStatus {
    /**
     * Stored locally, waiting for internet access or cloud upload acknowledgement.
     */
    PENDING,

    /**
     * Successfully uploaded and acknowledged by MongoDB Atlas backend.
     */
    SYNCED,

    /**
     * Permanent failure or client error (e.g. malformed or rejected by validation).
     */
    FAILED
}
