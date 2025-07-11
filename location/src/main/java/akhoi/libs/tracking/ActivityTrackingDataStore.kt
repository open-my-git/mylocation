package akhoi.libs.tracking

import akhoi.libs.location.model.Location

interface ActivityTrackingDataStore {
    fun initialize()
    suspend fun startTracking(): String
    suspend fun getStartTime(): Long
    suspend fun addTrackingLocations(locations: List<Location>)
    suspend fun clearAll()
    suspend fun getLocations(skip: Int): List<Location>
    suspend fun getLastLocationTime(): Long
}