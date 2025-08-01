package akhoi.libs.mlct.location

import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.location.model.LocationRequest
import kotlinx.coroutines.flow.Flow

internal interface LocationSource {
    suspend fun getLastLocation(): Location?
    fun getLastLocationFlow(): Flow<Location>
    fun getLocationUpdates(request: LocationRequest): Flow<List<Location>>
}
