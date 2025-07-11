package akhoi.libs.location.model

data class Location(
    /**
     * Miliseconds since epoch.
     */
    val time: Long,

    /**
     * Latitude and longitude in degrees.
     */
    val latitude: Double,
    val longitude: Double,

    /**
     * Altitude in meters above WGS84 ellipsoid.
     */
    val altitude: Double,

    /**
     * Speed in m/s.
     */
    val speed: Double,
)
