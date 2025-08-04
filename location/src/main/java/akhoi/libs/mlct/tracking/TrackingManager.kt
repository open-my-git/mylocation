package akhoi.libs.mlct.tracking

import akhoi.libs.mlct.location.model.Location

interface TrackingManager {
    fun start()
    fun pause()
    fun resume()
    fun stop()

    suspend fun recordLocations(locations: List<Location>)
}