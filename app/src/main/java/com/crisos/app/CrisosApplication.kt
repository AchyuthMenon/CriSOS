package com.crisos.app

import android.app.Application
import com.crisos.app.communication.NearbyConnectionManager
import com.crisos.app.data.local.CrisosDatabase
import com.crisos.app.data.repository.SosRepository
import com.crisos.app.location.LocationService

import com.crisos.app.domain.model.DeliveryStatus
import com.crisos.app.domain.model.MessageDirection
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

    override fun onCreate() {
        super.onCreate()
        database = CrisosDatabase.getInstance(this)
        sosRepository = SosRepository(database.sosMessageDao())
        locationService = LocationService(this)
        nearbyConnectionManager = NearbyConnectionManager(this)

        // Application-level message receive handler (persists offline even if UI changes)
        nearbyConnectionManager.onMessageReceived = { incomingMessage ->
            applicationScope.launch {
                if (!sosRepository.messageExists(incomingMessage.id)) {
                    val receivedMsg = incomingMessage.copy(
                        deliveryStatus = DeliveryStatus.RECEIVED,
                        direction = MessageDirection.INCOMING
                    )
                    sosRepository.saveMessage(receivedMsg)
                }
            }
        }

        // Application-level send confirmation handler (updates delivery status in Room)
        nearbyConnectionManager.onMessageSent = { messageId, success ->
            applicationScope.launch {
                if (success) {
                    sosRepository.updateDeliveryStatus(messageId, DeliveryStatus.SENT)
                } else {
                    sosRepository.updateDeliveryStatus(messageId, DeliveryStatus.FAILED)
                }
            }
        }

        // Milestone 5: Automatic queue flush when a nearby peer connects
        nearbyConnectionManager.onEndpointConnected = { endpointId ->
            applicationScope.launch {
                val pending = sosRepository.getPendingOutgoingMessagesOnce()
                for (msg in pending) {
                    nearbyConnectionManager.sendMessageToEndpoint(endpointId, msg)
                }
            }
        }
    }
}
