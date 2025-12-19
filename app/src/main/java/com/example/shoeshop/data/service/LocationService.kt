package com.example.shoeshop.data.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs

data class Address(val fullAddress: String)
class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return suspendCancellableCoroutine { continuation ->
            val hasPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.w("LocationService", "Нет разрешений")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            try {
                val cancellationTokenSource = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(Pair(location.latitude, location.longitude))
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
                continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): Address? {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val fullAddress = buildAddressString(address, latitude, longitude)
                    Address(fullAddress = fullAddress)
                } else {
                    handleFallback(latitude, longitude)
                }
            } catch (e: IOException) {
                Log.e("LocationService", "Ошибка сети Geocoder: ${e.message}")
                handleFallback(latitude, longitude)
            } catch (e: Exception) {
                Log.e("LocationService", "Ошибка Geocoder: ${e.message}")
                handleFallback(latitude, longitude)
            }
        }
    }

    private fun handleFallback(latitude: Double, longitude: Double): Address {
        val isEmulatorDefault =
            abs(latitude - 37.4219) < 0.01 && abs(longitude - (-122.084)) < 0.01

        return if (isEmulatorDefault) {
            Address(fullAddress = "1600 Amphitheatre Pkwy, Mountain View, CA (Emu)")
        } else {
            Address(fullAddress = String.format("%.6f, %.6f", latitude, longitude))
        }
    }

    private fun buildAddressString(
        address: android.location.Address,
        latitude: Double,
        longitude: Double
    ): String {
        val parts = mutableListOf<String>()

        address.countryName?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        address.adminArea?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        address.locality?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        address.thoroughfare?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }

        val houseNumber = address.subThoroughfare ?: address.featureName
        if (!houseNumber.isNullOrEmpty() && houseNumber != address.thoroughfare) {
            parts.add(houseNumber)
        }

        return parts.joinToString(", ").ifEmpty {
            String.format("%.6f, %.6f", latitude, longitude)
        }
    }
}
