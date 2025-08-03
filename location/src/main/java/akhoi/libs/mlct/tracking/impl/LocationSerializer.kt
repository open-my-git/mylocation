package akhoi.libs.mlct.tracking.impl

import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.tools.ByteConcat
import akhoi.libs.mlct.tools.ByteConcatReader
import akhoi.libs.mlct.tools.compactDouble
import akhoi.libs.mlct.tools.restoreDouble

internal interface LocationSerializer {
    fun serialize(locations: List<Location>): ByteArray
    fun deserialize(bytes: ByteArray): List<Location>
}

internal class TrackingLocationSerializerV1(private val startTime: Long) : LocationSerializer {
    override fun serialize(locations: List<Location>): ByteArray {
        val byteConcat = ByteConcat(21 * locations.size)
        locations.forEach { (elapsedTime, latitude, longitude, altitude, speed) ->
            val timeSinceStarted = elapsedTime - startTime
            byteConcat.appendLong(timeSinceStarted, TIME_FIELDSIZE)
            val compactLat = compactDouble(latitude, 4, 31)
            byteConcat.appendLong(compactLat, LAT_FIELDSIZE)
            val compactLong = compactDouble(longitude, 4, 31)
            byteConcat.appendLong(compactLong, LONG_FIELDSIZE)
            val compactAlt = compactDouble(altitude, 6, 29)
            byteConcat.appendLong(compactAlt, ALT_FIELDSIZE)
            val rawSpeed = java.lang.Float.floatToRawIntBits(speed)
            byteConcat.appendInt(rawSpeed, SPEED_FIELDSIZE)
        }
        return byteConcat.content
    }

    override fun deserialize(bytes: ByteArray): List<Location> {
        val reader = ByteConcatReader(bytes)
        val count = bytes.size / 21
        val locations = mutableListOf<Location>()
        repeat(count) {
            val elapsedTime = reader.readLong(TIME_FIELDSIZE) + startTime
            val latitude = reader.readLong(LAT_FIELDSIZE)
            val longitude = reader.readLong(LONG_FIELDSIZE)
            val altitude = reader.readLong(ALT_FIELDSIZE)
            val speed = reader.readInt(SPEED_FIELDSIZE)
            locations.add(
                Location(
                    elapsedTime,
                    restoreDouble(latitude, 4, 31),
                    restoreDouble(longitude, 4, 31),
                    restoreDouble(altitude, 6, 29),
                    java.lang.Float.intBitsToFloat(speed)
                )
            )
        }
        return locations
    }

    companion object {
        private const val TIME_FIELDSIZE = 27
        private const val LAT_FIELDSIZE = 36
        private const val LONG_FIELDSIZE = 36
        private const val ALT_FIELDSIZE = 36
        private const val SPEED_FIELDSIZE = 32
    }
}