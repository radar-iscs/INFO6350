package com.example.realtime_location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Model
class LocationRepository(private val context: Context) {

    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // check if the app has the FINE_LOCATION permission
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // retrieve a continuous stream of location updates using Kotlin Flow
    fun getLocationUpdates(intervalMs: Long): Flow<LocationModel?> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(null)
            close() // close the flow if permission is missing
            return@callbackFlow
        }

        // configure the location request for high accuracy
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs)
            .build()

        // define the callback that receives the location updates
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.lastLocation?.let { loc ->
                    trySend(LocationModel(loc.latitude, loc.longitude))
                }
            }
        }

        client.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // remove the updates when the flow collector is cancelled
        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }
}