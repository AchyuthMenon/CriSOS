package com.crisos.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 * Provides device status, message counts, and nearby connection state.
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

    val isNearbyActive: StateFlow<Boolean> = nearbyManager.isActive

    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    init {
        refreshDeviceStatus()
    }

    fun refreshDeviceStatus() {
        _deviceStatus.value = DeviceStatus(
            isLocationPermissionGranted = locationService.hasLocationPermission(),
            isLocationEnabled = locationService.isLocationEnabled(),
            isNearbyActive = nearbyManager.isActive.value,
            connectedDeviceCount = nearbyManager.connectedEndpoints.value.size,
            discoveredDeviceCount = nearbyManager.discoveredEndpoints.value.size
        )
    }

    fun toggleNearby() {
        if (nearbyManager.isActive.value) {
            nearbyManager.stop()
        } else {
            nearbyManager.start()
        }
        refreshDeviceStatus()
    }
}
