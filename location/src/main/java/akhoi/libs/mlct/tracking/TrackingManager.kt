package akhoi.libs.mlct.tracking

import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.tracking.impl.TrackingStatus

internal interface TrackingManager {
    val status: @TrackingStatus Int
    val startTimeElapsed: Long?
    val startTimeCalendar: Long?
    val currentSpeed: Float
    var routeDistance: Double
    var lastActiveTime: Long?
    var pausedDuration: Long

    fun start()
    fun pause()
    fun resume()
    fun stop()

    suspend fun recordLocations(locations: List<Location>)
}