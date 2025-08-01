package akhoi.libs.mlct.location.model

data class Location(
    /**
     * Miliseconds since boot.
     */
    val elapsedTime: Long,

    /**
     * Latitude and longitude in degrees.
     */
    val latitude: Double,
    val longitude: Double,

    /**
     * Altitude in meters above WGS84 ellipsoid.
     */
    val altitude: Double,

    val speed: Float = 0f
)
