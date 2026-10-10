package com.crisos.app

import android.app.Application
import com.crisos.app.communication.DeviceUtils
import com.crisos.app.communication.NearbyConnectionManager
import com.crisos.app.communication.SosRelayController
import com.crisos.app.data.local.CrisosDatabase
import com.crisos.app.data.repository.SosRepository
import com.crisos.app.domain.model.DeliveryStatus
import com.crisos.app.domain.model.MessageDirection
import com.crisos.app.location.LocationService
import com.crisos.app.sync.CloudApiClient
import com.crisos.app.sync.CloudSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application-level singleton for manual dependency injection.
 * Compatible with Hilt migration later — just add @HiltAndroidApp
 * and convert these to @Provides/@Binds in a module.
 */
class CrisosApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: CrisosDatabase
        private set
    lateinit var sosRepository: SosRepository
        private set
    lateinit var locationService: LocationService
        private set
    lateinit var nearbyConnectionManager: NearbyConnectionManager
        private set
    lateinit var relayController: SosRelayController
        private set
    lateinit var cloudApiClient: CloudApiClient
        private set
    lateinit var cloudSyncManager: CloudSyncManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = CrisosDatabase.getInstance(this)
        sosRepository = SosRepository(database.sosMessageDao())
        locationService = LocationService(this)
        nearbyConnectionManager = NearbyConnectionManager(this)
        cloudApiClient = CloudApiClient()
        cloudSyncManager = CloudSyncManager(this)

        val localDeviceId = DeviceUtils.getDeviceId(this)
        relayController = SosRelayController(
            repository = sosRepository,
            localDeviceId = localDeviceId,
            sendToEndpoint = { endpointId: String, msg: com.crisos.app.domain.model.SosMessage, onResult: (Boolean) -> Unit ->
                nearbyConnectionManager.sendMessageToEndpoint(endpointId, msg, onResult)
            },
            getConnectedEndpoints = { nearbyConnectionManager.connectedEndpoints.value },
            coroutineScope = applicationScope
        )

        // Trigger cloud synchronization whenever a received or relayed SOS is persisted
        relayController.onSosStored = {
            cloudSyncManager.triggerImmediateSyncIfOnline()
        }

        // Application-level message receive handler with multi-hop relaying
        nearbyConnectionManager.onMessageReceived = { incomingMessage, senderEndpointId ->
            applicationScope.launch {
                relayController.handleIncomingMessage(incomingMessage, senderEndpointId)
            }
        }

        // Application-level send confirmation handler (updates delivery status in Room)
        nearbyConnectionManager.onMessageSent = { messageId, success ->
            applicationScope.launch {
                if (success) {
                    val current = sosRepository.getMessageByIdOnce(messageId)
                    if (current?.direction == MessageDirection.INCOMING) {
                        sosRepository.updateDeliveryStatus(messageId, DeliveryStatus.RELAYED)
                    } else {
                        sosRepository.updateDeliveryStatus(messageId, DeliveryStatus.SENT)
                    }
                } else {
                    sosRepository.updateDeliveryStatus(messageId, DeliveryStatus.FAILED)
                }
            }
        }

        // Automatic queue flush when a nearby peer connects
        nearbyConnectionManager.onEndpointConnected = { endpointId ->
            applicationScope.launch {
                val pending = sosRepository.getPendingOutgoingMessagesOnce()
                for (msg in pending) {
                    nearbyConnectionManager.sendMessageToEndpoint(endpointId, msg)
                }
            }
        }

        // Schedule persistent WorkManager background sync for when internet is connected
        cloudSyncManager.scheduleCloudSync()
    }
}
