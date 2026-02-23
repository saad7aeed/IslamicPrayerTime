package com.islamic.prayertimes.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.islamic.prayertimes.data.models.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geocoder: Geocoder
) {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getCurrentLocation(): Result<Location> {
        return try {
            if (!hasLocationPermission()) {
                return Result.failure(SecurityException("Location permission not granted"))
            }

            val cancellationToken = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()

            if (location != null) {
                val address = getAddressFromLocation(location.latitude, location.longitude)
                Result.success(
                    Location(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        cityName = address.first,
                        countryName = address.second
                    )
                )
            } else {
                Result.failure(Exception("Unable to get location"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getAddressFromLocation(
        latitude: Double,
        longitude: Double
    ): Pair<String, String> = suspendCoroutine { continuation ->
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        continuation.resume(
                            Pair(
                                address.locality ?: address.subAdminArea ?: "",
                                address.countryName ?: ""
                            )
                        )
                    } else {
                        continuation.resume(Pair("", ""))
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    continuation.resume(
                        Pair(
                            address.locality ?: address.subAdminArea ?: "",
                            address.countryName ?: ""
                        )
                    )
                } else {
                    continuation.resume(Pair("", ""))
                }
            }
        } catch (e: IOException) {
            continuation.resume(Pair("", ""))
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}