package com.crisos.app.ui.viewmodel

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.DeviceStatus
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the Settings / Device Status screen.
 * Reactively monitors Bluetooth, Wi-Fi, Location, and Nearby Connection peer states.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CrisosApplication
    private val locationService = app.locationService
    private val nearbyManager = app.nearbyConnectionManager

    val isNearbyActive = nearbyManager.isActive
    val connectedPeerNames = nearbyManager.connectedPeerNames
    val discoveredEndpoints = nearbyManager.discoveredEndpoints

    private val manualRefreshTrigger = MutableStateFlow(0)

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

    fun refreshStatus() {
        manualRefreshTrigger.update { it + 1 }
        nearbyManager.rescanNow()
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
