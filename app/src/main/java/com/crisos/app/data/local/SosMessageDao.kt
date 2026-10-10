package com.crisos.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SosMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: SosMessageEntity)

    @Update
    suspend fun update(message: SosMessageEntity)

    @Delete
    suspend fun delete(message: SosMessageEntity)

    @Query("SELECT * FROM sos_messages ORDER BY createdAt DESC")
    fun getAllMessages(): Flow<List<SosMessageEntity>>

    @Query("SELECT * FROM sos_messages WHERE direction = 'OUTGOING' ORDER BY createdAt DESC")
    fun getSentMessages(): Flow<List<SosMessageEntity>>

    @Query("SELECT * FROM sos_messages WHERE direction = 'INCOMING' ORDER BY createdAt DESC")
    fun getReceivedMessages(): Flow<List<SosMessageEntity>>

    @Query("SELECT * FROM sos_messages WHERE id = :messageId LIMIT 1")
    fun getMessageById(messageId: String): Flow<SosMessageEntity?>

    @Query("SELECT * FROM sos_messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageByIdOnce(messageId: String): SosMessageEntity?

    @Query("SELECT * FROM sos_messages WHERE deliveryStatus = 'QUEUED' OR deliveryStatus = 'FAILED' ORDER BY createdAt ASC")
    fun getPendingMessages(): Flow<List<SosMessageEntity>>

    @Query("SELECT * FROM sos_messages WHERE (deliveryStatus = 'QUEUED' OR deliveryStatus = 'FAILED') AND direction = 'OUTGOING' ORDER BY createdAt ASC")
    suspend fun getPendingOutgoingMessagesOnce(): List<SosMessageEntity>

    // Cloud synchronization queries (MongoDB Atlas)
    @Query("SELECT * FROM sos_messages WHERE cloudSyncStatus = 'PENDING' OR cloudSyncStatus = 'FAILED' ORDER BY createdAt ASC")
    suspend fun getPendingCloudSyncMessages(): List<SosMessageEntity>

    @Query("UPDATE sos_messages SET cloudSyncStatus = :status, cloudSyncedAt = :syncedAt WHERE id = :messageId")
    suspend fun updateCloudSyncStatus(messageId: String, status: String, syncedAt: Long?)

    @Query("SELECT COUNT(*) FROM sos_messages WHERE cloudSyncStatus = 'PENDING' OR cloudSyncStatus = 'FAILED'")
    fun getPendingCloudSyncCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sos_messages WHERE cloudSyncStatus = 'SYNCED'")
    fun getSyncedCloudCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sos_messages")
    fun getMessageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sos_messages WHERE direction = 'INCOMING'")
    fun getReceivedCount(): Flow<Int>

    @Query("UPDATE sos_messages SET deliveryStatus = :status WHERE id = :messageId")
    suspend fun updateDeliveryStatus(messageId: String, status: String)

    @Query("SELECT EXISTS(SELECT 1 FROM sos_messages WHERE id = :messageId)")
    suspend fun messageExists(messageId: String): Boolean
}
