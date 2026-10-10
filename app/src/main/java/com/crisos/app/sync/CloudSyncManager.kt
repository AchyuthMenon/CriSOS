package com.crisos.app.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Coordinates background synchronization of SOS messages to MongoDB Atlas using Android WorkManager.
 */
class CloudSyncManager(private val context: Context) {

    companion object {
        private const val TAG = "CloudSyncManager"
        const val UNIQUE_WORK_NAME = "CriSOS_CloudSync_Work"
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedules a unique WorkManager background job with a network constraint.
     * WorkManager automatically executes this when validated internet is connected.
     */
    fun scheduleCloudSync() {
        Log.d(TAG, "Enqueuing unique cloud sync work with network constraint")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<CloudSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.SECONDS
            )
            .addTag("crisos_cloud_sync")
            .build()

        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    /**
     * If validated internet is available right now, triggers an immediate sync job.
     * Otherwise, schedules background sync so it executes the moment connectivity returns.
     */
    fun triggerImmediateSyncIfOnline() {
        if (NetworkUtils.hasValidatedInternet(context)) {
            Log.i(TAG, "Validated internet detected — triggering immediate cloud sync")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<CloudSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.SECONDS
                )
                .build()

            workManager.enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )
        } else {
            Log.d(TAG, "Device is offline — queuing cloud sync for when network becomes available")
            scheduleCloudSync()
        }
    }
}
