package com.crisos.app.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.CloudSyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager CoroutineWorker for synchronizing pending local SOS messages to MongoDB Atlas.
 * Runs with network constraints to ensure upload only occurs when internet is available.
 */
class CloudSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "CloudSyncWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as? CrisosApplication
            ?: return@withContext Result.failure()

        val repository = app.sosRepository
        val apiClient = app.cloudApiClient
        val backendUrl = CloudConfig.getBackendUrl(applicationContext)

        val pendingMessages = repository.getPendingCloudSyncMessages()
        if (pendingMessages.isEmpty()) {
            Log.d(TAG, "No pending SOS messages for cloud synchronization")
            return@withContext Result.success()
        }

        Log.i(TAG, "Starting cloud synchronization for ${pendingMessages.size} pending SOS message(s) to $backendUrl")

        var hasRetryableError = false

        for (message in pendingMessages) {
            when (val result = apiClient.uploadSos(backendUrl, message)) {
                is UploadResult.Success -> {
                    Log.i(TAG, "Successfully synced SOS ${message.id} to cloud storage")
                    repository.updateCloudSyncStatus(
                        messageId = message.id,
                        status = CloudSyncStatus.SYNCED,
                        syncedAt = result.timestamp
                    )
                }
                is UploadResult.ClientError -> {
                    Log.w(TAG, "Permanent client error for SOS ${message.id} (HTTP ${result.statusCode}): ${result.message}. Marking as FAILED to prevent loop.")
                    repository.updateCloudSyncStatus(
                        messageId = message.id,
                        status = CloudSyncStatus.FAILED,
                        syncedAt = null
                    )
                }
                is UploadResult.RetryableError -> {
                    Log.w(TAG, "Transient network/server error for SOS ${message.id}: ${result.message}. Retaining as PENDING for retry.")
                    hasRetryableError = true
                }
            }
        }

        if (hasRetryableError) {
            Log.w(TAG, "Cloud sync encountered retryable error(s); scheduling exponential backoff retry")
            Result.retry()
        } else {
            Log.i(TAG, "Cloud sync batch completed successfully")
            Result.success()
        }
    }
}
