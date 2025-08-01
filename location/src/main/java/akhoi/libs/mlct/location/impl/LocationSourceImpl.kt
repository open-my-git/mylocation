package akhoi.libs.mlct.location.impl

import akhoi.libs.mlct.location.LocationSource
import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.location.model.LocationRequest
import android.Manifest
import android.content.Context
import android.location.Location as AndroidLocation
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.android.gms.location.LocationRequest as AndroidLocationRequest

internal class LocationSourceImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
) : LocationSource {

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun getLastLocation(): Location? {
        val androidLocation = locationClient.lastLocation.await()
        return androidLocation?.toLocation()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getLastLocationFlow(): Flow<Location> = flow {
        val lastLocation = getLastLocation()
        if (lastLocation != null) {
            emit(lastLocation)
        } else {
            val request = LocationRequest()
            val result = getLocationUpdates(request).first { it.isNotEmpty() }
            emit(result.last())
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getLocationUpdates(request: LocationRequest): Flow<List<Location>> =
        callbackFlow {
            Log.d("LocationSource", "=== [START] Get location update, config=$request")
            val callback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    Log.d(
                        "LocationSource",
                        "[LocationDataSourceImpl] location Result: ${locationResult.locations.size}"
                    )
                    trySend(locationResult.locations.map { it.toLocation() })
                }
            }

            locationClient.requestLocationUpdates(
                request.toGmsLocationRequest(),
                callback,
                Looper.getMainLooper()
            )

            awaitClose {
                Log.d("LocationSource", "=== [STOP] close location update flow")
                locationClient.removeLocationUpdates(callback)
            }
        }
            .flowOn(Dispatchers.Main) // need Main to request updates from location client

    private fun LocationRequest.toGmsLocationRequest(): AndroidLocationRequest =
        AndroidLocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(minInterval)
            .setMinUpdateDistanceMeters(distance)
            .setMaxUpdateDelayMillis(maxInterval)
            .setWaitForAccurateLocation(true)
            .build()

    private fun AndroidLocation.toLocation(): Location =
        Location(
            elapsedRealtimeNanos / 1000000,
            latitude,
            longitude,
            altitude,
            speed
        )

    companion object {
//        private const val
    }
}
