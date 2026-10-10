package com.crisos.app.ui.viewmodel

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.DeviceStatus
import com.crisos.app.sync.CloudConfig
import com.crisos.app.sync.NetworkUtils
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the Settings / Device Status screen.
 * Reactively monitors Bluetooth, Wi-Fi, Location, Nearby Connection peer states,
 * and MongoDB Atlas remote cloud synchronization status.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CrisosApplication
    private val locationService = app.locationService
    private val nearbyManager = app.nearbyConnectionManager
    private val repository = app.sosRepository

    val isNearbyActive = nearbyManager.isActive
    val connectedPeerNames = nearbyManager.connectedPeerNames
    val discoveredEndpoints = nearbyManager.discoveredEndpoints

    // Cloud synchronization state
    val pendingCloudSyncCount: StateFlow<Int> = repository.getPendingCloudSyncCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val syncedCloudCount: StateFlow<Int> = repository.getSyncedCloudCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _backendUrl = MutableStateFlow(CloudConfig.getBackendUrl(app))
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    private val manualRefreshTrigger = MutableStateFlow(0)

    val isInternetAvailable: Boolean
        get() = NetworkUtils.hasValidatedInternet(app)

    val deviceStatus: StateFlow<DeviceStatus> = combine(
        nearbyManager.isActive,
        nearbyManager.connectedEndpoints,
        nearbyManager.discoveredEndpoints,
        manualRefreshTrigger
    ) { isActive, connected, discovered, _ ->
        val bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val wifiManager = app.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val unconnectedDiscovered = (discovered.keys - connected).size

        DeviceStatus(
            isLocationPermissionGranted = locationService.hasLocationPermission(),
            isLocationEnabled = locationService.isLocationEnabled(),
            isBluetoothEnabled = bluetoothManager?.adapter?.isEnabled == true,
            isWifiEnabled = wifiManager?.isWifiEnabled == true,
            isNearbyActive = isActive,
            connectedDeviceCount = connected.size,
            discoveredDeviceCount = unconnectedDiscovered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DeviceStatus())

    fun updateBackendUrl(url: String) {
        CloudConfig.setBackendUrl(app, url)
        _backendUrl.value = CloudConfig.getBackendUrl(app)
    }

    fun triggerCloudSyncNow() {
        app.cloudSyncManager.triggerImmediateSyncIfOnline()
    }

    fun refreshStatus() {
        manualRefreshTrigger.update { it + 1 }
        nearbyManager.rescanNow()
        app.cloudSyncManager.triggerImmediateSyncIfOnline()
    }

    fun toggleNearby() {
        if (nearbyManager.isActive.value) {
            nearbyManager.stop()
        } else {
            nearbyManager.start()
        }
        manualRefreshTrigger.update { it + 1 }
    }
}
