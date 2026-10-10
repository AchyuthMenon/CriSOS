package com.crisos.app.domain.model

/**
 * Represents current device connectivity capabilities.
 */
data class DeviceStatus(
    val isLocationPermissionGranted: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val isWifiEnabled: Boolean = false,
    val isNearbyActive: Boolean = false,
    val connectedDeviceCount: Int = 0,
    val discoveredDeviceCount: Int = 0
)
