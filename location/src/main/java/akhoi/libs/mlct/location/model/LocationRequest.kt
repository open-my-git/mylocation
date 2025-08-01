package akhoi.libs.mlct.location.model

data class LocationRequest(
    val interval: Long = 0L,
    val minInterval: Long = 0L,
    val maxInterval: Long = 0L,
    val distance: Float = 0f,
)
