package com.prog7314.locallens.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Result data class containing location coordinates and resolved country info.
 */
data class UserLocationResult(
    val latitude: Double,
    val longitude: Double,
    val countryCode: String, // 2-letter ISO code, e.g. "za"
    val countryName: String  // Full country name, e.g. "South Africa"
)

/**
 * Helper class to interact with Android Fused Location Provider and Geocoder.
 */
class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Check if coarse or fine location permissions are granted by the user.
     */
    fun hasLocationPermission(): Boolean {
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return finePermission || coarsePermission
    }

    /**
     * Retrieves the fresh device location using FusedLocationProviderClient.
     * Uses PRIORITY_HIGH_ACCURACY for fresh fix.
     */
    @SuppressLint("MissingPermission")
    suspend fun getFreshLocation(): UserLocationResult? {
        if (!hasLocationPermission()) return null

        val cancellationTokenSource = CancellationTokenSource()

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude

                    // Reverse geocode to get country code & name
                    val (countryCode, countryName) = reverseGeocode(lat, lng)

                    continuation.resume(
                        UserLocationResult(
                            latitude = lat,
                            longitude = lng,
                            countryCode = countryCode,
                            countryName = countryName
                        )
                    )
                } else {
                    // Fallback to last known location if fresh fix is unavailable
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            val (cCode, cName) = reverseGeocode(lastLoc.latitude, lastLoc.longitude)
                            continuation.resume(
                                UserLocationResult(
                                    latitude = lastLoc.latitude,
                                    longitude = lastLoc.longitude,
                                    countryCode = cCode,
                                    countryName = cName
                                )
                            )
                        } else {
                            continuation.resume(null)
                        }
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }
            }.addOnFailureListener {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    /**
     * Uses Geocoder to convert Latitude and Longitude to ISO Country Code & Country Name
     */
    fun reverseGeocode(lat: Double, lng: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val countryCode = address.countryCode?.lowercase() ?: "za"
                val countryName = address.countryName ?: "South Africa"
                Pair(countryCode, countryName)
            } else {
                Pair("za", "South Africa")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Default fallback if geocoder service fails or is offline
            Pair("za", "South Africa")
        }
    }
}
