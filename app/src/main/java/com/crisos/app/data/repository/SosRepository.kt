package com.crisos.app.data.repository

import com.crisos.app.data.local.SosMessageDao
import com.crisos.app.data.local.SosMessageEntity
import com.crisos.app.domain.model.CloudSyncStatus
import com.crisos.app.domain.model.DeliveryStatus
import com.crisos.app.domain.model.SosMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for SOS message persistence.
 * All operations go through Room — messages are always persisted before transmission.
 * Supports both peer-to-peer delivery tracking and remote MongoDB Atlas cloud synchronization.
 */
class SosRepository(private val dao: SosMessageDao) {

    fun getAllMessages(): Flow<List<SosMessage>> =
        dao.getAllMessages().map { entities -> entities.map { it.toDomainModel() } }

    fun getSentMessages(): Flow<List<SosMessage>> =
        dao.getSentMessages().map { entities -> entities.map { it.toDomainModel() } }

    fun getReceivedMessages(): Flow<List<SosMessage>> =
        dao.getReceivedMessages().map { entities -> entities.map { it.toDomainModel() } }

    fun getMessageById(id: String): Flow<SosMessage?> =
        dao.getMessageById(id).map { it?.toDomainModel() }

    fun getPendingMessages(): Flow<List<SosMessage>> =
        dao.getPendingMessages().map { entities -> entities.map { it.toDomainModel() } }

    suspend fun getPendingOutgoingMessagesOnce(): List<SosMessage> =
        dao.getPendingOutgoingMessagesOnce().map { it.toDomainModel() }

    // Cloud sync queries
    suspend fun getPendingCloudSyncMessages(): List<SosMessage> =
        dao.getPendingCloudSyncMessages().map { it.toDomainModel() }

    suspend fun updateCloudSyncStatus(messageId: String, status: CloudSyncStatus, syncedAt: Long? = null) {
        dao.updateCloudSyncStatus(messageId, status.name, syncedAt)
    }

    fun getPendingCloudSyncCount(): Flow<Int> = dao.getPendingCloudSyncCount()

    fun getSyncedCloudCount(): Flow<Int> = dao.getSyncedCloudCount()

    fun getMessageCount(): Flow<Int> = dao.getMessageCount()

    fun getReceivedCount(): Flow<Int> = dao.getReceivedCount()

    suspend fun saveMessage(message: SosMessage) {
        dao.insert(SosMessageEntity.fromDomainModel(message))
    }

    suspend fun updateMessage(message: SosMessage) {
        dao.update(SosMessageEntity.fromDomainModel(message))
    }

    suspend fun updateDeliveryStatus(messageId: String, status: DeliveryStatus) {
        dao.updateDeliveryStatus(messageId, status.name)
    }

    suspend fun messageExists(messageId: String): Boolean =
        dao.messageExists(messageId)

    suspend fun getMessageByIdOnce(messageId: String): SosMessage? =
        dao.getMessageByIdOnce(messageId)?.toDomainModel()
}
