package com.crisos.app.ui.viewmodel

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.DeviceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Settings / Device Status screen.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CrisosApplication
    private val locationService = app.locationService
    private val nearbyManager = app.nearbyConnectionManager

    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    val isNearbyActive = nearbyManager.isActive

    fun refreshStatus() {
        val bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val wifiManager = app.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        _deviceStatus.value = DeviceStatus(
            isLocationPermissionGranted = locationService.hasLocationPermission(),
            isLocationEnabled = locationService.isLocationEnabled(),
            isBluetoothEnabled = bluetoothManager?.adapter?.isEnabled == true,
            isWifiEnabled = wifiManager?.isWifiEnabled == true,
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
        refreshStatus()
    }
}
