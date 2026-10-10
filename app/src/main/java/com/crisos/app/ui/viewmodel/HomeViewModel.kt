package com.crisos.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.DeviceStatus
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the Home screen.
 * Provides reactive device status, message counts, and peer connection state.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CrisosApplication
    private val repository = app.sosRepository
    private val locationService = app.locationService
    private val nearbyManager = app.nearbyConnectionManager

    val messageCount: StateFlow<Int> = repository.getMessageCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val receivedCount: StateFlow<Int> = repository.getReceivedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val connectedDevices: StateFlow<Set<String>> = nearbyManager.connectedEndpoints
    val connectedPeerNames: StateFlow<Map<String, String>> = nearbyManager.connectedPeerNames
    val isNearbyActive: StateFlow<Boolean> = nearbyManager.isActive

    private val manualRefreshTrigger = MutableStateFlow(0)

    val deviceStatus: StateFlow<DeviceStatus> = combine(
        nearbyManager.isActive,
        nearbyManager.connectedEndpoints,
        nearbyManager.discoveredEndpoints,
        manualRefreshTrigger
    ) { isActive, connected, discovered, _ ->
        val unconnectedDiscovered = (discovered.keys - connected).size
        DeviceStatus(
            isLocationPermissionGranted = locationService.hasLocationPermission(),
            isLocationEnabled = locationService.isLocationEnabled(),
            isNearbyActive = isActive,
            connectedDeviceCount = connected.size,
            discoveredDeviceCount = unconnectedDiscovered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DeviceStatus())

    fun refreshDeviceStatus() {
        manualRefreshTrigger.update { it + 1 }
    }

    fun toggleNearby() {
        if (nearbyManager.isActive.value) {
            nearbyManager.stop()
        } else {
            nearbyManager.start()
        }
        refreshDeviceStatus()
    }

    fun rescanNow() {
        nearbyManager.rescanNow()
        refreshDeviceStatus()
    }
}
