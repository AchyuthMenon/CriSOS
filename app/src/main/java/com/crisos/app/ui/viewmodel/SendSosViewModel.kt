package com.crisos.app.ui.viewmodel

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.*
import com.crisos.app.location.LocationResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SendSosUiState(
    val category: EmergencyCategory = EmergencyCategory.OTHER,
    val description: String = "",
    val selectedAssistance: Set<AssistanceType> = emptySet(),
    val includeLocation: Boolean = true,
    val currentLocation: LocationResult? = null,
    val isLocationLoading: Boolean = false,
    val isSending: Boolean = false,
    val showConfirmation: Boolean = false,
    val sendResult: SendResult? = null
)

enum class SendResult {
    QUEUED,      // Saved locally, waiting for nearby device
    SENT,        // Successfully sent to a connected device
    FAILED       // Send attempted but failed — still queued
}

class SendSosViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CrisosApplication
    private val repository = app.sosRepository
    private val locationService = app.locationService
    private val nearbyManager = app.nearbyConnectionManager

    private val _uiState = MutableStateFlow(SendSosUiState())
    val uiState: StateFlow<SendSosUiState> = _uiState.asStateFlow()

    private val deviceId: String by lazy {
        com.crisos.app.communication.DeviceUtils.getDeviceId(app)
    }

    fun updateCategory(category: EmergencyCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateDescription(desc: String) {
        _uiState.update { it.copy(description = desc) }
    }

    fun toggleAssistance(type: AssistanceType) {
        _uiState.update { state ->
            val newSet = if (type in state.selectedAssistance) {
                state.selectedAssistance - type
            } else {
                state.selectedAssistance + type
            }
            state.copy(selectedAssistance = newSet)
        }
    }

    fun setIncludeLocation(include: Boolean) {
        _uiState.update { it.copy(includeLocation = include) }
        if (include) fetchLocation()
    }

    fun fetchLocation() {
        if (!locationService.hasLocationPermission()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLocationLoading = true) }
            val loc = locationService.getCurrentLocation()
            _uiState.update { it.copy(currentLocation = loc, isLocationLoading = false) }
        }
    }

    fun showConfirmation() {
        _uiState.update { it.copy(showConfirmation = true) }
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(showConfirmation = false) }
    }

    fun confirmAndSend() {
        val state = _uiState.value
        _uiState.update { it.copy(isSending = true, showConfirmation = false) }

        viewModelScope.launch {
            val loc = if (state.includeLocation) state.currentLocation else null

            val message = SosMessage(
                senderDeviceId = deviceId,
                emergencyCategory = state.category,
                description = state.description,
                assistanceTypes = state.selectedAssistance.toList(),
                latitude = loc?.latitude,
                longitude = loc?.longitude,
                locationAccuracyMeters = loc?.accuracyMeters,
                locationTimestamp = loc?.timestamp,
                deliveryStatus = DeliveryStatus.QUEUED,
                direction = MessageDirection.OUTGOING
            )

            // Always persist first
            repository.saveMessage(message)
            app.relayController.registerLocallyOriginatedMessage(message.id)

            // Trigger cloud sync to MongoDB Atlas if internet is available, or queue for later
            app.cloudSyncManager.triggerImmediateSyncIfOnline()

            // Attempt to send via Nearby
            val connected = nearbyManager.connectedEndpoints.value
            if (connected.isNotEmpty()) {
                nearbyManager.sendMessage(message) { success ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            sendResult = if (success) SendResult.SENT else SendResult.FAILED
                        )
                    }
                }
            } else {
                // No connected devices — message stays queued
                _uiState.update { it.copy(isSending = false, sendResult = SendResult.QUEUED) }
            }
        }
    }

    fun resetForm() {
        _uiState.value = SendSosUiState()
    }
}
