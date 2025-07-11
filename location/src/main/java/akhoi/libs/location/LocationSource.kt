package akhoi.libs.location

import akhoi.libs.location.model.Location
import akhoi.libs.location.model.LocationRequest
import kotlinx.coroutines.flow.Flow

interface LocationSource {
    suspend fun getLastLocation(): Location?
    fun getLastLocationFlow(): Flow<Location>
    fun getLocationUpdates(request: LocationRequest): Flow<List<Location>>
}
