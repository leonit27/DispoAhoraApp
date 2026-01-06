package com.example.dispoahora.location

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LocationService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getRawLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (_: Exception) {
            null
        }
    }

    fun getAddressFromLocation(location: Location): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                address.locality ?: address.subAdminArea ?: "Ciudad desconocida"
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun checkLocationSettings(
        onEnabled: () -> Unit,
        onDisabled: (com.google.android.gms.common.api.ResolvableApiException) -> Unit
    ) {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { onEnabled() }
        task.addOnFailureListener { exception ->
            if (exception is com.google.android.gms.common.api.ResolvableApiException) {
                onDisabled(exception)
            }
        }
    }
}