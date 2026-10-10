package com.crisos.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Holds a single location snapshot with accuracy and freshness info.
 */
data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestamp: Long,
    val provider: String
) {
    /** Returns true if the location is older than [thresholdMs] milliseconds. */
    fun isStale(thresholdMs: Long = 5 * 60 * 1000): Boolean =
        (System.currentTimeMillis() - timestamp) > thresholdMs
}

/**
 * Wrapper around Fused Location Provider.
 * Works offline — GPS does not require internet.
 */
class LocationService(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    fun isLocationEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Request a single fresh location fix.
     * Falls back to last known location if a fresh fix isn't available quickly.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { cont ->
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(60_000) // accept fixes up to 1 min old
                .setDurationMillis(10_000)     // wait up to 10s
                .build()

            fusedClient.getCurrentLocation(request, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(
                            LocationResult(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracyMeters = location.accuracy,
                                timestamp = location.time,
                                provider = location.provider ?: "fused"
                            )
                        )
                    } else {
                        // Try last known
                        fusedClient.lastLocation
                            .addOnSuccessListener { last ->
                                if (last != null) {
                                    cont.resume(
                                        LocationResult(
                                            latitude = last.latitude,
                                            longitude = last.longitude,
                                            accuracyMeters = last.accuracy,
                                            timestamp = last.time,
                                            provider = (last.provider ?: "cached") + " (last known)"
                                        )
                                    )
                                } else {
                                    cont.resume(null)
                                }
                            }
                            .addOnFailureListener {
                                cont.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
    }

    /**
     * Observe location updates as a Flow.
     * Useful for showing live coordinates on the SOS form.
     */
    @SuppressLint("MissingPermission")
    fun locationUpdates(intervalMs: Long = 5000): Flow<LocationResult> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(
                        LocationResult(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracyMeters = loc.accuracy,
                            timestamp = loc.time,
                            provider = loc.provider ?: "fused"
                        )
                    )
                }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }
}
