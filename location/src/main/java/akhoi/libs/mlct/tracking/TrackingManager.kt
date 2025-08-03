package akhoi.libs.mlct.tracking

import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.tracking.impl.TrackingStatus

interface TrackingManager {
    fun getStatus(): @TrackingStatus Int
    fun getElapsedStartTime(): Long?
    fun getCalendarStartTime(): Long?
    fun getCurrentSpeed(): Float
    fun getRouteDistance(): Double
    fun getLastActiveTime(): Long?
    fun getPausedDuration(): Long

    fun start()
    fun pause()
    fun resume()
    fun stop()

    suspend fun recordLocations(locations: List<Location>)
}